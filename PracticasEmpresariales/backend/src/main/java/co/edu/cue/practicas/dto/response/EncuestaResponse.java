package co.edu.cue.practicas.dto.response;

import co.edu.cue.practicas.model.entity.Encuesta;
import co.edu.cue.practicas.model.enums.EstadoEncuesta;
import co.edu.cue.practicas.model.enums.TipoEncuesta;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EncuestaResponse {

    private Long id;
    private Long practicaId;
    private String nombreEstudiante;
    private TipoEncuesta tipo;
    private EstadoEncuesta estado;
    private Long destinatarioId;
    private String nombreDestinatario;
    private String respuestasJson;
    private LocalDateTime invitacionEnviadaEn;
    private LocalDateTime ultimoRecordatorioEn;
    private LocalDateTime completadaEn;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public static EncuestaResponse desde(Encuesta e) {
        return EncuestaResponse.builder()
                .id(e.getId())
                .practicaId(e.getPractica().getId())
                .nombreEstudiante(e.getPractica().getEstudiante().getNombre())
                .tipo(e.getTipo())
                .estado(e.getEstado())
                .destinatarioId(e.getDestinatario().getId())
                .nombreDestinatario(e.getDestinatario().getNombre())
                .respuestasJson(e.getRespuestasJson())
                .invitacionEnviadaEn(e.getInvitacionEnviadaEn())
                .ultimoRecordatorioEn(e.getUltimoRecordatorioEn())
                .completadaEn(e.getCompletadaEn())
                .creadoEn(e.getCreadoEn())
                .actualizadoEn(e.getActualizadoEn())
                .build();
    }
}
