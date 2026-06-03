package co.edu.cue.practicas.repository.practica;

import co.edu.cue.practicas.model.entity.SeguimientoPractica;
import co.edu.cue.practicas.model.enums.EstadoSeguimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository para SeguimientoPractica.
 * 
 * GPE-170: Seguimientos semanales de prácticas.
 * El estudiante carga semanalmente sus reportes, el Docente los revisa.
 */
public interface SeguimientoPracticaRepository extends JpaRepository<SeguimientoPractica, Long> {

    /**
     * Obtiene el seguimiento de una semana específica.
     */
    Optional<SeguimientoPractica> findByPractica_IdAndSemana(Long practicaId, Integer semana);

    /**
     * Obtiene todos los seguimientos de una práctica, ordenados por semana.
     */
    List<SeguimientoPractica> findByPractica_IdOrderBySemanaAsc(Long practicaId);

    /**
     * Obtiene seguimientos pendientes de revisión por un docente.
     */
    Page<SeguimientoPractica> findByDocenteAsesor_IdAndEstado(
            Long docenteId,
            EstadoSeguimiento estado,
            Pageable pageable
    );

    /**
     * Obtiene seguimientos de una práctica con estados específicos.
     */
    List<SeguimientoPractica> findByPractica_IdAndEstadoIn(
            Long practicaId,
            List<EstadoSeguimiento> estados
    );

    /**
     * Obtiene el seguimiento más reciente de una práctica.
     */
    Optional<SeguimientoPractica> findFirstByPractica_IdOrderBySemanaDesc(Long practicaId);

    /**
     * Cuenta seguimientos RECHAZADO de una práctica.
     */
    long countByPractica_IdAndEstado(Long practicaId, EstadoSeguimiento estado);

    /**
     * Verifica si hay seguimiento RECHAZADO en la semana más reciente.
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM SeguimientoPractica s " +
           "WHERE s.practica.id = :practicaId " +
           "AND s.estado = :estado " +
           "AND s.semana = (SELECT MAX(s2.semana) FROM SeguimientoPractica s2 WHERE s2.practica.id = :practicaId)")
    boolean existsRechazadoEnSemanaReciente(
            @Param("practicaId") Long practicaId,
            @Param("estado") EstadoSeguimiento estado
    );

    /**
     * Cuenta seguimientos sin revisar (PENDIENTE) de un docente.
     */
    long countByDocenteAsesor_IdAndEstado(Long docenteId, EstadoSeguimiento estado);
}
