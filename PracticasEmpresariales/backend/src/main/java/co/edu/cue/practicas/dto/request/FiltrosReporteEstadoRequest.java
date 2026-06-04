package co.edu.cue.practicas.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Filtros opcionales para el reporte de estado del proceso (RF-10-01).
 * Cualquier filtro null aplica "todos".
 */
@Data
public class FiltrosReporteEstadoRequest {

    private Long facultadId;
    private Long programaId;
    private Integer numeroPractica;
    private LocalDateTime periodoDesde;
    private LocalDateTime periodoHasta;
}
