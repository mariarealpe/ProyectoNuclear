package co.edu.cue.practicas.dto.response;

import co.edu.cue.practicas.model.entity.EvaluacionTutor;
import co.edu.cue.practicas.model.enums.ResultadoEvaluacion;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EvaluacionTutorResponse {

    private Long id;
    private Long practicaId;
    private String nombreEstudiante;
    private Long tutorId;
    private String nombreTutor;
    private Double nota;
    private ResultadoEvaluacion resultado;
    private String observaciones;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public static EvaluacionTutorResponse desde(EvaluacionTutor e) {
        return EvaluacionTutorResponse.builder()
                .id(e.getId())
                .practicaId(e.getPractica().getId())
                .nombreEstudiante(e.getPractica().getEstudiante().getNombre())
                .tutorId(e.getTutor().getId())
                .nombreTutor(e.getTutor().getNombre())
                .nota(e.getNota())
                .resultado(e.getResultado())
                .observaciones(e.getObservaciones())
                .creadoEn(e.getCreadoEn())
                .actualizadoEn(e.getActualizadoEn())
                .build();
    }
}
