package co.edu.cue.practicas.service.reporte;

import co.edu.cue.practicas.dto.response.TableroGerencialResponse;
import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.model.entity.Facultad;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.ResultadoNotaFinal;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.repository.empresa.EmpresaRepository;
import co.edu.cue.practicas.repository.evaluacion.NotaFinalRepository;
import co.edu.cue.practicas.repository.facultad.FacultadRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.security.annotation.RequiereRol;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RF-10-04 — Indicadores agregados para el tablero gerencial.
 *
 * Patrones:
 *  - Facade: consolida métricas de prácticas, notas finales y empresas
 *    detrás de una única operación.
 *  - Builder: arma el response con las secciones que aplican según scope.
 *
 * Scope:
 *  - DIRECCION / ADMIN_DTI: ven indicadores de toda la institución.
 *  - COORDINACION_ACADEMICA: ve solo los indicadores de su facultad.
 *
 * El tablero NUNCA expone datos individuales; solo agregados.
 */
@Service
@RequiredArgsConstructor
public class TableroGerencialService {

    private final PracticaRepository practicaRepository;
    private final NotaFinalRepository notaFinalRepository;
    private final EmpresaRepository empresaRepository;
    private final FacultadRepository facultadRepository;

    @RequiereRol(roles = {Rol.ADMIN_DTI, Rol.DIRECCION, Rol.COORDINACION_ACADEMICA})
    @Transactional(readOnly = true)
    public TableroGerencialResponse obtener(
            LocalDateTime periodoDesde,
            LocalDateTime periodoHasta,
            CustomUserDetails actor) {

        Long facultadScope = facultadScope(actor);
        List<Facultad> facultades = facultadesEnScope(facultadScope);

        long totalAprobadas = totalPorResultado(facultadScope, ResultadoNotaFinal.APROBADO);
        long totalReprobadas = totalPorResultado(facultadScope, ResultadoNotaFinal.REPROBADO);
        long totalCalificadas = totalAprobadas + totalReprobadas;
        double tasaAprobacion = totalCalificadas == 0
                ? 0.0
                : (totalAprobadas * 100.0) / totalCalificadas;

        long practicasCerradasEnPeriodo = periodoDesde != null && periodoHasta != null
                ? notaFinalRepository.countByCerradaEnBetween(periodoDesde, periodoHasta)
                : 0L;

        long activos = facultadScope != null
                ? practicaRepository.countByPrograma_Facultad_IdAndEstado(facultadScope, EstadoPractica.EN_CURSO)
                : practicaRepository.countByEstado(EstadoPractica.EN_CURSO);

        long totalPracticas = totalAprobadas + totalReprobadas + activos;
        long empresasActivas = facultadScope != null
                ? -1L   // no contamos empresas por facultad — métrica institucional
                : empresaRepository.countByActivoTrue();

        List<TableroGerencialResponse.IndicadorFacultad> porFacultad = new ArrayList<>();
        for (Facultad f : facultades) {
            long ap = notaFinalRepository
                    .countByPractica_Programa_Facultad_IdAndResultado(
                            f.getId(), ResultadoNotaFinal.APROBADO);
            long re = notaFinalRepository
                    .countByPractica_Programa_Facultad_IdAndResultado(
                            f.getId(), ResultadoNotaFinal.REPROBADO);
            long act = practicaRepository
                    .countByPrograma_Facultad_IdAndEstado(f.getId(), EstadoPractica.EN_CURSO);
            long calif = ap + re;
            double tasa = calif == 0 ? 0.0 : (ap * 100.0) / calif;
            porFacultad.add(TableroGerencialResponse.IndicadorFacultad.builder()
                    .facultadId(f.getId())
                    .nombreFacultad(f.getNombre())
                    .practicantesActivos(act)
                    .aprobadas(ap)
                    .reprobadas(re)
                    .tasaAprobacion(tasa)
                    .build());
        }

        return TableroGerencialResponse.builder()
                .generadoEn(LocalDateTime.now())
                .periodoDesde(periodoDesde)
                .periodoHasta(periodoHasta)
                .totalPracticantesActivos(activos)
                .empresasActivas(empresasActivas)
                .practicasCerradasEnPeriodo(practicasCerradasEnPeriodo)
                .totalPracticas(totalPracticas)
                .totalAprobadas(totalAprobadas)
                .totalReprobadas(totalReprobadas)
                .tasaAprobacionGlobal(tasaAprobacion)
                .porFacultad(porFacultad)
                .build();
    }

    // =========================================================================

    private Long facultadScope(CustomUserDetails actor) {
        if (actor.getRol() == Rol.COORDINACION_ACADEMICA) {
            if (actor.getUsuario().getFacultad() == null) {
                throw new AccesoNoAutorizadoException(
                        "Coordinación Académica sin facultad asignada");
            }
            return actor.getUsuario().getFacultad().getId();
        }
        return null;
    }

    private List<Facultad> facultadesEnScope(Long facultadScope) {
        if (facultadScope != null) {
            return facultadRepository.findById(facultadScope)
                    .map(List::of)
                    .orElse(List.of());
        }
        return facultadRepository.findByActivaTrue(PageRequest.of(0, 100)).getContent();
    }

    private long totalPorResultado(Long facultadScope, ResultadoNotaFinal resultado) {
        return facultadScope != null
                ? notaFinalRepository.countByPractica_Programa_Facultad_IdAndResultado(facultadScope, resultado)
                : notaFinalRepository.countByResultado(resultado);
    }
}
