package co.edu.cue.practicas.controller.reporte;

import co.edu.cue.practicas.dto.request.FiltrosReporteEstadoRequest;
import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.dto.response.ReporteEstadoResponse;
import co.edu.cue.practicas.model.enums.FormatoExport;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.reporte.ReporteEstadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * RF-10-01 — Endpoints del reporte de estado del proceso.
 */
@RestController
@RequestMapping("/reportes/estado")
@RequiredArgsConstructor
public class ReporteEstadoController {

    private final ReporteEstadoService reporteService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReporteEstadoResponse>> generar(
            @RequestBody(required = false) FiltrosReporteEstadoRequest filtros,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(reporteService.generar(filtros, userDetails)));
    }

    @PostMapping("/exportar")
    public ResponseEntity<ByteArrayResource> exportar(
            @RequestParam FormatoExport formato,
            @RequestBody(required = false) FiltrosReporteEstadoRequest filtros,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ReporteEstadoService.ExportResult result =
                reporteService.exportar(filtros, formato, userDetails);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(result.fileName()).build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(result.contentType()))
                .contentLength(result.bytes().length)
                .body(new ByteArrayResource(result.bytes()));
    }
}
