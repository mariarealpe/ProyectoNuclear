package co.edu.cue.practicas.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Indicadores agregados para el tablero gerencial (RF-10-04).
 *
 * Incluye totales globales y desgloses por facultad (filtrados por scope:
 * la Coordinación Académica solo ve su facultad).
 */
@Data
@Builder
public class TableroGerencialResponse {

    private LocalDateTime generadoEn;
    private LocalDateTime periodoDesde;
    private LocalDateTime periodoHasta;

    private long totalPracticantesActivos;
    private long empresasActivas;
    private long practicasCerradasEnPeriodo;
    private long totalPracticas;
    private long totalAprobadas;
    private long totalReprobadas;
    /** % aprobación global (0–100), 0 si no hay calificadas. */
    private double tasaAprobacionGlobal;

    private List<IndicadorFacultad> porFacultad;

    @Data
    @Builder
    public static class IndicadorFacultad {
        private Long facultadId;
        private String nombreFacultad;
        private long practicantesActivos;
        private long aprobadas;
        private long reprobadas;
        private double tasaAprobacion;
    }
}
