package co.edu.cue.practicas.service.configuracion;

import co.edu.cue.practicas.audit.ModuloAuditoria;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.ActualizarConfiguracionProgramaRequest;
import co.edu.cue.practicas.dto.response.ConfiguracionProgramaResponse;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.BitacoraAuditoria;
import co.edu.cue.practicas.model.entity.ConfiguracionPrograma;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoAccion;
import co.edu.cue.practicas.repository.configuracion.ConfiguracionProgramaRepository;
import co.edu.cue.practicas.repository.programa.ProgramaRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.security.annotation.RequiereRol;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Servicio encargado de la configuración parametrizable por programa académico.
 * RF-11-02 - permite ajustar notas, número de prácticas/cortes y parámetros
 * de notificación sin modificar el catálogo principal de programas.
 */
@Service
@RequiredArgsConstructor
public class ConfiguracionProgramaService {

    private final ConfiguracionProgramaRepository configuracionProgramaRepository;
    private final ProgramaRepository programaRepository;
    private final AuditoriaLogger auditoriaLogger;
    private final ObjectMapper objectMapper;

    /**
     * Consulta la configuración de un programa. Si aún no existe registro,
     * retorna la configuración por defecto sin persistirla.
     */
    @Transactional(readOnly = true)
    public ConfiguracionProgramaResponse obtenerPorPrograma(Long programaId) {
        Programa programa = buscarPrograma(programaId);
        ConfiguracionPrograma configuracion = configuracionProgramaRepository.findByPrograma_Id(programaId)
                .orElseGet(() -> configuracionPorDefecto(programa));
        return ConfiguracionProgramaResponse.desde(configuracion);
    }

    /**
     * Crea o actualiza la configuración del programa indicado.
     * Solo el Administrador DTI o Coordinación Académica pueden modificar reglas académicas del programa.
     */
    @RequiereRol(roles = {Rol.ADMIN_DTI, Rol.COORDINACION_ACADEMICA})
    @Transactional
    public ConfiguracionProgramaResponse actualizarPorPrograma(
            Long programaId,
            ActualizarConfiguracionProgramaRequest request,
            CustomUserDetails editor) {

        validarNotas(request);
        validarCorreoRemitente(request);

        Programa programa = buscarPrograma(programaId);
        ConfiguracionPrograma configuracion = configuracionProgramaRepository.findByPrograma_Id(programaId)
                .orElseGet(() -> configuracionPorDefecto(programa));

        boolean esNueva = configuracion.getId() == null;
        String antes = esNueva ? "{}" : toJson(snapshot(configuracion));

        aplicarCambios(configuracion, request);
        configuracion = configuracionProgramaRepository.save(configuracion);

        auditoriaLogger.registrar(iniciarAuditoria(editor)
                .modulo(ModuloAuditoria.PROGRAMAS)
                .tipoAccion(esNueva ? TipoAccion.CREAR : TipoAccion.EDITAR)
                .registroAfectadoId(configuracion.getId())
                .registroAfectadoTipo("ConfiguracionPrograma")
                .valoresAnteriores(antes)
                .valoresNuevos(toJson(snapshot(configuracion)))
                .exitoso(true));

        return ConfiguracionProgramaResponse.desde(configuracion);
    }

    private Programa buscarPrograma(Long programaId) {
        return programaRepository.findById(programaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Programa no encontrado: " + programaId));
    }

    private ConfiguracionPrograma configuracionPorDefecto(Programa programa) {
        return ConfiguracionPrograma.builder()
                .programa(programa)
                .notaMinimaAprobacion(programa.getPromedioMinimoGeneral())
                .numeroTotalPracticas(programa.getNumeroTotalPracticas())
                .build();
    }

    private void aplicarCambios(
            ConfiguracionPrograma configuracion,
            ActualizarConfiguracionProgramaRequest request) {

        if (request.getNumeroTotalPracticas() != null) {
            configuracion.setNumeroTotalPracticas(request.getNumeroTotalPracticas());
        } else if (configuracion.getNumeroTotalPracticas() == null) {
            configuracion.setNumeroTotalPracticas(configuracion.getPrograma().getNumeroTotalPracticas());
        }
        configuracion.setDiasInactividadAlerta(request.getDiasInactividadAlerta());
        configuracion.setNotificacionesAutomaticas(request.getNotificacionesAutomaticas());
        configuracion.setNotaMinimaAprobacion(request.getNotaMinimaAprobacion());
        configuracion.setNotaMaxima(request.getNotaMaxima());
        configuracion.setNumeroCortes(request.getNumeroCortes());
        configuracion.setMaximoAsignacionesSimultaneas(request.getMaximoAsignacionesSimultaneas());
        configuracion.setPlantillaCorreoAsignacion(request.getPlantillaCorreoAsignacion());
        configuracion.setPlantillaCorreoSeguimiento(request.getPlantillaCorreoSeguimiento());
        configuracion.setPlantillaCorreoAlerta(request.getPlantillaCorreoAlerta());
        configuracion.setCorreoRemitente(request.getCorreoRemitente());
    }

    private void validarNotas(ActualizarConfiguracionProgramaRequest request) {
        if (request.getNotaMinimaAprobacion() > request.getNotaMaxima()) {
            throw new OperacionNoPermitidaException(
                    "La nota mínima de aprobación no puede ser mayor a la nota máxima.");
        }
    }

    private void validarCorreoRemitente(ActualizarConfiguracionProgramaRequest request) {
        String correoRemitente = request.getCorreoRemitente();
        if (correoRemitente == null || correoRemitente.isBlank()) {
            throw new OperacionNoPermitidaException("El correo remitente no puede estar vacío.");
        }
    }

    private Map<String, Object> snapshot(ConfiguracionPrograma configuracion) {
        Map<String, Object> valores = new LinkedHashMap<>();
        valores.put("programaId", configuracion.getPrograma().getId());
        valores.put("diasInactividadAlerta", configuracion.getDiasInactividadAlerta());
        valores.put("notificacionesAutomaticas", configuracion.getNotificacionesAutomaticas());
        valores.put("notaMinimaAprobacion", configuracion.getNotaMinimaAprobacion());
        valores.put("notaMaxima", configuracion.getNotaMaxima());
        valores.put("numeroTotalPracticas", numeroTotalPracticas(configuracion));
        valores.put("numeroCortes", configuracion.getNumeroCortes());
        valores.put("maximoAsignacionesSimultaneas", configuracion.getMaximoAsignacionesSimultaneas());
        valores.put("correoRemitente", configuracion.getCorreoRemitente());
        return valores;
    }

    private Integer numeroTotalPracticas(ConfiguracionPrograma configuracion) {
        return configuracion.getNumeroTotalPracticas() != null
                ? configuracion.getNumeroTotalPracticas()
                : configuracion.getPrograma().getNumeroTotalPracticas();
    }

    private BitacoraAuditoria.BitacoraAuditoriaBuilder iniciarAuditoria(CustomUserDetails actor) {
        return BitacoraAuditoria.builder()
                .usuario(actor.getUsuario())
                .nombreUsuario(actor.getNombre())
                .rolUsuario(actor.getRol())
                .etiquetaCargoUsuario(actor.getEtiquetaCargo());
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
