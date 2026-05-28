package co.edu.cue.practicas.event;

import co.edu.cue.practicas.model.entity.Vacante;
import org.springframework.context.ApplicationEvent;

/**
 * PATRON OBSERVER — GPE-153
 *
 * Publicado cuando el Coordinador de Prácticas rechaza una vacante.
 * Se incluye el motivo para que el listener pueda incluirlo en el correo
 * que enviará a la empresa.
 */
public class VacanteRechazadaEvent extends ApplicationEvent {

    private final Vacante vacante;
    private final String motivo;

    public VacanteRechazadaEvent(Object source, Vacante vacante, String motivo) {
        super(source);
        this.vacante = vacante;
        this.motivo = motivo;
    }

    public Vacante getVacante() {
        return vacante;
    }

    public String getMotivo() {
        return motivo;
    }
}
