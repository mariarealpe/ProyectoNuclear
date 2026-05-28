package co.edu.cue.practicas.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearTutorEmpresarialRequest {

    @NotBlank(message = "El nombre del tutor es obligatorio")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener formato válido")
    private String correo;

    private String telefono;

    @NotNull(message = "La empresa es obligatoria")
    private Long empresaId;

    @NotBlank(message = "El cargo del tutor es obligatorio")
    private String cargo;

    private String area;
    private String telefonoCorporativo;
    private boolean esResponsablePrincipal;
}
