package co.edu.cue.practicas.controller.seguimiento;

import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.enums.EstadoSeguimiento;
import co.edu.cue.practicas.service.seguimiento.SeguimientoPracticaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
class SeguimientoPracticaControllerTest {

    @Mock
    private SeguimientoPracticaService seguimientoService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SeguimientoPracticaController(seguimientoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deberiaCrearSeguimientoYResponderCreated() throws Exception {
        when(seguimientoService.crear(50L, 10L, 3, "Desarrollo API", "Modulo completado", "Sin bloqueos"))
                .thenReturn(seguimientoMap(EstadoSeguimiento.PENDIENTE));

        mockMvc.perform(post("/seguimientos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "practicaId": 50,
                                  "estudianteId": 10,
                                  "semana": 3,
                                  "actividades": "Desarrollo API",
                                  "logros": "Modulo completado",
                                  "dificultades": "Sin bloqueos"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Seguimiento semanal registrado"))
                .andExpect(jsonPath("$.datos.id").value(120))
                .andExpect(jsonPath("$.datos.estado").value("PENDIENTE"));
    }

    @Test
    void deberiaRevisarSeguimientoYResponderOk() throws Exception {
        when(seguimientoService.revisar(120L, 40L, EstadoSeguimiento.APROBADO, "Buen avance"))
                .thenReturn(seguimientoMap(EstadoSeguimiento.APROBADO));

        mockMvc.perform(patch("/seguimientos/{id}/revisar", 120L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "docenteId": 40,
                                  "estado": "APROBADO",
                                  "observaciones": "Buen avance"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Seguimiento revisado"))
                .andExpect(jsonPath("$.datos.estado").value("APROBADO"));
    }

    @Test
    void deberiaResponderConflictCuandoRevisionEsPendiente() throws Exception {
        when(seguimientoService.revisar(eq(120L), eq(40L), eq(EstadoSeguimiento.PENDIENTE), eq(null)))
                .thenThrow(new OperacionNoPermitidaException("La revision debe aprobar o rechazar el seguimiento"));

        mockMvc.perform(patch("/seguimientos/{id}/revisar", 120L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "docenteId": 40,
                                  "estado": "PENDIENTE"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("La revision debe aprobar o rechazar el seguimiento"));
    }

    @Test
    void deberiaCorregirSeguimientoRechazadoYResponderOk() throws Exception {
        when(seguimientoService.corregirRechazado(120L, "Actividades corregidas", "Logros corregidos", null))
                .thenReturn(seguimientoMap(EstadoSeguimiento.PENDIENTE));

        mockMvc.perform(patch("/seguimientos/{id}/corregir", 120L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "actividades": "Actividades corregidas",
                                  "logros": "Logros corregidos"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Seguimiento corregido y reenviado"))
                .andExpect(jsonPath("$.datos.estado").value("PENDIENTE"));
    }

    @Test
    void deberiaListarSeguimientosPorPractica() throws Exception {
        when(seguimientoService.listarPorPractica(50L))
                .thenReturn(List.of(seguimientoMap(EstadoSeguimiento.PENDIENTE)));

        mockMvc.perform(get("/seguimientos/practica/{practicaId}", 50L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos[0].semana").value(3));
    }

    private Map<String, Object> seguimientoMap(EstadoSeguimiento estado) {
        return Map.of(
                "id", 120L,
                "practicaId", 50L,
                "semana", 3,
                "estado", estado,
                "actividades", "Desarrollo API",
                "logros", "Modulo completado",
                "dificultades", "Sin bloqueos",
                "cargadoPorId", 10L,
                "version", 1);
    }
}
