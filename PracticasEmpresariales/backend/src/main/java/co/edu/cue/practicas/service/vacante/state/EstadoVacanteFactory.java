package co.edu.cue.practicas.service.vacante.state;

import co.edu.cue.practicas.model.enums.EstadoVacante;

/**
 * Selector de instancias de estado. Mantiene una única instancia por estado
 * concreto (PATRON SINGLETON aplicado a cada estado) porque las clases State
 * son stateless — toda su lógica depende del Vacante recibido como parámetro.
 *
 * Centraliza el mapeo enum → state, así VacanteService no necesita conocer
 * las clases concretas.
 */
public final class EstadoVacanteFactory {

    // Instancias únicas reutilizables — todas las clases State son inmutables (stateless)
    private static final EstadoVacanteState PENDIENTE  = new PendienteState();
    private static final EstadoVacanteState DISPONIBLE = new DisponibleState();
    private static final EstadoVacanteState CERRADA    = new CerradaState();
    private static final EstadoVacanteState RECHAZADA  = new RechazadaState();

    private EstadoVacanteFactory() {}

    /** Resuelve la instancia State correspondiente al valor enum del estado actual. */
    public static EstadoVacanteState para(EstadoVacante estado) {
        if (estado == null) {
            // Una vacante sin estado se trata como PENDIENTE por seguridad.
            return PENDIENTE;
        }
        return switch (estado) {
            case PENDIENTE  -> PENDIENTE;
            case DISPONIBLE -> DISPONIBLE;
            case CERRADA    -> CERRADA;
            case RECHAZADA  -> RECHAZADA;
        };
    }
}
