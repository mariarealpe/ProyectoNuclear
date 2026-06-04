package co.edu.cue.practicas.service.notificacion;

import co.edu.cue.practicas.audit.ModuloAuditoria;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.ActualizarPlantillaNotificacionRequest;
import co.edu.cue.practicas.dto.response.PlantillaNotificacionResponse;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.BitacoraAuditoria;
import co.edu.cue.practicas.model.entity.PlantillaNotificacion;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoAccion;
import co.edu.cue.practicas.model.enums.TipoEventoNotificacion;
import co.edu.cue.practicas.repository.notificacion.PlantillaNotificacionRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.security.annotation.RequiereRol;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RF-11-05 — Servicio único (Singleton) de plantillas de correo.
 *
 * Capacidades:
 *  - CRUD de plantillas por evento (upsert por TipoEventoNotificacion).
 *  - Renderizado del cuerpo HTML reemplazando variables {{clave}} por valores
 *    pasados en runtime (Decorator).
 *  - Consulta de la plantilla activa por evento, usada por los demás servicios
 *    (encuestas, alertas, asignaciones, etc.).
 */
@Service
@RequiredArgsConstructor
public class PlantillaNotificacionService {

    private static final Pattern VARIABLE = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*\\}\\}");

    private final PlantillaNotificacionRepository plantillaRepository;
    private final AuditoriaLogger auditoriaLogger;

    /**
     * Crea o actualiza la plantilla de un evento (upsert).
     * Solo el Administrador DTI puede modificar plantillas.
     */
    @RequiereRol(roles = {Rol.ADMIN_DTI})
    @Transactional
    public PlantillaNotificacionResponse upsert(
            TipoEventoNotificacion evento,
            ActualizarPlantillaNotificacionRequest request,
            CustomUserDetails admin) {

        PlantillaNotificacion plantilla = plantillaRepository.findByEvento(evento)
                .orElseGet(() -> PlantillaNotificacion.builder().evento(evento).build());

        plantilla.setAsunto(request.getAsunto());
        plantilla.setCuerpoHtml(request.getCuerpoHtml());
        plantilla.setRolReceptor(request.getRolReceptor());
        plantilla.setObligatorio(request.getObligatorio());
        plantilla.setFrecuenciaRecordatorioDias(request.getFrecuenciaRecordatorioDias());
        plantilla.setActiva(request.getActiva());

        plantilla = plantillaRepository.save(plantilla);

        auditoriaLogger.registrar(iniciarAuditoria(admin)
                .modulo(ModuloAuditoria.NOTIFICACIONES)
                .tipoAccion(plantilla.getId() == null ? TipoAccion.CREAR : TipoAccion.EDITAR)
                .registroAfectadoId(plantilla.getId())
                .registroAfectadoTipo("PlantillaNotificacion")
                .exitoso(true));

        return PlantillaNotificacionResponse.desde(plantilla);
    }

    @Transactional(readOnly = true)
    public PlantillaNotificacionResponse obtenerPorEvento(TipoEventoNotificacion evento) {
        return plantillaRepository.findByEvento(evento)
                .map(PlantillaNotificacionResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe plantilla configurada para el evento: " + evento));
    }

    @Transactional(readOnly = true)
    public List<PlantillaNotificacionResponse> listarActivas() {
        return plantillaRepository.findByActivaTrue().stream()
                .map(PlantillaNotificacionResponse::desde)
                .toList();
    }

    /**
     * Renderiza una plantilla para previsualización antes de guardar.
     * Reemplaza {{variable}} por el valor correspondiente del mapa.
     */
    public String previsualizar(String cuerpoHtml, Map<String, String> variables) {
        return aplicarVariables(cuerpoHtml, variables);
    }

    /**
     * Renderiza la plantilla activa de un evento con las variables provistas.
     * Devuelve null si la plantilla está inactiva o no existe — el caller debe
     * decidir si registrar igual o saltar el envío.
     */
    public RenderResult renderizarParaEvento(
            TipoEventoNotificacion evento, Map<String, String> variables) {

        return plantillaRepository.findByEvento(evento)
                .filter(PlantillaNotificacion::getActiva)
                .map(p -> new RenderResult(
                        aplicarVariables(p.getAsunto(), variables),
                        aplicarVariables(p.getCuerpoHtml(), variables),
                        p.getObligatorio(),
                        p.getFrecuenciaRecordatorioDias()))
                .orElse(null);
    }

    // =========================================================================

    private String aplicarVariables(String texto, Map<String, String> variables) {
        if (texto == null || variables == null || variables.isEmpty()) return texto;
        Matcher m = VARIABLE.matcher(texto);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String clave = m.group(1);
            String valor = variables.getOrDefault(clave, "");
            m.appendReplacement(sb, Matcher.quoteReplacement(valor));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private BitacoraAuditoria.BitacoraAuditoriaBuilder iniciarAuditoria(CustomUserDetails actor) {
        return BitacoraAuditoria.builder()
                .usuario(actor.getUsuario())
                .nombreUsuario(actor.getNombre())
                .rolUsuario(actor.getRol())
                .etiquetaCargoUsuario(actor.getEtiquetaCargo());
    }

    public record RenderResult(
            String asunto,
            String cuerpoHtml,
            Boolean obligatorio,
            Integer frecuenciaRecordatorioDias
    ) {}
}
