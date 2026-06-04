package co.edu.cue.practicas.service.asignacion;

import co.edu.cue.practicas.model.entity.Asignacion;
import co.edu.cue.practicas.model.entity.CambioEstadoAsignacion;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PATRON MAPPER — GPE-157, GPE-159
 *
 * Convierte entidades de asignacion a respuestas simples para REST.
 * Evita que el servicio principal mezcle logica de negocio con armado de DTOs.
 */
@Component
public class AsignacionMapper {

    public Map<String, Object> toMap(Asignacion a) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", a.getId());
        map.put("estudianteId", a.getEstudiante().getId());
        map.put("estudiante", a.getEstudiante().getNombre());
        map.put("vacanteId", a.getVacante().getId());
        map.put("vacante", a.getVacante().getTitulo());
        map.put("coordinadorId", a.getCoordinador().getId());
        map.put("estado", a.getEstado());
        map.put("motivoCancelacion", a.getMotivoCancelacion());
        map.put("fechaAsignacion", a.getFechaAsignacion());
        map.put("fechaVinculacion", a.getFechaVinculacion());
        map.put("fechaInicio", a.getFechaInicio());
        map.put("fechaFin", a.getFechaFin());
        return map;
    }

    public Map<String, Object> cambioToMap(CambioEstadoAsignacion c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("asignacionId", c.getAsignacion().getId());
        map.put("estadoAnterior", c.getEstadoAnterior());
        map.put("estadoNuevo", c.getEstadoNuevo());
        map.put("motivo", c.getMotivo());
        map.put("usuarioId", c.getUsuario().getId());
        map.put("usuario", c.getUsuario().getNombre());
        map.put("fechaHora", c.getFechaHora());
        return map;
    }
}
