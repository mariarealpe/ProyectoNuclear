package co.edu.cue.practicas.controller.configuracion;

import co.edu.cue.practicas.dto.request.ActualizarConfiguracionProgramaRequest;
import co.edu.cue.practicas.dto.response.ConfiguracionProgramaResponse;
import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.configuracion.ConfiguracionProgramaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConfiguracionProgramaControllerTest {

    @Mock
    private ConfiguracionProgramaService configuracionProgramaService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ConfiguracionProgramaController(configuracionProgramaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    void deberiaObtenerConfiguracionDelPrograma() throws Exception {
        when(configuracionProgramaService.obtenerPorPrograma(2L)).thenReturn(configuracionResponse());

        mockMvc.perform(get("/programas/{programaId}/configuracion", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos.programaId").value(2))
                .andExpect(jsonPath("$.datos.notaMinimaAprobacion").value(3.0))
                .andExpect(jsonPath("$.datos.numeroTotalPracticas").value(2))
                .andExpect(jsonPath("$.datos.maximoAsignacionesSimultaneas").value(1));
    }

    @Test
    void deberiaActualizarConfiguracionDelPrograma() throws Exception {
        when(configuracionProgramaService.actualizarPorPrograma(
                eq(2L),
                any(ActualizarConfiguracionProgramaRequest.class),
                nullable(CustomUserDetails.class)))
                .thenReturn(configuracionResponse());

        mockMvc.perform(put("/programas/{programaId}/configuracion", 2L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "diasInactividadAlerta": 6,
                                  "notificacionesAutomaticas": true,
                                  "notaMinimaAprobacion": 3.0,
                                  "notaMaxima": 5.0,
                                  "numeroCortes": 3,
                                  "maximoAsignacionesSimultaneas": 1,
                                  "plantillaCorreoAsignacion": "<p>Asignación creada</p>",
                                  "plantillaCorreoSeguimiento": "<p>Seguimiento actualizado</p>",
                                  "plantillaCorreoAlerta": "<p>Alerta de inactividad</p>",
                                  "correoRemitente": "practicas@cue.edu.co"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Configuración del programa actualizada"))
                .andExpect(jsonPath("$.datos.numeroCortes").value(3));
    }

    @Test
    void deberiaResponderBadRequestCuandoNumeroCortesEsCero() throws Exception {
        mockMvc.perform(put("/programas/{programaId}/configuracion", 2L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "diasInactividadAlerta": 6,
                                  "notificacionesAutomaticas": true,
                                  "notaMinimaAprobacion": 3.0,
                                  "notaMaxima": 5.0,
                                  "numeroCortes": 0,
                                  "maximoAsignacionesSimultaneas": 1,
                                  "correoRemitente": "practicas@cue.edu.co"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.datos.numeroCortes").exists());

        verify(configuracionProgramaService, never()).actualizarPorPrograma(any(), any(), any());
    }

    @Test
    void deberiaResponderNotFoundCuandoProgramaNoExiste() throws Exception {
        when(configuracionProgramaService.obtenerPorPrograma(99L))
                .thenThrow(new RecursoNoEncontradoException("Programa no encontrado: 99"));

        mockMvc.perform(get("/programas/{programaId}/configuracion", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Programa no encontrado: 99"));
    }

    private ConfiguracionProgramaResponse configuracionResponse() {
        return ConfiguracionProgramaResponse.builder()
                .id(10L)
                .programaId(2L)
                .programaNombre("Ingenieria de Sistemas")
                .diasInactividadAlerta(6)
                .notificacionesAutomaticas(true)
                .notaMinimaAprobacion(3.0)
                .notaMaxima(5.0)
                .numeroTotalPracticas(2)
                .numeroCortes(3)
                .maximoAsignacionesSimultaneas(1)
                .plantillaCorreoAsignacion("<p>Asignación creada</p>")
                .plantillaCorreoSeguimiento("<p>Seguimiento actualizado</p>")
                .plantillaCorreoAlerta("<p>Alerta de inactividad</p>")
                .correoRemitente("practicas@cue.edu.co")
                .build();
    }
}
