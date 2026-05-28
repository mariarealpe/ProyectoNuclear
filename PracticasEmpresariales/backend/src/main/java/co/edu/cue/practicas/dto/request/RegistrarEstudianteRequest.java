package co.edu.cue.practicas.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistrarEstudianteRequest {

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String correo;

    private String telefono;

    @NotNull(message = "El programa es obligatorio")
    private Long programaId;

    @Min(value = 0, message = "Los créditos aprobados no pueden ser negativos")
    private Integer creditosAprobados;

    private Double promedioAcumulado;

    /** URL del documento de hoja de vida (opcional al registrar). */
    private String hojaVidaUrl;

    /** Documentos entregados separados por coma. */
    private String documentosEntregados;
}
