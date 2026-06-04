package co.edu.cue.practicas.dto.response;

import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.ResultadoNotaFinal;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CierrePracticaResponse {

    private Long practicaId;
    private String nombreEstudiante;
    private Integer numeroPractica;
    private EstadoPractica estadoPractica;
    private ResultadoNotaFinal resultado;
    private Double notaFinal;
    private String etiquetaResultado;
    private LocalDateTime cerradaEn;
    private String urlActaCierre;
}
