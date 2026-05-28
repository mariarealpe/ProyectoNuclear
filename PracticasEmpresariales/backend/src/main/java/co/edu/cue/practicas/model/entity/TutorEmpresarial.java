package co.edu.cue.practicas.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Perfil de tutor empresarial vinculado a una empresa.
 *
 * GPE-151 — Gestión Empresarial y Vacantes (Persona 2)
 *
 * Modela los datos específicos del tutor que NO caben en la entidad Usuario
 * (cargo, área, empresa a la que pertenece). Se asocia a un Usuario con
 * rol TUTOR_EMPRESARIAL mediante relación OneToOne — así reutilizamos el
 * sistema de autenticación y permisos existente sin duplicar credenciales.
 *
 * Un tutor pertenece SIEMPRE a una sola empresa y puede ser asignado como
 * responsable de varias vacantes de esa misma empresa.
 */
@Entity
@Table(name = "tutores_empresariales", indexes = {
        @Index(name = "idx_tutor_usuario", columnList = "usuario_id", unique = true),
        @Index(name = "idx_tutor_empresa", columnList = "empresa_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorEmpresarial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cuenta de usuario asociada al tutor (siempre con rol TUTOR_EMPRESARIAL). */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    /** Empresa a la que pertenece el tutor. Un tutor solo puede estar en una empresa. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    /** Cargo del tutor dentro de la empresa (ej. "Líder de Desarrollo", "Gerente de TI"). */
    @Column(nullable = false, length = 150)
    private String cargo;

    /** Área o departamento al que pertenece el tutor dentro de la empresa. */
    @Column(length = 150)
    private String area;

    /** Teléfono corporativo del tutor; puede ser distinto al teléfono personal del Usuario. */
    @Column(name = "telefono_corporativo", length = 20)
    private String telefonoCorporativo;

    /**
     * Indica si este tutor es el contacto principal de la empresa.
     * Solo uno por empresa debería tener este indicador en true.
     */
    @Column(name = "es_responsable_principal", nullable = false)
    @Builder.Default
    private boolean esResponsablePrincipal = false;

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
}
