package co.edu.cue.practicas.service.vacante.state;

import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.entity.Vacante;
import co.edu.cue.practicas.model.enums.EstadoVacante;

import java.time.LocalDateTime;

/**
 * Estado inicial: la vacante fue creada por la empresa y espera revisión
 * del Coordinador de Prácticas.
 *
 * Transiciones permitidas:
 *   - aprobar()  → DISPONIBLE
 *   - rechazar() → RECHAZADA (motivo obligatorio)
 *
 * Transiciones prohibidas:
 *   - cerrar()  : una vacante PENDIENTE no se puede cerrar; no está publicada.
 *   - reabrir(): no aplica, ya está pendiente.
 */
public class PendienteState implements EstadoVacanteState {

    @Override
    public void aprobar(Vacante vacante, String motivo) {
        // Promovemos a DISPONIBLE; el motivo queda como observación opcional en la bitácora,
        // no se persiste en motivoRechazo porque no hubo rechazo.
        vacante.setEstado(EstadoVacante.DISPONIBLE);
        vacante.setMotivoRechazo(null);
        vacante.setFechaDecision(LocalDateTime.now());
    }

    @Override
    public void rechazar(Vacante vacante, String motivo) {
        // El motivo es obligatorio porque deja la traza de POR QUÉ se rechazó.
        if (motivo == null || motivo.isBlank()) {
            throw new OperacionNoPermitidaException("El motivo del rechazo es obligatorio.");
        }
        vacante.setEstado(EstadoVacante.RECHAZADA);
        vacante.setMotivoRechazo(motivo);
        vacante.setFechaDecision(LocalDateTime.now());
    }

    @Override
    public void cerrar(Vacante vacante, String motivo) {
        // Una vacante pendiente nunca llegó a publicarse, así que cerrarla no tiene sentido.
        throw new OperacionNoPermitidaException(
                "No se puede cerrar una vacante en estado PENDIENTE. Primero debe ser aprobada.");
    }

    @Override
    public void reabrir(Vacante vacante) {
        throw new OperacionNoPermitidaException("La vacante ya está pendiente de aprobación.");
    }
}
