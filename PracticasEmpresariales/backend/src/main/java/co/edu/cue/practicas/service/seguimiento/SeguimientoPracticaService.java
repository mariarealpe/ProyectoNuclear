package co.edu.cue.practicas.service.seguimiento;

import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.SeguimientoPractica;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoSeguimiento;
import co.edu.cue.practicas.model.enums.TipoNotificacion;
import co.edu.cue.practicas.repository.practica.SeguimientoPracticaRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import co.edu.cue.practicas.service.notificacion.NotificacionSprint3Service;
import co.edu.cue.practicas.service.practica.PracticaSprint3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * PATRON FACADE — GPE-168, GPE-170
 *
 * Agrupa la bitacora semanal del estudiante y la revision del Docente Asesor.
 * El controller expone acciones simples para Postman y este servicio conserva
 * las reglas: practica EN_CURSO, semana unica y observaciones al revisar.
 */
@Service
@RequiredArgsConstructor
public class SeguimientoPracticaService {

    private final SeguimientoPracticaRepository seguimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PracticaSprint3Service practicaService;
    private final NotificacionSprint3Service notificacionService;
    private final SeguimientoPracticaValidator validator;
    private final SeguimientoPracticaMapper mapper;

    @Transactional
    public Map<String, Object> crear(Long practicaId,
                                     Long estudianteId,
                                     Integer semana,
                                     String actividades,
                                     String logros,
                                     String dificultades) {
        Practica practica = practicaService.buscar(practicaId);
        Usuario estudiante = usuarioRepository.findById(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado"));

        validator.validarCreacion(practica, estudianteId, semana);

        SeguimientoPractica seguimiento = SeguimientoPractica.builder()
                .practica(practica)
                .semana(semana)
                .actividades(actividades)
                .logros(logros)
                .dificultades(dificultades)
                .estado(EstadoSeguimiento.PENDIENTE)
                .cargadoPor(estudiante)
                .build();

        return mapper.toMap(seguimientoRepository.save(seguimiento));
    }

    @Transactional
    public Map<String, Object> revisar(Long seguimientoId, Long docenteId, EstadoSeguimiento estado, String observaciones) {
        SeguimientoPractica seguimiento = buscar(seguimientoId);
        Usuario docente = usuarioRepository.findById(docenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Docente no encontrado"));

        validator.validarRevision(estado);

        seguimiento.setDocenteAsesor(docente);
        seguimiento.setEstado(estado);
        seguimiento.setObservacionesDocente(observaciones);
        seguimiento.setFechaRevision(LocalDateTime.now());

        if (estado == EstadoSeguimiento.RECHAZADO) {
            notificacionService.registrar(
                    seguimiento.getCargadoPor().getId(),
                    TipoNotificacion.SEGUIMIENTO_RECHAZADO,
                    "Seguimiento rechazado",
                    observaciones == null ? "El seguimiento requiere correccion." : observaciones,
                    null,
                    seguimiento.getPractica().getId()
            );
        }

        return mapper.toMap(seguimientoRepository.save(seguimiento));
    }

    @Transactional
    public Map<String, Object> corregirRechazado(Long seguimientoId, String actividades, String logros, String dificultades) {
        SeguimientoPractica seguimiento = buscar(seguimientoId);
        int semanaActual = seguimientoRepository.findFirstByPractica_IdOrderBySemanaDesc(seguimiento.getPractica().getId())
                .map(SeguimientoPractica::getSemana)
                .orElse(seguimiento.getSemana());

        validator.validarCorreccion(seguimiento, semanaActual);

        seguimiento.setActividades(actividades);
        seguimiento.setLogros(logros);
        seguimiento.setDificultades(dificultades);
        seguimiento.setEstado(EstadoSeguimiento.PENDIENTE);
        seguimiento.setVersion(seguimiento.getVersion() + 1);
        seguimiento.setObservacionesDocente(null);
        seguimiento.setFechaRevision(null);
        seguimiento.setDocenteAsesor(null);
        return mapper.toMap(seguimientoRepository.save(seguimiento));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPorPractica(Long practicaId) {
        return seguimientoRepository.findByPractica_IdOrderBySemanaAsc(practicaId)
                .stream()
                .map(mapper::toMap)
                .toList();
    }

    private SeguimientoPractica buscar(Long id) {
        return seguimientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Seguimiento no encontrado"));
    }

}
