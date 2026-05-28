package co.edu.cue.practicas.repository.expediente;

import co.edu.cue.practicas.model.entity.Expediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpedienteRepository extends JpaRepository<Expediente, Long> {

    Optional<Expediente> findByEstudiante_Id(Long estudianteId);
}
