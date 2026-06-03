package co.edu.cue.practicas.repository.configuracion;

import co.edu.cue.practicas.model.entity.ConfiguracionPrograma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository para ConfiguracionPrograma.
 * 
 * Configuraciones a nivel de programa académico.
 */
public interface ConfiguracionProgramaRepository extends JpaRepository<ConfiguracionPrograma, Long> {

    /**
     * Obtiene la configuración de un programa.
     */
    Optional<ConfiguracionPrograma> findByPrograma_Id(Long programaId);

    /**
     * Verifica si existe configuración para un programa.
     */
    boolean existsByPrograma_Id(Long programaId);
}
