package co.edu.cue.practicas.model.enums;

/**
 * Estados posibles de una asignación de estudiante a vacante.
 * 
 * FLUJO: ASIGNADA → EN_VINCULACION → EN_CURSO
 *                    ↓
 *                CANCELADA (si no completa vinculación)
 */
public enum EstadoAsignacion {
    ASIGNADA,           // Coordinador acaba de asignar estudiante a vacante
    EN_VINCULACION,     // Documentos cargados, esperando confirmación de vinculación
    EN_CURSO,           // Vinculación confirmada, práctica activa (transiciona a Practica.EN_CURSO)
    CANCELADA           // Cancelada por Coordinador antes de activación
}
