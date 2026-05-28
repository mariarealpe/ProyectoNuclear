package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.EstadoEstudiante;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Bitácora de validaciones de aptitud.
 * Conserva el historial de cambios de estado del estudiante.
 */
@Entity
@Table(name = "bitacora_validacion", indexes = {
        @Index(name = "idx_validacion_estudiante", columnList = "estudiante_id"),
        @Index(name = "idx_validacion_fecha", columnList = "fecha_hora")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BitacoraValidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private Expediente expediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Usuario estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validador_id")
    private Usuario validador;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 15)
    private EstadoEstudiante estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", length = 15)
    private EstadoEstudiante estadoNuevo;

    @Column(length = 500)
    private String motivo;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();
}
