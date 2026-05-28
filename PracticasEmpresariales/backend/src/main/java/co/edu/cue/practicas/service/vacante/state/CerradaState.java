package co.edu.cue.practicas.service.vacante.state;

import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.entity.Vacante;

/**
 * Estado final: la vacante fue cerrada y ya no acepta cambios.
 *
 * Una vez cerrada se conserva como histórico para reportes y auditoría.
 * Si se necesita reactivarla, hay que crear una nueva vacante.
 */
public class CerradaState implements EstadoVacanteState {

    @Override
    public void aprobar(Vacante vacante, String motivo) {
        throw new OperacionNoPermitidaException("La vacante está cerrada y no puede modificarse.");
    }

    @Override
    public void rechazar(Vacante vacante, String motivo) {
        throw new OperacionNoPermitidaException("La vacante está cerrada y no puede modificarse.");
    }

    @Override
    public void cerrar(Vacante vacante, String motivo) {
        throw new OperacionNoPermitidaException("La vacante ya está cerrada.");
    }

    @Override
    public void reabrir(Vacante vacante) {
        // Decisión de negocio: no permitimos reabrir vacantes cerradas — se debe crear una nueva.
        throw new OperacionNoPermitidaException(
                "Una vacante cerrada no se puede reabrir. Debe crear una nueva vacante.");
    }
}
