package co.edu.cue.practicas.controller.vacante;

import co.edu.cue.practicas.dto.request.CrearVacanteRequest;
import co.edu.cue.practicas.dto.request.DecisionVacanteRequest;
import co.edu.cue.practicas.dto.request.EditarVacanteRequest;
import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.dto.response.BitacoraVacanteResponse;
import co.edu.cue.practicas.dto.response.VacanteResponse;
import co.edu.cue.practicas.model.enums.EstadoVacante;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.vacante.VacanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vacantes")
@RequiredArgsConstructor
public class VacanteController {

    private final VacanteService vacanteService;

    @PostMapping
    public ResponseEntity<ApiResponse<VacanteResponse>> crear(
            @Valid @RequestBody CrearVacanteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        VacanteResponse creada = vacanteService.crearVacante(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Vacante registrada — pendiente de aprobación", creada));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VacanteResponse>>> listar(
            @PageableDefault(size = 20, sort = "fechaPublicacion") Pageable pageable,
            @RequestParam(required = false) EstadoVacante estado,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.ok(vacanteService.listar(estado, pageable, userDetails)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VacanteResponse>> obtener(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.ok(vacanteService.obtenerPorId(id, userDetails)));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<ApiResponse<List<BitacoraVacanteResponse>>> historial(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.ok(vacanteService.historial(id, userDetails)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VacanteResponse>> editar(
            @PathVariable Long id,
            @Valid @RequestBody EditarVacanteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.ok("Vacante actualizada",
                vacanteService.editarVacante(id, request, userDetails)));
    }

    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<ApiResponse<VacanteResponse>> aprobar(
            @PathVariable Long id,
            @RequestBody(required = false) DecisionVacanteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.ok("Vacante aprobada",
                vacanteService.aprobarVacante(id, request, userDetails)));
    }

    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<ApiResponse<VacanteResponse>> rechazar(
            @PathVariable Long id,
            @RequestBody DecisionVacanteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.ok("Vacante rechazada",
                vacanteService.rechazarVacante(id, request, userDetails)));
    }

    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<ApiResponse<VacanteResponse>> cerrar(
            @PathVariable Long id,
            @RequestBody(required = false) DecisionVacanteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.ok("Vacante cerrada",
                vacanteService.cerrarVacante(id, request, userDetails)));
    }
}
