package co.edu.cue.practicas.service.notificacion;

import co.edu.cue.practicas.config.singleton.SystemConfig;
import co.edu.cue.practicas.model.entity.NotificacionHistorial;
import co.edu.cue.practicas.model.enums.EstadoNotificacion;
import co.edu.cue.practicas.repository.notificacion.NotificacionHistorialRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler central de envío/reintento de notificaciones por correo (RF-11-05).
 *
 * Responsabilidades:
 *  - Cada N segundos toma notificaciones PENDIENTE y las envía.
 *  - Cada M minutos toma notificaciones FALLIDO con proxReintento <= ahora,
 *    aplica exponential backoff (5 → 15 → 60 min) y reintenta hasta 3 veces.
 *
 * El despacho de invitaciones y recordatorios de encuestas (RF-08-05/06)
 * encola notificaciones PENDIENTE; este scheduler las envía.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionScheduler {

    private final NotificacionHistorialRepository notificacionRepository;
    private final JavaMailSender mailSender;
    private final SystemConfig systemConfig;

    /**
     * Despacha notificaciones PENDIENTE.
     * Cada 60 segundos.
     */
    @Scheduled(fixedDelay = 60_000L, initialDelay = 30_000L)
    @Transactional
    public void despacharPendientes() {
        List<NotificacionHistorial> pendientes =
                notificacionRepository.findByEstado(EstadoNotificacion.PENDIENTE);
        if (pendientes.isEmpty()) return;
        log.info("[SCHEDULER] Despachando {} notificación(es) pendiente(s)", pendientes.size());
        pendientes.forEach(this::enviar);
    }

    /**
     * Reintenta notificaciones FALLIDO con proxReintento vencido.
     * Cada 5 minutos.
     */
    @Scheduled(fixedDelay = 300_000L, initialDelay = 120_000L)
    @Transactional
    public void reintentarFallidos() {
        List<NotificacionHistorial> aReintentar =
                notificacionRepository.findByEstadoAndProxReintentoLessThan(
                        EstadoNotificacion.FALLIDO, LocalDateTime.now());
        if (aReintentar.isEmpty()) return;
        log.info("[SCHEDULER] Reintentando {} notificación(es) fallida(s)", aReintentar.size());
        aReintentar.stream()
                .filter(NotificacionHistorial::puedeReintentarse)
                .forEach(this::enviar);
    }

    private void enviar(NotificacionHistorial n) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(systemConfig.getMailFromAddress(), systemConfig.getMailFromName());
            helper.setTo(n.getCorreoDestino());
            helper.setSubject(n.getAsunto());
            helper.setText(n.getCuerpo(), true);
            mailSender.send(message);

            n.setEstado(EstadoNotificacion.ENVIADO);
            n.setErrorMensaje(null);
            n.setProxReintento(null);
            notificacionRepository.save(n);
            log.info("[SCHEDULER] Enviado: id={} → {}", n.getId(), n.getCorreoDestino());

        } catch (Exception e) {
            log.error("[SCHEDULER] Fallo envío id={} → {}: {}",
                    n.getId(), n.getCorreoDestino(), e.getMessage());
            n.setEstado(EstadoNotificacion.FALLIDO);
            n.registrarReintento(e.getMessage());
            notificacionRepository.save(n);
        }
    }
}
