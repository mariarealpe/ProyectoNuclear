package co.edu.cue.practicas.service.practica;

import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.Asignacion;
import co.edu.cue.practicas.model.entity.DocumentoPractica;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoAsignacion;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoDocumento;
import co.edu.cue.practicas.model.enums.TipoNotificacion;
import co.edu.cue.practicas.repository.asignacion.AsignacionRepository;
import co.edu.cue.practicas.repository.practica.DocumentoPracticaRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import co.edu.cue.practicas.service.asignacion.AsignacionService;
import co.edu.cue.practicas.service.notificacion.NotificacionSprint3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * PATRON FACADE — GPE-164
 *
 * Orquesta el paso de una asignacion hacia una practica real.
 * Para Postman se separa en dos momentos:
 * 1. preparar practica desde asignacion (queda lista para documentos)
 * 2. confirmar vinculacion (activa EN_CURSO)
 */
@Service
@RequiredArgsConstructor
public class PracticaSprint3Service {

    private final PracticaRepository practicaRepository;
    private final AsignacionRepository asignacionRepository;
    private final DocumentoPracticaRepository documentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignacionService asignacionService;
    private final NotificacionSprint3Service notificacionService;
    private final PracticaSprint3Mapper mapper;

    @Transactional
    public Map<String, Object> prepararDesdeAsignacion(Long asignacionId, Long usuarioId) {
        Asignacion asignacion = asignacionService.buscar(asignacionId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (asignacion.getEstado() != EstadoAsignacion.ASIGNADA) {
            throw new OperacionNoPermitidaException("La asignacion debe estar ASIGNADA para iniciar vinculacion");
        }
        if (practicaRepository.existsByEstudiante_IdAndEstado(asignacion.getEstudiante().getId(), EstadoPractica.EN_CURSO)) {
            throw new OperacionNoPermitidaException("El estudiante ya tiene una practica EN_CURSO");
        }

        Practica practica = Practica.builder()
                .estudiante(asignacion.getEstudiante())
                .programa(asignacion.getVacante().getPrograma())
                .numeroPractica(1)
                .nombre(asignacion.getVacante().getTitulo())
                .descripcion(asignacion.getVacante().getDescripcion())
                .documentosRequeridos("CARTA_PRESENTACION,CONVENIO")
                .estado(EstadoPractica.PENDIENTE_ASIGNACION)
                .build();

        asignacion.setEstado(EstadoAsignacion.EN_VINCULACION);
        asignacion.setFechaVinculacion(LocalDateTime.now());
        asignacionService.registrarCambio(
                asignacion,
                EstadoAsignacion.ASIGNADA,
                EstadoAsignacion.EN_VINCULACION,
                "Practica preparada para documentos de vinculacion",
                usuario
        );

        return mapper.toMap(practicaRepository.save(practica));
    }

    @Transactional
    public Map<String, Object> confirmarVinculacion(Long practicaId, Long usuarioId, Long docenteAsesorId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        Practica practica = buscar(practicaId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        Asignacion asignacion = asignacionRepository.findFirstByEstudiante_IdOrderByFechaAsignacionDesc(practica.getEstudiante().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Asignacion del estudiante no encontrada"));

        if (practica.getEstado() == EstadoPractica.EN_CURSO) {
            throw new OperacionNoPermitidaException("La practica ya esta EN_CURSO");
        }
        if (asignacion.getEstado() != EstadoAsignacion.EN_VINCULACION) {
            throw new OperacionNoPermitidaException("La asignacion debe estar EN_VINCULACION");
        }

        DocumentoPractica convenio = documentoRepository.findByPractica_IdAndTipo(practicaId, TipoDocumento.CONVENIO)
                .stream()
                .findFirst()
                .orElseThrow(() -> new OperacionNoPermitidaException("Debe existir un convenio antes de confirmar vinculacion"));

        if (!convenio.tieneTodasLasFirmas()) {
            throw new OperacionNoPermitidaException("El convenio requiere las 3 firmas confirmadas");
        }

        Usuario docente = usuarioRepository.findById(docenteAsesorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Docente Asesor no encontrado: " + docenteAsesorId));
        if (docente.getRol() != Rol.DOCENTE_ASESOR) {
            throw new OperacionNoPermitidaException("El usuario indicado no tiene rol DOCENTE_ASESOR");
        }

        practica.setEstado(EstadoPractica.EN_CURSO);
        practica.setDocenteAsesor(docente);
        asignacion.setEstado(EstadoAsignacion.EN_CURSO);
        asignacion.setFechaInicio(fechaInicio);
        asignacion.setFechaFin(fechaFin);
        asignacionService.registrarCambio(
                asignacion,
                EstadoAsignacion.EN_VINCULACION,
                EstadoAsignacion.EN_CURSO,
                "Vinculacion confirmada y practica activada",
                usuario
        );

        notificacionService.registrar(
                practica.getEstudiante().getId(),
                TipoNotificacion.VINCULACION_CONFIRMADA,
                "Practica en curso",
                "Tu vinculacion fue confirmada. La practica queda EN_CURSO.",
                asignacion.getId(),
                practica.getId()
        );

        return mapper.toMap(practicaRepository.save(practica));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPorEstudiante(Long estudianteId) {
        return practicaRepository.findByEstudiante_IdOrderByCreadoEnDesc(estudianteId)
                .stream()
                .map(mapper::toMap)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtener(Long id) {
        return mapper.toMap(buscar(id));
    }

    public Practica buscar(Long id) {
        return practicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Practica no encontrada"));
    }

}
