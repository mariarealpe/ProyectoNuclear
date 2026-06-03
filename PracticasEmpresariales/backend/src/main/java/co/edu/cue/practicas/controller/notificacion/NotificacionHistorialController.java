package co.edu.cue.practicas.controller.notificacion;

import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.model.enums.TipoNotificacion;
import co.edu.cue.practicas.service.notificacion.NotificacionSprint3Service;
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
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionHistorialController {

    private final NotificacionSprint3Service notificacionService;

    /**
     * PATRON FACADE — GPE-160
     *
     * Registra una notificacion en historial sin acoplar Postman al servicio SMTP.
     * Esto permite probar cola, estados y trazabilidad del Sprint 3.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> registrar(@RequestBody Map<String, Object> request) {
        Map<String, Object> notificacion = notificacionService.registrar(
                longValue(request, "usuarioDestinoId"),
                TipoNotificacion.valueOf(stringValue(request, "tipo")),
                stringValue(request, "asunto"),
                stringValue(request, "cuerpo"),
                optionalLong(request, "asignacionId"),
                optionalLong(request, "practicaId")
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Notificacion registrada", notificacion));
    }

    @PatchMapping("/{id}/enviada")
    public ResponseEntity<ApiResponse<Map<String, Object>>> marcarEnviada(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Notificacion marcada como enviada",
                notificacionService.marcarEnviada(id)));
    }

    @PatchMapping("/{id}/fallida")
    public ResponseEntity<ApiResponse<Map<String, Object>>> marcarFallida(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> request) {

        String motivo = request == null ? null : optionalString(request, "motivo");
        return ResponseEntity.ok(ApiResponse.ok("Notificacion marcada como fallida",
                notificacionService.marcarFallida(id, motivo)));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> listarPorUsuario(
            @PathVariable Long usuarioId,
            @PageableDefault(size = 20, sort = "creadoEn") Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.ok(notificacionService.listarPorUsuario(usuarioId, pageable)));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> pendientes() {
        return ResponseEntity.ok(ApiResponse.ok(notificacionService.listarPendientes()));
    }

    @GetMapping("/reintentos")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> reintentos() {
        return ResponseEntity.ok(ApiResponse.ok(notificacionService.listarReintentosPendientes()));
    }

    private Long longValue(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private Long optionalLong(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value == null) {
            return null;
        }
        return value instanceof Number number ? number.longValue() : Long.valueOf(String.valueOf(value));
    }

    private String stringValue(Map<String, Object> request, String key) {
        return String.valueOf(request.get(key));
    }

    private String optionalString(Map<String, Object> request, String key) {
        Object value = request.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
