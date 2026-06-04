package co.edu.cue.practicas.model.enums;

/**
 * Tipos de documentos en el expediente de práctica.
 */
public enum TipoDocumento {
    CARTA_PRESENTACION,  // Carta de presentación dirigida a empresa
    CONVENIO,            // Convenio/contrato de práctica firmado
    PLAN,                // Plan de práctica (PDF generado del plan estructurado)
    EVIDENCIA,           // Evidencias de seguimiento (fotos, reportes, etc)
    INFORME_FINAL,       // Informe final del estudiante para cierre (RF-09-01)
    ACTA_CIERRE,         // Acta de cierre opcional cargada por el Coordinador (RF-09-02)
    OTRO                 // Otro tipo de documento
}
