package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.EstadoPlan;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Plan de Práctica del estudiante.
 * 
 * El estudiante crea el plan DESPUÉS de que la práctica está EN_CURSO.
 * El plan debe ser aprobado por Tutor Empresarial Y Docente Asesor.
 * 
 * DESBLOQUEADOR CRÍTICO: Seguimiento semanal NO puede iniciarse sin plan APROBADO_DOCENTE.
 * 
 * Estados: BORRADOR → APROBADO_TUTOR → APROBADO_DOCENTE
 *            ↓ (si rechaza Tutor o Docente)
 *          RECHAZADO (estudiante edita y resubmite)
 */
@Entity
@Table(name = "planes_practica", indexes = {
        @Index(name = "idx_plan_practica", columnList = "practica_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanPractica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Práctica a la que pertenece este plan.
     * Relación 1:1 inversa.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practica_id", nullable = false, unique = true)
    private Practica practica;

    /**
     * Objetivos de la práctica (listado en JSON o entidad separada).
     * Para esta versión se almacena como texto JSON para flexibilidad.
     */
    @Column(name = "objetivos", columnDefinition = "LONGTEXT")
    private String objetivos;

    /**
     * Cronograma de actividades (listado en JSON).
     * Ej: [{"semana": 1, "actividades": "..."}, ...]
     */
    @Column(name = "cronograma", columnDefinition = "LONGTEXT")
    private String cronograma;

    /**
     * Descripción general del plan.
     */
    @Column(length = 1000)
    private String descripcion;

    /**
     * Estado actual del plan.
     * Transición: BORRADOR → APROBADO_TUTOR → APROBADO_DOCENTE (desbloqueador)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPlan estado;

    /**
     * Usuario que cargó el plan (siempre el estudiante).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargado_por", nullable = false)
    private Usuario cargadoPor;

    /**
     * Fecha cuando se aprobó por Tutor Empresarial.
     * Null si no aprobado por tutor.
     */
    @Column(name = "fecha_aprobacion_tutor")
    private LocalDateTime fechaAprobacionTutor;

    /**
     * Usuario (Tutor Empresarial) que aprobó el plan.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_aprobacion_tutor")
    private Usuario usuarioAprobacionTutor;

    /**
     * Fecha cuando se aprobó por Docente Asesor.
     * Null si no aprobado por docente.
     */
    @Column(name = "fecha_aprobacion_docente")
    private LocalDateTime fechaAprobacionDocente;

    /**
     * Usuario (Docente Asesor) que aprobó el plan.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_aprobacion_docente")
    private Usuario usuarioAprobacionDocente;

    /**
     * Motivo del rechazo (si estado = RECHAZADO).
     * Nulo para otros estados.
     */
    @Column(name = "motivo_rechazo", length = 500)
    private String motivoRechazo;

    /**
     * Usuario que rechazó el plan (Tutor o Docente).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_rechazo")
    private Usuario usuarioRechazo;

    /**
     * Fecha del rechazo.
     */
    @Column(name = "fecha_rechazo")
    private LocalDateTime fechaRechazo;

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
     * Verifica si el plan está listo para iniciar seguimientos.
     */
    public boolean estaAprobadoPorDocente() {
        return this.estado == EstadoPlan.APROBADO_DOCENTE;
    }

    /**
     * Verifica si el plan puede ser editado por estudiante.
     * Solo editable si está en BORRADOR o fue RECHAZADO.
     */
    public boolean esEditable() {
        return this.estado == EstadoPlan.BORRADOR || this.estado == EstadoPlan.RECHAZADO;
    }
}
