package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.TipoDocumento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Documento de la práctica (expediente).
 * 
 * Almacena: carta presentación, convenio firmado, plan, evidencias, etc.
 * 
 * Regla crítica: Documentos de prácticas FINALIZADA o CANCELADA son INMUTABLES.
 * Para otros estados, pueden editarse/reemplazarse.
 * 
 * GPE-162: Carta de presentación
 * GPE-163: Convenio de práctica (con firmas)
 */
@Entity
@Table(name = "documentos_practica", indexes = {
        @Index(name = "idx_documento_practica", columnList = "practica_id"),
        @Index(name = "idx_documento_tipo", columnList = "tipo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoPractica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Práctica a la que pertenece el documento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practica_id", nullable = false)
    private Practica practica;

    /**
     * Tipo de documento.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipo;

    /**
     * URL/ruta del archivo almacenado (ej: en cloud storage, AWS S3, etc).
     */
    @Column(name = "url_archivo", nullable = false, length = 1000)
    private String urlArchivo;

    /**
     * Nombre original del archivo subido.
     */
    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    /**
     * MIME type del archivo (application/pdf, image/jpeg, etc).
     */
    @Column(name = "mime_type", length = 50)
    private String mimeType;

    /**
     * Tamaño del archivo en bytes.
     */
    @Column(name = "tamanho")
    private Long tamanho;

    /**
     * Número de páginas (si aplica para PDF).
     */
    @Column(name = "num_paginas")
    private Integer numPaginas;

    /**
     * Firmas requeridas del documento (solo para CONVENIO).
     * Para CARTA_PRESENTACION: vacío.
     * Para CONVENIO: lista de 3 firmas (Coordinador, Tutor, Estudiante).
     */
    @OneToMany(mappedBy = "documento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FirmaDocumento> firmas = new ArrayList<>();

    /**
     * Indica si el documento es mutable (editable/reemplazable).
     * False para documentos en prácticas FINALIZADA o CANCELADA.
     */
    @Column(name = "es_mutable", nullable = false)
    @Builder.Default
    private boolean esMutable = true;

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
     * Verifica si todas las firmas del documento están confirmadas.
     * Relevante para CONVENIO (debe tener 3 firmas todas con fechaFirma no nula).
     */
    public boolean tieneTodasLasFirmas() {
        if (this.tipo != TipoDocumento.CONVENIO) {
            return true; // otros documentos no requieren firmas
        }
        return this.firmas.size() == 3 &&
               this.firmas.stream().allMatch(f -> f.getFechaFirma() != null);
    }

    /**
     * Obtiene la firma para un tipo de firmante.
     */
    public FirmaDocumento obtenerFirma(co.edu.cue.practicas.model.enums.TipoFirmante tipo) {
        return this.firmas.stream()
                .filter(f -> f.getTipo() == tipo)
                .findFirst()
                .orElse(null);
    }
}
