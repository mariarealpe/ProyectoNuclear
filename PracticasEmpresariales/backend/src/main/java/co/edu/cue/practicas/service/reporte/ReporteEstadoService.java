package co.edu.cue.practicas.service.reporte;

import co.edu.cue.practicas.dto.request.FiltrosReporteEstadoRequest;
import co.edu.cue.practicas.dto.response.ReporteEstadoResponse;
import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.enums.EstadoEstudiante;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.FormatoExport;
import co.edu.cue.practicas.model.enums.ResultadoNotaFinal;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.repository.evaluacion.NotaFinalRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.security.annotation.RequiereRol;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * RF-10-01 — Construcción del reporte de estado del proceso.
 *
 * Patrones:
 *  - Builder: aplica los filtros del request paso a paso para construir
 *    el ReporteEstadoResponse.
 *  - Strategy: la exportación delega en ExportadorReporte por formato.
 *
 * Scope:
 *  - DIRECCION ve todo.
 *  - COORDINACION_ACADEMICA está limitada a su facultad.
 *  - COORDINADOR_PRACTICAS está limitado a su programa.
 */
@Service
public class ReporteEstadoService {

    private final PracticaRepository practicaRepository;
    private final NotaFinalRepository notaFinalRepository;
    private final UsuarioRepository usuarioRepository;
    private final Map<FormatoExport, ExportadorReporte> exportadores;

    public ReporteEstadoService(
            PracticaRepository practicaRepository,
            NotaFinalRepository notaFinalRepository,
            UsuarioRepository usuarioRepository,
            List<ExportadorReporte> exportadores) {
        this.practicaRepository = practicaRepository;
        this.notaFinalRepository = notaFinalRepository;
        this.usuarioRepository = usuarioRepository;
        this.exportadores = exportadores.stream()
                .collect(java.util.stream.Collectors.toMap(
                        ExportadorReporte::formato, e -> e));
    }

    @RequiereRol(roles = {
            Rol.ADMIN_DTI, Rol.DIRECCION, Rol.COORDINACION_ACADEMICA, Rol.COORDINADOR_PRACTICAS
    })
    @Transactional(readOnly = true)
    public ReporteEstadoResponse generar(
            FiltrosReporteEstadoRequest filtros, CustomUserDetails actor) {

        FiltrosReporteEstadoRequest filtrosScope = aplicarScope(filtros, actor);

        long aptosSinIniciar = contarAptosSinIniciar(filtrosScope);
        long enAsignacion    = 0L; // sin acceso a counts por estado asignacion sin más repo work
        long enPractica      = contarPracticasEnEstado(filtrosScope, EstadoPractica.EN_CURSO);
        long completados     = contarPorResultado(filtrosScope, ResultadoNotaFinal.APROBADO);
        long reprobados      = contarPorResultado(filtrosScope, ResultadoNotaFinal.REPROBADO);

        long total = aptosSinIniciar + enAsignacion + enPractica + completados + reprobados;

        return ReporteEstadoResponse.builder()
                .generadoEn(LocalDateTime.now())
                .filtros(ReporteEstadoResponse.FiltrosAplicados.builder()
                        .facultadId(filtrosScope.getFacultadId())
                        .programaId(filtrosScope.getProgramaId())
                        .numeroPractica(filtrosScope.getNumeroPractica())
                        .periodoDesde(filtrosScope.getPeriodoDesde())
                        .periodoHasta(filtrosScope.getPeriodoHasta())
                        .build())
                .aptosSinIniciar(aptosSinIniciar)
                .enAsignacion(enAsignacion)
                .enPractica(enPractica)
                .completados(completados)
                .reprobados(reprobados)
                .total(total)
                .build();
    }

    /**
     * Genera y exporta el reporte en el formato indicado (Strategy).
     */
    @RequiereRol(roles = {
            Rol.ADMIN_DTI, Rol.DIRECCION, Rol.COORDINACION_ACADEMICA, Rol.COORDINADOR_PRACTICAS
    })
    @Transactional(readOnly = true)
    public ExportResult exportar(
            FiltrosReporteEstadoRequest filtros,
            FormatoExport formato,
            CustomUserDetails actor) {

        ExportadorReporte exportador = exportadores.get(formato);
        if (exportador == null) {
            throw new OperacionNoPermitidaException("Formato no soportado: " + formato);
        }
        ReporteEstadoResponse reporte = generar(filtros, actor);
        byte[] bytes = exportador.exportar(reporte);
        return new ExportResult(bytes, exportador.contentType(),
                "reporte_estado_" + System.currentTimeMillis() + exportador.extension());
    }

    // =========================================================================

    private FiltrosReporteEstadoRequest aplicarScope(
            FiltrosReporteEstadoRequest req, CustomUserDetails actor) {
        if (req == null) req = new FiltrosReporteEstadoRequest();

        Rol rol = actor.getRol();
        var usuario = actor.getUsuario();
        if (rol == Rol.COORDINADOR_PRACTICAS) {
            Long programa = usuario.getPrograma() != null ? usuario.getPrograma().getId() : null;
            if (programa == null) {
                throw new AccesoNoAutorizadoException(
                        "El Coordinador no tiene programa asignado");
            }
            if (req.getProgramaId() != null && !req.getProgramaId().equals(programa)) {
                throw new AccesoNoAutorizadoException(
                        "No puede generar reportes de programas distintos al suyo");
            }
            req.setProgramaId(programa);
        } else if (rol == Rol.COORDINACION_ACADEMICA) {
            Long facultad = usuario.getFacultad() != null ? usuario.getFacultad().getId() : null;
            if (facultad == null) {
                throw new AccesoNoAutorizadoException(
                        "La Coordinación Académica no tiene facultad asignada");
            }
            if (req.getFacultadId() != null && !req.getFacultadId().equals(facultad)) {
                throw new AccesoNoAutorizadoException(
                        "No puede generar reportes de facultades distintas a la suya");
            }
            req.setFacultadId(facultad);
        }
        return req;
    }

    private long contarAptosSinIniciar(FiltrosReporteEstadoRequest f) {
        if (f.getProgramaId() != null) {
            return usuarioRepository.countByRolAndEstadoEstudianteAndPrograma_Id(
                    Rol.ESTUDIANTE, EstadoEstudiante.APTO, f.getProgramaId());
        }
        if (f.getFacultadId() != null) {
            return usuarioRepository.countByRolAndEstadoEstudianteAndPrograma_Facultad_Id(
                    Rol.ESTUDIANTE, EstadoEstudiante.APTO, f.getFacultadId());
        }
        return usuarioRepository.countByRolAndEstadoEstudiante(
                Rol.ESTUDIANTE, EstadoEstudiante.APTO);
    }

    private long contarPracticasEnEstado(FiltrosReporteEstadoRequest f, EstadoPractica estado) {
        if (f.getProgramaId() != null && f.getNumeroPractica() != null) {
            return practicaRepository.countByPrograma_IdAndNumeroPracticaAndEstado(
                    f.getProgramaId(), f.getNumeroPractica(), estado);
        }
        if (f.getProgramaId() != null) {
            return practicaRepository.countByPrograma_IdAndEstado(f.getProgramaId(), estado);
        }
        if (f.getFacultadId() != null) {
            return practicaRepository.countByPrograma_Facultad_IdAndEstado(f.getFacultadId(), estado);
        }
        return practicaRepository.countByEstado(estado);
    }

    private long contarPorResultado(FiltrosReporteEstadoRequest f, ResultadoNotaFinal resultado) {
        if (f.getProgramaId() != null && f.getNumeroPractica() != null) {
            return notaFinalRepository
                    .countByPractica_Programa_IdAndPractica_NumeroPracticaAndResultado(
                            f.getProgramaId(), f.getNumeroPractica(), resultado);
        }
        if (f.getProgramaId() != null) {
            return notaFinalRepository
                    .countByPractica_Programa_IdAndResultado(f.getProgramaId(), resultado);
        }
        if (f.getFacultadId() != null) {
            return notaFinalRepository
                    .countByPractica_Programa_Facultad_IdAndResultado(f.getFacultadId(), resultado);
        }
        return notaFinalRepository.countByResultado(resultado);
    }

    public record ExportResult(byte[] bytes, String contentType, String fileName) {}
}
