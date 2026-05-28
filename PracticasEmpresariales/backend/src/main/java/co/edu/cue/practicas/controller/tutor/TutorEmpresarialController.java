package co.edu.cue.practicas.controller.tutor;

import co.edu.cue.practicas.dto.request.CrearTutorEmpresarialRequest;
import co.edu.cue.practicas.dto.request.EditarTutorEmpresarialRequest;
import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.dto.response.TutorEmpresarialResponse;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.tutor.TutorEmpresarialService;
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
@RequestMapping("/tutores")
@RequiredArgsConstructor
public class TutorEmpresarialController {

    private final TutorEmpresarialService tutorService;

    @PostMapping
    public ResponseEntity<ApiResponse<TutorEmpresarialResponse>> crear(
            @Valid @RequestBody CrearTutorEmpresarialRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TutorEmpresarialResponse creado = tutorService.crearTutor(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tutor empresarial creado", creado));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TutorEmpresarialResponse>>> listar(
            @PageableDefault(size = 20, sort = "id") Pageable pageable,
            @RequestParam(required = false) Long empresaId) {

        return ResponseEntity.ok(ApiResponse.ok(tutorService.listar(empresaId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TutorEmpresarialResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(tutorService.obtenerPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TutorEmpresarialResponse>> editar(
            @PathVariable Long id,
            @Valid @RequestBody EditarTutorEmpresarialRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.ok("Tutor actualizado",
                tutorService.editarTutor(id, request, userDetails)));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<Void>> desactivar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        tutorService.desactivarTutor(id, userDetails);
        return ResponseEntity.ok(ApiResponse.ok("Tutor desactivado", null));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<Void>> activar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        tutorService.activarTutor(id, userDetails);
        return ResponseEntity.ok(ApiResponse.ok("Tutor activado", null));
    }
}
