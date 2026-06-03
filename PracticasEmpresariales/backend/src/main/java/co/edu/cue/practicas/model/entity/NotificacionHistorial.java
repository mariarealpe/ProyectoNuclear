package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.TipoNotificacion;
import co.edu.cue.practicas.model.enums.EstadoNotificacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Historial de notificaciones por correo electrónico.
 * 
 * GPE-160: Registra cada notificación enviada al usuario.
 * Permite reintento automático en caso de fallo.
 * 
 * Estados: PENDIENTE → ENVIADO / FALLIDO (con reintentos)
 */
@Entity
@Table(name = "notificaciones_historial", indexes = {
        @Index(name = "idx_notif_usuario", columnList = "usuario_destino_id"),
        @Index(name = "idx_notif_tipo", columnList = "tipo"),
        @Index(name = "idx_notif_estado", columnList = "estado"),
        @Index(name = "idx_notif_proxreintento", columnList = "prox_reintento")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario destino de la notificación.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_destino_id", nullable = false)
    private Usuario usuarioDestino;

    /**
     * Tipo de notificación (evento que la generó).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacion tipo;

    /**
     * Correo destino (redundante pero útil si usuario cambia correo).
     */
    @Column(name = "correo_destino", nullable = false, length = 150)
    private String correoDestino;

    /**
     * Asunto del correo.
     */
    @Column(name = "asunto", nullable = false, length = 255)
    private String asunto;

    /**
     * Cuerpo del correo (HTML).
     */
    @Column(name = "cuerpo", columnDefinition = "LONGTEXT", nullable = false)
    private String cuerpo;

    /**
     * Estado actual de la notificación.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoNotificacion estado;

    /**
     * Número de intentos de envío realizados.
     * Máximo 3 reintentos por política.
     */
    @Column(name = "reintentos", nullable = false)
    @Builder.Default
    private Integer reintentos = 0;

    /**
     * Próxima fecha de reintento (si estado = FALLIDO).
     * Exponential backoff: 5 min, 15 min, 60 min, stop.
     */
    @Column(name = "prox_reintento")
    private LocalDateTime proxReintento;

    /**
     * Mensaje de error (si estado = FALLIDO).
     */
    @Column(name = "error_mensaje", length = 500)
    private String errorMensaje;

    /**
     * Referencia a Asignacion (contexto de la notificación).
     * Nullable para notificaciones genéricas.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_id")
    private Asignacion asignacion;

    /**
     * Referencia a Practica (contexto de notificación).
     * Nullable para notificaciones genéricas.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practica_id")
    private Practica practica;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();

    /**
     * Verifica si la notificación puede reintentarse.
     * True si está FALLIDO y no ha alcanzado reintentos máximos.
     */
    public boolean puedeReintentarse() {
        return this.estado == EstadoNotificacion.FALLIDO &&
               this.reintentos < 3;
    }

    /**
     * Incrementa el contador de reintentos y calcula próximo reintento.
     * Exponential backoff: 5 min (1º), 15 min (2º), 60 min (3º).
     */
    public void registrarReintento(String motivoError) {
        this.errorMensaje = motivoError;
        this.reintentos++;
        if (this.reintentos == 1) {
            this.proxReintento = LocalDateTime.now().plusMinutes(5);
        } else if (this.reintentos == 2) {
            this.proxReintento = LocalDateTime.now().plusMinutes(15);
        } else if (this.reintentos == 3) {
            this.proxReintento = LocalDateTime.now().plusHours(1);
        }
    }
}
