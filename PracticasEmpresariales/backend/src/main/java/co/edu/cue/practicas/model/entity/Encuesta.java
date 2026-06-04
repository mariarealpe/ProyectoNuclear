package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.EstadoEncuesta;
import co.edu.cue.practicas.model.enums.TipoEncuesta;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Encuesta de cierre asociada a una práctica (RF-08-05 / RF-08-06).
 *
 * Una única encuesta por (práctica, tipo) — garantizado vía UNIQUE en BD.
 *
 * Las respuestas se almacenan como JSON en `respuestasJson` para que la
 * estructura sea configurable por el Administrador sin cambiar el schema.
 * Los reportes agregados se construyen sobre este blob.
 *
 * Patrones:
 *  - State: transiciones PENDIENTE → EN_BORRADOR (solo tutor) → COMPLETADA.
 *  - Proxy: una encuesta COMPLETADA es inmutable.
 *  - Decorator: el correo de invitación + recordatorios se construyen sobre
 *    la PlantillaNotificacion configurada por evento.
 */
@Entity
@Table(name = "encuestas", indexes = {
        @Index(name = "idx_encuesta_estado", columnList = "estado"),
        @Index(name = "idx_encuesta_destinatario", columnList = "destinatario_id"),
        @Index(name = "uq_encuesta_practica_tipo", columnList = "practica_id, tipo", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Encuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practica_id", nullable = false)
    private Practica practica;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoEncuesta tipo;

    /**
     * Usuario que debe completar la encuesta:
     *  - TUTOR_SATISFACCION: usuario con rol TUTOR_EMPRESARIAL asignado a la práctica.
     *  - ESTUDIANTE_*: el estudiante de la práctica.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoEncuesta estado = EstadoEncuesta.PENDIENTE;

    /**
     * JSON con las respuestas. Estructura libre, definida por la plantilla de
     * encuesta configurada por el Administrador.
     * En estado PENDIENTE puede ser null; en EN_BORRADOR contiene respuestas
     * parciales; en COMPLETADA contiene las respuestas definitivas.
     */
    @Column(name = "respuestas_json", columnDefinition = "LONGTEXT")
    private String respuestasJson;

    /** Última fecha y hora en que se envió un recordatorio (manual o automático). */
    @Column(name = "ultimo_recordatorio_en")
    private LocalDateTime ultimoRecordatorioEn;

    /** Marca de tiempo del envío de la invitación inicial. */
    @Column(name = "invitacion_enviada_en")
    private LocalDateTime invitacionEnviadaEn;

    /** Marca de tiempo del cambio a COMPLETADA — inmutabilidad. */
    @Column(name = "completada_en")
    private LocalDateTime completadaEn;

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

    public boolean esInmutable() {
        return this.estado == EstadoEncuesta.COMPLETADA;
    }
}
