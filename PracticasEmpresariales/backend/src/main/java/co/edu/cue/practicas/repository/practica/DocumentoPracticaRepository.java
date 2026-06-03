package co.edu.cue.practicas.repository.practica;

import co.edu.cue.practicas.model.entity.DocumentoPractica;
import co.edu.cue.practicas.model.enums.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository para DocumentoPractica.
 * 
 * Documentos del expediente: carta presentación, convenio, plan, evidencias.
 */
public interface DocumentoPracticaRepository extends JpaRepository<DocumentoPractica, Long> {

    /**
     * Obtiene documentos de una práctica por tipo.
     */
    List<DocumentoPractica> findByPractica_IdAndTipo(Long practicaId, TipoDocumento tipo);

    /**
     * Obtiene todos los documentos de una práctica.
     */
    List<DocumentoPractica> findByPractica_Id(Long practicaId);

    /**
     * Verifica si existe documento mutable de tipo específico en una práctica.
     */
    boolean existsByPractica_IdAndTipoAndEsMutableTrue(Long practicaId, TipoDocumento tipo);

    /**
     * Cuenta documentos por práctica.
     */
    long countByPractica_Id(Long practicaId);

    /**
     * Obtiene el documento convenio de una práctica.
     */
}
