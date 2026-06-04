package co.edu.cue.practicas.service.reporte;

import co.edu.cue.practicas.dto.response.ReporteEstadoResponse;
import co.edu.cue.practicas.model.enums.FormatoExport;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Stub temporal de exportación a PDF.
 *
 * No depende de iText / Apache PDFBox (no están en pom.xml). Devuelve un
 * archivo de texto plano con extensión .pdf para que el endpoint cumpla el
 * contrato. Cuando se agregue la librería real, basta sustituir esta
 * implementación de ExportadorReporte sin tocar el caller (Patrón Strategy).
 */
@Component
public class ExportadorPdfStub implements ExportadorReporte {

    @Override
    public FormatoExport formato() {
        return FormatoExport.PDF;
    }

    @Override
    public byte[] exportar(ReporteEstadoResponse r) {
        String contenido = """
                === Reporte de Estado del Proceso de Prácticas ===
                Generado: %s
                Filtros: facultad=%s programa=%s practica=%s periodo=%s -> %s

                Aptos sin iniciar  %d
                En asignación      %d
                En práctica        %d
                Completados        %d
                Reprobados         %d
                TOTAL              %d

                (Implementación temporal — sustituir por generación PDF real
                cuando se agregue Apache PDFBox o iText al pom.xml.)
                """.formatted(
                r.getGeneradoEn(),
                r.getFiltros().getFacultadId(), r.getFiltros().getProgramaId(),
                r.getFiltros().getNumeroPractica(),
                r.getFiltros().getPeriodoDesde(), r.getFiltros().getPeriodoHasta(),
                r.getAptosSinIniciar(), r.getEnAsignacion(), r.getEnPractica(),
                r.getCompletados(), r.getReprobados(), r.getTotal());
        return contenido.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String contentType() { return "application/pdf"; }

    @Override
    public String extension() { return ".pdf"; }
}
