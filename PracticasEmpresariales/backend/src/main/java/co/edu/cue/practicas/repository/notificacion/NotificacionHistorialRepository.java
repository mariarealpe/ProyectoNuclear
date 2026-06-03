package co.edu.cue.practicas.repository.notificacion;

import co.edu.cue.practicas.model.entity.NotificacionHistorial;
import co.edu.cue.practicas.model.enums.EstadoNotificacion;
import co.edu.cue.practicas.model.enums.TipoNotificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para NotificacionHistorial.
 * 
 * Historial de notificaciones por correo.
 * Soporta reintentos automáticos con exponential backoff.
 */
public interface NotificacionHistorialRepository extends JpaRepository<NotificacionHistorial, Long> {

    /**
     * Obtiene historial de notificaciones de un usuario, más recientes primero.
     */
    Page<NotificacionHistorial> findByUsuarioDestino_IdOrderByCreatedEnDesc(
            Long usuarioId,
            Pageable pageable
    );

    /**
     * Obtiene notificaciones FALLIDO que requieren reintento.
     * (proxReintento <= ahora)
     */
    List<NotificacionHistorial> findByEstadoAndProxReintentoLessThan(
            EstadoNotificacion estado,
            LocalDateTime ahora
    );

    /**
     * Obtiene notificaciones pendientes.
     */
    List<NotificacionHistorial> findByEstado(EstadoNotificacion estado);

    /**
     * Cuenta notificaciones por tipo (para estadísticas).
     */
    long countByTipo(TipoNotificacion tipo);

    /**
     * Obtiene notificaciones de un tipo específico para un usuario.
     */
    Page<NotificacionHistorial> findByUsuarioDestino_IdAndTipo(
            Long usuarioId,
            TipoNotificacion tipo,
            Pageable pageable
    );

    /**
     * Obtiene notificaciones de una asignación.
     */
    List<NotificacionHistorial> findByAsignacion_IdOrderByCreatedEnDesc(Long asignacionId);

    /**
     * Obtiene notificaciones de una práctica.
     */
    List<NotificacionHistorial> findByPractica_IdOrderByCreatedEnDesc(Long practicaId);

    /**
     * Verifica cuántas notificaciones FALLIDO requieren reintento.
     */
    long countByEstadoAndProxReintentoLessThan(
            EstadoNotificacion estado,
            LocalDateTime ahora
    );
}
