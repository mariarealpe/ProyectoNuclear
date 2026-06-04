package co.edu.cue.practicas.dto.request;

import co.edu.cue.practicas.model.enums.Rol;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActualizarPlantillaNotificacionRequest {

    @NotBlank(message = "El asunto es obligatorio")
    private String asunto;

    @NotBlank(message = "El cuerpo HTML es obligatorio")
    private String cuerpoHtml;

    private Rol rolReceptor;

    @NotNull(message = "Indique si la plantilla es obligatoria")
    private Boolean obligatorio;

    @NotNull(message = "La frecuencia de recordatorio es obligatoria")
    @Min(value = 1, message = "La frecuencia mínima es 1 día hábil")
    private Integer frecuenciaRecordatorioDias;

    @NotNull(message = "Indique si la plantilla está activa")
    private Boolean activa;
}
