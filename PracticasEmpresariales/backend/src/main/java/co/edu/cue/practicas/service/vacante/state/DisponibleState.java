package co.edu.cue.practicas.service.vacante.state;

import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.entity.Vacante;
import co.edu.cue.practicas.model.enums.EstadoVacante;

import java.time.LocalDateTime;

/**
 * Estado activo: la vacante ya fue aprobada y está visible para estudiantes aptos.
 *
 * Transiciones permitidas:
 *   - cerrar()   → CERRADA (cupos llenos o cierre manual)
 *
 * Transiciones prohibidas:
 *   - aprobar()  : ya está aprobada.
 *   - rechazar(): no se puede rechazar una vacante ya publicada; debe cerrarse.
 *   - reabrir(): no aplica, ya está disponible.
 */
public class DisponibleState implements EstadoVacanteState {

    @Override
    public void aprobar(Vacante vacante, String motivo) {
        throw new OperacionNoPermitidaException("La vacante ya está DISPONIBLE.");
    }

    @Override
    public void rechazar(Vacante vacante, String motivo) {
        // Si ya se publicó y un estudiante pudo verla, rechazarla retroactivamente
        // sería confuso. Para retirarla del flujo se usa cerrar() con motivo.
        throw new OperacionNoPermitidaException(
                "No se puede rechazar una vacante DISPONIBLE. Use 'Cerrar vacante' para retirarla del catálogo.");
    }

    @Override
    public void cerrar(Vacante vacante, String motivo) {
        vacante.setEstado(EstadoVacante.CERRADA);
        vacante.setFechaCierre(LocalDateTime.now());
        vacante.setFechaDecision(LocalDateTime.now());
    }

    @Override
    public void reabrir(Vacante vacante) {
        throw new OperacionNoPermitidaException("La vacante ya está disponible.");
    }
}
