package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.EstadoVacante;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Bitácora de cambios de estado de una vacante.
 *
 * Conserva el histórico completo de transiciones (PENDIENTE → DISPONIBLE,
 * DISPONIBLE → CERRADA, etc.) junto con el motivo y el usuario que ejecutó
 * la acción. Es inmutable: no se puede editar ni borrar una entrada.
 *
 * Complementa a BitacoraAuditoria — mientras esa es la traza GENERAL del sistema,
 * BitacoraVacante es la traza ESPECÍFICA del flujo de aprobación de vacantes,
 * lo que facilita auditorías rápidas sobre el ciclo de una vacante.
 */
@Entity
@Table(name = "bitacora_vacante", indexes = {
        @Index(name = "idx_bitacora_vacante_vacante", columnList = "vacante_id"),
        @Index(name = "idx_bitacora_vacante_fecha", columnList = "fecha_hora")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BitacoraVacante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacante_id", nullable = false)
    private Vacante vacante;

    /** Usuario que ejecutó el cambio (coordinador, empresa, tutor). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 20)
    private EstadoVacante estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoVacante estadoNuevo;

    /** Motivo del cambio (obligatorio en rechazos, opcional en aprobaciones). */
    @Column(length = 1000)
    private String motivo;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();
}
