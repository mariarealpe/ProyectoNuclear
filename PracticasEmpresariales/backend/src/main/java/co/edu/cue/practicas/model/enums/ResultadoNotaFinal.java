package co.edu.cue.practicas.model.enums;

/**
 * Resultado de la nota final registrada por el Coordinador (RF-08-04).
 * El Coordinador NO realiza cálculos automáticos: ingresa manualmente la nota
 * y el sistema clasifica el resultado contra ConfiguracionPrograma.notaMinimaAprobacion.
 */
public enum ResultadoNotaFinal {

    APROBADO,   // nota >= notaMinimaAprobacion
    REPROBADO   // nota <  notaMinimaAprobacion
}
