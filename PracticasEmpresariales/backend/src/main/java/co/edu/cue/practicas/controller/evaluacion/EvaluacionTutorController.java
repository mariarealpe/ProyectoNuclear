package co.edu.cue.practicas.controller.evaluacion;

import co.edu.cue.practicas.dto.request.RegistrarEvaluacionTutorRequest;
import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.dto.response.EvaluacionTutorResponse;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.evaluacion.EvaluacionTutorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * RF-08-02 — API de evaluación del Tutor Empresarial.
 *
 * Endpoints:
 *   POST  /evaluaciones-tutor/practica/{practicaId}  → registrar evaluación
 *   PUT   /evaluaciones-tutor/{id}                   → actualizar evaluación
 *   GET   /evaluaciones-tutor/practica/{practicaId}  → consultar por práctica
 *   GET   /evaluaciones-tutor/{id}                   → consultar por ID
 */
@RestController
@RequestMapping("/evaluaciones-tutor")
@RequiredArgsConstructor
public class EvaluacionTutorController {

    private final EvaluacionTutorService evaluacionService;

    @PostMapping("/practica/{practicaId}")
    public ResponseEntity<ApiResponse<EvaluacionTutorResponse>> registrar(
            @PathVariable Long practicaId,
            @Valid @RequestBody RegistrarEvaluacionTutorRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        EvaluacionTutorResponse response = evaluacionService.registrar(practicaId, request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Evaluación registrada exitosamente", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EvaluacionTutorResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarEvaluacionTutorRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        EvaluacionTutorResponse response = evaluacionService.actualizar(id, request, userDetails);
        return ResponseEntity.ok(ApiResponse.ok("Evaluación actualizada exitosamente", response));
    }

    @GetMapping("/practica/{practicaId}")
    public ResponseEntity<ApiResponse<EvaluacionTutorResponse>> obtenerPorPractica(
            @PathVariable Long practicaId) {

        return ResponseEntity.ok(ApiResponse.ok(evaluacionService.obtenerPorPractica(practicaId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EvaluacionTutorResponse>> obtenerPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.ok(evaluacionService.obtenerPorId(id)));
    }
}
