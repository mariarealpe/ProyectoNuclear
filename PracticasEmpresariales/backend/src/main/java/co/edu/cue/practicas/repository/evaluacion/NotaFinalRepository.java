package co.edu.cue.practicas.repository.evaluacion;

import co.edu.cue.practicas.model.entity.NotaFinal;
import co.edu.cue.practicas.model.enums.ResultadoNotaFinal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository para NotaFinal — RF-08-04.
 */
@Repository
public interface NotaFinalRepository extends JpaRepository<NotaFinal, Long> {

    Optional<NotaFinal> findByPractica_Id(Long practicaId);

    boolean existsByPractica_Id(Long practicaId);

    // ====== Agregaciones para reportes RF-10-01 / RF-10-04 ======

    long countByResultado(ResultadoNotaFinal resultado);

    long countByPractica_Programa_IdAndResultado(Long programaId, ResultadoNotaFinal resultado);

    long countByPractica_Programa_Facultad_IdAndResultado(Long facultadId, ResultadoNotaFinal resultado);

    long countByPractica_Programa_IdAndPractica_NumeroPracticaAndResultado(
            Long programaId, int numeroPractica, ResultadoNotaFinal resultado);

    long countByCerradaEnBetween(LocalDateTime desde, LocalDateTime hasta);
}
