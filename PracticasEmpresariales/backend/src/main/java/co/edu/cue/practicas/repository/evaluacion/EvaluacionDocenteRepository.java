package co.edu.cue.practicas.repository.evaluacion;

import co.edu.cue.practicas.model.entity.EvaluacionDocente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para EvaluacionDocente — RF-08-01.
 */
@Repository
public interface EvaluacionDocenteRepository extends JpaRepository<EvaluacionDocente, Long> {

    /** Obtiene la evaluación de una práctica (existe como máximo una). */
    Optional<EvaluacionDocente> findByPractica_Id(Long practicaId);

    /** Verifica si ya existe evaluación para una práctica antes de crear una nueva. */
    boolean existsByPractica_Id(Long practicaId);

    /** Lista todas las evaluaciones registradas por un docente. */
    List<EvaluacionDocente> findByDocente_Id(Long docenteId);
}
