package co.edu.cue.practicas.controller.cierre;

import co.edu.cue.practicas.dto.request.EjecutarCierrePracticaRequest;
import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.dto.response.CierrePracticaResponse;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.cierre.CierrePracticaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * RF-09-02 — Endpoint del cierre formal de práctica.
 */
@RestController
@RequestMapping("/cierre/practica")
@RequiredArgsConstructor
public class CierrePracticaController {

    private final CierrePracticaService cierreService;

    @PostMapping("/{practicaId}/ejecutar")
    public ResponseEntity<ApiResponse<CierrePracticaResponse>> ejecutar(
            @PathVariable Long practicaId,
            @Valid @RequestBody EjecutarCierrePracticaRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Cierre formal ejecutado",
                cierreService.ejecutarCierre(practicaId, request, userDetails)));
    }
}
