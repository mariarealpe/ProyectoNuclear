package co.edu.cue.practicas.controller.seguimiento;

import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.model.enums.EstadoSeguimiento;
import co.edu.cue.practicas.service.seguimiento.SeguimientoPracticaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/seguimientos")
@RequiredArgsConstructor
public class SeguimientoPracticaController {

    private final SeguimientoPracticaService seguimientoService;

    /**
     * PATRON FACADE — GPE-170
     *
     * El estudiante registra su bitacora semanal.
     * SeguimientoPracticaService valida practica EN_CURSO y evita semanas duplicadas.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> crear(@RequestBody Map<String, Object> request) {
        Map<String, Object> seguimiento = seguimientoService.crear(
                longValue(request, "practicaId"),
                longValue(request, "estudianteId"),
                integerValue(request, "semana"),
                stringValue(request, "actividades"),
                stringValue(request, "logros"),
                optionalString(request, "dificultades")
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Seguimiento semanal registrado", seguimiento));
    }

    /**
     * PATRON FACADE — GPE-168
     *
     * El Docente Asesor aprueba o rechaza y agrega observaciones.
     * Si rechaza, se crea una notificacion para el estudiante.
     */
    @PatchMapping("/{id}/revisar")
    public ResponseEntity<ApiResponse<Map<String, Object>>> revisar(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> seguimiento = seguimientoService.revisar(
                id,
                longValue(request, "docenteId"),
                EstadoSeguimiento.valueOf(stringValue(request, "estado")),
                optionalString(request, "observaciones")
        );
        return ResponseEntity.ok(ApiResponse.ok("Seguimiento revisado", seguimiento));
    }

    @PatchMapping("/{id}/corregir")
    public ResponseEntity<ApiResponse<Map<String, Object>>> corregir(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> seguimiento = seguimientoService.corregirRechazado(
                id,
                stringValue(request, "actividades"),
                stringValue(request, "logros"),
                optionalString(request, "dificultades")
        );
        return ResponseEntity.ok(ApiResponse.ok("Seguimiento corregido y reenviado", seguimiento));
    }

    @GetMapping("/practica/{practicaId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listarPorPractica(@PathVariable Long practicaId) {
        return ResponseEntity.ok(ApiResponse.ok(seguimientoService.listarPorPractica(practicaId)));
    }

    private Long longValue(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private Integer integerValue(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private String stringValue(Map<String, Object> request, String key) {
        return String.valueOf(request.get(key));
    }

    private String optionalString(Map<String, Object> request, String key) {
        Object value = request.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
