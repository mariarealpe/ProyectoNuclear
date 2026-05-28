package co.edu.cue.practicas.dto.response;

import co.edu.cue.practicas.model.entity.BitacoraVacante;
import co.edu.cue.practicas.model.enums.EstadoVacante;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BitacoraVacanteResponse {

    private Long id;
    private Long vacanteId;
    private Long usuarioId;
    private String usuarioNombre;
    private EstadoVacante estadoAnterior;
    private EstadoVacante estadoNuevo;
    private String motivo;
    private LocalDateTime fechaHora;

    public static BitacoraVacanteResponse desde(BitacoraVacante b) {
        return BitacoraVacanteResponse.builder()
                .id(b.getId())
                .vacanteId(b.getVacante() != null ? b.getVacante().getId() : null)
                .usuarioId(b.getUsuario() != null ? b.getUsuario().getId() : null)
                .usuarioNombre(b.getUsuario() != null ? b.getUsuario().getNombre() : null)
                .estadoAnterior(b.getEstadoAnterior())
                .estadoNuevo(b.getEstadoNuevo())
                .motivo(b.getMotivo())
                .fechaHora(b.getFechaHora())
                .build();
    }
}
