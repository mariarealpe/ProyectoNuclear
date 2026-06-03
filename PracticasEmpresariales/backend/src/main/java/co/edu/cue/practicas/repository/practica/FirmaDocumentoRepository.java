package co.edu.cue.practicas.repository.practica;

import co.edu.cue.practicas.model.entity.FirmaDocumento;
import co.edu.cue.practicas.model.enums.TipoFirmante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para FirmaDocumento.
 * 
 * Firmas del convenio - requiere 3 firmas (Coordinador, Tutor, Estudiante).
 */
public interface FirmaDocumentoRepository extends JpaRepository<FirmaDocumento, Long> {

    /**
     * Obtiene la firma de un tipo específico para un documento.
     */
    Optional<FirmaDocumento> findByDocumento_IdAndTipo(Long documentoId, TipoFirmante tipo);

    /**
     * Obtiene todas las firmas de un documento.
     */
    List<FirmaDocumento> findByDocumento_Id(Long documentoId);

    /**
     * Cuenta firmas confirmadas (con fecha) en un documento.
     */
    long countByDocumento_IdAndFechaFirmaNotNull(Long documentoId);

    /**
     * Verifica si todas las firmas están confirmadas.
     */
    long countByDocumento_Id(Long documentoId);
}
