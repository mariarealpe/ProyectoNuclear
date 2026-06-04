package co.edu.cue.practicas.model.enums;

/**
 * Resultado de la evaluación del Docente Asesor al finalizar o en el corte de la práctica.
 * Se calcula comparando la nota ingresada contra ConfiguracionPrograma.notaMinimaAprobacion.
 */
public enum ResultadoEvaluacion {

    APROBADO,     // nota >= notaMinimaAprobacion
    DESAPROBADO   // nota <  notaMinimaAprobacion
}
