package co.edu.cue.practicas.service.vacante.state;

import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.entity.Vacante;
import co.edu.cue.practicas.model.enums.EstadoVacante;

/**
 * Estado de rechazo: la vacante fue rechazada con un motivo y la empresa debe
 * corregirla antes de reenviarla a aprobación.
 *
 * Transiciones permitidas:
 *   - reabrir() → PENDIENTE (la empresa la edita y la reenvía)
 *
 * Transiciones prohibidas:
 *   - aprobar()  : el coordinador debe esperar a que la empresa la reenvíe corregida.
 *   - rechazar(): ya está rechazada.
 *   - cerrar()   : no está publicada.
 */
public class RechazadaState implements EstadoVacanteState {

    @Override
    public void aprobar(Vacante vacante, String motivo) {
        throw new OperacionNoPermitidaException(
                "La vacante está rechazada. La empresa debe corregirla y reenviarla a aprobación.");
    }

    @Override
    public void rechazar(Vacante vacante, String motivo) {
        throw new OperacionNoPermitidaException("La vacante ya está rechazada.");
    }

    @Override
    public void cerrar(Vacante vacante, String motivo) {
        throw new OperacionNoPermitidaException("La vacante está rechazada, no puede cerrarse.");
    }

    @Override
    public void reabrir(Vacante vacante) {
        // Limpiamos el motivo de rechazo porque la empresa la corrigió; vuelve al inicio del flujo.
        vacante.setEstado(EstadoVacante.PENDIENTE);
        vacante.setMotivoRechazo(null);
        vacante.setFechaDecision(null);
        vacante.setAprobador(null);
    }
}
