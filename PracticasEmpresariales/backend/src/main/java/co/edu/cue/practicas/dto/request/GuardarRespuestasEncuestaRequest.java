package co.edu.cue.practicas.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GuardarRespuestasEncuestaRequest {

    /** JSON con las respuestas. La estructura la define la plantilla configurable. */
    @NotBlank(message = "Las respuestas son obligatorias")
    private String respuestasJson;
}
