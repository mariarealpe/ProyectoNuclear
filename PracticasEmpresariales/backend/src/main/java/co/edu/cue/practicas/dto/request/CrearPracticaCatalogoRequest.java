package co.edu.cue.practicas.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearPracticaCatalogoRequest {

    @NotBlank(message = "El nombre de la práctica es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El programa es obligatorio")
    private Long programaId;

    @Min(value = 1, message = "El número de práctica debe ser mayor a cero")
    private int numeroPractica;

    @Min(value = 0, message = "Los créditos mínimos no pueden ser negativos")
    private int creditosMinimos;

    @Min(value = 0, message = "El promedio mínimo no puede ser negativo")
    private double promedioMinimo;

    private boolean requierePracticaAnteriorAprobada;

    private String documentosRequeridos;
}
