package co.edu.cue.practicas.event;

import co.edu.cue.practicas.model.entity.NotaFinal;
import co.edu.cue.practicas.model.entity.Practica;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento publicado cuando el Coordinador ejecuta el cierre formal de una
 * práctica (RF-09-02). Lo consumen los Observers (Coordinación Académica,
 * dashboard, etc.) — RF-09-03.
 */
@Getter
public class CierrePracticaEvent extends ApplicationEvent {

    private final Practica practica;
    private final NotaFinal notaFinal;

    public CierrePracticaEvent(Object source, Practica practica, NotaFinal notaFinal) {
        super(source);
        this.practica = practica;
        this.notaFinal = notaFinal;
    }
}
