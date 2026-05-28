package co.edu.cue.practicas.dto.response;

import co.edu.cue.practicas.model.entity.TutorEmpresarial;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TutorEmpresarialResponse {

    private Long id;
    private Long usuarioId;
    private String nombre;
    private String correo;
    private String telefono;
    private Long empresaId;
    private String empresaRazonSocial;
    private String cargo;
    private String area;
    private String telefonoCorporativo;
    private boolean esResponsablePrincipal;
    private boolean activo;
    private LocalDateTime creadoEn;

    public static TutorEmpresarialResponse desde(TutorEmpresarial t) {
        return TutorEmpresarialResponse.builder()
                .id(t.getId())
                .usuarioId(t.getUsuario() != null ? t.getUsuario().getId() : null)
                .nombre(t.getUsuario() != null ? t.getUsuario().getNombre() : null)
                .correo(t.getUsuario() != null ? t.getUsuario().getCorreo() : null)
                .telefono(t.getUsuario() != null ? t.getUsuario().getTelefono() : null)
                .empresaId(t.getEmpresa() != null ? t.getEmpresa().getId() : null)
                .empresaRazonSocial(t.getEmpresa() != null ? t.getEmpresa().getRazonSocial() : null)
                .cargo(t.getCargo())
                .area(t.getArea())
                .telefonoCorporativo(t.getTelefonoCorporativo())
                .esResponsablePrincipal(t.isEsResponsablePrincipal())
                .activo(t.isActivo())
                .creadoEn(t.getCreadoEn())
                .build();
    }
}
