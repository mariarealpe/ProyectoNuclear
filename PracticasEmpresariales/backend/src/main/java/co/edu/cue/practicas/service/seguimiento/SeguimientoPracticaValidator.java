package co.edu.cue.practicas.service.seguimiento;

import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.SeguimientoPractica;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.EstadoSeguimiento;
import co.edu.cue.practicas.repository.practica.SeguimientoPracticaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * PATRON SPECIFICATION — GPE-168, GPE-170
 *
 * Reglas de la bitacora semanal. Mantiene el servicio de seguimiento
 * enfocado en orquestar el caso de uso.
 */
@Component
@RequiredArgsConstructor
public class SeguimientoPracticaValidator {

    private final SeguimientoPracticaRepository seguimientoRepository;

    public void validarCreacion(Practica practica, Long estudianteId, Integer semana) {
        if (practica.getEstado() != EstadoPractica.EN_CURSO) {
            throw new OperacionNoPermitidaException("La practica debe estar EN_CURSO para registrar seguimiento");
        }
        if (!practica.getEstudiante().getId().equals(estudianteId)) {
            throw new OperacionNoPermitidaException("El seguimiento debe ser cargado por el estudiante de la practica");
        }
        if (seguimientoRepository.findByPractica_IdAndSemana(practica.getId(), semana).isPresent()) {
            throw new OperacionNoPermitidaException("Ya existe seguimiento para esa semana");
        }
    }

    public void validarRevision(EstadoSeguimiento estado) {
        if (estado == EstadoSeguimiento.PENDIENTE) {
            throw new OperacionNoPermitidaException("La revision debe aprobar o rechazar el seguimiento");
        }
    }

    public void validarCorreccion(SeguimientoPractica seguimiento, int semanaActual) {
        if (!seguimiento.esEditablePorEstudiante(semanaActual)) {
            throw new OperacionNoPermitidaException("Solo se puede corregir el seguimiento rechazado de la semana mas reciente");
        }
    }
}
