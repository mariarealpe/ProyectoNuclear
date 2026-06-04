package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.ResultadoEvaluacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Evaluación registrada por el Tutor Empresarial al concluir la práctica.
 * RF-08-02: una única evaluación por práctica (@OneToOne con restricción UNIQUE en BD).
 *
 * Patrones aplicados:
 *  - Template Method: mismo flujo de validación y cálculo que EvaluacionDocente,
 *    adaptado a las restricciones del portal externo del tutor.
 *  - Adapter: el tutor accede desde un portal externo a la interfaz interna del sistema.
 *  - Proxy: la nota queda inmutable una vez que el Coordinador ejecuta el cierre.
 *
 * La nota se valida contra ConfiguracionPrograma:
 *   rango válido:  0.0 <= nota <= notaMaxima
 *   resultado:     nota >= notaMinimaAprobacion ? APROBADO : DESAPROBADO
 */
@Entity
@Table(name = "evaluaciones_tutor", indexes = {
        @Index(name = "idx_eval_tutor", columnList = "tutor_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionTutor {

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
     * Usuario con rol TUTOR_EMPRESARIAL que registra la evaluación.
     * Debe coincidir con Practica.tutorEmpresarial.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Usuario tutor;

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
