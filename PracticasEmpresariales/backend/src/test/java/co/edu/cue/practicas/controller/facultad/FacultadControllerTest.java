package co.edu.cue.practicas.controller.facultad;

import co.edu.cue.practicas.dto.request.CrearFacultadRequest;
import co.edu.cue.practicas.dto.response.FacultadResponse;
import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.facultad.FacultadService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
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
class FacultadControllerTest {

    @Mock
    private FacultadService facultadService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FacultadController(facultadService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    void deberiaCrearFacultadYResponderCreated() throws Exception {
        when(facultadService.crearFacultad(any(CrearFacultadRequest.class), nullable(CustomUserDetails.class)))
                .thenReturn(facultadResponse());

        mockMvc.perform(post("/facultades")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Ingenieria",
                                  "descripcion": "Facultad de Ingenieria"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Facultad creada"))
                .andExpect(jsonPath("$.datos.nombre").value("Ingenieria"));
    }

    @Test
    void deberiaResponderBadRequestCuandoNombreEsVacio() throws Exception {
        mockMvc.perform(post("/facultades")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "",
                                  "descripcion": "Sin nombre"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.datos.nombre").exists());

        verify(facultadService, never()).crearFacultad(any(), any());
    }

    @Test
    void deberiaResponderNotFoundCuandoFacultadNoExiste() throws Exception {
        when(facultadService.obtenerPorId(77L))
                .thenThrow(new RecursoNoEncontradoException("Facultad no encontrada: 77"));

        mockMvc.perform(get("/facultades/{id}", 77L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Facultad no encontrada: 77"));
    }

    @Test
    void deberiaResponderConflictCuandoFacultadTieneProgramasActivos() throws Exception {
        doThrow(new OperacionNoPermitidaException("No se puede desactivar la facultad porque tiene programas activos."))
                .when(facultadService)
                .desactivarFacultad(eq(1L), nullable(CustomUserDetails.class));

        mockMvc.perform(patch("/facultades/{id}/desactivar", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("No se puede desactivar la facultad porque tiene programas activos."));
    }

    private FacultadResponse facultadResponse() {
        return FacultadResponse.builder()
                .id(1L)
                .nombre("Ingenieria")
                .descripcion("Facultad de Ingenieria")
                .activa(true)
                .numeroProgramas(2)
                .build();
    }
}
