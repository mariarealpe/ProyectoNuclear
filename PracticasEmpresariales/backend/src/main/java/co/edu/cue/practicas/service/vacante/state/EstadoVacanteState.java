package co.edu.cue.practicas.service.vacante.state;

import co.edu.cue.practicas.model.entity.Vacante;

/**
 * PATRON STATE — GPE-152, GPE-153
 *
 * Contrato común de todos los estados posibles de una vacante.
 * Cada estado concreto (Pendiente, Disponible, Cerrada, Rechazada) decide
 * si una transición es válida y cómo aplicar el cambio.
 *
 * El servicio (VacanteService) NO contiene if/switch sobre el estado:
 * delega la lógica al estado actual, respetando el principio Open/Closed.
 * Agregar un nuevo estado solo implica crear una nueva implementación
 * — el resto del código no necesita cambiar.
 */
public interface EstadoVacanteState {

    /**
     * Aprueba la vacante (PENDIENTE → DISPONIBLE).
     * Lanza OperacionNoPermitidaException si el estado actual no lo permite.
     *
     * @param vacante  vacante sobre la que se aplica la transición
     * @param motivo   observación opcional del coordinador al aprobar
     */
    void aprobar(Vacante vacante, String motivo);

    /**
     * Rechaza la vacante (PENDIENTE → RECHAZADA).
     * El motivo es obligatorio para dejar trazabilidad de la decisión.
     */
    void rechazar(Vacante vacante, String motivo);

    /**
     * Cierra la vacante (DISPONIBLE → CERRADA).
     * Aplica cuando se llenaron los cupos o cuando el coordinador la cierra manualmente.
     */
    void cerrar(Vacante vacante, String motivo);

    /**
     * Reabre una vacante rechazada para reenviarla a aprobación (RECHAZADA → PENDIENTE).
     * Se ejecuta cuando la empresa edita la vacante después de un rechazo.
     */
    void reabrir(Vacante vacante);
}
