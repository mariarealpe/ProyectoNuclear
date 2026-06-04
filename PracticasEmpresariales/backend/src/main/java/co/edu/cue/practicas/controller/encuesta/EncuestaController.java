package co.edu.cue.practicas.controller.encuesta;

import co.edu.cue.practicas.dto.request.GuardarRespuestasEncuestaRequest;
import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.dto.response.EncuestaResponse;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.encuesta.EncuestaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-08-05 + RF-08-06: API de encuestas de cierre y autoevaluación.
 */
@RestController
@RequestMapping("/encuestas")
@RequiredArgsConstructor
public class EncuestaController {

    private final EncuestaService encuestaService;

    @PostMapping("/practica/{practicaId}/iniciar-cierre")
    public ResponseEntity<ApiResponse<List<EncuestaResponse>>> iniciarFaseCierre(
            @PathVariable Long practicaId) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Encuestas de cierre creadas",
                encuestaService.iniciarFaseCierre(practicaId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EncuestaResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(encuestaService.obtenerPorId(id)));
    }

    @GetMapping("/practica/{practicaId}")
    public ResponseEntity<ApiResponse<List<EncuestaResponse>>> listarPorPractica(
            @PathVariable Long practicaId) {
        return ResponseEntity.ok(ApiResponse.ok(encuestaService.listarPorPractica(practicaId)));
    }

    @GetMapping("/pendientes/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<List<EncuestaResponse>>> listarPendientesDelUsuario(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(ApiResponse.ok(
                encuestaService.listarPendientesDelUsuario(usuarioId)));
    }

    @PutMapping("/{id}/borrador")
    public ResponseEntity<ApiResponse<EncuestaResponse>> guardarBorrador(
            @PathVariable Long id,
            @Valid @RequestBody GuardarRespuestasEncuestaRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Borrador guardado",
                encuestaService.guardarBorradorTutor(id, request, userDetails)));
    }

    @PutMapping("/{id}/completar")
    public ResponseEntity<ApiResponse<EncuestaResponse>> completar(
            @PathVariable Long id,
            @Valid @RequestBody GuardarRespuestasEncuestaRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Encuesta completada",
                encuestaService.completar(id, request, userDetails)));
    }

    @PostMapping("/{id}/recordatorio")
    public ResponseEntity<ApiResponse<EncuestaResponse>> enviarRecordatorio(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Recordatorio enviado",
                encuestaService.enviarRecordatorio(id, userDetails)));
    }
}
