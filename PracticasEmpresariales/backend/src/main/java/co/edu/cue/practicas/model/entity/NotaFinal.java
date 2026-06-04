package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.ResultadoNotaFinal;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Nota final de la práctica, registrada por el Coordinador de Prácticas (RF-08-04).
 *
 * Patrones aplicados:
 *  - Strategy: la política de aprobación (notaMinimaAprobacion) es intercambiable
 *    por programa vía ConfiguracionPrograma.
 *  - Observer: al cerrar la nota se actualizan los indicadores y el dashboard.
 *  - Proxy: una vez cerrada (cerrada = true) el registro y las notas referencias
 *    quedan inmutables.
 *
 * El Coordinador NO calcula automáticamente: ingresa la nota manualmente con las
 * notas del Docente y del Tutor como referencia (visibles al momento del registro).
 */
@Entity
@Table(name = "notas_finales", indexes = {
        @Index(name = "idx_nota_final_coord", columnList = "coordinador_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaFinal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Práctica a la que pertenece la nota final.
     * @OneToOne — exactamente una nota final por práctica.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practica_id", nullable = false, unique = true)
    private Practica practica;

    /**
     * Coordinador de Prácticas que registra la nota final.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinador_id", nullable = false)
    private Usuario coordinador;

    /**
     * Nota final ingresada manualmente por el Coordinador.
     */
    @Column(nullable = false)
    private Double nota;

    /**
     * Resultado calculado contra ConfiguracionPrograma.notaMinimaAprobacion.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResultadoNotaFinal resultado;

    /**
     * Observaciones del Coordinador (opcional pero suele incluir justificación
     * cuando difiere de las notas de docente o tutor).
     */
    @Column(columnDefinition = "LONGTEXT")
    private String observaciones;

    /**
     * Indica si el Coordinador ya ejecutó el cierre del proceso de evaluación.
     * Una vez true, el registro es inmutable y se sincroniza con
     * Practica.notasCerradas para bloquear cambios en docente y tutor.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean cerrada = false;

    /**
     * Fecha y hora del cierre formal. Null mientras no se ejecute.
     */
    @Column(name = "cerrada_en")
    private LocalDateTime cerradaEn;

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
