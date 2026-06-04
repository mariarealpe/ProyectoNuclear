package co.edu.cue.practicas.controller.practica;

import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.service.practica.PracticaSprint3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/practicas")
@RequiredArgsConstructor
public class PracticaSprint3Controller {

    private final PracticaSprint3Service practicaService;

    /**
     * PATRON FACADE — GPE-164
     *
     * Prepara una practica desde una asignacion existente.
     * El servicio cambia la asignacion a EN_VINCULACION y deja lista la practica
     * para cargar carta de presentacion y convenio.
     */
    @PostMapping("/desde-asignacion/{asignacionId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> preparar(
            @PathVariable Long asignacionId,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> practica = practicaService.prepararDesdeAsignacion(
                asignacionId,
                longValue(request, "usuarioId")
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Practica preparada para vinculacion", practica));
    }

    /**
     * PATRON STATE + FACADE — GPE-164
     *
     * Confirma la vinculacion solo si el convenio tiene sus 3 firmas.
     * Al confirmar, la practica queda EN_CURSO y la asignacion avanza a EN_CURSO.
     */
    @PatchMapping("/{id}/confirmar-vinculacion")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirmar(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> practica = practicaService.confirmarVinculacion(
                id,
                longValue(request, "usuarioId"),
                longValue(request, "docenteAsesorId"),
                dateTimeValue(request, "fechaInicio"),
                dateTimeValue(request, "fechaFin")
        );
        return ResponseEntity.ok(ApiResponse.ok("Vinculacion confirmada", practica));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(practicaService.obtener(id)));
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listarPorEstudiante(@PathVariable Long estudianteId) {
        return ResponseEntity.ok(ApiResponse.ok(practicaService.listarPorEstudiante(estudianteId)));
    }

    private Long longValue(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private LocalDateTime dateTimeValue(Map<String, Object> request, String key) {
        Object value = request.get(key);
        return value == null ? null : LocalDateTime.parse(String.valueOf(value));
    }
}
