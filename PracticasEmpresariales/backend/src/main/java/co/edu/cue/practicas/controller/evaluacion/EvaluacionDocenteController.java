package co.edu.cue.practicas.controller.evaluacion;

import co.edu.cue.practicas.dto.request.RegistrarEvaluacionDocenteRequest;
import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.dto.response.EvaluacionDocenteResponse;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.evaluacion.EvaluacionDocenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * RF-08-01 — API de evaluación del Docente Asesor.
 *
 * Endpoints:
 *   POST  /evaluaciones-docente/practica/{practicaId}  → registrar evaluación
 *   PUT   /evaluaciones-docente/{id}                   → actualizar evaluación
 *   GET   /evaluaciones-docente/practica/{practicaId}  → consultar por práctica
 *   GET   /evaluaciones-docente/{id}                   → consultar por ID
 */
@RestController
@RequestMapping("/evaluaciones-docente")
@RequiredArgsConstructor
public class EvaluacionDocenteController {

    private final EvaluacionDocenteService evaluacionService;

    @PostMapping("/practica/{practicaId}")
    public ResponseEntity<ApiResponse<EvaluacionDocenteResponse>> registrar(
            @PathVariable Long practicaId,
            @Valid @RequestBody RegistrarEvaluacionDocenteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        EvaluacionDocenteResponse response = evaluacionService.registrar(practicaId, request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Evaluación registrada exitosamente", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EvaluacionDocenteResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarEvaluacionDocenteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        EvaluacionDocenteResponse response = evaluacionService.actualizar(id, request, userDetails);
        return ResponseEntity.ok(ApiResponse.ok("Evaluación actualizada exitosamente", response));
    }

    @GetMapping("/practica/{practicaId}")
    public ResponseEntity<ApiResponse<EvaluacionDocenteResponse>> obtenerPorPractica(
            @PathVariable Long practicaId) {

        return ResponseEntity.ok(ApiResponse.ok(evaluacionService.obtenerPorPractica(practicaId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EvaluacionDocenteResponse>> obtenerPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.ok(evaluacionService.obtenerPorId(id)));
    }
}
