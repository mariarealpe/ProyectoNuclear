package co.edu.cue.practicas.service.empresa;

import co.edu.cue.practicas.audit.ModuloAuditoria;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.CrearEmpresaRequest;
import co.edu.cue.practicas.dto.request.EditarEmpresaRequest;
import co.edu.cue.practicas.dto.response.EmpresaResponse;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.BitacoraAuditoria;
import co.edu.cue.practicas.model.entity.Empresa;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoAccion;
import co.edu.cue.practicas.repository.empresa.EmpresaRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.security.annotation.RequiereRol;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Servicio de gestión de empresas que ofrecen vacantes de práctica.
 * GPE-150 — Persona 2.
 *
 * Solo el Administrador DTI y el Coordinador de Prácticas pueden registrar
 * empresas en el sistema. Cualquier usuario autenticado puede consultarlas
 * (estudiantes y docentes necesitan ver el directorio).
 *
 * El soft delete (campo activo) preserva el histórico de vacantes y tutores
 * asociados a la empresa aunque se desactive el registro.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmpresaService {

    // Acceso JPA a la tabla de empresas
    private final EmpresaRepository empresaRepository;

    // Registra cada operación en la bitácora central de auditoría (PATRON SINGLETON)
    private final AuditoriaLogger auditoriaLogger;

    // Convierte objetos a JSON para el "antes/después" en la bitácora
    private final ObjectMapper objectMapper;

    /**
     * Registra una nueva empresa en el sistema.
     * Solo DTI o Coordinador de Prácticas pueden ejecutarlo.
     */
    @RequiereRol(roles = {Rol.ADMIN_DTI, Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public EmpresaResponse crearEmpresa(CrearEmpresaRequest request, CustomUserDetails creador) {

        // El NIT es el identificador único de negocio; no se permiten duplicados.
        if (empresaRepository.existsByNit(request.getNit())) {
            throw new OperacionNoPermitidaException("Ya existe una empresa registrada con el NIT " + request.getNit());
        }

        Empresa empresa = Empresa.builder()
                .nit(request.getNit())
                .razonSocial(request.getRazonSocial())
                .nombreComercial(request.getNombreComercial())
                .sector(request.getSector())
                .direccion(request.getDireccion())
                .ciudad(request.getCiudad())
                .correoContacto(request.getCorreoContacto())
                .telefono(request.getTelefono())
                .sitioWeb(request.getSitioWeb())
                .representanteLegal(request.getRepresentanteLegal())
                .descripcion(request.getDescripcion())
                .activo(true)
                .build();

        empresa = empresaRepository.save(empresa);

        auditoriaLogger.registrar(iniciarAuditoria(creador)
                .modulo(ModuloAuditoria.EMPRESAS)
                .tipoAccion(TipoAccion.CREAR)
                .registroAfectadoId(empresa.getId())
                .registroAfectadoTipo("Empresa")
                .valoresNuevos(toJson(Map.of(
                        "nit", empresa.getNit(),
                        "razonSocial", empresa.getRazonSocial())))
                .exitoso(true));

        return EmpresaResponse.desde(empresa);
    }

    /**
     * Edita los datos de una empresa. Mantenemos el id intacto y validamos que el NIT
     * no se duplique con el de otra empresa distinta.
     */
    @RequiereRol(roles = {Rol.ADMIN_DTI, Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public EmpresaResponse editarEmpresa(Long id, EditarEmpresaRequest request, CustomUserDetails editor) {
        Empresa empresa = buscarPorId(id);

        // Capturamos el snapshot antes de cualquier cambio para la bitácora.
        String antes = toJson(empresa);

        // Si cambian el NIT, validamos que no exista en otra empresa.
        if (!empresa.getNit().equals(request.getNit())
                && empresaRepository.existsByNitAndIdNot(request.getNit(), id)) {
            throw new OperacionNoPermitidaException("El NIT " + request.getNit() + " ya está en uso por otra empresa.");
        }

        empresa.setNit(request.getNit());
        empresa.setRazonSocial(request.getRazonSocial());
        empresa.setNombreComercial(request.getNombreComercial());
        empresa.setSector(request.getSector());
        empresa.setDireccion(request.getDireccion());
        empresa.setCiudad(request.getCiudad());
        empresa.setCorreoContacto(request.getCorreoContacto());
        empresa.setTelefono(request.getTelefono());
        empresa.setSitioWeb(request.getSitioWeb());
        empresa.setRepresentanteLegal(request.getRepresentanteLegal());
        empresa.setDescripcion(request.getDescripcion());
        empresaRepository.save(empresa);

        auditoriaLogger.registrar(iniciarAuditoria(editor)
                .modulo(ModuloAuditoria.EMPRESAS)
                .tipoAccion(TipoAccion.EDITAR)
                .registroAfectadoId(id)
                .registroAfectadoTipo("Empresa")
                .valoresAnteriores(antes)
                .valoresNuevos(toJson(empresa))
                .exitoso(true));

        return EmpresaResponse.desde(empresa);
    }

    @RequiereRol(roles = {Rol.ADMIN_DTI, Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public void desactivarEmpresa(Long id, CustomUserDetails ejecutor) {
        Empresa empresa = buscarPorId(id);
        empresa.setActivo(false);
        empresaRepository.save(empresa);

        auditoriaLogger.registrar(iniciarAuditoria(ejecutor)
                .modulo(ModuloAuditoria.EMPRESAS)
                .tipoAccion(TipoAccion.DESACTIVAR)
                .registroAfectadoId(id)
                .registroAfectadoTipo("Empresa")
                .exitoso(true));
    }

    @RequiereRol(roles = {Rol.ADMIN_DTI, Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public void activarEmpresa(Long id, CustomUserDetails ejecutor) {
        Empresa empresa = buscarPorId(id);
        empresa.setActivo(true);
        empresaRepository.save(empresa);

        auditoriaLogger.registrar(iniciarAuditoria(ejecutor)
                .modulo(ModuloAuditoria.EMPRESAS)
                .tipoAccion(TipoAccion.ACTIVAR)
                .registroAfectadoId(id)
                .registroAfectadoTipo("Empresa")
                .exitoso(true));
    }

    /**
     * Lista empresas activas paginadas.
     * El @Transactional(readOnly = true) mantiene la sesión JPA abierta porque el
     * mapeo a DTO no toca relaciones LAZY — pero lo dejamos por consistencia con el resto.
     */
    @Transactional(readOnly = true)
    public Page<EmpresaResponse> listar(Pageable pageable, String filtro) {
        if (filtro != null && !filtro.isBlank()) {
            return empresaRepository
                    .findByRazonSocialContainingIgnoreCaseAndActivoTrue(filtro, pageable)
                    .map(EmpresaResponse::desde);
        }
        return empresaRepository.findByActivoTrue(pageable).map(EmpresaResponse::desde);
    }

    @Transactional(readOnly = true)
    public EmpresaResponse obtenerPorId(Long id) {
        return EmpresaResponse.desde(buscarPorId(id));
    }

    // ==========================================================================
    // Métodos privados de apoyo
    // ==========================================================================

    private Empresa buscarPorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada: " + id));
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
