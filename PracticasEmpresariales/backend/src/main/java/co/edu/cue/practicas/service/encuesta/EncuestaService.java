package co.edu.cue.practicas.service.encuesta;

import co.edu.cue.practicas.audit.ModuloAuditoria;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.GuardarRespuestasEncuestaRequest;
import co.edu.cue.practicas.dto.response.EncuestaResponse;
import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.BitacoraAuditoria;
import co.edu.cue.practicas.model.entity.Encuesta;
import co.edu.cue.practicas.model.entity.NotificacionHistorial;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoEncuesta;
import co.edu.cue.practicas.model.enums.EstadoNotificacion;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoAccion;
import co.edu.cue.practicas.model.enums.TipoEncuesta;
import co.edu.cue.practicas.model.enums.TipoEventoNotificacion;
import co.edu.cue.practicas.model.enums.TipoNotificacion;
import co.edu.cue.practicas.repository.encuesta.EncuestaRepository;
import co.edu.cue.practicas.repository.notificacion.NotificacionHistorialRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.security.annotation.RequiereRol;
import co.edu.cue.practicas.service.notificacion.PlantillaNotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RF-08-05 (Tutor) + RF-08-06 (Estudiante): encuestas y autoevaluación de cierre.
 *
 * Reglas:
 *  - Una única encuesta por (práctica, tipo).
 *  - El Tutor admite guardar borrador (EN_BORRADOR) y completarla luego.
 *  - El Estudiante NO admite borrador y queda inmutable al enviar.
 *  - Solo el destinatario asignado puede responder su encuesta.
 *  - Una encuesta COMPLETADA es inmutable (Patrón Proxy).
 *  - Al iniciar fase de cierre se crean las 3 encuestas y se encolan los
 *    correos de invitación; los recordatorios automáticos los maneja el
 *    EncuestaRecordatorioScheduler según la frecuencia configurada en la
 *    plantilla del evento (RF-11-05).
 */
@Service
@RequiredArgsConstructor
public class EncuestaService {

    private final EncuestaRepository encuestaRepository;
    private final PracticaRepository practicaRepository;
    private final NotificacionHistorialRepository notificacionRepository;
    private final PlantillaNotificacionService plantillaService;
    private final AuditoriaLogger auditoriaLogger;

    /**
     * Inicia la fase de cierre creando las 3 encuestas (tutor satisfacción,
     * estudiante satisfacción, estudiante autoevaluación) en estado
     * PENDIENTE y encolando los correos de invitación.
     *
     * Idempotente: si una encuesta ya existe no se recrea.
     * Lo invoca el flujo de cierre (RF-09-01 / RF-09-02) cuando la práctica
     * cumple las precondiciones.
     */
    @Transactional
    public List<EncuestaResponse> iniciarFaseCierre(Long practicaId) {
        Practica practica = practicaRepository.findById(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Práctica no encontrada: " + practicaId));

        List<EncuestaResponse> creadas = new ArrayList<>();
        creadas.add(crearSiNoExiste(practica, TipoEncuesta.TUTOR_SATISFACCION,
                practica.getTutorEmpresarial(),
                TipoEventoNotificacion.ENCUESTA_TUTOR_INVITACION));
        creadas.add(crearSiNoExiste(practica, TipoEncuesta.ESTUDIANTE_SATISFACCION,
                practica.getEstudiante(),
                TipoEventoNotificacion.ENCUESTA_ESTUDIANTE_INVITACION));
        creadas.add(crearSiNoExiste(practica, TipoEncuesta.ESTUDIANTE_AUTOEVALUACION,
                practica.getEstudiante(),
                TipoEventoNotificacion.ENCUESTA_ESTUDIANTE_INVITACION));
        return creadas;
    }

    /**
     * Guarda en borrador la encuesta del Tutor (solo TUTOR_SATISFACCION admite
     * borrador). El estudiante NO puede guardar borradores — debe completar.
     */
    @RequiereRol(roles = {Rol.TUTOR_EMPRESARIAL})
    @Transactional
    public EncuestaResponse guardarBorradorTutor(
            Long encuestaId,
            GuardarRespuestasEncuestaRequest request,
            CustomUserDetails actor) {

        Encuesta encuesta = buscar(encuestaId);
        validarTipoSoportaBorrador(encuesta);
        validarDestinatario(encuesta, actor);
        validarNoCompletada(encuesta);

        encuesta.setRespuestasJson(request.getRespuestasJson());
        encuesta.setEstado(EstadoEncuesta.EN_BORRADOR);
        encuesta = encuestaRepository.save(encuesta);

        auditar(actor, encuesta, TipoAccion.EDITAR, "guardar_borrador");
        return EncuestaResponse.desde(encuesta);
    }

    /**
     * Marca la encuesta como COMPLETADA — inmutable a partir de aquí.
     * Aplica tanto al Tutor como al Estudiante.
     */
    @RequiereRol(roles = {Rol.TUTOR_EMPRESARIAL, Rol.ESTUDIANTE})
    @Transactional
    public EncuestaResponse completar(
            Long encuestaId,
            GuardarRespuestasEncuestaRequest request,
            CustomUserDetails actor) {

        Encuesta encuesta = buscar(encuestaId);
        validarDestinatario(encuesta, actor);
        validarNoCompletada(encuesta);

        encuesta.setRespuestasJson(request.getRespuestasJson());
        encuesta.setEstado(EstadoEncuesta.COMPLETADA);
        encuesta.setCompletadaEn(LocalDateTime.now());
        encuesta = encuestaRepository.save(encuesta);

        auditar(actor, encuesta, TipoAccion.CAMBIO_ESTADO, "completar");
        return EncuestaResponse.desde(encuesta);
    }

    /**
     * Encola un correo de recordatorio para la encuesta indicada.
     * Aplica regla "máximo un recordatorio por actor por día" (RF-09-01):
     * si el último recordatorio se envió hoy, devuelve sin encolar.
     */
    @Transactional
    public EncuestaResponse enviarRecordatorio(Long encuestaId, CustomUserDetails actor) {
        Encuesta encuesta = buscar(encuestaId);

        if (encuesta.getEstado() == EstadoEncuesta.COMPLETADA) {
            throw new OperacionNoPermitidaException(
                    "La encuesta ya fue completada; no requiere recordatorio");
        }

        if (yaSeEnvioRecordatorioHoy(encuesta)) {
            throw new OperacionNoPermitidaException(
                    "Ya se envió un recordatorio para esta encuesta hoy; reintente mañana");
        }

        encolarRecordatorio(encuesta);
        encuesta.setUltimoRecordatorioEn(LocalDateTime.now());
        encuesta = encuestaRepository.save(encuesta);

        auditar(actor, encuesta, TipoAccion.EDITAR, "recordatorio_manual");
        return EncuestaResponse.desde(encuesta);
    }

    @Transactional(readOnly = true)
    public EncuestaResponse obtenerPorId(Long encuestaId) {
        return EncuestaResponse.desde(buscar(encuestaId));
    }

    @Transactional(readOnly = true)
    public List<EncuestaResponse> listarPorPractica(Long practicaId) {
        return encuestaRepository.findByPractica_Id(practicaId).stream()
                .map(EncuestaResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EncuestaResponse> listarPendientesDelUsuario(Long usuarioId) {
        return encuestaRepository.findByDestinatario_IdAndEstadoIn(
                        usuarioId, List.of(EstadoEncuesta.PENDIENTE, EstadoEncuesta.EN_BORRADOR))
                .stream()
                .map(EncuestaResponse::desde)
                .toList();
    }

    // =========================================================================
    // Helpers paquete-privado (usados por el scheduler de recordatorios)
    // =========================================================================

    boolean yaSeEnvioRecordatorioHoy(Encuesta encuesta) {
        return encuesta.getUltimoRecordatorioEn() != null
                && encuesta.getUltimoRecordatorioEn().toLocalDate().equals(LocalDate.now());
    }

    void encolarRecordatorio(Encuesta encuesta) {
        TipoEventoNotificacion evento = switch (encuesta.getTipo()) {
            case TUTOR_SATISFACCION -> TipoEventoNotificacion.ENCUESTA_TUTOR_RECORDATORIO;
            case ESTUDIANTE_SATISFACCION, ESTUDIANTE_AUTOEVALUACION ->
                    TipoEventoNotificacion.ENCUESTA_ESTUDIANTE_RECORDATORIO;
        };
        encolarCorreo(encuesta, evento);
    }

    // =========================================================================

    private EncuestaResponse crearSiNoExiste(
            Practica practica,
            TipoEncuesta tipo,
            Usuario destinatario,
            TipoEventoNotificacion eventoInvitacion) {

        if (destinatario == null) {
            throw new OperacionNoPermitidaException(
                    "No se puede crear encuesta " + tipo + ": destinatario no asignado en la práctica");
        }

        Encuesta existente = encuestaRepository
                .findByPractica_IdAndTipo(practica.getId(), tipo)
                .orElse(null);
        if (existente != null) return EncuestaResponse.desde(existente);

        Encuesta encuesta = Encuesta.builder()
                .practica(practica)
                .tipo(tipo)
                .destinatario(destinatario)
                .estado(EstadoEncuesta.PENDIENTE)
                .invitacionEnviadaEn(LocalDateTime.now())
                .build();
        encuesta = encuestaRepository.save(encuesta);

        encolarCorreo(encuesta, eventoInvitacion);
        return EncuestaResponse.desde(encuesta);
    }

    private void encolarCorreo(Encuesta encuesta, TipoEventoNotificacion evento) {
        Map<String, String> variables = Map.of(
                "nombre_estudiante", encuesta.getPractica().getEstudiante().getNombre(),
                "nombre_destinatario", encuesta.getDestinatario().getNombre(),
                "nombre_practica", encuesta.getPractica().getNombre(),
                "enlace_encuesta", "/encuestas/" + encuesta.getId());

        PlantillaNotificacionService.RenderResult render =
                plantillaService.renderizarParaEvento(evento, variables);

        // Si la plantilla no existe o está inactiva, no encolamos correo,
        // pero la encuesta se crea igualmente — el Coordinador puede usar
        // el endpoint de recordatorio manual cuando la plantilla esté lista.
        if (render == null) return;

        NotificacionHistorial n = NotificacionHistorial.builder()
                .usuarioDestino(encuesta.getDestinatario())
                .tipo(toTipoNotificacion(evento))
                .correoDestino(encuesta.getDestinatario().getCorreo())
                .asunto(render.asunto())
                .cuerpo(render.cuerpoHtml())
                .estado(EstadoNotificacion.PENDIENTE)
                .practica(encuesta.getPractica())
                .build();
        notificacionRepository.save(n);
    }

    private TipoNotificacion toTipoNotificacion(TipoEventoNotificacion evento) {
        // Reutilizamos el catálogo existente; los eventos específicos de encuesta
        // caen en OTRO porque TipoNotificacion no enumera cada subcaso.
        return switch (evento) {
            case NUEVA_ASIGNACION -> TipoNotificacion.ASIGNACION_CREADA;
            case VINCULACION_CONFIRMADA -> TipoNotificacion.VINCULACION_CONFIRMADA;
            case PLAN_PRACTICA_APROBADO -> TipoNotificacion.PLAN_APROBADO;
            case PLAN_PRACTICA_RECHAZADO -> TipoNotificacion.PLAN_RECHAZADO;
            case SEGUIMIENTO_RECHAZADO -> TipoNotificacion.SEGUIMIENTO_RECHAZADO;
            case ALERTA_INACTIVIDAD -> TipoNotificacion.ALERTA_INACTIVIDAD;
            default -> TipoNotificacion.OTRO;
        };
    }

    private Encuesta buscar(Long id) {
        return encuestaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Encuesta no encontrada: " + id));
    }

    private void validarTipoSoportaBorrador(Encuesta encuesta) {
        if (encuesta.getTipo() != TipoEncuesta.TUTOR_SATISFACCION) {
            throw new OperacionNoPermitidaException(
                    "Solo la encuesta del Tutor admite guardar como borrador");
        }
    }

    private void validarDestinatario(Encuesta encuesta, CustomUserDetails actor) {
        if (!encuesta.getDestinatario().getId().equals(actor.getUsuario().getId())) {
            throw new AccesoNoAutorizadoException(
                    "Solo el destinatario asignado puede responder esta encuesta");
        }
    }

    private void validarNoCompletada(Encuesta encuesta) {
        if (encuesta.esInmutable()) {
            throw new OperacionNoPermitidaException(
                    "La encuesta ya fue completada y es inmutable");
        }
    }

    private void auditar(CustomUserDetails actor, Encuesta encuesta, TipoAccion accion, String detalle) {
        auditoriaLogger.registrar(BitacoraAuditoria.builder()
                .usuario(actor.getUsuario())
                .nombreUsuario(actor.getNombre())
                .rolUsuario(actor.getRol())
                .etiquetaCargoUsuario(actor.getEtiquetaCargo())
                .modulo(ModuloAuditoria.ENCUESTAS)
                .tipoAccion(accion)
                .registroAfectadoId(encuesta.getId())
                .registroAfectadoTipo("Encuesta:" + encuesta.getTipo() + ":" + detalle)
                .exitoso(true));
    }

    // Utilidad para tests
    static long diasHabilesEntre(LocalDate a, LocalDate b) {
        long dias = ChronoUnit.DAYS.between(a, b);
        return Math.max(0, dias);
    }
}
