package co.edu.cue.practicas.dto.response;

import co.edu.cue.practicas.model.entity.PlantillaNotificacion;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoEventoNotificacion;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PlantillaNotificacionResponse {

    private Long id;
    private TipoEventoNotificacion evento;
    private String asunto;
    private String cuerpoHtml;
    private Rol rolReceptor;
    private Boolean obligatorio;
    private Integer frecuenciaRecordatorioDias;
    private Boolean activa;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public static PlantillaNotificacionResponse desde(PlantillaNotificacion p) {
        return PlantillaNotificacionResponse.builder()
                .id(p.getId())
                .evento(p.getEvento())
                .asunto(p.getAsunto())
                .cuerpoHtml(p.getCuerpoHtml())
                .rolReceptor(p.getRolReceptor())
                .obligatorio(p.getObligatorio())
                .frecuenciaRecordatorioDias(p.getFrecuenciaRecordatorioDias())
                .activa(p.getActiva())
                .creadoEn(p.getCreadoEn())
                .actualizadoEn(p.getActualizadoEn())
                .build();
    }
}
