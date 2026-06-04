package co.edu.cue.practicas.controller.evaluacion;

import co.edu.cue.practicas.dto.response.NotaFinalResponse;
import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.enums.ResultadoEvaluacion;
import co.edu.cue.practicas.model.enums.ResultadoNotaFinal;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.evaluacion.NotaFinalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotaFinalControllerTest {

    @Mock
    private NotaFinalService notaFinalService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new NotaFinalController(notaFinalService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    // =========================================================================
    // POST /notas-finales/practica/{practicaId}
    // =========================================================================

    @Test
    void deberiaRegistrarNotaFinalYRetornar201() throws Exception {
        when(notaFinalService.registrar(eq(7L), any(), nullable(CustomUserDetails.class)))
                .thenReturn(notaResponse(4.3, ResultadoNotaFinal.APROBADO, false));

        mockMvc.perform(post("/notas-finales/practica/{id}", 7L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                { "nota": 4.3, "observaciones": "Aprueba" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos.nota").value(4.3))
                .andExpect(jsonPath("$.datos.resultado").value("APROBADO"))
                .andExpect(jsonPath("$.datos.notaReferenciaDocente").value(4.0))
                .andExpect(jsonPath("$.datos.notaReferenciaTutor").value(4.2));
    }

    @Test
    void deberiaRetornar400CuandoNotaEsNula() throws Exception {
        mockMvc.perform(post("/notas-finales/practica/{id}", 7L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                { "observaciones": "obs" }
                                """))
                .andExpect(status().isBadRequest());

        verify(notaFinalService, never()).registrar(any(), any(), any());
    }

    @Test
    void deberiaRetornar422CuandoFaltanEvaluacionesPrevias() throws Exception {
        when(notaFinalService.registrar(eq(7L), any(), nullable(CustomUserDetails.class)))
                .thenThrow(new OperacionNoPermitidaException("falta la evaluación del Docente Asesor"));

        mockMvc.perform(post("/notas-finales/practica/{id}", 7L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                { "nota": 4.0 }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("falta la evaluación del Docente Asesor"));
    }

    @Test
    void deberiaRetornar404CuandoPracticaNoExiste() throws Exception {
        when(notaFinalService.registrar(eq(99L), any(), nullable(CustomUserDetails.class)))
                .thenThrow(new RecursoNoEncontradoException("Práctica no encontrada: 99"));

        mockMvc.perform(post("/notas-finales/practica/{id}", 99L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                { "nota": 4.0 }
                                """))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // PUT /notas-finales/{id}
    // =========================================================================

    @Test
    void deberiaActualizarNotaFinalYRetornar200() throws Exception {
        when(notaFinalService.actualizar(eq(99L), any(), nullable(CustomUserDetails.class)))
                .thenReturn(notaResponse(3.5, ResultadoNotaFinal.APROBADO, false));

        mockMvc.perform(put("/notas-finales/{id}", 99L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                { "nota": 3.5 }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.nota").value(3.5));
    }

    @Test
    void deberiaRetornar422CuandoNotaYaCerrada() throws Exception {
        when(notaFinalService.actualizar(eq(99L), any(), nullable(CustomUserDetails.class)))
                .thenThrow(new OperacionNoPermitidaException("La nota final ya fue cerrada y es inmutable"));

        mockMvc.perform(put("/notas-finales/{id}", 99L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                { "nota": 4.0 }
                                """))
                .andExpect(status().isConflict());
    }

    // =========================================================================
    // POST /notas-finales/{id}/cerrar
    // =========================================================================

    @Test
    void deberiaCerrarNotaFinalYRetornar200() throws Exception {
        when(notaFinalService.cerrar(eq(99L), nullable(CustomUserDetails.class)))
                .thenReturn(notaResponse(4.3, ResultadoNotaFinal.APROBADO, true));

        mockMvc.perform(post("/notas-finales/{id}/cerrar", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Proceso de evaluación cerrado"))
                .andExpect(jsonPath("$.datos.cerrada").value(true));
    }

    @Test
    void deberiaRetornar422CuandoSeintentaCerrarYaCerrada() throws Exception {
        when(notaFinalService.cerrar(eq(99L), nullable(CustomUserDetails.class)))
                .thenThrow(new OperacionNoPermitidaException("La nota final ya está cerrada"));

        mockMvc.perform(post("/notas-finales/{id}/cerrar", 99L))
                .andExpect(status().isConflict());
    }

    // =========================================================================
    // GET /notas-finales/practica/{practicaId}
    // =========================================================================

    @Test
    void deberiaObtenerNotaPorPracticaYRetornar200() throws Exception {
        when(notaFinalService.obtenerPorPractica(7L))
                .thenReturn(notaResponse(4.3, ResultadoNotaFinal.APROBADO, false));

        mockMvc.perform(get("/notas-finales/practica/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.nota").value(4.3))
                .andExpect(jsonPath("$.datos.nombreCoordinador").value("Coordinador Prácticas"));
    }

    @Test
    void deberiaRetornar404CuandoNoExisteNota() throws Exception {
        when(notaFinalService.obtenerPorPractica(7L))
                .thenThrow(new RecursoNoEncontradoException("No existe nota final para la práctica: 7"));

        mockMvc.perform(get("/notas-finales/practica/{id}", 7L))
                .andExpect(status().isNotFound());
    }

    private NotaFinalResponse notaResponse(double nota, ResultadoNotaFinal resultado, boolean cerrada) {
        return NotaFinalResponse.builder()
                .id(99L)
                .practicaId(7L)
                .nombreEstudiante("Estudiante A")
                .coordinadorId(12L)
                .nombreCoordinador("Coordinador Prácticas")
                .nota(nota)
                .resultado(resultado)
                .observaciones("Observaciones")
                .cerrada(cerrada)
                .cerradaEn(cerrada ? LocalDateTime.now() : null)
                .notaReferenciaDocente(4.0)
                .resultadoDocente(ResultadoEvaluacion.APROBADO)
                .notaReferenciaTutor(4.2)
                .resultadoTutor(ResultadoEvaluacion.APROBADO)
                .creadoEn(LocalDateTime.now())
                .actualizadoEn(LocalDateTime.now())
                .build();
    }
}
