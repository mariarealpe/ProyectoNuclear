package co.edu.cue.practicas.controller.cierre;

import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.dto.response.ChecklistCierreResponse;
import co.edu.cue.practicas.dto.response.ItemChecklistResponse;
import co.edu.cue.practicas.model.enums.TipoItemChecklist;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.cierre.ChecklistCierreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * RF-09-01 — Endpoints del checklist de cierre.
 */
@RestController
@RequestMapping("/cierre/checklist")
@RequiredArgsConstructor
public class ChecklistCierreController {

    private final ChecklistCierreService checklistService;

    @GetMapping("/practica/{practicaId}")
    public ResponseEntity<ApiResponse<ChecklistCierreResponse>> obtener(
            @PathVariable Long practicaId) {
        return ResponseEntity.ok(ApiResponse.ok(
                checklistService.evaluarChecklist(practicaId)));
    }

    @PostMapping("/practica/{practicaId}/recordatorio/{item}")
    public ResponseEntity<ApiResponse<ItemChecklistResponse>> enviarRecordatorio(
            @PathVariable Long practicaId,
            @PathVariable TipoItemChecklist item,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Recordatorio enviado",
                checklistService.enviarRecordatorioItem(practicaId, item, userDetails)));
    }
}
