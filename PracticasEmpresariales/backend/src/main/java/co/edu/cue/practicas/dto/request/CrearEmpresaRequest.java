package co.edu.cue.practicas.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CrearEmpresaRequest {

    @NotBlank(message = "El NIT es obligatorio")
    private String nit;

    @NotBlank(message = "La razón social es obligatoria")
    private String razonSocial;

    private String nombreComercial;
    private String sector;
    private String direccion;
    private String ciudad;

    @NotBlank(message = "El correo de contacto es obligatorio")
    @Email(message = "El correo de contacto debe tener formato válido")
    private String correoContacto;

    private String telefono;
    private String sitioWeb;
    private String representanteLegal;
    private String descripcion;
}
