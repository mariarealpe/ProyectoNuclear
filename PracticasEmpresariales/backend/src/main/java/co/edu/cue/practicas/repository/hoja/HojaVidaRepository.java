package co.edu.cue.practicas.repository.hoja;

import co.edu.cue.practicas.model.entity.HojaVida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HojaVidaRepository extends JpaRepository<HojaVida, Long> {

    Optional<HojaVida> findByEstudiante_Id(Long estudianteId);
}
