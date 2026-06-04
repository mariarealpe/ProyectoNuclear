package co.edu.cue.practicas.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Datos que el Coordinador envía al registrar o actualizar la nota final.
 * La validación de rango se hace en el servicio usando ConfiguracionPrograma.
 */
@Data
public class RegistrarNotaFinalRequest {

    @NotNull(message = "La nota es obligatoria")
    private Double nota;

    /** Observaciones opcionales del Coordinador. */
    private String observaciones;
}
