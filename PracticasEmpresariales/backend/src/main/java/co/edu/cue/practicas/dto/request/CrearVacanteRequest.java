package co.edu.cue.practicas.dto.request;

import co.edu.cue.practicas.model.enums.JornadaVacante;
import co.edu.cue.practicas.model.enums.ModalidadVacante;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CrearVacanteRequest {

    @NotBlank(message = "El título de la vacante es obligatorio")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "La empresa es obligatoria")
    private Long empresaId;

    /** Tutor empresarial responsable; opcional al crear, se puede asignar luego. */
    private Long tutorId;

    @NotNull(message = "El programa académico es obligatorio")
    private Long programaId;

    @NotNull(message = "La modalidad es obligatoria")
    private ModalidadVacante modalidad;

    @NotNull(message = "La jornada es obligatoria")
    private JornadaVacante jornada;

    private String ciudad;

    @Min(value = 1, message = "Debe ofrecer al menos 1 cupo")
    private int cupos = 1;

    @Min(value = 1, message = "La duración mínima es 1 mes")
    private int duracionMeses = 6;

    private boolean remunerada;
    private Double valorRemuneracion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String requisitos;
    private String responsabilidades;
}
