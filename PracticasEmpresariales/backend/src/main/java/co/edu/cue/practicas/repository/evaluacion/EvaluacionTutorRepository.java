package co.edu.cue.practicas.repository.evaluacion;

import co.edu.cue.practicas.model.entity.EvaluacionTutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para EvaluacionTutor — RF-08-02.
 */
@Repository
public interface EvaluacionTutorRepository extends JpaRepository<EvaluacionTutor, Long> {

    /** Obtiene la evaluación de una práctica (existe como máximo una). */
    Optional<EvaluacionTutor> findByPractica_Id(Long practicaId);

    /** Verifica si ya existe evaluación para una práctica antes de crear una nueva. */
    boolean existsByPractica_Id(Long practicaId);

    /** Lista todas las evaluaciones registradas por un tutor. */
    List<EvaluacionTutor> findByTutor_Id(Long tutorId);
}
