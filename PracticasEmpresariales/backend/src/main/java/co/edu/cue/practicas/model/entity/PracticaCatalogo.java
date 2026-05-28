package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.EstadoPractica;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * PATRON PROTOTYPE — GPE-141, GPE-145
 *
 * Plantilla de práctica dentro del catálogo académico.
 * Sirve como prototipo para crear la práctica real de un estudiante
 * cuando se valida su aptitud.
 */
@Entity
@Table(name = "practicas_catalogo", indexes = {
        @Index(name = "idx_catalogo_programa", columnList = "programa_id"),
        @Index(name = "idx_catalogo_numero", columnList = "numero_practica")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticaCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", nullable = false)
    private Programa programa;

    /** Número de práctica dentro del plan (1, 2, 3...) */
    @Column(name = "numero_practica", nullable = false)
    private int numeroPractica;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(name = "creditos_minimos")
    @Builder.Default
    private int creditosMinimos = 0;

    @Column(name = "promedio_minimo")
    @Builder.Default
    private double promedioMinimo = 0.0;

    @Column(name = "requiere_practica_anterior_aprobada")
    @Builder.Default
    private boolean requierePracticaAnteriorAprobada = false;

    /** Documentos requeridos separados por coma */
    @Column(name = "documentos_requeridos", length = 1000)
    private String documentosRequeridos;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

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
     * Clona el prototipo para construir la práctica real del estudiante.
     */
    public Practica clonarPara(Usuario estudiante, Expediente expediente) {
        return Practica.builder()
                .catalogo(this)
                .programa(this.programa)
                .numeroPractica(this.numeroPractica)
                .nombre(this.nombre)
                .descripcion(this.descripcion)
                .documentosRequeridos(this.documentosRequeridos)
                .estado(EstadoPractica.PENDIENTE_ASIGNACION)
                .estudiante(estudiante)
                .expediente(expediente)
                .build();
    }
}
