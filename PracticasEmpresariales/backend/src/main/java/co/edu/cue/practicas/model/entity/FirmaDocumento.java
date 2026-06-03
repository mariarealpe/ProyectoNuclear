package co.edu.cue.practicas.model.entity;

import co.edu.cue.practicas.model.enums.TipoFirmante;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Firma de un documento de práctica.
 * 
 * Usado para el convenio de práctica que requiere 3 firmas:
 * 1. Coordinador de Prácticas (institución)
 * 2. Tutor Empresarial (empresa)
 * 3. Estudiante (practicante)
 * 
 * GPE-163: Convenio de práctica - requiere las 3 firmas para activar EN_CURSO.
 */
@Entity
@Table(name = "firmas_documento", indexes = {
        @Index(name = "idx_firma_documento", columnList = "documento_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirmaDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Documento al que pertenece la firma.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    private DocumentoPractica documento;

    /**
     * Tipo de firmante (quién debe firmar).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoFirmante tipo;

    /**
     * Usuario que firma el documento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Fecha cuando se confirmó la firma.
     * Null si aún no se ha confirmado.
     */
    @Column(name = "fecha_firma")
    private LocalDateTime fechaFirma;

    /**
     * Hash de validación (para verificar integridad/autenticidad).
     * Puede ser vacío si no se requiere validación criptográfica.
     */
    @Column(name = "hash_validacion", length = 255)
    private String hashValidacion;

    /**
     * Verifica si esta firma está confirmada.
     */
    public boolean estaConfirmada() {
        return this.fechaFirma != null;
    }
}
