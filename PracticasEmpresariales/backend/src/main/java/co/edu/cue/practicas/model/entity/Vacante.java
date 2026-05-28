package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.EstadoVacante;
import co.edu.cue.practicas.model.enums.JornadaVacante;
import co.edu.cue.practicas.model.enums.ModalidadVacante;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Oferta de práctica empresarial publicada por una empresa.
 *
 * GPE-152, GPE-153 — Gestión Empresarial y Vacantes (Persona 2)
 *
 * El ciclo de vida está controlado por el PATRON STATE a través del campo {@link #estado}:
 *   PENDIENTE → DISPONIBLE (aprobada por Coordinador de Prácticas)
 *   PENDIENTE → RECHAZADA  (con motivo)
 *   RECHAZADA → PENDIENTE  (la empresa la edita y reenvía)
 *   DISPONIBLE → CERRADA    (cupos llenos o cierre manual)
 *
 * La validez de las transiciones se gestiona en EstadoVacanteState (paquete state),
 * no en este entity — la entidad es solo el "contenedor de datos".
 */
@Entity
@Table(name = "vacantes", indexes = {
        @Index(name = "idx_vacante_empresa", columnList = "empresa_id"),
        @Index(name = "idx_vacante_programa", columnList = "programa_id"),
        @Index(name = "idx_vacante_estado", columnList = "estado"),
        @Index(name = "idx_vacante_publicacion", columnList = "fecha_publicacion")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vacante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    /** Tutor empresarial responsable de esta vacante. Puede ser asignado posteriormente. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private TutorEmpresarial tutor;

    /** Programa académico al que va dirigida la vacante (Ingeniería de Sistemas, etc.). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", nullable = false)
    private Programa programa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ModalidadVacante modalidad = ModalidadVacante.PRESENCIAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private JornadaVacante jornada = JornadaVacante.TIEMPO_COMPLETO;

    @Column(length = 100)
    private String ciudad;

    /** Número total de cupos disponibles para la vacante. */
    @Column(nullable = false)
    @Builder.Default
    private int cupos = 1;

    /** Cupos ocupados — se incrementa cuando un estudiante es asignado (sprint posterior). */
    @Column(name = "cupos_ocupados", nullable = false)
    @Builder.Default
    private int cuposOcupados = 0;

    /** Duración esperada de la práctica en meses. */
    @Column(name = "duracion_meses")
    @Builder.Default
    private int duracionMeses = 6;

    @Column(nullable = false)
    @Builder.Default
    private boolean remunerada = false;

    /** Monto mensual de la remuneración cuando aplica (en COP). */
    @Column(name = "valor_remuneracion")
    private Double valorRemuneracion;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "fecha_publicacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    /** Fecha límite para que estudiantes apliquen — null = sin fecha de cierre fijada. */
    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(length = 2000)
    private String requisitos;

    @Column(length = 2000)
    private String responsabilidades;

    /** Estado actual de la vacante; las transiciones se controlan en EstadoVacanteState. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoVacante estado = EstadoVacante.PENDIENTE;

    /** Motivo del rechazo (solo se llena cuando estado = RECHAZADA). */
    @Column(name = "motivo_rechazo", length = 1000)
    private String motivoRechazo;

    /** Usuario (coordinador) que aprobó o rechazó la vacante por última vez. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobador_id")
    private Usuario aprobador;

    /** Fecha del último cambio de estado (aprobación, rechazo o cierre). */
    @Column(name = "fecha_decision")
    private LocalDateTime fechaDecision;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    @PreUpdate
    void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
