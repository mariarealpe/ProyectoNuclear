package co.edu.cue.practicas.event;

import co.edu.cue.practicas.model.entity.Vacante;
import org.springframework.context.ApplicationEvent;

/**
 * PATRON OBSERVER — GPE-152
 *
 * Publicado por VacanteService cuando una empresa registra una nueva vacante.
 * El listener NotificacionVacanteListener avisa al Coordinador de Prácticas
 * para que la revise — sin que VacanteService dependa de EmailService.
 */
public class VacanteCreadaEvent extends ApplicationEvent {

    private final Vacante vacante;

    public VacanteCreadaEvent(Object source, Vacante vacante) {
        super(source);
        this.vacante = vacante;
    }

    public Vacante getVacante() {
        return vacante;
    }
}
