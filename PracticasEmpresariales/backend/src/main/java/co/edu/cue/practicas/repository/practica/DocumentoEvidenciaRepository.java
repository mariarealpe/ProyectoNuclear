package co.edu.cue.practicas.repository.practica;

import co.edu.cue.practicas.model.entity.DocumentoEvidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository para DocumentoEvidencia.
 * 
 * Archivos adjuntos en seguimientos semanales.
 */
public interface DocumentoEvidenciaRepository extends JpaRepository<DocumentoEvidencia, Long> {

    /**
     * Obtiene evidencias de un seguimiento.
     */
    List<DocumentoEvidencia> findBySeguimiento_Id(Long seguimientoId);

    /**
     * Cuenta evidencias de un seguimiento.
     */
    long countBySeguimiento_Id(Long seguimientoId);
}
