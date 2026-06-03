package co.edu.cue.practicas.repository.asignacion;

import co.edu.cue.practicas.model.entity.CambioEstadoAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository para CambioEstadoAsignacion.
 * 
 * Bitácora de cambios de estado para trazabilidad de asignaciones.
 */
public interface CambioEstadoAsignacionRepository extends JpaRepository<CambioEstadoAsignacion, Long> {

    /**
     * Obtiene el historial de cambios de una asignación, ordenado por más reciente primero.
     */
    List<CambioEstadoAsignacion> findByAsignacion_IdOrderByFechaHoraDesc(Long asignacionId);

    /**
     * Cuenta cambios de un tipo específico.
     */
    @Query("SELECT COUNT(c) FROM CambioEstadoAsignacion c " +
           "WHERE c.asignacion.id = :asignacionId " +
           "AND c.estadoNuevo = :estado")
    long countCambiosAlEstado(
            @Param("asignacionId") Long asignacionId,
            @Param("estado") co.edu.cue.practicas.model.enums.EstadoAsignacion estado
    );

    /**
     * Obtiene el primer cambio (creación de asignación).
     */
    @Query("SELECT c FROM CambioEstadoAsignacion c " +
           "WHERE c.asignacion.id = :asignacionId " +
           "ORDER BY c.fechaHora ASC LIMIT 1")
    CambioEstadoAsignacion findPrimerCambio(@Param("asignacionId") Long asignacionId);
}
