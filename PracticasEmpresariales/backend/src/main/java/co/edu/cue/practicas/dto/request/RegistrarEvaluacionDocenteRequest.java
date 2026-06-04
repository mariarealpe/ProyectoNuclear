package co.edu.cue.practicas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Datos que el Docente Asesor envía al registrar o actualizar su evaluación.
 * La validación del rango numérico (0.0 a notaMaxima) se realiza en el servicio
 * usando los valores de ConfiguracionPrograma del programa correspondiente.
 */
@Data
public class RegistrarEvaluacionDocenteRequest {

    @NotNull(message = "La nota es obligatoria")
    private Double nota;

    @NotBlank(message = "Las observaciones son obligatorias")
    private String observaciones;
}
