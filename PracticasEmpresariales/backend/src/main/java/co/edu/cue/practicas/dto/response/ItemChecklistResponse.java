package co.edu.cue.practicas.dto.response;

import co.edu.cue.practicas.model.enums.TipoItemChecklist;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Estado de un item del checklist de cierre (RF-09-01).
 */
@Data
@Builder
public class ItemChecklistResponse {

    private TipoItemChecklist tipo;
    private String etiqueta;
    private boolean completado;
    /** Estado visual: COMPLETADO, EN_BORRADOR, PENDIENTE. */
    private String estado;
    /** Enlace o ruta directa a la acción requerida (si no está completado). */
    private String enlaceAccion;
    /** Para encuestas: marca de tiempo del último recordatorio enviado. */
    private LocalDateTime ultimoRecordatorioEn;
    /** Para encuestas: id de la encuesta para usar en el endpoint de recordatorio. */
    private Long encuestaId;
}
