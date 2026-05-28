package co.edu.cue.practicas.service.vacante;

import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.model.entity.TutorEmpresarial;
import co.edu.cue.practicas.model.entity.Vacante;
import co.edu.cue.practicas.model.enums.EstadoVacante;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.repository.tutor.TutorEmpresarialRepository;
import co.edu.cue.practicas.repository.vacante.VacanteRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * PATRON PROXY — GPE-152, GPE-153
 *
 * Restringe la visibilidad de vacantes según el rol del usuario autenticado.
 *
 * El servicio VacanteService delega aquí las consultas; ni el controlador ni el
 * servicio saben qué reglas de filtrado aplicar — todas viven dentro del Proxy.
 *
 * Reglas por rol:
 *   - ADMIN_DTI / DIRECCION / COORDINACION_ACADEMICA: ven TODAS las vacantes
 *     (filtro libre opcional por estado).
 *   - COORDINADOR_PRACTICAS: ve las vacantes dirigidas a SU programa.
 *   - ESTUDIANTE: ve solo vacantes DISPONIBLES dirigidas a su programa.
 *   - TUTOR_EMPRESARIAL: ve solo las vacantes asignadas a él como tutor.
 *
 * Para el detalle individual, valida que el usuario tenga permiso de leer esa vacante.
 */
@Component
@RequiredArgsConstructor
public class VacanteAccessProxy {

    private final VacanteRepository vacanteRepository;
    private final TutorEmpresarialRepository tutorRepository;

    /**
     * Lista vacantes aplicando el filtro implícito del rol del usuario autenticado.
     *
     * @param user      principal autenticado
     * @param estado    estado opcional (si null, no filtra por estado salvo en ESTUDIANTE)
     * @param pageable  configuración de paginación
     */
    public Page<Vacante> listarSegunRol(CustomUserDetails user, EstadoVacante estado, Pageable pageable) {

        if (user == null || user.getRol() == null) {
            throw new AccesoNoAutorizadoException("Sesión inválida.");
        }

        return switch (user.getRol()) {
            // Roles administrativos ven todo, con filtro opcional por estado.
            case ADMIN_DTI, DIRECCION, COORDINACION_ACADEMICA -> {
                yield estado != null
                        ? vacanteRepository.findByEstado(estado, pageable)
                        : vacanteRepository.findAll(pageable);
            }
            // El coordinador de prácticas ve las del programa que coordina.
            case COORDINADOR_PRACTICAS -> {
                Long programaId = user.getProgramaId();
                if (programaId == null) {
                    // Coordinador sin programa asignado: no ve nada para evitar fugas de datos.
                    yield Page.empty(pageable);
                }
                yield estado != null
                        ? vacanteRepository.findByPrograma_IdAndEstado(programaId, estado, pageable)
                        : vacanteRepository.findByPrograma_Id(programaId, pageable);
            }
            // El estudiante solo ve las DISPONIBLES de su programa, sin importar el filtro de estado.
            case ESTUDIANTE -> {
                Long programaId = user.getProgramaId();
                if (programaId == null) yield Page.empty(pageable);
                yield vacanteRepository.findByPrograma_IdAndEstado(programaId, EstadoVacante.DISPONIBLE, pageable);
            }
            // El tutor empresarial solo ve las vacantes asignadas a él.
            case TUTOR_EMPRESARIAL -> {
                TutorEmpresarial tutor = tutorRepository.findByUsuario_Id(user.getId()).orElse(null);
                if (tutor == null) yield Page.empty(pageable);
                yield vacanteRepository.findByTutor_Id(tutor.getId(), pageable);
            }
            // Roles no listados explícitamente no ven nada.
            default -> Page.empty(pageable);
        };
    }

    /**
     * Verifica que el usuario tenga permiso para leer la vacante indicada.
     * Lanza AccesoNoAutorizadoException (403) si no aplica el rol.
     */
    public void verificarAccesoLectura(CustomUserDetails user, Vacante vacante) {
        if (user == null || vacante == null) {
            throw new AccesoNoAutorizadoException("Acceso no autorizado a la vacante.");
        }

        switch (user.getRol()) {
            case ADMIN_DTI, DIRECCION, COORDINACION_ACADEMICA -> {
                // Roles administrativos pueden ver cualquier vacante.
            }
            case COORDINADOR_PRACTICAS -> {
                // Solo si pertenece al programa que coordina.
                Long programaIdVac = vacante.getPrograma() != null ? vacante.getPrograma().getId() : null;
                if (programaIdVac == null || !programaIdVac.equals(user.getProgramaId())) {
                    throw new AccesoNoAutorizadoException("Esta vacante no pertenece a su programa.");
                }
            }
            case ESTUDIANTE -> {
                // Solo si está DISPONIBLE y va dirigida a su programa.
                Long programaIdVac = vacante.getPrograma() != null ? vacante.getPrograma().getId() : null;
                boolean mismoPrograma = programaIdVac != null && programaIdVac.equals(user.getProgramaId());
                boolean visible = EstadoVacante.DISPONIBLE.equals(vacante.getEstado());
                if (!(mismoPrograma && visible)) {
                    throw new AccesoNoAutorizadoException("Esta vacante no está disponible para usted.");
                }
            }
            case TUTOR_EMPRESARIAL -> {
                // Solo si está asignado como tutor de esa vacante.
                Long tutorUsuarioId = vacante.getTutor() != null && vacante.getTutor().getUsuario() != null
                        ? vacante.getTutor().getUsuario().getId() : null;
                if (tutorUsuarioId == null || !tutorUsuarioId.equals(user.getId())) {
                    throw new AccesoNoAutorizadoException("No está asignado como tutor de esta vacante.");
                }
            }
            default -> throw new AccesoNoAutorizadoException("Rol sin permisos sobre vacantes.");
        }
    }
}
