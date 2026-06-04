package co.edu.cue.practicas.model.enums;

/**
 * Items del checklist de cierre formal (RF-09-01).
 *
 * El checklist se verifica como Chain of Responsibility: cada item es un
 * handler que retorna OK / pendiente con su enlace de acción.
 */
public enum TipoItemChecklist {
    NOTA_DOCENTE,
    NOTA_TUTOR,
    NOTA_FINAL,
    ENCUESTA_TUTOR,
    ENCUESTA_ESTUDIANTE_SATISFACCION,
    ENCUESTA_ESTUDIANTE_AUTOEVALUACION,
    DOCUMENTOS_REQUERIDOS,
    INFORME_FINAL
}
