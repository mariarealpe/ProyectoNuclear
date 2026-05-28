package co.edu.cue.practicas.repository.practica;

import co.edu.cue.practicas.model.entity.PracticaCatalogo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PracticaCatalogoRepository extends JpaRepository<PracticaCatalogo, Long> {

    boolean existsByPrograma_IdAndNumeroPractica(Long programaId, int numeroPractica);

    Page<PracticaCatalogo> findByActivoTrue(Pageable pageable);

    Page<PracticaCatalogo> findByPrograma_IdAndActivoTrue(Long programaId, Pageable pageable);

    Optional<PracticaCatalogo> findByPrograma_IdAndNumeroPracticaAndActivoTrue(Long programaId, int numeroPractica);
}
