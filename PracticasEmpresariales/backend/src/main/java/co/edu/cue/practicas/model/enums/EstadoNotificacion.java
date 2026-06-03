package co.edu.cue.practicas.model.enums;

/**
 * Estados del historial de notificaciones por correo.
 * 
 * FLUJO: PENDIENTE → ENVIADO
 *          ↓ (si falla)
 *        FALLIDO (con reintento automático)
 */
public enum EstadoNotificacion {
    PENDIENTE,          // Pendiente de envío
    ENVIADO,            // Enviado exitosamente
    FALLIDO             // Fallo en envío (con reintento automático hasta 3 veces)
}
