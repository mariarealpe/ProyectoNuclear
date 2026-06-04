package co.edu.cue.practicas.controller.configuracion;

import co.edu.cue.practicas.dto.request.ActualizarConfiguracionProgramaRequest;
import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.dto.response.ConfiguracionProgramaResponse;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.configuracion.ConfiguracionProgramaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/programas/{programaId}/configuracion")
@RequiredArgsConstructor
public class ConfiguracionProgramaController {

    private final ConfiguracionProgramaService configuracionProgramaService;

    @GetMapping
    public ResponseEntity<ApiResponse<ConfiguracionProgramaResponse>> obtener(@PathVariable Long programaId) {
        return ResponseEntity.ok(ApiResponse.ok(configuracionProgramaService.obtenerPorPrograma(programaId)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ConfiguracionProgramaResponse>> actualizar(
            @PathVariable Long programaId,
            @Valid @RequestBody ActualizarConfiguracionProgramaRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.ok("Configuración del programa actualizada",
                configuracionProgramaService.actualizarPorPrograma(programaId, request, userDetails)));
    }
}
