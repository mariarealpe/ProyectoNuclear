package co.edu.cue.practicas.dto.response;

import co.edu.cue.practicas.model.entity.NotaFinal;
import co.edu.cue.practicas.model.enums.ResultadoEvaluacion;
import co.edu.cue.practicas.model.enums.ResultadoNotaFinal;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Respuesta del registro/consulta de la nota final.
 * Expone las notas de referencia (docente y tutor) para que el Coordinador
 * tenga visibilidad completa al momento de decidir la nota final (RF-08-04).
 */
@Data
@Builder
public class NotaFinalResponse {

    private Long id;
    private Long practicaId;
    private String nombreEstudiante;
    private Long coordinadorId;
    private String nombreCoordinador;

    private Double nota;
    private ResultadoNotaFinal resultado;
    private String observaciones;

    private Boolean cerrada;
    private LocalDateTime cerradaEn;

    /** Nota de referencia registrada por el Docente Asesor (puede ser null). */
    private Double notaReferenciaDocente;
    private ResultadoEvaluacion resultadoDocente;

    /** Nota de referencia registrada por el Tutor Empresarial (puede ser null). */
    private Double notaReferenciaTutor;
    private ResultadoEvaluacion resultadoTutor;

    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public static NotaFinalResponse desde(NotaFinal n) {
        return base(n).build();
    }

    public static NotaFinalResponse desde(
            NotaFinal n,
            Double notaDocente, ResultadoEvaluacion resultadoDocente,
            Double notaTutor, ResultadoEvaluacion resultadoTutor) {

        return base(n)
                .notaReferenciaDocente(notaDocente)
                .resultadoDocente(resultadoDocente)
                .notaReferenciaTutor(notaTutor)
                .resultadoTutor(resultadoTutor)
                .build();
    }

    private static NotaFinalResponseBuilder base(NotaFinal n) {
        return NotaFinalResponse.builder()
                .id(n.getId())
                .practicaId(n.getPractica().getId())
                .nombreEstudiante(n.getPractica().getEstudiante().getNombre())
                .coordinadorId(n.getCoordinador().getId())
                .nombreCoordinador(n.getCoordinador().getNombre())
                .nota(n.getNota())
                .resultado(n.getResultado())
                .observaciones(n.getObservaciones())
                .cerrada(n.getCerrada())
                .cerradaEn(n.getCerradaEn())
                .creadoEn(n.getCreadoEn())
                .actualizadoEn(n.getActualizadoEn());
    }
}
