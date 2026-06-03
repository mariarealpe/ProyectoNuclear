package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.EstadoSeguimiento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seguimiento semanal de la práctica empresarial.
 * 
 * GPE-170: El estudiante carga semanalmente sus actividades, logros y dificultades.
 * El Docente Asesor revisa y aprueba o rechaza.
 * 
 * PRERREQUISITO: Plan de práctica debe estar en APROBADO_DOCENTE.
 * 
 * Regla crítica: Solo el seguimiento de la SEMANA MÁS RECIENTE es editable por estudiante,
 * y SOLO si fue rechazado. Seguimientos de semanas anteriores son inmutables.
 * 
 * Estados: PENDIENTE → APROBADO / RECHAZADO
 * Si RECHAZADO: estudiante edita y resubmite (incrementa versión).
 */
@Entity
@Table(name = "seguimientos_practica", indexes = {
        @Index(name = "idx_seguimiento_practica", columnList = "practica_id"),
        @Index(name = "idx_seguimiento_semana", columnList = "practica_id, semana"),
        @Index(name = "idx_seguimiento_estado", columnList = "estado"),
        @Index(name = "idx_seguimiento_docente", columnList = "docente_asesor_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeguimientoPractica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Práctica a la que pertenece este seguimiento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practica_id", nullable = false)
    private Practica practica;

    /**
     * Número de semana (1, 2, 3, ..., típicamente hasta 26).
     * Debe ser único dentro de la práctica.
     */
    @Column(nullable = false)
    private Integer semana;

    /**
     * Actividades realizadas en la semana.
     * Requerido.
     */
    @Column(name = "actividades", columnDefinition = "LONGTEXT", nullable = false)
    private String actividades;

    /**
     * Logros alcanzados en la semana.
     * Requerido.
     */
    @Column(name = "logros", columnDefinition = "LONGTEXT", nullable = false)
    private String logros;

    /**
     * Dificultades encontradas.
     * Opcional.
     */
    @Column(name = "dificultades", columnDefinition = "LONGTEXT")
    private String dificultades;

    /**
     * Observaciones agregadas por Docente Asesor durante la revisión.
     * Agrupa retroalimentación para el estudiante.
     */
    @Column(name = "observaciones_docente", columnDefinition = "LONGTEXT")
    private String observacionesDocente;

    /**
     * Estado actual del seguimiento.
     * Transición: PENDIENTE → APROBADO / RECHAZADO
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSeguimiento estado;

    /**
     * Usuario que cargó el seguimiento (siempre el estudiante).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargado_por", nullable = false)
    private Usuario cargadoPor;

    /**
     * Fecha cuando se cargó el seguimiento.
     */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCarga = LocalDateTime.now();

    /**
     * Docente Asesor que revisó/aprobó el seguimiento.
     * Null si aún PENDIENTE.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_asesor_id")
    private Usuario docenteAsesor;

    /**
     * Fecha cuando se revisó el seguimiento.
     * Null si aún PENDIENTE.
     */
    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    /**
     * Versión del seguimiento (para ediciones en caso de rechazo).
     * Versión 1 = carga inicial. Si se rechaza y edita, versión += 1.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    /**
     * Evidencias (archivos, fotos, reportes) del seguimiento.
     * OneToMany para permite múltiples archivos por seguimiento.
     */
    @OneToMany(mappedBy = "seguimiento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DocumentoEvidencia> evidencias = new ArrayList<>();

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
     * Verifica si este seguimiento puede ser editado por el estudiante.
     * Solo editable si:
     * 1. Es la semana más reciente de la práctica
     * 2. Estado es RECHAZADO
     */
    public boolean esEditablePorEstudiante(int semamaActualPractica) {
        return this.semana == semamaActualPractica &&
               this.estado == EstadoSeguimiento.RECHAZADO;
    }

    /**
     * Verifica si el seguimiento está completo (todas los campos requeridos).
     */
    public boolean estaCompleto() {
        return this.actividades != null && !this.actividades.isBlank() &&
               this.logros != null && !this.logros.isBlank();
    }
}
