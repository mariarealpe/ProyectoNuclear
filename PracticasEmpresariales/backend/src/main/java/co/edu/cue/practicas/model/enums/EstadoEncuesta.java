package co.edu.cue.practicas.model.enums;

/**
 * Estado del ciclo de vida de una encuesta de cierre (Patrón State).
 *
 * Transiciones permitidas:
 *  PENDIENTE → EN_BORRADOR (solo TUTOR_SATISFACCION) → COMPLETADA
 *  PENDIENTE → COMPLETADA (estudiante)
 *
 * Una encuesta COMPLETADA es inmutable (Patrón Proxy en el servicio).
 */
public enum EstadoEncuesta {
    PENDIENTE,
    EN_BORRADOR,
    COMPLETADA
}
