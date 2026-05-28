package co.edu.cue.practicas.controller.empresa;

import co.edu.cue.practicas.dto.request.CrearEmpresaRequest;
import co.edu.cue.practicas.dto.request.EditarEmpresaRequest;
import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.dto.response.EmpresaResponse;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.empresa.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmpresaResponse>> crear(
            @Valid @RequestBody CrearEmpresaRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        EmpresaResponse creada = empresaService.crearEmpresa(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Empresa registrada exitosamente", creada));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EmpresaResponse>>> listar(
            @PageableDefault(size = 20, sort = "razonSocial") Pageable pageable,
            @RequestParam(required = false) String filtro) {

        return ResponseEntity.ok(ApiResponse.ok(empresaService.listar(pageable, filtro)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(empresaService.obtenerPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaResponse>> editar(
            @PathVariable Long id,
            @Valid @RequestBody EditarEmpresaRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.ok("Empresa actualizada",
                empresaService.editarEmpresa(id, request, userDetails)));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<Void>> desactivar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        empresaService.desactivarEmpresa(id, userDetails);
        return ResponseEntity.ok(ApiResponse.ok("Empresa desactivada", null));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<Void>> activar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        empresaService.activarEmpresa(id, userDetails);
        return ResponseEntity.ok(ApiResponse.ok("Empresa activada", null));
    }
}
