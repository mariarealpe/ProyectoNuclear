package co.edu.cue.practicas.event.listener;

import co.edu.cue.practicas.event.VacanteAprobadaEvent;
import co.edu.cue.practicas.event.VacanteCreadaEvent;
import co.edu.cue.practicas.event.VacanteRechazadaEvent;
import co.edu.cue.practicas.service.notificacion.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * PATRON OBSERVER — GPE-152, GPE-153
 *
 * Listener que reacciona a los eventos del ciclo de vida de una vacante:
 *   - Creada: notifica al Coordinador de Prácticas que debe revisarla.
 *   - Aprobada: notifica a la empresa que su vacante ya está publicada.
 *   - Rechazada: notifica a la empresa con el motivo del rechazo.
 *
 * Se ejecuta de forma asíncrona (@Async) para no bloquear la respuesta HTTP
 * del usuario mientras se envían los correos.
 *
 * Si la app aún no tiene SMTP configurado, los métodos del EmailService deben
 * funcionar como "no-op" o solo loguear — el listener no falla por eso.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionVacanteListener {

    private final EmailService emailService;

    @EventListener
    @Async
    public void manejarVacanteCreada(VacanteCreadaEvent evento) {
        var v = evento.getVacante();
        log.info("[OBSERVER] Vacante creada (ID:{}) — notificando al Coordinador de Prácticas: {}",
                v.getId(), v.getTitulo());
        try {
            emailService.notificarVacantePendiente(v);
        } catch (Exception e) {
            // Si el correo falla, dejamos rastro pero no rompemos el flujo principal.
            log.warn("[OBSERVER] No se pudo notificar vacante creada: {}", e.getMessage());
        }
    }

    @EventListener
    @Async
    public void manejarVacanteAprobada(VacanteAprobadaEvent evento) {
        var v = evento.getVacante();
        log.info("[OBSERVER] Vacante aprobada (ID:{}) — notificando a la empresa: {}",
                v.getId(), v.getEmpresa() != null ? v.getEmpresa().getRazonSocial() : "(sin empresa)");
        try {
            emailService.notificarVacanteAprobada(v, evento.getObservacion());
        } catch (Exception e) {
            log.warn("[OBSERVER] No se pudo notificar vacante aprobada: {}", e.getMessage());
        }
    }

    @EventListener
    @Async
    public void manejarVacanteRechazada(VacanteRechazadaEvent evento) {
        var v = evento.getVacante();
        log.info("[OBSERVER] Vacante rechazada (ID:{}) — notificando a la empresa con motivo",
                v.getId());
        try {
            emailService.notificarVacanteRechazada(v, evento.getMotivo());
        } catch (Exception e) {
            log.warn("[OBSERVER] No se pudo notificar vacante rechazada: {}", e.getMessage());
        }
    }
}
