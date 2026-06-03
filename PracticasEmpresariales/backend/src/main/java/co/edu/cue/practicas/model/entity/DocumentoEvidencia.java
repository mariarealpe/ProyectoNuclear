package co.edu.cue.practicas.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Evidencia (archivo adjunto) de un seguimiento de práctica.
 * 
 * El estudiante puede adjuntar documentos, fotos, reportes, etc.
 * como parte del seguimiento semanal.
 */
@Entity
@Table(name = "documentos_evidencia", indexes = {
        @Index(name = "idx_evidencia_seguimiento", columnList = "seguimiento_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoEvidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Seguimiento al que pertenece esta evidencia.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seguimiento_id", nullable = false)
    private SeguimientoPractica seguimiento;

    /**
     * URL del archivo (en cloud storage, AWS S3, etc).
     */
    @Column(name = "url_archivo", nullable = false, length = 1000)
    private String urlArchivo;

    /**
     * Nombre original del archivo.
     */
    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    /**
     * Tipo MIME del archivo.
     */
    @Column(name = "mime_type", length = 50)
    private String mimeType;

    /**
     * Tamaño en bytes.
     */
    @Column(name = "tamanho")
    private Long tamanho;

    /**
     * Descripción/título de la evidencia.
     */
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();
}
