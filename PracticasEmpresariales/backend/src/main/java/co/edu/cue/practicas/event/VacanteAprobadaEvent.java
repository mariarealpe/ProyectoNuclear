package co.edu.cue.practicas.event;

import co.edu.cue.practicas.model.entity.Vacante;
import org.springframework.context.ApplicationEvent;

/**
 * PATRON OBSERVER — GPE-153
 *
 * Publicado cuando el Coordinador de Prácticas aprueba una vacante.
 * El listener notifica a la empresa que su vacante ya está visible para estudiantes.
 */
public class VacanteAprobadaEvent extends ApplicationEvent {

    private final Vacante vacante;
    private final String observacion;

    public VacanteAprobadaEvent(Object source, Vacante vacante, String observacion) {
        super(source);
        this.vacante = vacante;
        this.observacion = observacion;
    }

    public Vacante getVacante() {
        return vacante;
    }

    public String getObservacion() {
        return observacion;
    }
}
