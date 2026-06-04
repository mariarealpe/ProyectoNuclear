package co.edu.cue.practicas.service.seguimiento;

import co.edu.cue.practicas.model.entity.SeguimientoPractica;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PATRON MAPPER — GPE-168, GPE-170
 *
 * Convierte seguimientos a respuestas REST simples.
 */
@Component
public class SeguimientoPracticaMapper {

    public Map<String, Object> toMap(SeguimientoPractica s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId());
        map.put("practicaId", s.getPractica().getId());
        map.put("semana", s.getSemana());
        map.put("estado", s.getEstado());
        map.put("actividades", s.getActividades());
        map.put("logros", s.getLogros());
        map.put("dificultades", s.getDificultades());
        map.put("observacionesDocente", s.getObservacionesDocente());
        map.put("cargadoPorId", s.getCargadoPor().getId());
        map.put("docenteAsesorId", s.getDocenteAsesor() == null ? null : s.getDocenteAsesor().getId());
        map.put("fechaCarga", s.getFechaCarga());
        map.put("fechaRevision", s.getFechaRevision());
        map.put("version", s.getVersion());
        return map;
    }
}
