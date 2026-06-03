package co.edu.cue.practicas.repository.asignacion;

import co.edu.cue.practicas.model.entity.Asignacion;
import co.edu.cue.practicas.model.enums.EstadoAsignacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository para Asignacion.
 * 
 * GPE-157: Asignaciones del Coordinador de Prácticas.
 * Solo el Coordinador puede crear/gestionar asignaciones.
 */
public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {

    /**
     * Obtiene asignaciones activas (no canceladas) de un coordinador.
     */
    Page<Asignacion> findByCoordinador_IdAndEstado(
            Long coordinadorId,
            EstadoAsignacion estado,
            Pageable pageable
    );

    /**
     * Obtiene todas las asignaciones de un estudiante.
     */
    Page<Asignacion> findByEstudiante_Id(Long estudianteId, Pageable pageable);

    /**
     * Obtiene asignaciones para una vacante en estado específico.
     */
    List<Asignacion> findByVacante_IdAndEstado(Long vacanteId, EstadoAsignacion estado);

    /**
     * Verifica si el estudiante tiene asignación activa (no cancelada).
     * Usado para validar que no tenga múltiples prácticas simultáneas.
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM Asignacion a " +
           "WHERE a.estudiante.id = :estudianteId " +
           "AND a.estado IN (:estados)")
    boolean existsByEstudiante_IdAndEstadoIn(
            @Param("estudianteId") Long estudianteId,
            @Param("estados") List<EstadoAsignacion> estados
    );

    /**
     * Obtiene la asignación más reciente de un estudiante.
     */
    Optional<Asignacion> findFirstByEstudiante_IdOrderByFechaAsignacionDesc(Long estudianteId);

    /**
     * Obtiene asignaciones por coordinador sin filtro de estado.
     */
    Page<Asignacion> findByCoordinador_Id(Long coordinadorId, Pageable pageable);

    /**
     * Cuenta asignaciones en estado específico para un coordinador.
     */
    long countByCoordinador_IdAndEstado(Long coordinadorId, EstadoAsignacion estado);
}
