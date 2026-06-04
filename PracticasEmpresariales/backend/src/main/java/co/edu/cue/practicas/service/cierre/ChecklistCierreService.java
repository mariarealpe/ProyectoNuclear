package co.edu.cue.practicas.service.cierre;

import co.edu.cue.practicas.dto.response.ChecklistCierreResponse;
import co.edu.cue.practicas.dto.response.ItemChecklistResponse;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.Encuesta;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.enums.EstadoEncuesta;
import co.edu.cue.practicas.model.enums.TipoDocumento;
import co.edu.cue.practicas.model.enums.TipoEncuesta;
import co.edu.cue.practicas.model.enums.TipoItemChecklist;
import co.edu.cue.practicas.repository.encuesta.EncuestaRepository;
import co.edu.cue.practicas.repository.evaluacion.EvaluacionDocenteRepository;
import co.edu.cue.practicas.repository.evaluacion.EvaluacionTutorRepository;
import co.edu.cue.practicas.repository.evaluacion.NotaFinalRepository;
import co.edu.cue.practicas.repository.practica.DocumentoPracticaRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.encuesta.EncuestaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * RF-09-01 — Checklist de requisitos previos al cierre formal.
 *
 * Aplica Chain of Responsibility: cada item es una verificación independiente
 * que se compone para construir la respuesta completa. El cierre solo se
 * habilita si TODOS los items obligatorios están completos.
 *
 * Recordatorios manuales para encuestas pendientes se delegan al
 * EncuestaService (que respeta la regla "máx 1 por día por encuesta").
 */
@Service
@RequiredArgsConstructor
public class ChecklistCierreService {

    private final PracticaRepository practicaRepository;
    private final EvaluacionDocenteRepository evaluacionDocenteRepository;
    private final EvaluacionTutorRepository evaluacionTutorRepository;
    private final NotaFinalRepository notaFinalRepository;
    private final EncuestaRepository encuestaRepository;
    private final DocumentoPracticaRepository documentoRepository;
    private final EncuestaService encuestaService;

    @Transactional(readOnly = true)
    public ChecklistCierreResponse evaluarChecklist(Long practicaId) {
        Practica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Práctica no encontrada: " + practicaId));

        Map<TipoEncuesta, Encuesta> encuestasPorTipo =
                encuestaRepository.findByPractica_Id(practicaId).stream()
                        .collect(java.util.stream.Collectors.toMap(
                                Encuesta::getTipo, e -> e, (a, b) -> a));

        List<ItemChecklistResponse> items = new ArrayList<>();
        items.add(verificarNotaDocente(practicaId));
        items.add(verificarNotaTutor(practicaId));
        items.add(verificarNotaFinal(practicaId));
        items.add(verificarEncuesta(practicaId,
                TipoItemChecklist.ENCUESTA_TUTOR,
                "Encuesta del Tutor Empresarial",
                encuestasPorTipo.get(TipoEncuesta.TUTOR_SATISFACCION)));
        items.add(verificarEncuesta(practicaId,
                TipoItemChecklist.ENCUESTA_ESTUDIANTE_SATISFACCION,
                "Encuesta de satisfacción del Estudiante",
                encuestasPorTipo.get(TipoEncuesta.ESTUDIANTE_SATISFACCION)));
        items.add(verificarEncuesta(practicaId,
                TipoItemChecklist.ENCUESTA_ESTUDIANTE_AUTOEVALUACION,
                "Autoevaluación del Estudiante",
                encuestasPorTipo.get(TipoEncuesta.ESTUDIANTE_AUTOEVALUACION)));
        items.add(verificarDocumentosRequeridos(practicaId));
        items.add(verificarInformeFinal(practicaId));

        int completados = (int) items.stream().filter(ItemChecklistResponse::isCompletado).count();
        boolean puede = completados == items.size();

        return ChecklistCierreResponse.builder()
                .practicaId(practicaId)
                .nombreEstudiante(practica.getEstudiante().getNombre())
                .items(items)
                .puedeEjecutarCierre(puede)
                .totalItems(items.size())
                .itemsCompletados(completados)
                .build();
    }

    /**
     * Envía un recordatorio manual para un item del checklist (solo aplica a
     * encuestas pendientes). La validación de "máx 1 por día" la enforca
     * EncuestaService.
     */
    @Transactional
    public ItemChecklistResponse enviarRecordatorioItem(
            Long practicaId,
            TipoItemChecklist item,
            CustomUserDetails actor) {

        TipoEncuesta tipo = mapearItemAEncuesta(item);
        if (tipo == null) {
            throw new OperacionNoPermitidaException(
                    "El item " + item + " no soporta recordatorios — solo aplica a encuestas");
        }
        Encuesta encuesta = encuestaRepository.findByPractica_IdAndTipo(practicaId, tipo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Encuesta " + tipo + " no creada aún para la práctica " + practicaId));

        encuestaService.enviarRecordatorio(encuesta.getId(), actor);

        // Reconstruir el item con la fecha actualizada
        return verificarEncuesta(practicaId, item, etiquetaPara(item), encuesta);
    }

    // =========================================================================
    // Handlers de la Chain of Responsibility
    // =========================================================================

    private ItemChecklistResponse verificarNotaDocente(Long practicaId) {
        boolean ok = evaluacionDocenteRepository.existsByPractica_Id(practicaId);
        return base(TipoItemChecklist.NOTA_DOCENTE, "Nota del Docente Asesor", ok,
                ok ? null : "/evaluaciones-docente/practica/" + practicaId);
    }

    private ItemChecklistResponse verificarNotaTutor(Long practicaId) {
        boolean ok = evaluacionTutorRepository.existsByPractica_Id(practicaId);
        return base(TipoItemChecklist.NOTA_TUTOR, "Nota del Tutor Empresarial", ok,
                ok ? null : "/evaluaciones-tutor/practica/" + practicaId);
    }

    private ItemChecklistResponse verificarNotaFinal(Long practicaId) {
        boolean ok = notaFinalRepository.existsByPractica_Id(practicaId);
        return base(TipoItemChecklist.NOTA_FINAL, "Nota final del Coordinador", ok,
                ok ? null : "/notas-finales/practica/" + practicaId);
    }

    private ItemChecklistResponse verificarEncuesta(
            Long practicaId, TipoItemChecklist tipo, String etiqueta, Encuesta encuesta) {

        if (encuesta == null) {
            return ItemChecklistResponse.builder()
                    .tipo(tipo)
                    .etiqueta(etiqueta)
                    .completado(false)
                    .estado("PENDIENTE")
                    .enlaceAccion("/encuestas/practica/" + practicaId)
                    .build();
        }
        String estado = switch (encuesta.getEstado()) {
            case COMPLETADA -> "COMPLETADO";
            case EN_BORRADOR -> "EN_BORRADOR";
            case PENDIENTE -> "PENDIENTE";
        };
        boolean ok = encuesta.getEstado() == EstadoEncuesta.COMPLETADA;
        return ItemChecklistResponse.builder()
                .tipo(tipo)
                .etiqueta(etiqueta)
                .completado(ok)
                .estado(estado)
                .enlaceAccion(ok ? null : "/encuestas/" + encuesta.getId())
                .ultimoRecordatorioEn(encuesta.getUltimoRecordatorioEn())
                .encuestaId(encuesta.getId())
                .build();
    }

    private ItemChecklistResponse verificarDocumentosRequeridos(Long practicaId) {
        boolean tieneConvenio = !documentoRepository
                .findByPractica_IdAndTipo(practicaId, TipoDocumento.CONVENIO).isEmpty();
        boolean tieneCarta = !documentoRepository
                .findByPractica_IdAndTipo(practicaId, TipoDocumento.CARTA_PRESENTACION).isEmpty();
        boolean tienePlan = !documentoRepository
                .findByPractica_IdAndTipo(practicaId, TipoDocumento.PLAN).isEmpty();

        boolean ok = tieneConvenio && tieneCarta && tienePlan;
        return base(TipoItemChecklist.DOCUMENTOS_REQUERIDOS,
                "Documentos requeridos (convenio, carta, plan)", ok,
                ok ? null : "/documentos-practica/practica/" + practicaId);
    }

    private ItemChecklistResponse verificarInformeFinal(Long practicaId) {
        boolean ok = !documentoRepository
                .findByPractica_IdAndTipo(practicaId, TipoDocumento.INFORME_FINAL).isEmpty();
        return base(TipoItemChecklist.INFORME_FINAL, "Informe final del Estudiante", ok,
                ok ? null : "/documentos-practica/practica/" + practicaId);
    }

    // =========================================================================

    private ItemChecklistResponse base(TipoItemChecklist tipo, String etiqueta, boolean ok, String enlace) {
        return ItemChecklistResponse.builder()
                .tipo(tipo)
                .etiqueta(etiqueta)
                .completado(ok)
                .estado(ok ? "COMPLETADO" : "PENDIENTE")
                .enlaceAccion(enlace)
                .build();
    }

    private TipoEncuesta mapearItemAEncuesta(TipoItemChecklist item) {
        return switch (item) {
            case ENCUESTA_TUTOR -> TipoEncuesta.TUTOR_SATISFACCION;
            case ENCUESTA_ESTUDIANTE_SATISFACCION -> TipoEncuesta.ESTUDIANTE_SATISFACCION;
            case ENCUESTA_ESTUDIANTE_AUTOEVALUACION -> TipoEncuesta.ESTUDIANTE_AUTOEVALUACION;
            default -> null;
        };
    }

    private String etiquetaPara(TipoItemChecklist item) {
        return switch (item) {
            case ENCUESTA_TUTOR -> "Encuesta del Tutor Empresarial";
            case ENCUESTA_ESTUDIANTE_SATISFACCION -> "Encuesta de satisfacción del Estudiante";
            case ENCUESTA_ESTUDIANTE_AUTOEVALUACION -> "Autoevaluación del Estudiante";
            default -> item.name();
        };
    }
}
