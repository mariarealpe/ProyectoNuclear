package co.edu.cue.practicas.dto.response;

import co.edu.cue.practicas.model.entity.EvaluacionDocente;
import co.edu.cue.practicas.model.enums.ResultadoEvaluacion;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EvaluacionDocenteResponse {

    private Long id;
    private Long practicaId;
    private String nombreEstudiante;
    private Long docenteId;
    private String nombreDocente;
    private Double nota;
    private ResultadoEvaluacion resultado;
    private String observaciones;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public static EvaluacionDocenteResponse desde(EvaluacionDocente e) {
        return EvaluacionDocenteResponse.builder()
                .id(e.getId())
                .practicaId(e.getPractica().getId())
                .nombreEstudiante(e.getPractica().getEstudiante().getNombre())
                .docenteId(e.getDocente().getId())
                .nombreDocente(e.getDocente().getNombre())
                .nota(e.getNota())
                .resultado(e.getResultado())
                .observaciones(e.getObservaciones())
                .creadoEn(e.getCreadoEn())
                .actualizadoEn(e.getActualizadoEn())
                .build();
    }
}
