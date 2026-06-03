package co.edu.cue.practicas.controller.practica;

import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.service.practica.PracticaSprint3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PracticaSprint3ControllerTest {

    @Mock
    private PracticaSprint3Service practicaService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PracticaSprint3Controller(practicaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deberiaPrepararPracticaDesdeAsignacionYResponderCreated() throws Exception {
        when(practicaService.prepararDesdeAsignacion(99L, 30L))
                .thenReturn(practicaMap(EstadoPractica.PENDIENTE_ASIGNACION));

        mockMvc.perform(post("/practicas/desde-asignacion/{asignacionId}", 99L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioId": 30
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Practica preparada para vinculacion"))
                .andExpect(jsonPath("$.datos.id").value(50))
                .andExpect(jsonPath("$.datos.estado").value("PENDIENTE_ASIGNACION"));
    }

    @Test
    void deberiaConfirmarVinculacionYResponderOk() throws Exception {
        LocalDateTime inicio = LocalDateTime.parse("2026-06-10T08:00:00");
        LocalDateTime fin = LocalDateTime.parse("2026-12-10T17:00:00");
        when(practicaService.confirmarVinculacion(eq(50L), eq(30L), eq(inicio), eq(fin)))
                .thenReturn(practicaMap(EstadoPractica.EN_CURSO));

        mockMvc.perform(patch("/practicas/{id}/confirmar-vinculacion", 50L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioId": 30,
                                  "fechaInicio": "2026-06-10T08:00:00",
                                  "fechaFin": "2026-12-10T17:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Vinculacion confirmada"))
                .andExpect(jsonPath("$.datos.estado").value("EN_CURSO"));
    }

    @Test
    void deberiaResponderConflictCuandoNoTieneFirmasCompletas() throws Exception {
        when(practicaService.confirmarVinculacion(eq(50L), eq(30L), eq(null), eq(null)))
                .thenThrow(new OperacionNoPermitidaException("El convenio requiere las 3 firmas confirmadas"));

        mockMvc.perform(patch("/practicas/{id}/confirmar-vinculacion", 50L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioId": 30
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("El convenio requiere las 3 firmas confirmadas"));
    }

    @Test
    void deberiaResponderNotFoundCuandoPracticaNoExiste() throws Exception {
        when(practicaService.obtener(404L))
                .thenThrow(new RecursoNoEncontradoException("Practica no encontrada"));

        mockMvc.perform(get("/practicas/{id}", 404L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Practica no encontrada"));
    }

    @Test
    void deberiaListarPracticasPorEstudiante() throws Exception {
        when(practicaService.listarPorEstudiante(10L))
                .thenReturn(List.of(practicaMap(EstadoPractica.EN_CURSO)));

        mockMvc.perform(get("/practicas/estudiante/{estudianteId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos[0].estudiante").value("Estudiante Uno"));
    }

    private Map<String, Object> practicaMap(EstadoPractica estado) {
        return Map.of(
                "id", 50L,
                "estudianteId", 10L,
                "estudiante", "Estudiante Uno",
                "programaId", 2L,
                "numeroPractica", 1,
                "nombre", "Practica de desarrollo",
                "estado", estado,
                "documentosRequeridos", "CARTA_PRESENTACION,CONVENIO");
    }
}
