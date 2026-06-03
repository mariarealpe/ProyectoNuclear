package co.edu.cue.practicas.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActualizarConfiguracionProgramaRequest {

    @NotNull(message = "Los días de inactividad para alerta son obligatorios")
    @Min(value = 1, message = "Los días de inactividad deben ser mayores a cero")
    private Integer diasInactividadAlerta;

    @NotNull(message = "El estado de notificaciones automáticas es obligatorio")
    private Boolean notificacionesAutomaticas;

    @NotNull(message = "La nota mínima de aprobación es obligatoria")
    @DecimalMin(value = "0.0", message = "La nota mínima no puede ser negativa")
    private Double notaMinimaAprobacion;

    @NotNull(message = "La nota máxima es obligatoria")
    @DecimalMin(value = "0.1", message = "La nota máxima debe ser mayor a cero")
    private Double notaMaxima;

    @Min(value = 1, message = "El número total de prácticas debe ser mayor a cero")
    private Integer numeroTotalPracticas;

    @NotNull(message = "El número de cortes es obligatorio")
    @Min(value = 1, message = "Debe configurar al menos 1 corte")
    private Integer numeroCortes;

    @NotNull(message = "El máximo de asignaciones simultáneas es obligatorio")
    @Min(value = 1, message = "Debe permitir al menos 1 asignación simultánea")
    private Integer maximoAsignacionesSimultaneas;

    @NotNull(message = "La plantilla de correo de asignación es obligatoria")
    private String plantillaCorreoAsignacion;

    @NotNull(message = "La plantilla de correo de seguimiento es obligatoria")
    private String plantillaCorreoSeguimiento;

    @NotNull(message = "La plantilla de correo de alerta es obligatoria")
    private String plantillaCorreoAlerta;

    @NotBlank(message = "El correo remitente no puede estar vacío")
    @Email(message = "El correo remitente debe tener formato válido")
    private String correoRemitente;
}
