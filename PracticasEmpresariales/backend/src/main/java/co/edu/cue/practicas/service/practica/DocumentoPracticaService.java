package co.edu.cue.practicas.service.practica;

import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.DocumentoPractica;
import co.edu.cue.practicas.model.entity.FirmaDocumento;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.TipoDocumento;
import co.edu.cue.practicas.model.enums.TipoFirmante;
import co.edu.cue.practicas.repository.practica.DocumentoPracticaRepository;
import co.edu.cue.practicas.repository.practica.FirmaDocumentoRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PATRON FACADE — GPE-162, GPE-163
 *
 * Simplifica la prueba de documentos en Postman:
 * carga carta/convenio, crea firmantes del convenio y confirma firmas.
 * La regla de inmutabilidad se valida antes de modificar documentos.
 */
@Service
@RequiredArgsConstructor
public class DocumentoPracticaService {

    private final DocumentoPracticaRepository documentoRepository;
    private final FirmaDocumentoRepository firmaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PracticaSprint3Service practicaService;

    @Transactional
    public Map<String, Object> cargar(Long practicaId,
                                      TipoDocumento tipo,
                                      String urlArchivo,
                                      String nombreArchivo,
                                      String mimeType,
                                      Long tamanho,
                                      Integer numPaginas) {
        Practica practica = practicaService.buscar(practicaId);
        validarMutable(practica);

        DocumentoPractica documento = DocumentoPractica.builder()
                .practica(practica)
                .tipo(tipo)
                .urlArchivo(urlArchivo)
                .nombreArchivo(nombreArchivo)
                .mimeType(mimeType)
                .tamanho(tamanho)
                .numPaginas(numPaginas)
                .esMutable(true)
                .build();

        return toMap(documentoRepository.save(documento));
    }

    @Transactional
    public Map<String, Object> crearFirma(Long documentoId, TipoFirmante tipoFirmante, Long usuarioId) {
        DocumentoPractica documento = buscar(documentoId);
        validarMutable(documento.getPractica());
        if (documento.getTipo() != TipoDocumento.CONVENIO) {
            throw new OperacionNoPermitidaException("Solo los convenios tienen firmas");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario firmante no encontrado"));

        FirmaDocumento firma = firmaRepository.findByDocumento_IdAndTipo(documentoId, tipoFirmante)
                .orElse(FirmaDocumento.builder()
                        .documento(documento)
                        .tipo(tipoFirmante)
                        .usuario(usuario)
                        .build());
        firma.setUsuario(usuario);

        return firmaToMap(firmaRepository.save(firma));
    }

    @Transactional
    public Map<String, Object> confirmarFirma(Long documentoId, TipoFirmante tipoFirmante, Long usuarioId, String hashValidacion) {
        FirmaDocumento firma = firmaRepository.findByDocumento_IdAndTipo(documentoId, tipoFirmante)
                .orElseThrow(() -> new RecursoNoEncontradoException("Firma requerida no encontrada"));
        if (!firma.getUsuario().getId().equals(usuarioId)) {
            throw new OperacionNoPermitidaException("El usuario no corresponde al firmante registrado");
        }
        validarMutable(firma.getDocumento().getPractica());
        firma.setFechaFirma(LocalDateTime.now());
        firma.setHashValidacion(hashValidacion);
        return firmaToMap(firmaRepository.save(firma));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPorPractica(Long practicaId) {
        return documentoRepository.findByPractica_Id(practicaId)
                .stream()
                .map(this::toMap)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> firmas(Long documentoId) {
        return firmaRepository.findByDocumento_Id(documentoId)
                .stream()
                .map(this::firmaToMap)
                .toList();
    }

    private DocumentoPractica buscar(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Documento no encontrado"));
    }

    private void validarMutable(Practica practica) {
        if (practica.getEstado() == EstadoPractica.FINALIZADA || practica.getEstado() == EstadoPractica.CANCELADA) {
            throw new OperacionNoPermitidaException("Los documentos de practicas finalizadas o canceladas son inmutables");
        }
    }

    private Map<String, Object> toMap(DocumentoPractica d) {
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

    private Map<String, Object> firmaToMap(FirmaDocumento f) {
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
