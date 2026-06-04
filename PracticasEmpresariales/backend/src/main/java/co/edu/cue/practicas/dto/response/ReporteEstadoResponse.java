package co.edu.cue.practicas.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Respuesta del reporte de estado del proceso de prácticas (RF-10-01).
 * Construido por ReporteEstadoBuilder con los filtros activos.
 */
@Data
@Builder
public class ReporteEstadoResponse {

    private LocalDateTime generadoEn;
    private FiltrosAplicados filtros;
    private long aptosSinIniciar;
    private long enAsignacion;
    private long enPractica;
    private long completados;
    private long reprobados;
    private long total;

    @Data
    @Builder
    public static class FiltrosAplicados {
        private Long facultadId;
        private Long programaId;
        private Integer numeroPractica;
        private LocalDateTime periodoDesde;
        private LocalDateTime periodoHasta;
    }
}
