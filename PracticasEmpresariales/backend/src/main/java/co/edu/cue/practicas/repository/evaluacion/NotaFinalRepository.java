package co.edu.cue.practicas.repository.evaluacion;

import co.edu.cue.practicas.model.entity.NotaFinal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para NotaFinal — RF-08-04.
 */
@Repository
public interface NotaFinalRepository extends JpaRepository<NotaFinal, Long> {

    Optional<NotaFinal> findByPractica_Id(Long practicaId);

    boolean existsByPractica_Id(Long practicaId);
}
