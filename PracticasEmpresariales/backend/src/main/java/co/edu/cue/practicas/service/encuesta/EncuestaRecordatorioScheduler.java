package co.edu.cue.practicas.service.encuesta;

import co.edu.cue.practicas.model.entity.Encuesta;
import co.edu.cue.practicas.model.entity.PlantillaNotificacion;
import co.edu.cue.practicas.model.enums.EstadoEncuesta;
import co.edu.cue.practicas.model.enums.TipoEncuesta;
import co.edu.cue.practicas.model.enums.TipoEventoNotificacion;
import co.edu.cue.practicas.repository.encuesta.EncuestaRepository;
import co.edu.cue.practicas.repository.notificacion.PlantillaNotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Recordatorios automáticos de encuestas pendientes (RF-08-05, RF-08-06).
 *
 * Cada día a las 09:00 revisa encuestas PENDIENTE/EN_BORRADOR y, si han pasado
 * N días hábiles desde el último recordatorio (o la invitación inicial, si aún
 * no se ha enviado ninguno), encola un nuevo correo.
 *
 * La frecuencia N se toma de la plantilla del evento de recordatorio
 * configurada por el Administrador (RF-11-05). Por defecto 3 días hábiles.
 *
 * Si el cierre ya se ejecutó (Practica.notasCerradas = true) los recordatorios
 * automáticos se detienen aunque la encuesta esté pendiente.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EncuestaRecordatorioScheduler {

    private final EncuestaRepository encuestaRepository;
    private final PlantillaNotificacionRepository plantillaRepository;
    private final EncuestaService encuestaService;

    /** Diario a las 09:00. */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void enviarRecordatoriosDiarios() {
        List<Encuesta> activas = encuestaRepository.findByEstadoIn(
                List.of(EstadoEncuesta.PENDIENTE, EstadoEncuesta.EN_BORRADOR));

        for (Encuesta encuesta : activas) {
            if (cierreYaEjecutado(encuesta)) continue;
            if (encuestaService.yaSeEnvioRecordatorioHoy(encuesta)) continue;

            int frecuencia = frecuenciaParaEvento(encuesta.getTipo());
            if (debeEnviarse(encuesta, frecuencia)) {
                encuestaService.encolarRecordatorio(encuesta);
                encuesta.setUltimoRecordatorioEn(LocalDateTime.now());
                encuestaRepository.save(encuesta);
                log.info("[SCHEDULER] Recordatorio automático encolado para encuesta {}", encuesta.getId());
            }
        }
    }

    private boolean cierreYaEjecutado(Encuesta encuesta) {
        return Boolean.TRUE.equals(encuesta.getPractica().getNotasCerradas());
    }

    private boolean debeEnviarse(Encuesta encuesta, int frecuenciaDias) {
        LocalDateTime referencia = encuesta.getUltimoRecordatorioEn() != null
                ? encuesta.getUltimoRecordatorioEn()
                : encuesta.getInvitacionEnviadaEn();
        if (referencia == null) return true;

        long diasTranscurridos = ChronoUnit.DAYS.between(
                referencia.toLocalDate(), LocalDate.now());
        return diasTranscurridos >= frecuenciaDias;
    }

    private int frecuenciaParaEvento(TipoEncuesta tipo) {
        TipoEventoNotificacion evento = tipo == TipoEncuesta.TUTOR_SATISFACCION
                ? TipoEventoNotificacion.ENCUESTA_TUTOR_RECORDATORIO
                : TipoEventoNotificacion.ENCUESTA_ESTUDIANTE_RECORDATORIO;
        return plantillaRepository.findByEvento(evento)
                .map(PlantillaNotificacion::getFrecuenciaRecordatorioDias)
                .orElse(3);
    }
}
