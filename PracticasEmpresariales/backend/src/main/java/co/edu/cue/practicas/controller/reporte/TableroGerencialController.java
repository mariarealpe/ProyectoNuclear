package co.edu.cue.practicas.controller.reporte;

import co.edu.cue.practicas.dto.response.ApiResponse;
import co.edu.cue.practicas.dto.response.TableroGerencialResponse;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.reporte.TableroGerencialService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * RF-10-04 — Endpoint del tablero gerencial.
 */
@RestController
@RequestMapping("/tablero-gerencial")
@RequiredArgsConstructor
public class TableroGerencialController {

    private final TableroGerencialService tableroService;

    @GetMapping
    public ResponseEntity<ApiResponse<TableroGerencialResponse>> obtener(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime periodoDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime periodoHasta,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                tableroService.obtener(periodoDesde, periodoHasta, userDetails)));
    }
}
