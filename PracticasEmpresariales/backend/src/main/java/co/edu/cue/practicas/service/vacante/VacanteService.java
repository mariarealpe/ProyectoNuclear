package co.edu.cue.practicas.service.vacante;

import co.edu.cue.practicas.audit.ModuloAuditoria;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.CrearVacanteRequest;
import co.edu.cue.practicas.dto.request.DecisionVacanteRequest;
import co.edu.cue.practicas.dto.request.EditarVacanteRequest;
import co.edu.cue.practicas.dto.response.BitacoraVacanteResponse;
import co.edu.cue.practicas.dto.response.VacanteResponse;
import co.edu.cue.practicas.event.VacanteAprobadaEvent;
import co.edu.cue.practicas.event.VacanteCreadaEvent;
import co.edu.cue.practicas.event.VacanteRechazadaEvent;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.BitacoraAuditoria;
import co.edu.cue.practicas.model.entity.BitacoraVacante;
import co.edu.cue.practicas.model.entity.Empresa;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.model.entity.TutorEmpresarial;
import co.edu.cue.practicas.model.entity.Vacante;
import co.edu.cue.practicas.model.enums.EstadoVacante;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoAccion;
import co.edu.cue.practicas.repository.empresa.EmpresaRepository;
import co.edu.cue.practicas.repository.programa.ProgramaRepository;
import co.edu.cue.practicas.repository.tutor.TutorEmpresarialRepository;
import co.edu.cue.practicas.repository.vacante.BitacoraVacanteRepository;
import co.edu.cue.practicas.repository.vacante.VacanteRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.security.annotation.RequiereRol;
import co.edu.cue.practicas.service.vacante.state.EstadoVacanteFactory;
import co.edu.cue.practicas.service.vacante.state.EstadoVacanteState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio central de gestión de vacantes de práctica empresarial.
 * GPE-152, GPE-153 — Persona 2.
 *
 * Combina cuatro patrones de diseño:
 *   - BUILDER:  VacanteBuilder construye la vacante paso a paso.
 *   - STATE:    EstadoVacanteState controla las transiciones del ciclo de vida.
 *   - OBSERVER: VacanteCreadaEvent / VacanteAprobadaEvent / VacanteRechazadaEvent
 *               desacoplan la lógica de notificación.
 *   - PROXY:    VacanteAccessProxy restringe qué vacantes ve cada rol.
 *
 * El flujo de aprobación (GPE-153) lo conduce el Coordinador de Prácticas;
 * el Admin DTI también puede ejecutarlo como respaldo administrativo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VacanteService {

    private final VacanteRepository vacanteRepository;
    private final BitacoraVacanteRepository bitacoraVacanteRepository;
    private final EmpresaRepository empresaRepository;
    private final ProgramaRepository programaRepository;
    private final TutorEmpresarialRepository tutorRepository;

    private final VacanteAccessProxy accessProxy;
    private final AuditoriaLogger auditoriaLogger;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    // ==========================================================================
    // CRUD básico
    // ==========================================================================

    /**
     * Registra una nueva vacante (estado inicial: PENDIENTE).
     * El Coordinador de Prácticas la crea en nombre de la empresa, o el DTI como respaldo.
     */
    @RequiereRol(roles = {Rol.ADMIN_DTI, Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public VacanteResponse crearVacante(CrearVacanteRequest request, CustomUserDetails creador) {

        Empresa empresa = empresaRepository.findById(request.getEmpresaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada: " + request.getEmpresaId()));
        if (!empresa.isActivo()) {
            throw new OperacionNoPermitidaException("No se pueden publicar vacantes para una empresa desactivada.");
        }

        Programa programa = programaRepository.findById(request.getProgramaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Programa no encontrado: " + request.getProgramaId()));

        // El tutor es opcional al crear. Si llega, debe pertenecer a la misma empresa.
        TutorEmpresarial tutor = null;
        if (request.getTutorId() != null) {
            tutor = tutorRepository.findById(request.getTutorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Tutor no encontrado: " + request.getTutorId()));
            if (tutor.getEmpresa() == null || !tutor.getEmpresa().getId().equals(empresa.getId())) {
                throw new OperacionNoPermitidaException("El tutor no pertenece a la empresa indicada.");
            }
        }

        // PATRON BUILDER: construye la vacante con todas las validaciones de negocio.
        Vacante vacante = VacanteBuilder.nuevo()
                .conTitulo(request.getTitulo())
                .conDescripcion(request.getDescripcion())
                .paraEmpresa(empresa)
                .paraPrograma(programa)
                .conTutor(tutor)
                .conModalidad(request.getModalidad())
                .conJornada(request.getJornada())
                .enCiudad(request.getCiudad())
                .conCupos(request.getCupos())
                .conDuracion(request.getDuracionMeses())
                .conRemuneracion(request.isRemunerada(), request.getValorRemuneracion())
                .entreFechas(request.getFechaInicio(), request.getFechaFin())
                .conRequisitos(request.getRequisitos())
                .conResponsabilidades(request.getResponsabilidades())
                .construir();

        vacante = vacanteRepository.save(vacante);

        // Registramos el primer evento de bitácora (creación de la vacante).
        registrarBitacora(vacante, creador, null, EstadoVacante.PENDIENTE, "Vacante creada");

        // PATRON OBSERVER: notifica al Coordinador de Prácticas que hay nueva vacante para revisar.
        eventPublisher.publishEvent(new VacanteCreadaEvent(this, vacante));

        // Auditoría general del sistema.
        auditoriaLogger.registrar(iniciarAuditoria(creador)
                .modulo(ModuloAuditoria.VACANTES)
                .tipoAccion(TipoAccion.CREAR)
                .registroAfectadoId(vacante.getId())
                .registroAfectadoTipo("Vacante")
                .valoresNuevos(toJson(vacante))
                .exitoso(true));

        return VacanteResponse.desde(vacante);
    }

    /**
     * Edita los datos generales de una vacante.
     * Si estaba RECHAZADA, la edición la devuelve a PENDIENTE (re-solicitud de aprobación).
     */
    @RequiereRol(roles = {Rol.ADMIN_DTI, Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public VacanteResponse editarVacante(Long id, EditarVacanteRequest request, CustomUserDetails editor) {
        Vacante vacante = buscarPorId(id);
        String antes = toJson(vacante);

        // Las vacantes cerradas no se editan — son históricas.
        if (EstadoVacante.CERRADA.equals(vacante.getEstado())) {
            throw new OperacionNoPermitidaException("Una vacante CERRADA no se puede editar.");
        }

        // Resolvemos el tutor si cambió, validando consistencia con la empresa.
        TutorEmpresarial tutor = null;
        if (request.getTutorId() != null) {
            tutor = tutorRepository.findById(request.getTutorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Tutor no encontrado: " + request.getTutorId()));
            if (vacante.getEmpresa() != null && !tutor.getEmpresa().getId().equals(vacante.getEmpresa().getId())) {
                throw new OperacionNoPermitidaException("El tutor no pertenece a la empresa de la vacante.");
            }
        }

        vacante.setTitulo(request.getTitulo());
        vacante.setDescripcion(request.getDescripcion());
        vacante.setTutor(tutor);
        vacante.setModalidad(request.getModalidad());
        vacante.setJornada(request.getJornada());
        vacante.setCiudad(request.getCiudad());
        vacante.setCupos(request.getCupos());
        vacante.setDuracionMeses(request.getDuracionMeses());
        vacante.setRemunerada(request.isRemunerada());
        vacante.setValorRemuneracion(request.isRemunerada() ? request.getValorRemuneracion() : null);
        vacante.setFechaInicio(request.getFechaInicio());
        vacante.setFechaFin(request.getFechaFin());
        vacante.setRequisitos(request.getRequisitos());
        vacante.setResponsabilidades(request.getResponsabilidades());

        // Si estaba RECHAZADA, al editarla la regresamos a PENDIENTE — flujo "reenviar a aprobación".
        EstadoVacante estadoAnterior = vacante.getEstado();
        if (EstadoVacante.RECHAZADA.equals(estadoAnterior)) {
            EstadoVacanteState state = EstadoVacanteFactory.para(estadoAnterior);
            state.reabrir(vacante);
            registrarBitacora(vacante, editor, estadoAnterior, EstadoVacante.PENDIENTE,
                    "Editada por la empresa y reenviada a aprobación");
        }

        vacante = vacanteRepository.save(vacante);

        auditoriaLogger.registrar(iniciarAuditoria(editor)
                .modulo(ModuloAuditoria.VACANTES)
                .tipoAccion(TipoAccion.EDITAR)
                .registroAfectadoId(id)
                .registroAfectadoTipo("Vacante")
                .valoresAnteriores(antes)
                .valoresNuevos(toJson(vacante))
                .exitoso(true));

        return VacanteResponse.desde(vacante);
    }

    // ==========================================================================
    // Flujo de aprobación (PATRON STATE)
    // ==========================================================================

    /**
     * Aprueba una vacante: PENDIENTE → DISPONIBLE.
     * Solo el Coordinador de Prácticas o el DTI pueden ejecutarlo.
     */
    @RequiereRol(roles = {Rol.ADMIN_DTI, Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public VacanteResponse aprobarVacante(Long id, DecisionVacanteRequest request, CustomUserDetails coordinador) {
        Vacante vacante = buscarPorId(id);
        EstadoVacante estadoAnterior = vacante.getEstado();

        // PATRON STATE: pedimos al estado actual que aplique la transición; si no la permite, lanza excepción.
        EstadoVacanteState state = EstadoVacanteFactory.para(estadoAnterior);
        state.aprobar(vacante, request != null ? request.getMotivo() : null);

        // Quién aprobó queda registrado en la vacante para reporte.
        vacante.setAprobador(coordinador.getUsuario());
        vacante = vacanteRepository.save(vacante);

        registrarBitacora(vacante, coordinador, estadoAnterior, EstadoVacante.DISPONIBLE,
                request != null ? request.getMotivo() : null);

        // PATRON OBSERVER: notifica a la empresa que su vacante ya está publicada.
        eventPublisher.publishEvent(new VacanteAprobadaEvent(this, vacante,
                request != null ? request.getMotivo() : null));

        auditoriaLogger.registrar(iniciarAuditoria(coordinador)
                .modulo(ModuloAuditoria.VACANTES)
                .tipoAccion(TipoAccion.CAMBIO_ESTADO)
                .registroAfectadoId(id)
                .registroAfectadoTipo("Vacante")
                .valoresAnteriores(toJson(java.util.Map.of("estado", estadoAnterior)))
                .valoresNuevos(toJson(java.util.Map.of("estado", EstadoVacante.DISPONIBLE)))
                .exitoso(true));

        return VacanteResponse.desde(vacante);
    }

    /**
     * Rechaza una vacante: PENDIENTE → RECHAZADA. El motivo es obligatorio.
     */
    @RequiereRol(roles = {Rol.ADMIN_DTI, Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public VacanteResponse rechazarVacante(Long id, DecisionVacanteRequest request, CustomUserDetails coordinador) {
        if (request == null || request.getMotivo() == null || request.getMotivo().isBlank()) {
            throw new OperacionNoPermitidaException("El motivo del rechazo es obligatorio.");
        }
        Vacante vacante = buscarPorId(id);
        EstadoVacante estadoAnterior = vacante.getEstado();

        EstadoVacanteState state = EstadoVacanteFactory.para(estadoAnterior);
        state.rechazar(vacante, request.getMotivo());

        vacante.setAprobador(coordinador.getUsuario());
        vacante = vacanteRepository.save(vacante);

        registrarBitacora(vacante, coordinador, estadoAnterior, EstadoVacante.RECHAZADA, request.getMotivo());

        eventPublisher.publishEvent(new VacanteRechazadaEvent(this, vacante, request.getMotivo()));

        auditoriaLogger.registrar(iniciarAuditoria(coordinador)
                .modulo(ModuloAuditoria.VACANTES)
                .tipoAccion(TipoAccion.CAMBIO_ESTADO)
                .registroAfectadoId(id)
                .registroAfectadoTipo("Vacante")
                .valoresAnteriores(toJson(java.util.Map.of("estado", estadoAnterior)))
                .valoresNuevos(toJson(java.util.Map.of(
                        "estado", EstadoVacante.RECHAZADA,
                        "motivo", request.getMotivo())))
                .exitoso(true));

        return VacanteResponse.desde(vacante);
    }

    /**
     * Cierra una vacante: DISPONIBLE → CERRADA (cupos llenos o cierre manual).
     */
    @RequiereRol(roles = {Rol.ADMIN_DTI, Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public VacanteResponse cerrarVacante(Long id, DecisionVacanteRequest request, CustomUserDetails coordinador) {
        Vacante vacante = buscarPorId(id);
        EstadoVacante estadoAnterior = vacante.getEstado();

        EstadoVacanteState state = EstadoVacanteFactory.para(estadoAnterior);
        state.cerrar(vacante, request != null ? request.getMotivo() : null);

        vacante = vacanteRepository.save(vacante);

        registrarBitacora(vacante, coordinador, estadoAnterior, EstadoVacante.CERRADA,
                request != null ? request.getMotivo() : "Cierre manual");

        auditoriaLogger.registrar(iniciarAuditoria(coordinador)
                .modulo(ModuloAuditoria.VACANTES)
                .tipoAccion(TipoAccion.CAMBIO_ESTADO)
                .registroAfectadoId(id)
                .registroAfectadoTipo("Vacante")
                .valoresAnteriores(toJson(java.util.Map.of("estado", estadoAnterior)))
                .valoresNuevos(toJson(java.util.Map.of("estado", EstadoVacante.CERRADA)))
                .exitoso(true));

        return VacanteResponse.desde(vacante);
    }

    // ==========================================================================
    // Lecturas (PATRON PROXY decide qué se ve por rol)
    // ==========================================================================

    @Transactional(readOnly = true)
    public Page<VacanteResponse> listar(EstadoVacante estado, Pageable pageable, CustomUserDetails user) {
        // El Proxy aplica el filtro implícito del rol.
        return accessProxy.listarSegunRol(user, estado, pageable).map(VacanteResponse::desde);
    }

    @Transactional(readOnly = true)
    public VacanteResponse obtenerPorId(Long id, CustomUserDetails user) {
        Vacante vacante = buscarPorId(id);
        // El Proxy lanza 403 si el rol no tiene permiso de ver esta vacante en particular.
        accessProxy.verificarAccesoLectura(user, vacante);
        return VacanteResponse.desde(vacante);
    }

    @Transactional(readOnly = true)
    public List<BitacoraVacanteResponse> historial(Long vacanteId, CustomUserDetails user) {
        Vacante vacante = buscarPorId(vacanteId);
        accessProxy.verificarAccesoLectura(user, vacante);
        return bitacoraVacanteRepository.findByVacante_IdOrderByFechaHoraDesc(vacanteId)
                .stream().map(BitacoraVacanteResponse::desde).toList();
    }

    // ==========================================================================
    // Métodos privados de apoyo
    // ==========================================================================

    private Vacante buscarPorId(Long id) {
        return vacanteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vacante no encontrada: " + id));
    }

    /** Persiste un registro en la bitácora específica de vacantes. */
    private void registrarBitacora(Vacante vacante, CustomUserDetails actor,
                                   EstadoVacante anterior, EstadoVacante nuevo, String motivo) {
        BitacoraVacante registro = BitacoraVacante.builder()
                .vacante(vacante)
                .usuario(actor != null ? actor.getUsuario() : null)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .motivo(motivo)
                .build();
        bitacoraVacanteRepository.save(registro);
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
