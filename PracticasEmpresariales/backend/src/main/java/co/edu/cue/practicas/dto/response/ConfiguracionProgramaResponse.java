package co.edu.cue.practicas.dto.response;

import co.edu.cue.practicas.model.entity.ConfiguracionPrograma;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConfiguracionProgramaResponse {

    private Long id;
    private Long programaId;
    private String programaNombre;
    private Integer diasInactividadAlerta;
    private Boolean notificacionesAutomaticas;
    private Double notaMinimaAprobacion;
    private Double notaMaxima;
    private Integer numeroTotalPracticas;
    private Integer numeroCortes;
    private Integer maximoAsignacionesSimultaneas;
    private String plantillaCorreoAsignacion;
    private String plantillaCorreoSeguimiento;
    private String plantillaCorreoAlerta;
    private String correoRemitente;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public static ConfiguracionProgramaResponse desde(ConfiguracionPrograma c) {
        Integer numeroTotalPracticas = c.getNumeroTotalPracticas() != null
                ? c.getNumeroTotalPracticas()
                : c.getPrograma().getNumeroTotalPracticas();
        return ConfiguracionProgramaResponse.builder()
                .id(c.getId())
                .programaId(c.getPrograma().getId())
                .programaNombre(c.getPrograma().getNombre())
                .diasInactividadAlerta(c.getDiasInactividadAlerta())
                .notificacionesAutomaticas(c.getNotificacionesAutomaticas())
                .notaMinimaAprobacion(c.getNotaMinimaAprobacion())
                .notaMaxima(c.getNotaMaxima())
                .numeroTotalPracticas(numeroTotalPracticas)
                .numeroCortes(c.getNumeroCortes())
                .maximoAsignacionesSimultaneas(c.getMaximoAsignacionesSimultaneas())
                .plantillaCorreoAsignacion(c.getPlantillaCorreoAsignacion())
                .plantillaCorreoSeguimiento(c.getPlantillaCorreoSeguimiento())
                .plantillaCorreoAlerta(c.getPlantillaCorreoAlerta())
                .correoRemitente(c.getCorreoRemitente())
                .creadoEn(c.getCreadoEn())
                .actualizadoEn(c.getActualizadoEn())
                .build();
    }
}
