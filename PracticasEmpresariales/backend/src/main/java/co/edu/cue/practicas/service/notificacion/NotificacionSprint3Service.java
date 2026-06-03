package co.edu.cue.practicas.service.notificacion;

import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.Asignacion;
import co.edu.cue.practicas.model.entity.NotificacionHistorial;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoNotificacion;
import co.edu.cue.practicas.model.enums.TipoNotificacion;
import co.edu.cue.practicas.repository.asignacion.AsignacionRepository;
import co.edu.cue.practicas.repository.notificacion.NotificacionHistorialRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PATRON FACADE — GPE-160
 *
 * Expone una puerta simple para registrar y consultar notificaciones del Sprint 3.
 * El controller de Postman no conoce los repositorios ni la estructura interna
 * de NotificacionHistorial; delega aqui la persistencia y el mapeo de respuesta.
 */
@Service
@RequiredArgsConstructor
public class NotificacionSprint3Service {

    private final NotificacionHistorialRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignacionRepository asignacionRepository;
    private final PracticaRepository practicaRepository;

    @Transactional
    public Map<String, Object> registrar(Long usuarioDestinoId,
                                         TipoNotificacion tipo,
                                         String asunto,
                                         String cuerpo,
                                         Long asignacionId,
                                         Long practicaId) {
        Usuario usuario = usuarioRepository.findById(usuarioDestinoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario destino no encontrado"));

        Asignacion asignacion = asignacionId == null ? null : asignacionRepository.findById(asignacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asignacion no encontrada"));

        Practica practica = practicaId == null ? null : practicaRepository.findById(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Practica no encontrada"));

        NotificacionHistorial notificacion = NotificacionHistorial.builder()
                .usuarioDestino(usuario)
                .tipo(tipo)
                .correoDestino(usuario.getCorreo())
                .asunto(asunto)
                .cuerpo(cuerpo)
                .estado(EstadoNotificacion.PENDIENTE)
                .asignacion(asignacion)
                .practica(practica)
                .build();

        return toMap(notificacionRepository.save(notificacion));
    }

    @Transactional
    public Map<String, Object> marcarEnviada(Long id) {
        NotificacionHistorial notificacion = buscar(id);
        notificacion.setEstado(EstadoNotificacion.ENVIADO);
        notificacion.setErrorMensaje(null);
        notificacion.setProxReintento(null);
        return toMap(notificacionRepository.save(notificacion));
    }

    @Transactional
    public Map<String, Object> marcarFallida(Long id, String motivo) {
        NotificacionHistorial notificacion = buscar(id);
        notificacion.setEstado(EstadoNotificacion.FALLIDO);
        notificacion.registrarReintento(motivo == null ? "Fallo registrado desde Postman" : motivo);
        return toMap(notificacionRepository.save(notificacion));
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> listarPorUsuario(Long usuarioId, Pageable pageable) {
        return notificacionRepository.findByUsuarioDestino_IdOrderByCreadoEnDesc(usuarioId, pageable)
                .map(this::toMap);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPendientes() {
        return notificacionRepository.findByEstado(EstadoNotificacion.PENDIENTE)
                .stream()
                .map(this::toMap)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarReintentosPendientes() {
        return notificacionRepository.findByEstadoAndProxReintentoLessThan(
                        EstadoNotificacion.FALLIDO, LocalDateTime.now())
                .stream()
                .map(this::toMap)
                .toList();
    }

    private NotificacionHistorial buscar(Long id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificacion no encontrada"));
    }

    public Map<String, Object> toMap(NotificacionHistorial n) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", n.getId());
        map.put("usuarioDestinoId", n.getUsuarioDestino().getId());
        map.put("correoDestino", n.getCorreoDestino());
        map.put("tipo", n.getTipo());
        map.put("asunto", n.getAsunto());
        map.put("estado", n.getEstado());
        map.put("reintentos", n.getReintentos());
        map.put("proxReintento", n.getProxReintento());
        map.put("errorMensaje", n.getErrorMensaje());
        map.put("asignacionId", n.getAsignacion() == null ? null : n.getAsignacion().getId());
        map.put("practicaId", n.getPractica() == null ? null : n.getPractica().getId());
        map.put("creadoEn", n.getCreadoEn());
        return map;
    }
}
