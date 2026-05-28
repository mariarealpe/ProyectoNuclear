package co.edu.cue.practicas.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Empresa registrada en el sistema que ofrece vacantes de práctica empresarial.
 *
 * GPE-150 — Gestión Empresarial y Vacantes (Persona 2)
 *
 * Una empresa puede tener varios tutores empresariales asignados y varias vacantes
 * publicadas. El identificador único de negocio es el NIT (Número de Identificación
 * Tributaria), por eso lleva índice único.
 *
 * El soft delete (campo activo) permite desactivar la empresa sin perder el histórico
 * de vacantes ni tutores asociados.
 */
@Entity
@Table(name = "empresas", indexes = {
        @Index(name = "idx_empresa_nit", columnList = "nit", unique = true),
        @Index(name = "idx_empresa_razon_social", columnList = "razon_social")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** NIT colombiano de la empresa. Identificador único de negocio. */
    @Column(nullable = false, unique = true, length = 30)
    private String nit;

    /** Razón social registrada en cámara de comercio. */
    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    /** Nombre comercial con el que se conoce a la empresa públicamente. */
    @Column(name = "nombre_comercial", length = 200)
    private String nombreComercial;

    /** Sector económico (TI, salud, manufactura, etc.). Se deja como String para flexibilidad. */
    @Column(length = 100)
    private String sector;

    @Column(length = 300)
    private String direccion;

    @Column(length = 100)
    private String ciudad;

    /** Correo principal de contacto institucional de la empresa. */
    @Column(name = "correo_contacto", nullable = false, length = 150)
    private String correoContacto;

    @Column(length = 20)
    private String telefono;

    @Column(name = "sitio_web", length = 300)
    private String sitioWeb;

    @Column(name = "representante_legal", length = 200)
    private String representanteLegal;

    @Column(length = 1000)
    private String descripcion;

    /** Soft delete: false = empresa desactivada, sin posibilidad de crear nuevas vacantes. */
    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    /** Tutores empresariales registrados para esta empresa. */
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    @Builder.Default
    private List<TutorEmpresarial> tutores = new ArrayList<>();

    /** Vacantes publicadas por la empresa, sin importar su estado. */
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Vacante> vacantes = new ArrayList<>();

    @PreUpdate
    void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
