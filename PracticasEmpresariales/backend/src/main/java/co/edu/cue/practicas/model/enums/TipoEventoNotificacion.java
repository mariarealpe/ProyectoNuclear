package co.edu.cue.practicas.model.enums;

/**
 * Eventos del sistema que disparan envío de correo (RF-11-05).
 * Cada evento tiene una plantilla HTML independiente y reglas propias
 * (rol receptor, obligatorio/informativo, frecuencia de recordatorio).
 */
public enum TipoEventoNotificacion {
    NUEVA_ASIGNACION,
    VINCULACION_CONFIRMADA,
    PLAN_PRACTICA_APROBADO,
    PLAN_PRACTICA_RECHAZADO,
    SEGUIMIENTO_RECHAZADO,
    ALERTA_INACTIVIDAD,
    ENCUESTA_TUTOR_INVITACION,
    ENCUESTA_TUTOR_RECORDATORIO,
    ENCUESTA_ESTUDIANTE_INVITACION,
    ENCUESTA_ESTUDIANTE_RECORDATORIO,
    CIERRE_PRACTICA,
    NOTIFICACION_GENERICA
}
