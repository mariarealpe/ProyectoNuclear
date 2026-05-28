package co.edu.cue.practicas.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ActualizarDatosAcademicosRequest {

    @Min(value = 0, message = "Los créditos aprobados no pueden ser negativos")
    private Integer creditosAprobados;

    private Double promedioAcumulado;

    private String documentosEntregados;

    private String hojaVidaUrl;
}
