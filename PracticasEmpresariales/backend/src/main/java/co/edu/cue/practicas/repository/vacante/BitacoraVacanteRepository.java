package co.edu.cue.practicas.repository.vacante;

import co.edu.cue.practicas.model.entity.BitacoraVacante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BitacoraVacanteRepository extends JpaRepository<BitacoraVacante, Long> {

    /** Historial de cambios de una vacante, ordenado del más reciente al más antiguo. */
    List<BitacoraVacante> findByVacante_IdOrderByFechaHoraDesc(Long vacanteId);
}
