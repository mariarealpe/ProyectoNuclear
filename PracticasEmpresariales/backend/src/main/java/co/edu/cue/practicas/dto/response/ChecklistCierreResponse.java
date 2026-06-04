package co.edu.cue.practicas.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Estado completo del checklist de cierre para una práctica (RF-09-01).
 * Incluye la decisión global sobre habilitar el botón de "Ejecutar cierre".
 */
@Data
@Builder
public class ChecklistCierreResponse {

    private Long practicaId;
    private String nombreEstudiante;
    private List<ItemChecklistResponse> items;
    private boolean puedeEjecutarCierre;
    private int totalItems;
    private int itemsCompletados;
}
