package co.edu.cue.practicas.controller.practica;

import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.enums.TipoDocumento;
import co.edu.cue.practicas.model.enums.TipoFirmante;
import co.edu.cue.practicas.service.practica.DocumentoPracticaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DocumentoPracticaControllerTest {

    @Mock
    private DocumentoPracticaService documentoService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DocumentoPracticaController(documentoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deberiaCargarDocumentoYResponderCreated() throws Exception {
        when(documentoService.cargar(
                50L,
                TipoDocumento.CONVENIO,
                "s3://docs/convenio.pdf",
                "convenio.pdf",
                "application/pdf",
                2048L,
                3))
                .thenReturn(documentoMap());

        mockMvc.perform(post("/documentos-practica")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "practicaId": 50,
                                  "tipo": "CONVENIO",
                                  "urlArchivo": "s3://docs/convenio.pdf",
                                  "nombreArchivo": "convenio.pdf",
                                  "mimeType": "application/pdf",
                                  "tamanho": 2048,
                                  "numPaginas": 3
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Documento registrado"))
                .andExpect(jsonPath("$.datos.id").value(70))
                .andExpect(jsonPath("$.datos.tipo").value("CONVENIO"));
    }

    @Test
    void deberiaListarDocumentosPorPractica() throws Exception {
        when(documentoService.listarPorPractica(50L)).thenReturn(List.of(documentoMap()));

        mockMvc.perform(get("/documentos-practica/practica/{practicaId}", 50L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos[0].nombreArchivo").value("convenio.pdf"));
    }

    @Test
    void deberiaCrearFirmaYResponderCreated() throws Exception {
        when(documentoService.crearFirma(70L, TipoFirmante.ESTUDIANTE, 10L))
                .thenReturn(firmaMap(false));

        mockMvc.perform(post("/documentos-practica/{documentoId}/firmas", 70L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoFirmante": "ESTUDIANTE",
                                  "usuarioId": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Firma requerida registrada"))
                .andExpect(jsonPath("$.datos.confirmada").value(false));
    }

    @Test
    void deberiaConfirmarFirmaYResponderOk() throws Exception {
        when(documentoService.confirmarFirma(70L, TipoFirmante.ESTUDIANTE, 10L, "hash-123"))
                .thenReturn(firmaMap(true));

        mockMvc.perform(patch("/documentos-practica/{documentoId}/firmas/{tipoFirmante}/confirmar",
                        70L,
                        TipoFirmante.ESTUDIANTE)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioId": 10,
                                  "hashValidacion": "hash-123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Firma confirmada"))
                .andExpect(jsonPath("$.datos.confirmada").value(true));
    }

    @Test
    void deberiaResponderConflictCuandoServicioRechazaFirma() throws Exception {
        when(documentoService.confirmarFirma(70L, TipoFirmante.ESTUDIANTE, 99L, null))
                .thenThrow(new OperacionNoPermitidaException("El usuario no corresponde al firmante registrado"));

        mockMvc.perform(patch("/documentos-practica/{documentoId}/firmas/{tipoFirmante}/confirmar",
                        70L,
                        TipoFirmante.ESTUDIANTE)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioId": 99
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("El usuario no corresponde al firmante registrado"));
    }

    @Test
    void deberiaResponderErrorInternoCuandoTipoDocumentoNoExiste() throws Exception {
        mockMvc.perform(post("/documentos-practica")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "practicaId": 50,
                                  "tipo": "INVALIDO",
                                  "urlArchivo": "s3://docs/convenio.pdf",
                                  "nombreArchivo": "convenio.pdf"
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.exitoso").value(false));

        verify(documentoService, never()).cargar(any(), any(), any(), any(), any(), any(), any());
    }

    private Map<String, Object> documentoMap() {
        return Map.of(
                "id", 70L,
                "practicaId", 50L,
                "tipo", TipoDocumento.CONVENIO,
                "urlArchivo", "s3://docs/convenio.pdf",
                "nombreArchivo", "convenio.pdf",
                "mimeType", "application/pdf",
                "tamanho", 2048L,
                "numPaginas", 3,
                "esMutable", true,
                "firmasCompletas", false);
    }

    private Map<String, Object> firmaMap(boolean confirmada) {
        return Map.of(
                "id", 80L,
                "documentoId", 70L,
                "tipo", TipoFirmante.ESTUDIANTE,
                "usuarioId", 10L,
                "usuario", "Estudiante Uno",
                "confirmada", confirmada);
    }
}
