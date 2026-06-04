package co.edu.cue.practicas.event.listener;

import co.edu.cue.practicas.event.CierrePracticaEvent;
import co.edu.cue.practicas.model.entity.NotaFinal;
import co.edu.cue.practicas.model.entity.NotificacionHistorial;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoNotificacion;
import co.edu.cue.practicas.model.enums.ResultadoNotaFinal;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoNotificacion;
import co.edu.cue.practicas.repository.notificacion.NotificacionHistorialRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RF-09-03 — Observer que reacciona al cierre formal de una práctica.
 *
 * Comportamiento:
 *  - Notifica a TODOS los usuarios COORDINACION_ACADEMICA activos de la
 *    facultad del estudiante con el resultado (incluye nombre, número de
 *    práctica, nota y estado).
 *  - Si el resultado es REPROBADO, deja constancia que la decisión sobre
 *    habilitar la siguiente práctica requiere acción explícita de
 *    Coordinación Académica (no se habilita automáticamente).
 *
 * El estado "Práctica N Completada / Reprobada" se deriva en tiempo de
 * consulta a partir de NotaFinal.resultado + Practica.estado, por lo que
 * NO se persiste un campo nuevo en Usuario — el listener solo dispara la
 * notificación a Coordinación Académica.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActualizacionEstadoEstudianteListener {

    private final UsuarioRepository usuarioRepository;
    private final NotificacionHistorialRepository notificacionRepository;

    @Async
    @EventListener
    @Transactional
    public void alCerrarPractica(CierrePracticaEvent event) {
        Practica practica = event.getPractica();
        NotaFinal notaFinal = event.getNotaFinal();

        Long facultadId = facultadDelEstudiante(practica);
        if (facultadId == null) {
            log.warn("[RF-09-03] Estudiante {} no tiene facultad — no se notifica a Coordinación Académica",
                    practica.getEstudiante().getId());
            return;
        }

        List<Usuario> coordinadores = usuarioRepository
                .findByRolAndFacultad_IdAndActivoTrue(Rol.COORDINACION_ACADEMICA, facultadId);

        if (coordinadores.isEmpty()) {
            log.warn("[RF-09-03] Sin coordinadores académicos activos en facultad {} — sin notificación", facultadId);
            return;
        }

        String etiqueta = etiquetaResultado(practica, notaFinal);
        String asunto = "Cierre de práctica — " + practica.getEstudiante().getNombre()
                + " — " + etiqueta;
        String cuerpo = construirCuerpoNotificacion(practica, notaFinal, etiqueta);

        for (Usuario coord : coordinadores) {
            NotificacionHistorial n = NotificacionHistorial.builder()
                    .usuarioDestino(coord)
                    .tipo(TipoNotificacion.OTRO)
                    .correoDestino(coord.getCorreo())
                    .asunto(asunto)
                    .cuerpo(cuerpo)
                    .estado(EstadoNotificacion.PENDIENTE)
                    .practica(practica)
                    .build();
            notificacionRepository.save(n);
        }

        log.info("[RF-09-03] Notificadas {} coordinaciones académicas del cierre de práctica {} ({})",
                coordinadores.size(), practica.getId(), etiqueta);
    }

    private Long facultadDelEstudiante(Practica practica) {
        if (practica.getEstudiante() == null) return null;
        if (practica.getEstudiante().getPrograma() != null
                && practica.getEstudiante().getPrograma().getFacultad() != null) {
            return practica.getEstudiante().getPrograma().getFacultad().getId();
        }
        return practica.getEstudiante().getFacultad() != null
                ? practica.getEstudiante().getFacultad().getId()
                : null;
    }

    private String etiquetaResultado(Practica practica, NotaFinal notaFinal) {
        return notaFinal.getResultado() == ResultadoNotaFinal.APROBADO
                ? "Práctica " + practica.getNumeroPractica() + " Completada"
                : "Práctica " + practica.getNumeroPractica() + " Reprobada";
    }

    private String construirCuerpoNotificacion(Practica practica, NotaFinal notaFinal, String etiqueta) {
        String nota = notaFinal.getResultado() == ResultadoNotaFinal.REPROBADO
                ? "<p><strong>Importante:</strong> al ser un resultado REPROBADO, "
                  + "la habilitación de la siguiente práctica requiere decisión explícita "
                  + "de Coordinación Académica.</p>"
                : "";
        return """
                <p>Se ha cerrado formalmente la práctica del estudiante <strong>%s</strong>.</p>
                <ul>
                  <li>Número de práctica: %d</li>
                  <li>Nota final: %s</li>
                  <li>Resultado: <strong>%s</strong></li>
                  <li>Etiqueta: %s</li>
                </ul>
                %s
                """.formatted(
                practica.getEstudiante().getNombre(),
                practica.getNumeroPractica(),
                notaFinal.getNota(),
                notaFinal.getResultado(),
                etiqueta,
                nota);
    }
}
