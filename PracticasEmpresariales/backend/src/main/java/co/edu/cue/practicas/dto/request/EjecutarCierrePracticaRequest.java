package co.edu.cue.practicas.dto.request;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

/**
 * Body para ejecutar el cierre formal de una práctica (RF-09-02).
 * Requiere confirmación explícita del Coordinador (no se puede deshacer).
 */
@Data
public class EjecutarCierrePracticaRequest {

    /** Confirmación explícita del Coordinador. Debe ser true. */
    @AssertTrue(message = "Debe confirmar explícitamente la ejecución del cierre")
    private boolean confirmar;

    /** URL al acta de cierre (opcional). Se persiste como DocumentoPractica.ACTA_CIERRE. */
    private String urlActaCierre;

    /** Nombre del archivo del acta (si urlActaCierre fue provisto). */
    private String nombreActaCierre;
}
