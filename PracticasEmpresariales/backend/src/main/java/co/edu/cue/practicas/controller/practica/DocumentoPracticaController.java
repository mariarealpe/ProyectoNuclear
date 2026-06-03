package co.edu.cue.practicas.controller.practica;

import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.model.enums.TipoDocumento;
import co.edu.cue.practicas.model.enums.TipoFirmante;
import co.edu.cue.practicas.service.practica.DocumentoPracticaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/documentos-practica")
@RequiredArgsConstructor
public class DocumentoPracticaController {

    private final DocumentoPracticaService documentoService;

    /**
     * PATRON FACADE — GPE-162, GPE-163
     *
     * Carga metadatos de carta de presentacion o convenio.
     * Para Postman se registra la URL/ruta del archivo, no se sube binario.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> cargar(@RequestBody Map<String, Object> request) {
        Map<String, Object> documento = documentoService.cargar(
                longValue(request, "practicaId"),
                TipoDocumento.valueOf(stringValue(request, "tipo")),
                stringValue(request, "urlArchivo"),
                stringValue(request, "nombreArchivo"),
                optionalString(request, "mimeType"),
                optionalLong(request, "tamanho"),
                optionalInteger(request, "numPaginas")
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Documento registrado", documento));
    }

    @GetMapping("/practica/{practicaId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listarPorPractica(@PathVariable Long practicaId) {
        return ResponseEntity.ok(ApiResponse.ok(documentoService.listarPorPractica(practicaId)));
    }

    /**
     * PATRON FACADE — GPE-163
     *
     * Registra cada firmante requerido del convenio.
     * Luego se confirma cada firma de forma individual.
     */
    @PostMapping("/{documentoId}/firmas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> crearFirma(
            @PathVariable Long documentoId,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> firma = documentoService.crearFirma(
                documentoId,
                TipoFirmante.valueOf(stringValue(request, "tipoFirmante")),
                longValue(request, "usuarioId")
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Firma requerida registrada", firma));
    }

    @PatchMapping("/{documentoId}/firmas/{tipoFirmante}/confirmar")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirmarFirma(
            @PathVariable Long documentoId,
            @PathVariable TipoFirmante tipoFirmante,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> firma = documentoService.confirmarFirma(
                documentoId,
                tipoFirmante,
                longValue(request, "usuarioId"),
                optionalString(request, "hashValidacion")
        );
        return ResponseEntity.ok(ApiResponse.ok("Firma confirmada", firma));
    }

    @GetMapping("/{documentoId}/firmas")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> firmas(@PathVariable Long documentoId) {
        return ResponseEntity.ok(ApiResponse.ok(documentoService.firmas(documentoId)));
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

    private Integer optionalInteger(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value == null) {
            return null;
        }
        return value instanceof Number number ? number.intValue() : Integer.valueOf(String.valueOf(value));
    }

    private String stringValue(Map<String, Object> request, String key) {
        return String.valueOf(request.get(key));
    }

    private String optionalString(Map<String, Object> request, String key) {
        Object value = request.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
