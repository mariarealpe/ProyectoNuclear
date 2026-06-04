package co.edu.cue.practicas.controller.evaluacion;

import co.edu.cue.practicas.dto.request.RegistrarNotaFinalRequest;
import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.dto.response.NotaFinalResponse;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.evaluacion.NotaFinalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * RF-08-04 — API de la nota final.
 *
 * Endpoints:
 *   POST  /notas-finales/practica/{practicaId}  → registrar nota final
 *   PUT   /notas-finales/{id}                   → actualizar (solo si no cerrada)
 *   POST  /notas-finales/{id}/cerrar            → cierre formal (irreversible)
 *   GET   /notas-finales/practica/{practicaId}  → consultar por práctica (incluye notas referencia)
 *   GET   /notas-finales/{id}                   → consultar por ID
 */
@RestController
@RequestMapping("/notas-finales")
@RequiredArgsConstructor
public class NotaFinalController {

    private final NotaFinalService notaFinalService;

    @PostMapping("/practica/{practicaId}")
    public ResponseEntity<ApiResponse<NotaFinalResponse>> registrar(
            @PathVariable Long practicaId,
            @Valid @RequestBody RegistrarNotaFinalRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        NotaFinalResponse response = notaFinalService.registrar(practicaId, request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Nota final registrada exitosamente", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NotaFinalResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarNotaFinalRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        NotaFinalResponse response = notaFinalService.actualizar(id, request, userDetails);
        return ResponseEntity.ok(ApiResponse.ok("Nota final actualizada exitosamente", response));
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<ApiResponse<NotaFinalResponse>> cerrar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        NotaFinalResponse response = notaFinalService.cerrar(id, userDetails);
        return ResponseEntity.ok(ApiResponse.ok("Proceso de evaluación cerrado", response));
    }

    @GetMapping("/practica/{practicaId}")
    public ResponseEntity<ApiResponse<NotaFinalResponse>> obtenerPorPractica(
            @PathVariable Long practicaId) {

        return ResponseEntity.ok(ApiResponse.ok(notaFinalService.obtenerPorPractica(practicaId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotaFinalResponse>> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(notaFinalService.obtenerPorId(id)));
    }
}
