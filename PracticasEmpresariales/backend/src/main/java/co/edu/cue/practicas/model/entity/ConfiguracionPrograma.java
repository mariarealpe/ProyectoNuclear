package co.edu.cue.practicas.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Configuración a nivel de Programa Académico.
 * 
 * Almacena settings del programa para alertas de inactividad,
 * notificaciones automáticas, plantillas de correo, etc.
 * 
 * GPE-167: Alertas de inactividad por programa.
 */
@Entity
@Table(name = "configuraciones_programa", indexes = {
        @Index(name = "idx_config_programa", columnList = "programa_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionPrograma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Programa académico al que aplica la configuración.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", nullable = false, unique = true)
    private Programa programa;

    /**
     * Días de inactividad para generar alerta.
     * Por defecto: 7 días.
     * Si último seguimiento fue hace 7+ días, genera alerta.
     */
    @Column(name = "dias_inactividad_alerta", nullable = false)
    @Builder.Default
    private Integer diasInactividadAlerta = 7;

    /**
     * Activa/desactiva notificaciones automáticas por correo.
     * True: envía notificaciones automáticamente.
     * False: solo registra, pero no envía.
     */
    @Column(name = "notificaciones_automaticas", nullable = false)
    @Builder.Default
    private Boolean notificacionesAutomaticas = true;

    /**
     * Nota mínima para aprobar una práctica.
     */
    @Column(name = "nota_minima_aprobacion", nullable = false)
    @Builder.Default
    private Double notaMinimaAprobacion = 3.0;

    /**
     * Nota máxima permitida en las evaluaciones del programa.
     */
    @Column(name = "nota_maxima", nullable = false)
    @Builder.Default
    private Double notaMaxima = 5.0;

    /**
     * Número total de prácticas definido para el programa.
     * Si es null, se usa el valor del programa asociado.
     */
    @Column(name = "numero_total_practicas")
    private Integer numeroTotalPracticas;

    /**
     * Número de cortes académicos usados para evaluar la práctica.
     */
    @Column(name = "numero_cortes", nullable = false)
    @Builder.Default
    private Integer numeroCortes = 3;

    /**
     * Máximo de asignaciones activas que puede tener un estudiante simultáneamente.
     */
    @Column(name = "maximo_asignaciones_simultaneas", nullable = false)
    @Builder.Default
    private Integer maximoAsignacionesSimultaneas = 1;

    /**
     * Plantilla HTML de correo para asignación creada.
     * Puede usar placeholders: {nombreEstudiante}, {nombreVacante}, etc.
     */
    @Column(name = "plantilla_correo_asignacion", columnDefinition = "LONGTEXT")
    private String plantillaCorreoAsignacion;

    /**
     * Plantilla HTML de correo para seguimiento rechazado.
     */
    @Column(name = "plantilla_correo_seguimiento", columnDefinition = "LONGTEXT")
    private String plantillaCorreoSeguimiento;

    /**
     * Plantilla HTML de correo para alerta de inactividad.
     */
    @Column(name = "plantilla_correo_alerta", columnDefinition = "LONGTEXT")
    private String plantillaCorreoAlerta;

    /**
     * Correo remitente para las notificaciones (si no es el sistema default).
     * Default: noreply@cue.edu.co
     */
    @Column(name = "correo_remitente", length = 150)
    @Builder.Default
    private String correoRemitente = "noreply@cue.edu.co";

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
