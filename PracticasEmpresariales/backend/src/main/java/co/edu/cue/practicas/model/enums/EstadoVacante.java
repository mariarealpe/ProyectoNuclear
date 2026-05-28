package co.edu.cue.practicas.model.enums;

/**
 * Estados posibles de una vacante de práctica empresarial.
 *
 * Flujo válido de transiciones (PATRON STATE):
 *   PENDIENTE   → DISPONIBLE  (el Coordinador de Prácticas la aprueba)
 *   PENDIENTE   → RECHAZADA   (el Coordinador de Prácticas la rechaza con motivo)
 *   DISPONIBLE  → CERRADA     (se llenaron todos los cupos o el coordinador la cierra)
 *   RECHAZADA   → PENDIENTE   (la empresa la edita y la vuelve a enviar a aprobación)
 *
 * Los estados CERRADA y DISPONIBLE no pueden volver a PENDIENTE,
 * porque ya pasaron por el flujo de aprobación.
 */
public enum EstadoVacante {

    /** Recién creada por la empresa, esperando aprobación del Coordinador de Prácticas. */
    PENDIENTE,

    /** Aprobada y visible para estudiantes que cumplan los requisitos. */
    DISPONIBLE,

    /** Cerrada porque se llenaron los cupos o el coordinador la cerró manualmente. */
    CERRADA,

    /** Rechazada por el Coordinador de Prácticas con un motivo registrado. */
    RECHAZADA
}
