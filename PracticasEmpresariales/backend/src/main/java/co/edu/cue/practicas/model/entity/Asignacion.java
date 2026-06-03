package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.EstadoAsignacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Asignación de Estudiante a Vacante.
 * 
 * GPE-157: El Coordinador es el ÚNICO responsable de asignar estudiantes a vacantes.
 * Los estudiantes NO se postulan de forma autónoma.
 * 
 * Estados: ASIGNADA → EN_VINCULACION → EN_CURSO / CANCELADA
 * 
 * Una asignación NO puede cancelarse una vez iniciada la vinculación (solo estado ASIGNADA).
 * Al confirmar vinculación, se crea una instancia Practica con estado EN_CURSO.
 */
@Entity
@Table(name = "asignaciones", indexes = {
        @Index(name = "idx_asignacion_estudiante", columnList = "estudiante_id"),
        @Index(name = "idx_asignacion_vacante", columnList = "vacante_id"),
        @Index(name = "idx_asignacion_coordinador", columnList = "coordinador_id"),
        @Index(name = "idx_asignacion_estado", columnList = "estado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Estudiante asignado a la vacante.
     * Debe estar en estado APTO y sin práctica activa.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Usuario estudiante;

    /**
     * Vacante a la que se asigna el estudiante.
     * Debe tener cupo disponible.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacante_id", nullable = false)
    private Vacante vacante;

    /**
     * Coordinador de Prácticas que realizó la asignación.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinador_id", nullable = false)
    private Usuario coordinador;

    /**
     * Estado actual de la asignación.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAsignacion estado;

    /**
     * Motivo de cancelación (solo si estado = CANCELADA).
     * Requerido al cancelar, nulo para otros estados.
     */
    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

    /**
     * Fecha cuando se realizó la asignación.
     */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaAsignacion = LocalDateTime.now();

    /**
     * Fecha cuando pasó a EN_VINCULACION (carga de documentos iniciada).
     * Null si no ha entrado a vinculación.
     */
    @Column(name = "fecha_vinculacion")
    private LocalDateTime fechaVinculacion;

    /**
     * Fecha oficial de inicio de práctica (cuando pasó a EN_CURSO).
     * Null si no confirmada.
     */
    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    /**
     * Fecha oficial de finalización de práctica (cuando se confirma vinculación).
     * Null si no confirmada.
     */
    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    /**
     * Bitácora de cambios de estado de la asignación.
     * Cada transición de estado genera un registro aquí.
     */
    @OneToMany(mappedBy = "asignacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CambioEstadoAsignacion> cambiosEstado = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    @PreUpdate
    void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }

    /**
     * Verifica si la asignación puede ser cancelada.
     * Solo es cancelable si está en estado ASIGNADA.
     */
    public boolean esCancelable() {
        return this.estado == EstadoAsignacion.ASIGNADA;
    }

    /**
     * Verifica si la asignación puede pasar a EN_VINCULACION.
     * Solo si está en ASIGNADA.
     */
    public boolean puedeEntrarVinculacion() {
        return this.estado == EstadoAsignacion.ASIGNADA;
    }

    /**
     * Verifica si la asignación puede confirmar vinculación y crear Practica EN_CURSO.
     * Solo si está en EN_VINCULACION.
     */
    public boolean puedeConfirmarVinculacion() {
        return this.estado == EstadoAsignacion.EN_VINCULACION;
    }
}
