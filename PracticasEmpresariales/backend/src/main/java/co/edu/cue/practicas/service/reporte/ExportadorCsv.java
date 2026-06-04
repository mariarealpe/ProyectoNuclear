package co.edu.cue.practicas.service.reporte;

import co.edu.cue.practicas.dto.response.ReporteEstadoResponse;
import co.edu.cue.practicas.model.enums.FormatoExport;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class ExportadorCsv implements ExportadorReporte {

    @Override
    public FormatoExport formato() {
        return FormatoExport.CSV;
    }

    @Override
    public byte[] exportar(ReporteEstadoResponse r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Reporte de Estado del Proceso de Prácticas\n");
        sb.append("Generado;").append(r.getGeneradoEn()).append('\n');
        sb.append("Facultad;").append(nullSafe(r.getFiltros().getFacultadId())).append('\n');
        sb.append("Programa;").append(nullSafe(r.getFiltros().getProgramaId())).append('\n');
        sb.append("Número práctica;").append(nullSafe(r.getFiltros().getNumeroPractica())).append('\n');
        sb.append("Periodo desde;").append(nullSafe(r.getFiltros().getPeriodoDesde())).append('\n');
        sb.append("Periodo hasta;").append(nullSafe(r.getFiltros().getPeriodoHasta())).append('\n');
        sb.append('\n');
        sb.append("Estado;Total\n");
        sb.append("Aptos sin iniciar;").append(r.getAptosSinIniciar()).append('\n');
        sb.append("En asignación;").append(r.getEnAsignacion()).append('\n');
        sb.append("En práctica;").append(r.getEnPractica()).append('\n');
        sb.append("Completados;").append(r.getCompletados()).append('\n');
        sb.append("Reprobados;").append(r.getReprobados()).append('\n');
        sb.append("TOTAL;").append(r.getTotal()).append('\n');
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String contentType() { return "text/csv; charset=utf-8"; }

    @Override
    public String extension() { return ".csv"; }

    private String nullSafe(Object v) { return v == null ? "" : v.toString(); }
}
