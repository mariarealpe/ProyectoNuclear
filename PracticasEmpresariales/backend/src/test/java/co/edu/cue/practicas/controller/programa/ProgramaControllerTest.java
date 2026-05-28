package co.edu.cue.practicas.controller.programa;

import co.edu.cue.practicas.dto.request.CrearProgramaRequest;
import co.edu.cue.practicas.dto.response.ProgramaResponse;
import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.programa.ProgramaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProgramaControllerTest {

    @Mock
    private ProgramaService programaService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProgramaController(programaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    void deberiaCrearProgramaYResponderCreated() throws Exception {
        when(programaService.crearPrograma(any(CrearProgramaRequest.class), nullable(CustomUserDetails.class)))
                .thenReturn(programaResponse());

        mockMvc.perform(post("/programas")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Ingenieria de Sistemas",
                                  "descripcion": "Programa academico",
                                  "facultadId": 1,
                                  "numeroTotalPracticas": 2,
                                  "promedioMinimoGeneral": 3.5,
                                  "requisitos": [
                                    {
                                      "numeroPractica": 1,
                                      "creditosMinimos": 80,
                                      "promedioMinimo": 3.2,
                                      "requierePracticaAnteriorAprobada": false,
                                      "documentosRequeridos": "Hoja de vida"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Programa creado"))
                .andExpect(jsonPath("$.datos.nombre").value("Ingenieria de Sistemas"))
                .andExpect(jsonPath("$.datos.requisitos[0].creditosMinimos").value(80));
    }

    @Test
    void deberiaResponderBadRequestCuandoNumeroPracticasEsCero() throws Exception {
        mockMvc.perform(post("/programas")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Ingenieria de Sistemas",
                                  "facultadId": 1,
                                  "numeroTotalPracticas": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.datos.numeroTotalPracticas").exists());

        verify(programaService, never()).crearPrograma(any(), any());
    }

    @Test
    void deberiaListarProgramasPorFacultad() throws Exception {
        when(programaService.listarPorFacultad(1L)).thenReturn(List.of(programaResponse()));

        mockMvc.perform(get("/programas/por-facultad/{facultadId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos[0].facultadNombre").value("Ingenieria"));
    }

    @Test
    void deberiaResponderNotFoundCuandoProgramaNoExiste() throws Exception {
        when(programaService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Programa no encontrado: 99"));

        mockMvc.perform(get("/programas/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Programa no encontrado: 99"));
    }

    private ProgramaResponse programaResponse() {
        return ProgramaResponse.builder()
                .id(2L)
                .nombre("Ingenieria de Sistemas")
                .descripcion("Programa academico")
                .facultadId(1L)
                .facultadNombre("Ingenieria")
                .numeroTotalPracticas(2)
                .promedioMinimoGeneral(3.5)
                .activo(true)
                .requisitos(List.of(ProgramaResponse.RequisitoResponse.builder()
                        .id(5L)
                        .numeroPractica(1)
                        .creditosMinimos(80)
                        .promedioMinimo(3.2)
                        .requierePracticaAnteriorAprobada(false)
                        .documentosRequeridos("Hoja de vida")
                        .build()))
                .build();
    }
}
