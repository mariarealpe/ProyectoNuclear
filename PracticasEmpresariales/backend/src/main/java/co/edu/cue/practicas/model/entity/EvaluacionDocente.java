package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.ResultadoEvaluacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Evaluación registrada por el Docente Asesor al concluir la práctica.
 * RF-08-01: una única evaluación por práctica (@OneToOne con restricción UNIQUE en BD).
 *
 * La nota se valida contra ConfiguracionPrograma:
 *   rango válido:  0.0 <= nota <= notaMaxima
 *   resultado:     nota >= notaMinimaAprobacion ? APROBADO : DESAPROBADO
 */
@Entity
@Table(name = "evaluaciones_docente", indexes = {
        @Index(name = "idx_eval_docente", columnList = "docente_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Práctica evaluada. Relación @OneToOne: exactamente una evaluación por práctica.
     * La restricción UNIQUE sobre practica_id se garantiza a nivel de BD.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practica_id", nullable = false, unique = true)
    private Practica practica;

    /**
     * Docente Asesor que registra la evaluación.
     * Debe coincidir con Practica.docenteAsesor.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id", nullable = false)
    private Usuario docente;

    @Column(nullable = false)
    private Double nota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResultadoEvaluacion resultado;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String observaciones;

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
