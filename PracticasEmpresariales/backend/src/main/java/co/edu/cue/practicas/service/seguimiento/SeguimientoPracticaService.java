package co.edu.cue.practicas.service.seguimiento;

import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.SeguimientoPractica;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoPractica;
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
import java.util.LinkedHashMap;
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

        if (practica.getEstado() != EstadoPractica.EN_CURSO) {
            throw new OperacionNoPermitidaException("La practica debe estar EN_CURSO para registrar seguimiento");
        }
        if (!practica.getEstudiante().getId().equals(estudianteId)) {
            throw new OperacionNoPermitidaException("El seguimiento debe ser cargado por el estudiante de la practica");
        }
        if (seguimientoRepository.findByPractica_IdAndSemana(practicaId, semana).isPresent()) {
            throw new OperacionNoPermitidaException("Ya existe seguimiento para esa semana");
        }

        SeguimientoPractica seguimiento = SeguimientoPractica.builder()
                .practica(practica)
                .semana(semana)
                .actividades(actividades)
                .logros(logros)
                .dificultades(dificultades)
                .estado(EstadoSeguimiento.PENDIENTE)
                .cargadoPor(estudiante)
                .build();

        return toMap(seguimientoRepository.save(seguimiento));
    }

    @Transactional
    public Map<String, Object> revisar(Long seguimientoId, Long docenteId, EstadoSeguimiento estado, String observaciones) {
        SeguimientoPractica seguimiento = buscar(seguimientoId);
        Usuario docente = usuarioRepository.findById(docenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Docente no encontrado"));

        if (estado == EstadoSeguimiento.PENDIENTE) {
            throw new OperacionNoPermitidaException("La revision debe aprobar o rechazar el seguimiento");
        }

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

        return toMap(seguimientoRepository.save(seguimiento));
    }

    @Transactional
    public Map<String, Object> corregirRechazado(Long seguimientoId, String actividades, String logros, String dificultades) {
        SeguimientoPractica seguimiento = buscar(seguimientoId);
        int semanaActual = seguimientoRepository.findFirstByPractica_IdOrderBySemanaDesc(seguimiento.getPractica().getId())
                .map(SeguimientoPractica::getSemana)
                .orElse(seguimiento.getSemana());

        if (!seguimiento.esEditablePorEstudiante(semanaActual)) {
            throw new OperacionNoPermitidaException("Solo se puede corregir el seguimiento rechazado de la semana mas reciente");
        }

        seguimiento.setActividades(actividades);
        seguimiento.setLogros(logros);
        seguimiento.setDificultades(dificultades);
        seguimiento.setEstado(EstadoSeguimiento.PENDIENTE);
        seguimiento.setVersion(seguimiento.getVersion() + 1);
        seguimiento.setObservacionesDocente(null);
        seguimiento.setFechaRevision(null);
        seguimiento.setDocenteAsesor(null);
        return toMap(seguimientoRepository.save(seguimiento));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPorPractica(Long practicaId) {
        return seguimientoRepository.findByPractica_IdOrderBySemanaAsc(practicaId)
                .stream()
                .map(this::toMap)
                .toList();
    }

    private SeguimientoPractica buscar(Long id) {
        return seguimientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Seguimiento no encontrado"));
    }

    private Map<String, Object> toMap(SeguimientoPractica s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId());
        map.put("practicaId", s.getPractica().getId());
        map.put("semana", s.getSemana());
        map.put("estado", s.getEstado());
        map.put("actividades", s.getActividades());
        map.put("logros", s.getLogros());
        map.put("dificultades", s.getDificultades());
        map.put("observacionesDocente", s.getObservacionesDocente());
        map.put("cargadoPorId", s.getCargadoPor().getId());
        map.put("docenteAsesorId", s.getDocenteAsesor() == null ? null : s.getDocenteAsesor().getId());
        map.put("fechaCarga", s.getFechaCarga());
        map.put("fechaRevision", s.getFechaRevision());
        map.put("version", s.getVersion());
        return map;
    }
}
