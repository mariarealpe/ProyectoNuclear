package co.edu.cue.practicas.service.reporte;

import co.edu.cue.practicas.dto.response.ReporteEstadoResponse;
import co.edu.cue.practicas.model.enums.FormatoExport;

/**
 * Patrón Strategy: cada formato de exportación implementa esta interfaz.
 * Permite agregar nuevos formatos (XLSX vía POI, JSON, etc.) sin
 * cambiar el caller.
 */
public interface ExportadorReporte {

    FormatoExport formato();

    /**
     * @return bytes del archivo exportado listos para retornar como adjunto HTTP.
     */
    byte[] exportar(ReporteEstadoResponse reporte);

    /** Content-Type para la respuesta HTTP. */
    String contentType();

    /** Sufijo del nombre de archivo, ej. ".csv", ".pdf". */
    String extension();
}
