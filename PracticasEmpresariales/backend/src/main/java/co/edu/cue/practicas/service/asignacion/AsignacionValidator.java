package co.edu.cue.practicas.service.asignacion;

import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.entity.Vacante;
import co.edu.cue.practicas.model.enums.EstadoAsignacion;
import co.edu.cue.practicas.model.enums.EstadoEstudiante;
import co.edu.cue.practicas.model.enums.EstadoVacante;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.repository.asignacion.AsignacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PATRON SPECIFICATION — GPE-157
 *
 * Agrupa las reglas que determinan si un estudiante puede ser asignado
 * a una vacante. Asi AsignacionService no se convierte en una clase Dios.
 */
@Component
@RequiredArgsConstructor
public class AsignacionValidator {

    private static final List<EstadoAsignacion> ESTADOS_ACTIVOS = List.of(
            EstadoAsignacion.ASIGNADA,
            EstadoAsignacion.EN_VINCULACION,
            EstadoAsignacion.EN_CURSO
    );

    private final AsignacionRepository asignacionRepository;

    public void validarAsignacion(Usuario estudiante, Usuario coordinador, Vacante vacante) {
        if (estudiante.getRol() != Rol.ESTUDIANTE) {
            throw new OperacionNoPermitidaException("El usuario seleccionado no es estudiante");
        }
        if (estudiante.getEstadoEstudiante() != EstadoEstudiante.APTO) {
            throw new OperacionNoPermitidaException("El estudiante debe estar APTO para asignacion");
        }
        if (coordinador.getRol() != Rol.COORDINADOR_PRACTICAS) {
            throw new OperacionNoPermitidaException("Solo un Coordinador de Practicas puede asignar");
        }
        if (vacante.getEstado() != EstadoVacante.DISPONIBLE) {
            throw new OperacionNoPermitidaException("La vacante debe estar DISPONIBLE");
        }
        if (vacante.getCuposOcupados() >= vacante.getCupos()) {
            throw new OperacionNoPermitidaException("La vacante no tiene cupos disponibles");
        }
        if (asignacionRepository.existsByEstudiante_IdAndEstadoIn(estudiante.getId(), ESTADOS_ACTIVOS)) {
            throw new OperacionNoPermitidaException("El estudiante ya tiene asignacion activa");
        }
    }

    public void validarTransicion(EstadoAsignacion anterior, EstadoAsignacion nuevo) {
        boolean valida = (anterior == EstadoAsignacion.ASIGNADA && nuevo == EstadoAsignacion.EN_VINCULACION)
                || (anterior == EstadoAsignacion.ASIGNADA && nuevo == EstadoAsignacion.CANCELADA)
                || (anterior == EstadoAsignacion.EN_VINCULACION && nuevo == EstadoAsignacion.EN_CURSO);
        if (!valida) {
            throw new OperacionNoPermitidaException("Transicion de estado no permitida: " + anterior + " -> " + nuevo);
        }
    }
}
