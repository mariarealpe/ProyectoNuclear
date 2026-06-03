package co.edu.cue.practicas.model.enums;

/**
 * Tipos de notificaciones por correo en el sistema.
 * Cada tipo gatilla en eventos diferentes del flujo de asignación y seguimiento.
 */
public enum TipoNotificacion {
    ASIGNACION_CREADA,           // Nuevo estudiante asignado a vacante
    ASIGNACION_CANCELADA,        // Asignación cancelada por Coordinador
    VINCULACION_CONFIRMADA,      // Vinculación confirmada, práctica EN_CURSO
    PLAN_APROBADO,               // Plan de práctica aprobado
    PLAN_RECHAZADO,              // Plan de práctica rechazado
    SEGUIMIENTO_RECHAZADO,       // Seguimiento rechazado, requiere corrección
    ALERTA_INACTIVIDAD,          // Estudiante sin actividad N días
    OTRO                         // Otro tipo de notificación
}
