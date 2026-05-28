package co.edu.cue.practicas.dto.response;

import co.edu.cue.practicas.model.entity.Empresa;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EmpresaResponse {

    private Long id;
    private String nit;
    private String razonSocial;
    private String nombreComercial;
    private String sector;
    private String direccion;
    private String ciudad;
    private String correoContacto;
    private String telefono;
    private String sitioWeb;
    private String representanteLegal;
    private String descripcion;
    private boolean activo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public static EmpresaResponse desde(Empresa e) {
        return EmpresaResponse.builder()
                .id(e.getId())
                .nit(e.getNit())
                .razonSocial(e.getRazonSocial())
                .nombreComercial(e.getNombreComercial())
                .sector(e.getSector())
                .direccion(e.getDireccion())
                .ciudad(e.getCiudad())
                .correoContacto(e.getCorreoContacto())
                .telefono(e.getTelefono())
                .sitioWeb(e.getSitioWeb())
                .representanteLegal(e.getRepresentanteLegal())
                .descripcion(e.getDescripcion())
                .activo(e.isActivo())
                .creadoEn(e.getCreadoEn())
                .actualizadoEn(e.getActualizadoEn())
                .build();
    }
}
