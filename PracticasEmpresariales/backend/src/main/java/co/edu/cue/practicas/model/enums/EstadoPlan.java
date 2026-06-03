package co.edu.cue.practicas.model.enums;

/**
 * Estados del Plan de Práctica.
 * 
 * El seguimiento semanal NO puede iniciarse sin que el plan esté APROBADO_DOCENTE.
 * 
 * FLUJO: BORRADOR → APROBADO_TUTOR → APROBADO_DOCENTE (DESBLOQUEADOR)
 *                ↓ (si rechaza)
 *              RECHAZADO → BORRADOR (estudiante edita y resubmite)
 */
public enum EstadoPlan {
    BORRADOR,           // Creado por estudiante, pendiente revisión
    APROBADO_TUTOR,     // Aprobado por Tutor Empresarial
    APROBADO_DOCENTE,   // Aprobado por Docente Asesor - DESBLOQUEADOR para seguimiento
    RECHAZADO           // Rechazado (por Tutor o Docente) - estudiante puede editar
}
