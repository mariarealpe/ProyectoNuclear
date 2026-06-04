package co.edu.cue.practicas.service.practica;

import co.edu.cue.practicas.model.entity.Practica;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PATRON MAPPER — GPE-164
 *
 * Convierte Practica a una respuesta simple para REST.
 * Mantiene la fachada de vinculacion enfocada en el caso de uso.
 */
@Component
public class PracticaSprint3Mapper {

    public Map<String, Object> toMap(Practica p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("estudianteId", p.getEstudiante().getId());
        map.put("estudiante", p.getEstudiante().getNombre());
        map.put("programaId", p.getPrograma().getId());
        map.put("numeroPractica", p.getNumeroPractica());
        map.put("nombre", p.getNombre());
        map.put("estado", p.getEstado());
        map.put("docenteAsesorId", p.getDocenteAsesor() == null ? null : p.getDocenteAsesor().getId());
        map.put("docenteAsesor", p.getDocenteAsesor() == null ? null : p.getDocenteAsesor().getNombre());
        map.put("documentosRequeridos", p.getDocumentosRequeridos());
        map.put("creadoEn", p.getCreadoEn());
        map.put("actualizadoEn", p.getActualizadoEn());
        return map;
    }
}
