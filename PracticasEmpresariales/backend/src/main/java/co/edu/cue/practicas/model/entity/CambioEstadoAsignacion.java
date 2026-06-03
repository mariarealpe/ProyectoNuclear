package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.EstadoAsignacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Bitácora de cambios de estado de Asignación.
 * 
 * Cada transición de estado de Asignacion genera un registro aquí.
 * Permite trazabilidad completa: quién cambió, cuándo, por qué.
 */
@Entity
@Table(name = "cambios_estado_asignacion", indexes = {
        @Index(name = "idx_cambio_asignacion", columnList = "asignacion_id"),
        @Index(name = "idx_cambio_fecha", columnList = "fecha_hora")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioEstadoAsignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Asignación a la que pertenece este cambio de estado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_id", nullable = false)
    private Asignacion asignacion;

    /**
     * Estado anterior.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAsignacion estadoAnterior;

    /**
     * Estado nuevo.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAsignacion estadoNuevo;

    /**
     * Motivo del cambio de estado (ej: "Documentos completos", "Cancelada por coordinador").
     * Puede ser nulo.
     */
    @Column(length = 500)
    private String motivo;

    /**
     * Usuario que realizó el cambio de estado (Coordinador, Sistema, etc).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Marca de tiempo del cambio.
     */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();
}
