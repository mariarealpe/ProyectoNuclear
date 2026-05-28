package co.edu.cue.practicas.model.enums;

/**
 * Estado de la práctica empresarial.
 * El estado solo avanza: PENDIENTE_ASIGNACION → EN_CURSO → FINALIZADA → CANCELADA.
 */
public enum EstadoPractica {
    PENDIENTE_ASIGNACION,
    EN_CURSO,
    FINALIZADA,
    CANCELADA
}
