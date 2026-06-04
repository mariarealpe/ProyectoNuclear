package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoEventoNotificacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Plantilla de correo configurable por tipo de evento (RF-11-05).
 *
 * Cada evento del sistema (NUEVA_ASIGNACION, ENCUESTA_TUTOR_RECORDATORIO,
 * ALERTA_INACTIVIDAD, etc.) tiene exactamente UNA plantilla activa con su
 * propio HTML, asunto, rol receptor, criticidad y frecuencia de recordatorio.
 *
 * Patrones:
 *  - Decorator: el cuerpo HTML se decora con variables dinámicas y reglas de
 *    recordatorio en tiempo de envío.
 *  - Template Method: el flujo de envío (construir → personalizar → enviar →
 *    registrar) es fijo; lo único variable es la plantilla.
 *  - Singleton: el servicio que consume las plantillas existe como instancia única.
 */
@Entity
@Table(name = "plantillas_notificacion", indexes = {
        @Index(name = "idx_plantilla_evento", columnList = "evento", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantillaNotificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Evento del sistema al que aplica la plantilla.
     * Único por evento — el endpoint de actualización hace upsert sobre este campo.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60, unique = true)
    private TipoEventoNotificacion evento;

    /** Asunto del correo. Soporta variables ({{nombre_estudiante}}, etc.). */
    @Column(nullable = false, length = 250)
    private String asunto;

    /** Cuerpo HTML del correo. Soporta variables dinámicas. */
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String cuerpoHtml;

    /**
     * Rol que recibe el correo principalmente (informativo).
     * Permite que el Administrador vea de un vistazo a quién apunta cada plantilla.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "rol_receptor", length = 30)
    private Rol rolReceptor;

    /**
     * Si true, el correo es obligatorio (failing email se reintenta y alerta).
     * Si false, es informativo y un fallo no bloquea el flujo.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean obligatorio = false;

    /**
     * Frecuencia de recordatorios automáticos en DÍAS HÁBILES.
     * Aplica a notificaciones de tipo "recordatorio" (encuestas, etc.).
     * Por defecto 3 días (configuración base institucional).
     */
    @Column(name = "frecuencia_recordatorio_dias", nullable = false)
    @Builder.Default
    private Integer frecuenciaRecordatorioDias = 3;

    /**
     * Plantilla activa/inactiva. Inactiva = el evento no envía correo.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "actualizado_en", nullable = false)
    @Builder.Default
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    @PreUpdate
    void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
