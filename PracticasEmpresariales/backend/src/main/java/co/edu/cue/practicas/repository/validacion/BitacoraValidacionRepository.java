package co.edu.cue.practicas.repository.validacion;

import co.edu.cue.practicas.model.entity.BitacoraValidacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BitacoraValidacionRepository extends JpaRepository<BitacoraValidacion, Long> {

    List<BitacoraValidacion> findByEstudiante_IdOrderByFechaHoraDesc(Long estudianteId);
}
