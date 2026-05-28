package co.edu.cue.practicas.dto.response;

import co.edu.cue.practicas.model.entity.Vacante;
import co.edu.cue.practicas.model.enums.EstadoVacante;
import co.edu.cue.practicas.model.enums.JornadaVacante;
import co.edu.cue.practicas.model.enums.ModalidadVacante;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class VacanteResponse {

    private Long id;
    private String titulo;
    private String descripcion;

    private Long empresaId;
    private String empresaRazonSocial;
    private String empresaNit;

    private Long tutorId;
    private String tutorNombre;

    private Long programaId;
    private String programaNombre;

    private ModalidadVacante modalidad;
    private JornadaVacante jornada;
    private String ciudad;
    private int cupos;
    private int cuposOcupados;
    private int duracionMeses;
    private boolean remunerada;
    private Double valorRemuneracion;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDateTime fechaPublicacion;
    private LocalDateTime fechaCierre;

    private String requisitos;
    private String responsabilidades;

    private EstadoVacante estado;
    private String motivoRechazo;
    private Long aprobadorId;
    private String aprobadorNombre;
    private LocalDateTime fechaDecision;

    public static VacanteResponse desde(Vacante v) {
        return VacanteResponse.builder()
                .id(v.getId())
                .titulo(v.getTitulo())
                .descripcion(v.getDescripcion())
                .empresaId(v.getEmpresa() != null ? v.getEmpresa().getId() : null)
                .empresaRazonSocial(v.getEmpresa() != null ? v.getEmpresa().getRazonSocial() : null)
                .empresaNit(v.getEmpresa() != null ? v.getEmpresa().getNit() : null)
                .tutorId(v.getTutor() != null ? v.getTutor().getId() : null)
                .tutorNombre(v.getTutor() != null && v.getTutor().getUsuario() != null
                        ? v.getTutor().getUsuario().getNombre() : null)
                .programaId(v.getPrograma() != null ? v.getPrograma().getId() : null)
                .programaNombre(v.getPrograma() != null ? v.getPrograma().getNombre() : null)
                .modalidad(v.getModalidad())
                .jornada(v.getJornada())
                .ciudad(v.getCiudad())
                .cupos(v.getCupos())
                .cuposOcupados(v.getCuposOcupados())
                .duracionMeses(v.getDuracionMeses())
                .remunerada(v.isRemunerada())
                .valorRemuneracion(v.getValorRemuneracion())
                .fechaInicio(v.getFechaInicio())
                .fechaFin(v.getFechaFin())
                .fechaPublicacion(v.getFechaPublicacion())
                .fechaCierre(v.getFechaCierre())
                .requisitos(v.getRequisitos())
                .responsabilidades(v.getResponsabilidades())
                .estado(v.getEstado())
                .motivoRechazo(v.getMotivoRechazo())
                .aprobadorId(v.getAprobador() != null ? v.getAprobador().getId() : null)
                .aprobadorNombre(v.getAprobador() != null ? v.getAprobador().getNombre() : null)
                .fechaDecision(v.getFechaDecision())
                .build();
    }
}
