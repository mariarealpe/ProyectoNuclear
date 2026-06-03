package co.edu.cue.practicas.controller.asignacion;

import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.enums.EstadoAsignacion;
import co.edu.cue.practicas.service.asignacion.AsignacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
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
class AsignacionControllerTest {

    @Mock
    private AsignacionService asignacionService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AsignacionController(asignacionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void deberiaCrearAsignacionYResponderCreated() throws Exception {
        when(asignacionService.crear(10L, 20L, 30L))
                .thenReturn(asignacionMap());

        mockMvc.perform(post("/asignaciones")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "estudianteId": 10,
                                  "vacanteId": 20,
                                  "coordinadorId": 30
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Asignacion creada"))
                .andExpect(jsonPath("$.datos.id").value(99))
                .andExpect(jsonPath("$.datos.estado").value("ASIGNADA"));
    }

    @Test
    void deberiaListarAsignacionesConFiltros() throws Exception {
        when(asignacionService.listar(eq(EstadoAsignacion.ASIGNADA), eq(30L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(asignacionMap()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/asignaciones")
                        .param("estado", "ASIGNADA")
                        .param("coordinadorId", "30")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos.content[0].id").value(99))
                .andExpect(jsonPath("$.datos.content[0].vacante").value("Desarrollador Java"));
    }

    @Test
    void deberiaResponderNotFoundCuandoAsignacionNoExiste() throws Exception {
        when(asignacionService.obtener(404L))
                .thenThrow(new RecursoNoEncontradoException("Asignacion no encontrada"));

        mockMvc.perform(get("/asignaciones/{id}", 404L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Asignacion no encontrada"));
    }

    @Test
    void deberiaResponderConflictCuandoTransicionNoEsPermitida() throws Exception {
        when(asignacionService.cambiarEstado(
                eq(99L),
                eq(EstadoAsignacion.EN_CURSO),
                eq(30L),
                eq("Sin documentos")))
                .thenThrow(new OperacionNoPermitidaException("Transicion de estado no permitida"));

        mockMvc.perform(patch("/asignaciones/{id}/estado", 99L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "estado": "EN_CURSO",
                                  "usuarioId": 30,
                                  "motivo": "Sin documentos"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Transicion de estado no permitida"));
    }

    @Test
    void deberiaResponderOkConHistorial() throws Exception {
        when(asignacionService.historial(99L))
                .thenReturn(List.of(Map.of(
                        "id", 1L,
                        "asignacionId", 99L,
                        "estadoAnterior", EstadoAsignacion.ASIGNADA,
                        "estadoNuevo", EstadoAsignacion.EN_VINCULACION)));

        mockMvc.perform(get("/asignaciones/{id}/historial", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos[0].estadoNuevo").value("EN_VINCULACION"));
    }

    @Test
    void deberiaResponderErrorInternoCuandoEstadoNoExiste() throws Exception {
        mockMvc.perform(patch("/asignaciones/{id}/estado", 99L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "estado": "INVALIDO",
                                  "usuarioId": 30
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.exitoso").value(false));

        verify(asignacionService, never()).cambiarEstado(any(), any(), any(), any());
    }

    private Map<String, Object> asignacionMap() {
        return Map.of(
                "id", 99L,
                "estudianteId", 10L,
                "estudiante", "Estudiante Uno",
                "vacanteId", 20L,
                "vacante", "Desarrollador Java",
                "coordinadorId", 30L,
                "estado", EstadoAsignacion.ASIGNADA);
    }
}
