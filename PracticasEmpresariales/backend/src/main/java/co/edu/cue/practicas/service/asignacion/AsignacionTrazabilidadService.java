package co.edu.cue.practicas.service.asignacion;

import co.edu.cue.practicas.model.entity.Asignacion;
import co.edu.cue.practicas.model.entity.CambioEstadoAsignacion;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoAsignacion;
import co.edu.cue.practicas.repository.asignacion.CambioEstadoAsignacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PATRON AUDIT TRAIL — GPE-159
 *
 * Unica responsabilidad: persistir y consultar la trazabilidad de estados.
 * Con esto AsignacionService no administra directamente la bitacora.
 */
@Service
@RequiredArgsConstructor
public class AsignacionTrazabilidadService {

    private final CambioEstadoAsignacionRepository cambioRepository;

    public void registrarCambio(Asignacion asignacion,
                                EstadoAsignacion anterior,
                                EstadoAsignacion nuevo,
                                String motivo,
                                Usuario usuario) {
        cambioRepository.save(CambioEstadoAsignacion.builder()
                .asignacion(asignacion)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .motivo(motivo)
                .usuario(usuario)
                .build());
    }

    public List<CambioEstadoAsignacion> historial(Long asignacionId) {
        return cambioRepository.findByAsignacion_IdOrderByFechaHoraDesc(asignacionId);
    }
}
