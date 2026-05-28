package co.edu.cue.practicas.dto.request;

import lombok.Data;

/**
 * Payload usado por el Coordinador de Prácticas para aprobar, rechazar o cerrar una vacante.
 * Para rechazos el motivo es obligatorio; para aprobaciones es opcional (queda como observación).
 */
@Data
public class DecisionVacanteRequest {
    private String motivo;
}
