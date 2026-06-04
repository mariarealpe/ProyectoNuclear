package co.edu.cue.practicas.service.practica;

import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import org.springframework.stereotype.Component;

/**
 * PATRON SPECIFICATION — GPE-162, GPE-163
 *
 * Reglas de documentos de practica. Evita que DocumentoPracticaService mezcle
 * persistencia, firmas y validaciones de mutabilidad.
 */
@Component
public class DocumentoPracticaValidator {

    public void validarMutable(Practica practica) {
        if (practica.getEstado() == EstadoPractica.FINALIZADA || practica.getEstado() == EstadoPractica.CANCELADA) {
            throw new OperacionNoPermitidaException("Los documentos de practicas finalizadas o canceladas son inmutables");
        }
    }
}
