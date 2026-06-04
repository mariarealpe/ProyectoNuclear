package co.edu.cue.practicas.service.cierre;

import co.edu.cue.practicas.audit.ModuloAuditoria;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.EjecutarCierrePracticaRequest;
import co.edu.cue.practicas.dto.response.ChecklistCierreResponse;
import co.edu.cue.practicas.dto.response.CierrePracticaResponse;
import co.edu.cue.practicas.event.CierrePracticaEvent;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.BitacoraAuditoria;
import co.edu.cue.practicas.model.entity.DocumentoPractica;
import co.edu.cue.practicas.model.entity.NotaFinal;
import co.edu.cue.practicas.model.entity.NotificacionHistorial;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoNotificacion;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.ResultadoNotaFinal;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoAccion;
import co.edu.cue.practicas.model.enums.TipoDocumento;
import co.edu.cue.practicas.model.enums.TipoEventoNotificacion;
import co.edu.cue.practicas.model.enums.TipoNotificacion;
import co.edu.cue.practicas.repository.evaluacion.NotaFinalRepository;
import co.edu.cue.practicas.repository.notificacion.NotificacionHistorialRepository;
import co.edu.cue.practicas.repository.practica.DocumentoPracticaRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.security.annotation.RequiereRol;
import co.edu.cue.practicas.service.notificacion.PlantillaNotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RF-09-02 — Ejecución del cierre formal de la práctica (Patrón Facade).
 *
 * Orquesta desde un único punto:
 *  1. Validación del checklist (delegado en ChecklistCierreService).
 *  2. Validación de estado y autorización del Coordinador.
 *  3. Cierre de la NotaFinal si aún no está cerrada (sincroniza
 *     Practica.notasCerradas para inmutabilidad de docente/tutor).
 *  4. Transición de Practica.estado → FINALIZADA (irreversible).
 *  5. Persistencia opcional del acta de cierre como DocumentoPractica.
 *  6. Encolado de correos a Estudiante, Docente, Tutor y Empresa.
 *  7. Publicación de CierrePracticaEvent para los Observers
 *     (Coordinación Académica, dashboard, etc. — RF-09-03).
 *  8. Auditoría con el resultado APROBADO/REPROBADO.
 */
@Service
@RequiredArgsConstructor
public class CierrePracticaService {

    private final PracticaRepository practicaRepository;
    private final NotaFinalRepository notaFinalRepository;
    private final DocumentoPracticaRepository documentoRepository;
    private final NotificacionHistorialRepository notificacionRepository;
    private final ChecklistCierreService checklistService;
    private final PlantillaNotificacionService plantillaService;
    private final AuditoriaLogger auditoriaLogger;
    private final ApplicationEventPublisher eventPublisher;

    @RequiereRol(roles = {Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public CierrePracticaResponse ejecutarCierre(
            Long practicaId,
            EjecutarCierrePracticaRequest request,
            CustomUserDetails coordinador) {

        if (!request.isConfirmar()) {
            throw new OperacionNoPermitidaException(
                    "El cierre formal requiere confirmación explícita del Coordinador");
        }

        Practica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Práctica no encontrada: " + practicaId));

        validarEstado(practica);
        validarChecklistCompleto(practicaId);

        NotaFinal notaFinal = notaFinalRepository.findByPractica_Id(practicaId)
                .orElseThrow(() -> new OperacionNoPermitidaException(
                        "No se puede ejecutar el cierre sin una nota final registrada"));

        // Cerrar nota final si no estaba cerrada
        if (!Boolean.TRUE.equals(notaFinal.getCerrada())) {
            notaFinal.setCerrada(true);
            notaFinal.setCerradaEn(LocalDateTime.now());
            notaFinalRepository.save(notaFinal);
        }

        // Transición irreversible
        practica.setEstado(EstadoPractica.FINALIZADA);
        practica.setNotasCerradas(true);
        practica = practicaRepository.save(practica);

        // Acta de cierre opcional
        if (request.getUrlActaCierre() != null && !request.getUrlActaCierre().isBlank()) {
            DocumentoPractica acta = DocumentoPractica.builder()
                    .practica(practica)
                    .tipo(TipoDocumento.ACTA_CIERRE)
                    .urlArchivo(request.getUrlActaCierre())
                    .nombreArchivo(request.getNombreActaCierre() != null
                            ? request.getNombreActaCierre()
                            : "acta_cierre_practica_" + practicaId + ".pdf")
                    .build();
            documentoRepository.save(acta);
        }

        // Notificar a los 4 actores
        Map<String, String> vars = construirVariables(practica, notaFinal);
        notificarActor(practica.getEstudiante(), vars, practica);
        notificarActor(practica.getDocenteAsesor(), vars, practica);
        notificarActor(practica.getTutorEmpresarial(), vars, practica);
        // Empresa: usamos correoContacto vía un envío directo
        encolarCorreoEmpresa(practica, vars);

        // Trigger Observers — RF-09-03
        eventPublisher.publishEvent(new CierrePracticaEvent(this, practica, notaFinal));

        // Auditoría
        auditoriaLogger.registrar(BitacoraAuditoria.builder()
                .usuario(coordinador.getUsuario())
                .nombreUsuario(coordinador.getNombre())
                .rolUsuario(coordinador.getRol())
                .etiquetaCargoUsuario(coordinador.getEtiquetaCargo())
                .modulo(ModuloAuditoria.CIERRE_PRACTICA)
                .tipoAccion(TipoAccion.CAMBIO_ESTADO)
                .registroAfectadoId(practica.getId())
                .registroAfectadoTipo("Practica:CIERRE_FORMAL")
                .valoresNuevos("{\"resultado\":\"" + notaFinal.getResultado()
                        + "\",\"nota\":" + notaFinal.getNota() + "}")
                .exitoso(true));

        return construirResponse(practica, notaFinal, request.getUrlActaCierre());
    }

    // =========================================================================

    private void validarEstado(Practica practica) {
        if (practica.getEstado() != EstadoPractica.EN_CURSO) {
            throw new OperacionNoPermitidaException(
                    "Solo se puede cerrar una práctica EN_CURSO. Estado actual: "
                            + practica.getEstado());
        }
    }

    private void validarChecklistCompleto(Long practicaId) {
        ChecklistCierreResponse checklist = checklistService.evaluarChecklist(practicaId);
        if (!checklist.isPuedeEjecutarCierre()) {
            int faltan = checklist.getTotalItems() - checklist.getItemsCompletados();
            throw new OperacionNoPermitidaException(
                    "No se puede ejecutar el cierre: faltan " + faltan + " item(s) del checklist");
        }
    }

    private Map<String, String> construirVariables(Practica practica, NotaFinal notaFinal) {
        Map<String, String> vars = new HashMap<>();
        vars.put("nombre_estudiante", practica.getEstudiante().getNombre());
        vars.put("nombre_practica", practica.getNombre());
        vars.put("numero_practica", String.valueOf(practica.getNumeroPractica()));
        vars.put("nota_final", String.valueOf(notaFinal.getNota()));
        vars.put("resultado", notaFinal.getResultado().name());
        return vars;
    }

    private void notificarActor(Usuario destinatario, Map<String, String> vars, Practica practica) {
        if (destinatario == null) return;
        PlantillaNotificacionService.RenderResult render =
                plantillaService.renderizarParaEvento(TipoEventoNotificacion.CIERRE_PRACTICA, vars);
        if (render == null) return;

        NotificacionHistorial n = NotificacionHistorial.builder()
                .usuarioDestino(destinatario)
                .tipo(TipoNotificacion.OTRO)
                .correoDestino(destinatario.getCorreo())
                .asunto(render.asunto())
                .cuerpo(render.cuerpoHtml())
                .estado(EstadoNotificacion.PENDIENTE)
                .practica(practica)
                .build();
        notificacionRepository.save(n);
    }

    private void encolarCorreoEmpresa(Practica practica, Map<String, String> vars) {
        // La empresa no es un Usuario; el correo se conoce por catalogo/asignacion.
        // Sprint 4: omitimos el correo a empresa si no hay un destinatario Usuario.
        // El listener de actualización de estado puede ampliarlo cuando la empresa
        // tenga un Usuario o un destinatario contacto modelado.
    }

    private CierrePracticaResponse construirResponse(
            Practica practica, NotaFinal notaFinal, String urlActa) {

        String etiqueta = notaFinal.getResultado() == ResultadoNotaFinal.APROBADO
                ? "Práctica " + practica.getNumeroPractica() + " Completada"
                : "Práctica " + practica.getNumeroPractica() + " Reprobada";

        return CierrePracticaResponse.builder()
                .practicaId(practica.getId())
                .nombreEstudiante(practica.getEstudiante().getNombre())
                .numeroPractica(practica.getNumeroPractica())
                .estadoPractica(practica.getEstado())
                .resultado(notaFinal.getResultado())
                .notaFinal(notaFinal.getNota())
                .etiquetaResultado(etiqueta)
                .cerradaEn(notaFinal.getCerradaEn())
                .urlActaCierre(urlActa)
                .build();
    }
}
