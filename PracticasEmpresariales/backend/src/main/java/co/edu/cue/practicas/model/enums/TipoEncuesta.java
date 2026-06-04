package co.edu.cue.practicas.model.enums;

/**
 * Tipos de encuesta de cierre (RF-08-05 / RF-08-06).
 *
 * - TUTOR_SATISFACCION: encuesta del Tutor Empresarial sobre el practicante
 *   y el proceso (admite borrador).
 * - ESTUDIANTE_SATISFACCION: encuesta del estudiante sobre la empresa,
 *   el Tutor y el proceso (sin borrador, inmutable al enviar).
 * - ESTUDIANTE_AUTOEVALUACION: autoevaluación del estudiante sobre su
 *   propio desempeño (sin borrador, inmutable al enviar).
 */
public enum TipoEncuesta {
    TUTOR_SATISFACCION,
    ESTUDIANTE_SATISFACCION,
    ESTUDIANTE_AUTOEVALUACION
}
