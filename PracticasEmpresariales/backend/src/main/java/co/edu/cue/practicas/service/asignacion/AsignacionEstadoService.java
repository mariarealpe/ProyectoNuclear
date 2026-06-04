package co.edu.cue.practicas.service.asignacion;

import co.edu.cue.practicas.model.entity.Asignacion;
import co.edu.cue.practicas.model.entity.Vacante;
import co.edu.cue.practicas.model.enums.EstadoAsignacion;
import co.edu.cue.practicas.model.enums.EstadoVacante;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * PATRON STATE — GPE-158, GPE-159
 *
 * Aplica cambios de estado y sus efectos sobre fechas/cupos.
 * El servicio de asignacion delega aqui para no mezclar transiciones con
 * validaciones, trazabilidad y notificaciones.
 */
@Service
@RequiredArgsConstructor
public class AsignacionEstadoService {

    private final AsignacionValidator validator;

    public void ocuparCupo(Vacante vacante) {
        vacante.setCuposOcupados(vacante.getCuposOcupados() + 1);
        if (vacante.getCuposOcupados() >= vacante.getCupos()) {
            vacante.setEstado(EstadoVacante.CERRADA);
        }
    }

    public void aplicarTransicion(Asignacion asignacion, EstadoAsignacion nuevoEstado, String motivo) {
        validator.validarTransicion(asignacion.getEstado(), nuevoEstado);

        asignacion.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoAsignacion.EN_VINCULACION) {
            asignacion.setFechaVinculacion(LocalDateTime.now());
        }
        if (nuevoEstado == EstadoAsignacion.CANCELADA) {
            asignacion.setMotivoCancelacion(motivo);
            liberarCupo(asignacion.getVacante());
        }
    }

    private void liberarCupo(Vacante vacante) {
        if (vacante.getCuposOcupados() > 0) {
            vacante.setCuposOcupados(vacante.getCuposOcupados() - 1);
        }
        if (vacante.getEstado() == EstadoVacante.CERRADA && vacante.getCuposOcupados() < vacante.getCupos()) {
            vacante.setEstado(EstadoVacante.DISPONIBLE);
        }
    }
}
