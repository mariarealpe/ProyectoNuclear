package co.edu.cue.practicas.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EditarTutorEmpresarialRequest {

    @NotBlank(message = "El nombre del tutor es obligatorio")
    private String nombre;

    private String telefono;

    @NotBlank(message = "El cargo del tutor es obligatorio")
    private String cargo;

    private String area;
    private String telefonoCorporativo;
    private boolean esResponsablePrincipal;
}
