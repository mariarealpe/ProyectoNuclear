package co.edu.cue.practicas.service.practica;

import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.entity.DocumentoPractica;
import co.edu.cue.practicas.model.entity.FirmaDocumento;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.TipoDocumento;
import co.edu.cue.practicas.model.enums.TipoFirmante;
import co.edu.cue.practicas.repository.practica.DocumentoPracticaRepository;
import co.edu.cue.practicas.repository.practica.FirmaDocumentoRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentoPracticaServiceTest {

    @Mock
    private DocumentoPracticaRepository documentoRepository;

    @Mock
    private FirmaDocumentoRepository firmaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PracticaSprint3Service practicaService;

    private DocumentoPracticaService documentoService;

    @BeforeEach
    void configurar() {
        documentoService = new DocumentoPracticaService(
                documentoRepository,
                firmaRepository,
                usuarioRepository,
                practicaService);
    }

    @Test
    void deberiaCargarDocumentoCuandoPracticaEsMutable() {
        Practica practica = practica(EstadoPractica.EN_CURSO);
        when(practicaService.buscar(50L)).thenReturn(practica);
        when(documentoRepository.save(any(DocumentoPractica.class))).thenAnswer(invocation -> {
            DocumentoPractica documento = invocation.getArgument(0);
            documento.setId(70L);
            return documento;
        });

        Map<String, Object> response = documentoService.cargar(
                50L,
                TipoDocumento.CONVENIO,
                "s3://docs/convenio.pdf",
                "convenio.pdf",
                "application/pdf",
                2048L,
                3);

        assertThat(response.get("id")).isEqualTo(70L);
        assertThat(response.get("practicaId")).isEqualTo(50L);
        assertThat(response.get("tipo")).isEqualTo(TipoDocumento.CONVENIO);
        assertThat(response.get("firmasCompletas")).isEqualTo(false);
        verify(documentoRepository).save(any(DocumentoPractica.class));
    }

    @Test
    void deberiaRechazarCargaCuandoPracticaEstaFinalizada() {
        when(practicaService.buscar(50L)).thenReturn(practica(EstadoPractica.FINALIZADA));

        assertThatThrownBy(() -> documentoService.cargar(
                50L,
                TipoDocumento.CONVENIO,
                "s3://docs/convenio.pdf",
                "convenio.pdf",
                "application/pdf",
                2048L,
                3))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("inmutables");

        verify(documentoRepository, never()).save(any());
    }

    @Test
    void deberiaCrearFirmaParaConvenio() {
        DocumentoPractica documento = documento(TipoDocumento.CONVENIO, EstadoPractica.EN_CURSO);
        Usuario usuario = usuario(10L, "Estudiante Uno");
        when(documentoRepository.findById(70L)).thenReturn(Optional.of(documento));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(firmaRepository.findByDocumento_IdAndTipo(70L, TipoFirmante.ESTUDIANTE))
                .thenReturn(Optional.empty());
        when(firmaRepository.save(any(FirmaDocumento.class))).thenAnswer(invocation -> {
            FirmaDocumento firma = invocation.getArgument(0);
            firma.setId(80L);
            return firma;
        });

        Map<String, Object> response = documentoService.crearFirma(
                70L,
                TipoFirmante.ESTUDIANTE,
                10L);

        assertThat(response.get("id")).isEqualTo(80L);
        assertThat(response.get("documentoId")).isEqualTo(70L);
        assertThat(response.get("usuarioId")).isEqualTo(10L);
        assertThat(response.get("confirmada")).isEqualTo(false);
    }

    @Test
    void deberiaRechazarFirmaCuandoDocumentoNoEsConvenio() {
        DocumentoPractica documento = documento(TipoDocumento.CARTA_PRESENTACION, EstadoPractica.EN_CURSO);
        when(documentoRepository.findById(70L)).thenReturn(Optional.of(documento));

        assertThatThrownBy(() -> documentoService.crearFirma(
                70L,
                TipoFirmante.ESTUDIANTE,
                10L))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("Solo los convenios tienen firmas");

        verify(usuarioRepository, never()).findById(any());
        verify(firmaRepository, never()).save(any());
    }

    @Test
    void deberiaConfirmarFirmaDelUsuarioRegistrado() {
        DocumentoPractica documento = documento(TipoDocumento.CONVENIO, EstadoPractica.EN_CURSO);
        FirmaDocumento firma = FirmaDocumento.builder()
                .id(80L)
                .documento(documento)
                .tipo(TipoFirmante.ESTUDIANTE)
                .usuario(usuario(10L, "Estudiante Uno"))
                .build();
        when(firmaRepository.findByDocumento_IdAndTipo(70L, TipoFirmante.ESTUDIANTE))
                .thenReturn(Optional.of(firma));
        when(firmaRepository.save(any(FirmaDocumento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> response = documentoService.confirmarFirma(
                70L,
                TipoFirmante.ESTUDIANTE,
                10L,
                "hash-123");

        assertThat(response.get("confirmada")).isEqualTo(true);
        assertThat(response.get("hashValidacion")).isEqualTo("hash-123");
        assertThat(firma.getFechaFirma()).isNotNull();
    }

    @Test
    void deberiaRechazarConfirmacionDeUsuarioDistintoAlFirmante() {
        DocumentoPractica documento = documento(TipoDocumento.CONVENIO, EstadoPractica.EN_CURSO);
        FirmaDocumento firma = FirmaDocumento.builder()
                .id(80L)
                .documento(documento)
                .tipo(TipoFirmante.ESTUDIANTE)
                .usuario(usuario(10L, "Estudiante Uno"))
                .build();
        when(firmaRepository.findByDocumento_IdAndTipo(70L, TipoFirmante.ESTUDIANTE))
                .thenReturn(Optional.of(firma));

        assertThatThrownBy(() -> documentoService.confirmarFirma(
                70L,
                TipoFirmante.ESTUDIANTE,
                99L,
                null))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("usuario no corresponde");

        verify(firmaRepository, never()).save(any());
    }

    @Test
    void deberiaListarFirmasDelDocumento() {
        DocumentoPractica documento = documento(TipoDocumento.CONVENIO, EstadoPractica.EN_CURSO);
        FirmaDocumento firma = FirmaDocumento.builder()
                .id(80L)
                .documento(documento)
                .tipo(TipoFirmante.ESTUDIANTE)
                .usuario(usuario(10L, "Estudiante Uno"))
                .build();
        when(firmaRepository.findByDocumento_Id(70L)).thenReturn(List.of(firma));

        List<Map<String, Object>> response = documentoService.firmas(70L);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().get("tipo")).isEqualTo(TipoFirmante.ESTUDIANTE);
    }

    private DocumentoPractica documento(TipoDocumento tipo, EstadoPractica estadoPractica) {
        return DocumentoPractica.builder()
                .id(70L)
                .practica(practica(estadoPractica))
                .tipo(tipo)
                .urlArchivo("s3://docs/convenio.pdf")
                .nombreArchivo("convenio.pdf")
                .mimeType("application/pdf")
                .tamanho(2048L)
                .numPaginas(3)
                .build();
    }

    private Practica practica(EstadoPractica estado) {
        return Practica.builder()
                .id(50L)
                .estudiante(usuario(10L, "Estudiante Uno"))
                .programa(Programa.builder().id(2L).nombre("Ingenieria de Sistemas").build())
                .numeroPractica(1)
                .nombre("Practica de desarrollo")
                .estado(estado)
                .build();
    }

    private Usuario usuario(Long id, String nombre) {
        return Usuario.builder()
                .id(id)
                .nombre(nombre)
                .correo("usuario" + id + "@cue.edu.co")
                .passwordHash("hash")
                .build();
    }
}
