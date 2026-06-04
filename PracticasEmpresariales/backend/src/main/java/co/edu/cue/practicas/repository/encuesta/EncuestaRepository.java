package co.edu.cue.practicas.repository.encuesta;

import co.edu.cue.practicas.model.entity.Encuesta;
import co.edu.cue.practicas.model.enums.EstadoEncuesta;
import co.edu.cue.practicas.model.enums.TipoEncuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EncuestaRepository extends JpaRepository<Encuesta, Long> {

    Optional<Encuesta> findByPractica_IdAndTipo(Long practicaId, TipoEncuesta tipo);

    List<Encuesta> findByPractica_Id(Long practicaId);

    boolean existsByPractica_IdAndTipo(Long practicaId, TipoEncuesta tipo);

    /** Encuestas no completadas a las que aplica enviar recordatorio. */
    List<Encuesta> findByEstadoIn(List<EstadoEncuesta> estados);

    List<Encuesta> findByDestinatario_IdAndEstadoIn(Long destinatarioId, List<EstadoEncuesta> estados);
}
