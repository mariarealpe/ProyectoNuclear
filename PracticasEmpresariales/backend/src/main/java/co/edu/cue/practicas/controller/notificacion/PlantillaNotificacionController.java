package co.edu.cue.practicas.controller.notificacion;

import co.edu.cue.practicas.dto.request.ActualizarPlantillaNotificacionRequest;
import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.dto.response.PlantillaNotificacionResponse;
import co.edu.cue.practicas.model.enums.TipoEventoNotificacion;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.notificacion.PlantillaNotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * RF-11-05 — Endpoints para administrar y previsualizar plantillas de correo.
 */
@RestController
@RequestMapping("/plantillas-notificacion")
@RequiredArgsConstructor
public class PlantillaNotificacionController {

    private final PlantillaNotificacionService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlantillaNotificacionResponse>>> listarActivas() {
        return ResponseEntity.ok(ApiResponse.ok(service.listarActivas()));
    }

    @GetMapping("/{evento}")
    public ResponseEntity<ApiResponse<PlantillaNotificacionResponse>> obtener(
            @PathVariable TipoEventoNotificacion evento) {
        return ResponseEntity.ok(ApiResponse.ok(service.obtenerPorEvento(evento)));
    }

    @PutMapping("/{evento}")
    public ResponseEntity<ApiResponse<PlantillaNotificacionResponse>> actualizar(
            @PathVariable TipoEventoNotificacion evento,
            @Valid @RequestBody ActualizarPlantillaNotificacionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Plantilla actualizada",
                service.upsert(evento, request, userDetails)));
    }

    /**
     * Previsualiza un cuerpo HTML aplicando un mapa de variables.
     * Permite al Administrador ver cómo queda el correo antes de guardar.
     */
    @PostMapping("/previsualizar")
    public ResponseEntity<ApiResponse<String>> previsualizar(
            @RequestBody PreviewRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(
                service.previsualizar(request.cuerpoHtml(), request.variables())));
    }

    public record PreviewRequest(String cuerpoHtml, Map<String, String> variables) {}
}
