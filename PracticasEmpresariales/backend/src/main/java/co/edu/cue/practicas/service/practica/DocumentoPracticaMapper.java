package co.edu.cue.practicas.service.practica;

import co.edu.cue.practicas.model.entity.DocumentoPractica;
import co.edu.cue.practicas.model.entity.FirmaDocumento;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PATRON MAPPER — GPE-162, GPE-163
 *
 * Separa la representacion REST de documentos y firmas.
 */
@Component
public class DocumentoPracticaMapper {

    public Map<String, Object> documentoToMap(DocumentoPractica d) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", d.getId());
        map.put("practicaId", d.getPractica().getId());
        map.put("tipo", d.getTipo());
        map.put("urlArchivo", d.getUrlArchivo());
        map.put("nombreArchivo", d.getNombreArchivo());
        map.put("mimeType", d.getMimeType());
        map.put("tamanho", d.getTamanho());
        map.put("numPaginas", d.getNumPaginas());
        map.put("esMutable", d.isEsMutable());
        map.put("firmasCompletas", d.tieneTodasLasFirmas());
        map.put("creadoEn", d.getCreadoEn());
        return map;
    }

    public Map<String, Object> firmaToMap(FirmaDocumento f) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", f.getId());
        map.put("documentoId", f.getDocumento().getId());
        map.put("tipo", f.getTipo());
        map.put("usuarioId", f.getUsuario().getId());
        map.put("usuario", f.getUsuario().getNombre());
        map.put("fechaFirma", f.getFechaFirma());
        map.put("hashValidacion", f.getHashValidacion());
        map.put("confirmada", f.estaConfirmada());
        return map;
    }
}
