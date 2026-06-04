package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.EstadoPractica;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "practicas", indexes = {
        @Index(name = "idx_practica_estudiante", columnList = "estudiante_id"),
        @Index(name = "idx_practica_estado",     columnList = "estado"),
        @Index(name = "idx_practica_docente",    columnList = "docente_asesor_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Practica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Usuario estudiante;

    /**
     * Docente Asesor responsable del seguimiento académico de esta práctica.
     * Se asigna al confirmar la vinculación (cuando la práctica pasa a EN_CURSO).
     * Nullable para compatibilidad con prácticas creadas antes de este campo.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_asesor_id")
    private Usuario docenteAsesor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", nullable = false)
    private Programa programa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id")
    private Expediente expediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalogo_id")
    private PracticaCatalogo catalogo;

    @Column(name = "numero_practica", nullable = false)
    private int numeroPractica;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(name = "documentos_requeridos", length = 1000)
    private String documentosRequeridos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoPractica estado = EstadoPractica.PENDIENTE_ASIGNACION;

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
}
