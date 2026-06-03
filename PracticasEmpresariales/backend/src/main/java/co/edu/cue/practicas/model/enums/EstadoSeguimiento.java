package co.edu.cue.practicas.model.enums;

/**
 * Estados de un seguimiento semanal de práctica.
 * 
 * FLUJO: PENDIENTE → APROBADO / RECHAZADO
 * 
 * Si RECHAZADO: estudiante puede editar SOLO el seguimiento de la semana más reciente.
 * Seguimientos de semanas anteriores son inmutables.
 */
public enum EstadoSeguimiento {
    PENDIENTE,          // Esperando revisión por Docente Asesor
    APROBADO,           // Aprobado por Docente Asesor
    RECHAZADO           // Rechazado por Docente Asesor - estudiante puede corregir (solo semana actual)
}
