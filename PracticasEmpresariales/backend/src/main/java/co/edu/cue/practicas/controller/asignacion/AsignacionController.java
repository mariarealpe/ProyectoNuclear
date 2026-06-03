package co.edu.cue.practicas.controller.asignacion;

import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.model.enums.EstadoAsignacion;
import co.edu.cue.practicas.service.asignacion.AsignacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/asignaciones")
@RequiredArgsConstructor
public class AsignacionController {

    private final AsignacionService asignacionService;

    /**
     * PATRON FACADE — GPE-157
     *
     * Postman envia estudiante, vacante y coordinador.
     * AsignacionService valida cupos/estado y registra trazabilidad inicial.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> crear(@RequestBody Map<String, Object> request) {
        Map<String, Object> creada = asignacionService.crear(
                longValue(request, "estudianteId"),
                longValue(request, "vacanteId"),
                longValue(request, "coordinadorId")
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Asignacion creada", creada));
    }

    /**
     * PATRON FACADE — GPE-158
     *
     * Lista asignaciones activas o filtradas por estado/coordinador.
     * El servicio encapsula el acceso a repositorios y devuelve una vista simple.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> listar(
            @RequestParam(required = false) EstadoAsignacion estado,
            @RequestParam(required = false) Long coordinadorId,
            @PageableDefault(size = 20, sort = "fechaAsignacion") Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.ok(asignacionService.listar(estado, coordinadorId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(asignacionService.obtener(id)));
    }

    /**
     * PATRON STATE + BITACORA — GPE-159
     *
     * Cambia el estado con transiciones controladas:
     * ASIGNADA -> EN_VINCULACION/CANCELADA, EN_VINCULACION -> EN_CURSO.
     * Cada cambio queda registrado en CambioEstadoAsignacion.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> actualizada = asignacionService.cambiarEstado(
                id,
                EstadoAsignacion.valueOf(stringValue(request, "estado")),
                longValue(request, "usuarioId"),
                optionalString(request, "motivo")
        );
        return ResponseEntity.ok(ApiResponse.ok("Estado de asignacion actualizado", actualizada));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(asignacionService.historial(id)));
    }

    private Long longValue(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private String stringValue(Map<String, Object> request, String key) {
        return String.valueOf(request.get(key));
    }

    private String optionalString(Map<String, Object> request, String key) {
        Object value = request.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
