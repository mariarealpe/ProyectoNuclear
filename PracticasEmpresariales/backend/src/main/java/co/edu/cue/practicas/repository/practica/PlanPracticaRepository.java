package co.edu.cue.practicas.repository.practica;

import co.edu.cue.practicas.model.entity.PlanPractica;
import co.edu.cue.practicas.model.enums.EstadoPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository para PlanPractica.
 * 
 * Plan de la práctica - requiere aprobación de Tutor y Docente antes de iniciar seguimientos.
 */
public interface PlanPracticaRepository extends JpaRepository<PlanPractica, Long> {

    /**
     * Obtiene el plan de una práctica.
     */
    Optional<PlanPractica> findByPractica_Id(Long practicaId);

    /**
     * Obtiene planes en estado RECHAZADO (para lista de pendientes).
     */
    List<PlanPractica> findByEstado(EstadoPlan estado);

    /**
     * Obtiene planes pendientes de aprobación por Tutor Empresarial.
     */
    @Query("SELECT p FROM PlanPractica p " +
           "WHERE p.estado = 'BORRADOR' " +
           "AND p.practica.tutorEmpresarial.id = :tutorId")
    List<PlanPractica> findPendientesTutor(@Param("tutorId") Long tutorId);

    /**
     * Obtiene planes pendientes de aprobación por Docente Asesor.
     */
    @Query("SELECT p FROM PlanPractica p " +
           "WHERE p.estado = 'APROBADO_TUTOR' " +
           "AND p.practica.docenteAsesor.id = :docenteId")
    List<PlanPractica> findPendientesDocente(@Param("docenteId") Long docenteId);

    /**
     * Verifica si un plan está aprobado por docente (desbloqueador).
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM PlanPractica p " +
           "WHERE p.practica.id = :practicaId " +
           "AND p.estado = 'APROBADO_DOCENTE'")
    boolean estaAprobadoDocente(@Param("practicaId") Long practicaId);
}
