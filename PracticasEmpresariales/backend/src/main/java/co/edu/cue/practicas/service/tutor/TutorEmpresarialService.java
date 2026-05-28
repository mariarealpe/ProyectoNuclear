package co.edu.cue.practicas.service.tutor;

import co.edu.cue.practicas.audit.ModuloAuditoria;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.CrearTutorEmpresarialRequest;
import co.edu.cue.practicas.dto.request.EditarTutorEmpresarialRequest;
import co.edu.cue.practicas.dto.response.TutorEmpresarialResponse;
import co.edu.cue.practicas.event.UsuarioCreadoEvent;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.BitacoraAuditoria;
import co.edu.cue.practicas.model.entity.Empresa;
import co.edu.cue.practicas.model.entity.TutorEmpresarial;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoAccion;
import co.edu.cue.practicas.repository.empresa.EmpresaRepository;
import co.edu.cue.practicas.repository.tutor.TutorEmpresarialRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.security.annotation.RequiereRol;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Servicio de gestión de tutores empresariales.
 * GPE-151 — Persona 2.
 *
 * Crea la cuenta de Usuario (rol TUTOR_EMPRESARIAL) Y la entidad satélite
 * TutorEmpresarial que liga al usuario con su empresa, cargo y área.
 *
 * Reutiliza el mismo flujo de creación de usuarios:
 *   1. Genera contraseña temporal.
 *   2. Persiste Usuario y TutorEmpresarial.
 *   3. Publica UsuarioCreadoEvent para que NotificacionEventListener envíe el correo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TutorEmpresarialService {

    private final TutorEmpresarialRepository tutorRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaLogger auditoriaLogger;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * Crea el tutor empresarial junto con su cuenta de usuario.
     * Solo DTI y Coordinador de Prácticas pueden vincular un tutor a una empresa.
     */
    @RequiereRol(roles = {Rol.ADMIN_DTI, Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public TutorEmpresarialResponse crearTutor(CrearTutorEmpresarialRequest request, CustomUserDetails creador) {

        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new OperacionNoPermitidaException("El correo ya está registrado en el sistema.");
        }

        Empresa empresa = empresaRepository.findById(request.getEmpresaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada: " + request.getEmpresaId()));

        if (!empresa.isActivo()) {
            throw new OperacionNoPermitidaException("No se pueden crear tutores para una empresa desactivada.");
        }

        // Generamos contraseña temporal; el tutor la cambia en su primer login.
        String passwordTemporal = generarPasswordTemporal();

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .correo(request.getCorreo())
                .passwordHash(passwordEncoder.encode(passwordTemporal))
                .telefono(request.getTelefono())
                .rol(Rol.TUTOR_EMPRESARIAL)
                .activo(true)
                .primerIngreso(true)
                .build();
        usuario = usuarioRepository.save(usuario);

        TutorEmpresarial tutor = TutorEmpresarial.builder()
                .usuario(usuario)
                .empresa(empresa)
                .cargo(request.getCargo())
                .area(request.getArea())
                .telefonoCorporativo(request.getTelefonoCorporativo())
                .esResponsablePrincipal(request.isEsResponsablePrincipal())
                .activo(true)
                .build();
        tutor = tutorRepository.save(tutor);

        // PATRON OBSERVER: avisamos para que se envíe el correo con la contraseña temporal.
        eventPublisher.publishEvent(new UsuarioCreadoEvent(this, usuario, passwordTemporal));

        auditoriaLogger.registrar(iniciarAuditoria(creador)
                .modulo(ModuloAuditoria.TUTORES_EMPRESARIALES)
                .tipoAccion(TipoAccion.CREAR)
                .registroAfectadoId(tutor.getId())
                .registroAfectadoTipo("TutorEmpresarial")
                .valoresNuevos(toJson(tutor))
                .exitoso(true));

        return TutorEmpresarialResponse.desde(tutor);
    }

    /**
     * Edita los datos del tutor y, si aplica, sincroniza nombre/teléfono en su Usuario.
     * No se puede cambiar la empresa desde aquí — para eso hay que desactivar y crear uno nuevo.
     */
    @RequiereRol(roles = {Rol.ADMIN_DTI, Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public TutorEmpresarialResponse editarTutor(Long id, EditarTutorEmpresarialRequest request, CustomUserDetails editor) {
        TutorEmpresarial tutor = buscarPorId(id);
        String antes = toJson(tutor);

        // Sincronizamos los datos básicos en la cuenta Usuario.
        Usuario usuario = tutor.getUsuario();
        usuario.setNombre(request.getNombre());
        usuario.setTelefono(request.getTelefono());
        usuarioRepository.save(usuario);

        tutor.setCargo(request.getCargo());
        tutor.setArea(request.getArea());
        tutor.setTelefonoCorporativo(request.getTelefonoCorporativo());
        tutor.setEsResponsablePrincipal(request.isEsResponsablePrincipal());
        tutorRepository.save(tutor);

        auditoriaLogger.registrar(iniciarAuditoria(editor)
                .modulo(ModuloAuditoria.TUTORES_EMPRESARIALES)
                .tipoAccion(TipoAccion.EDITAR)
                .registroAfectadoId(id)
                .registroAfectadoTipo("TutorEmpresarial")
                .valoresAnteriores(antes)
                .valoresNuevos(toJson(tutor))
                .exitoso(true));

        return TutorEmpresarialResponse.desde(tutor);
    }

    @RequiereRol(roles = {Rol.ADMIN_DTI, Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public void desactivarTutor(Long id, CustomUserDetails ejecutor) {
        TutorEmpresarial tutor = buscarPorId(id);
        tutor.setActivo(false);
        // Desactivamos también la cuenta Usuario para que no pueda iniciar sesión.
        tutor.getUsuario().setActivo(false);
        tutorRepository.save(tutor);
        usuarioRepository.save(tutor.getUsuario());

        auditoriaLogger.registrar(iniciarAuditoria(ejecutor)
                .modulo(ModuloAuditoria.TUTORES_EMPRESARIALES)
                .tipoAccion(TipoAccion.DESACTIVAR)
                .registroAfectadoId(id)
                .registroAfectadoTipo("TutorEmpresarial")
                .exitoso(true));
    }

    @RequiereRol(roles = {Rol.ADMIN_DTI, Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public void activarTutor(Long id, CustomUserDetails ejecutor) {
        TutorEmpresarial tutor = buscarPorId(id);
        tutor.setActivo(true);
        tutor.getUsuario().setActivo(true);
        tutorRepository.save(tutor);
        usuarioRepository.save(tutor.getUsuario());

        auditoriaLogger.registrar(iniciarAuditoria(ejecutor)
                .modulo(ModuloAuditoria.TUTORES_EMPRESARIALES)
                .tipoAccion(TipoAccion.ACTIVAR)
                .registroAfectadoId(id)
                .registroAfectadoTipo("TutorEmpresarial")
                .exitoso(true));
    }

    /**
     * Lista tutores con filtro opcional por empresa.
     * Si empresaId es null, retorna todos los tutores activos paginados.
     */
    @Transactional(readOnly = true)
    public Page<TutorEmpresarialResponse> listar(Long empresaId, Pageable pageable) {
        if (empresaId != null) {
            return tutorRepository.findByEmpresa_Id(empresaId, pageable).map(TutorEmpresarialResponse::desde);
        }
        return tutorRepository.findByActivoTrue(pageable).map(TutorEmpresarialResponse::desde);
    }

    @Transactional(readOnly = true)
    public TutorEmpresarialResponse obtenerPorId(Long id) {
        return TutorEmpresarialResponse.desde(buscarPorId(id));
    }

    // ==========================================================================
    // Métodos privados de apoyo
    // ==========================================================================

    private TutorEmpresarial buscarPorId(Long id) {
        return tutorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tutor empresarial no encontrado: " + id));
    }

    private String generarPasswordTemporal() {
        byte[] bytes = new byte[9];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
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
