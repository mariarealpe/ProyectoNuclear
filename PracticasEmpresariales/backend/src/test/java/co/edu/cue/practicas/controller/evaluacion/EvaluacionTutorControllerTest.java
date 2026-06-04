package co.edu.cue.practicas.controller.evaluacion;

import co.edu.cue.practicas.dto.response.EvaluacionTutorResponse;
import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.enums.ResultadoEvaluacion;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.evaluacion.EvaluacionTutorService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EvaluacionTutorControllerTest {

    @Mock
    private EvaluacionTutorService evaluacionService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new EvaluacionTutorController(evaluacionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    // =========================================================================
    // POST /evaluaciones-tutor/practica/{practicaId}
    // =========================================================================

    @Test
    void deberiaRegistrarEvaluacionYRetornar201() throws Exception {
        when(evaluacionService.registrar(eq(7L), any(), nullable(CustomUserDetails.class)))
                .thenReturn(evaluacionResponse(4.5, ResultadoEvaluacion.APROBADO));

        mockMvc.perform(post("/evaluaciones-tutor/practica/{id}", 7L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nota": 4.5,
                                  "observaciones": "Excelente desempeño en la empresa"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Evaluación registrada exitosamente"))
                .andExpect(jsonPath("$.datos.nota").value(4.5))
                .andExpect(jsonPath("$.datos.resultado").value("APROBADO"));
    }

    @Test
    void deberiaRetornar400CuandoNotaEsNula() throws Exception {
        mockMvc.perform(post("/evaluaciones-tutor/practica/{id}", 7L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                { "observaciones": "Sin nota" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false));

        verify(evaluacionService, never()).registrar(any(), any(), any());
    }

    @Test
    void deberiaRetornar400CuandoObservacionesEstanVacias() throws Exception {
        mockMvc.perform(post("/evaluaciones-tutor/practica/{id}", 7L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                { "nota": 4.0, "observaciones": "" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false));

        verify(evaluacionService, never()).registrar(any(), any(), any());
    }

    @Test
    void deberiaRetornar422CuandoPracticaNoEstaEnCurso() throws Exception {
        when(evaluacionService.registrar(eq(7L), any(), nullable(CustomUserDetails.class)))
                .thenThrow(new OperacionNoPermitidaException("Solo se pueden registrar evaluaciones en prácticas EN_CURSO"));

        mockMvc.perform(post("/evaluaciones-tutor/practica/{id}", 7L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nota": 4.0, "observaciones": "obs"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.exitoso").value(false));
    }

    @Test
    void deberiaRetornar403CuandoTutorNoEsElAsignado() throws Exception {
        when(evaluacionService.registrar(eq(7L), any(), nullable(CustomUserDetails.class)))
                .thenThrow(new AccesoNoAutorizadoException("Solo el Tutor Empresarial asignado puede registrar la evaluación"));

        mockMvc.perform(post("/evaluaciones-tutor/practica/{id}", 7L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nota": 4.0, "observaciones": "obs"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.exitoso").value(false));
    }

    @Test
    void deberiaRetornar404CuandoPracticaNoExiste() throws Exception {
        when(evaluacionService.registrar(eq(99L), any(), nullable(CustomUserDetails.class)))
                .thenThrow(new RecursoNoEncontradoException("Práctica no encontrada: 99"));

        mockMvc.perform(post("/evaluaciones-tutor/practica/{id}", 99L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nota": 4.0, "observaciones": "obs"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false));
    }

    // =========================================================================
    // PUT /evaluaciones-tutor/{id}
    // =========================================================================

    @Test
    void deberiaActualizarEvaluacionYRetornar200() throws Exception {
        when(evaluacionService.actualizar(eq(1L), any(), nullable(CustomUserDetails.class)))
                .thenReturn(evaluacionResponse(3.8, ResultadoEvaluacion.APROBADO));

        mockMvc.perform(put("/evaluaciones-tutor/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                { "nota": 3.8, "observaciones": "Mejoró" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.nota").value(3.8));
    }

    @Test
    void deberiaRetornar422CuandoNotasYaEstanCerradas() throws Exception {
        when(evaluacionService.actualizar(eq(1L), any(), nullable(CustomUserDetails.class)))
                .thenThrow(new OperacionNoPermitidaException("El Coordinador ya cerró el proceso de evaluación"));

        mockMvc.perform(put("/evaluaciones-tutor/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nota": 4.0, "observaciones": "obs"}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    // =========================================================================
    // GET /evaluaciones-tutor/practica/{practicaId}
    // =========================================================================

    @Test
    void deberiaObtenerEvaluacionPorPracticaYRetornar200() throws Exception {
        when(evaluacionService.obtenerPorPractica(7L))
                .thenReturn(evaluacionResponse(4.0, ResultadoEvaluacion.APROBADO));

        mockMvc.perform(get("/evaluaciones-tutor/practica/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.nota").value(4.0))
                .andExpect(jsonPath("$.datos.nombreTutor").value("Tutor Empresarial"));
    }

    @Test
    void deberiaRetornar404CuandoNoExisteEvaluacion() throws Exception {
        when(evaluacionService.obtenerPorPractica(7L))
                .thenThrow(new RecursoNoEncontradoException("No existe evaluación de tutor para la práctica: 7"));

        mockMvc.perform(get("/evaluaciones-tutor/practica/{id}", 7L))
                .andExpect(status().isNotFound());
    }

    private EvaluacionTutorResponse evaluacionResponse(double nota, ResultadoEvaluacion resultado) {
        return EvaluacionTutorResponse.builder()
                .id(1L)
                .practicaId(7L)
                .nombreEstudiante("Estudiante A")
                .tutorId(11L)
                .nombreTutor("Tutor Empresarial")
                .nota(nota)
                .resultado(resultado)
                .observaciones("Observaciones")
                .creadoEn(LocalDateTime.now())
                .actualizadoEn(LocalDateTime.now())
                .build();
    }
}
