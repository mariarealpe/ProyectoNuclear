package co.edu.cue.practicas.controller.tutor;

import co.edu.cue.practicas.dto.request.CrearTutorEmpresarialRequest;
import co.edu.cue.practicas.dto.request.EditarTutorEmpresarialRequest;
import co.edu.cue.practicas.dto.response.TutorEmpresarialResponse;
import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.tutor.TutorEmpresarialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TutorEmpresarialControllerTest {

    @Mock
    private TutorEmpresarialService tutorService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TutorEmpresarialController(tutorService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    void deberiaCrearTutorYResponderCreated() throws Exception {
        when(tutorService.crearTutor(any(CrearTutorEmpresarialRequest.class), nullable(CustomUserDetails.class)))
                .thenReturn(tutorResponse());

        mockMvc.perform(post("/tutores")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Laura Tutor",
                                  "correo": "laura.tutor@empresa.com",
                                  "telefono": "3001234567",
                                  "empresaId": 8,
                                  "cargo": "Lider de Desarrollo",
                                  "area": "TI",
                                  "telefonoCorporativo": "6061234567",
                                  "esResponsablePrincipal": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Tutor empresarial creado"))
                .andExpect(jsonPath("$.datos.id").value(5))
                .andExpect(jsonPath("$.datos.nombre").value("Laura Tutor"));
    }

    @Test
    void deberiaResponderBadRequestCuandoCorreoNoEsValido() throws Exception {
        mockMvc.perform(post("/tutores")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Laura Tutor",
                                  "correo": "correo-invalido",
                                  "empresaId": 8,
                                  "cargo": "Lider de Desarrollo"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.datos.correo").exists());

        verify(tutorService, never()).crearTutor(any(), any());
    }

    @Test
    void deberiaListarTutoresFiltradosPorEmpresa() throws Exception {
        when(tutorService.listar(eq(8L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tutorResponse()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/tutores")
                        .param("empresaId", "8")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos.content[0].empresaId").value(8));
    }

    @Test
    void deberiaResponderNotFoundCuandoTutorNoExiste() throws Exception {
        when(tutorService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Tutor empresarial no encontrado: 99"));

        mockMvc.perform(get("/tutores/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Tutor empresarial no encontrado: 99"));
    }

    @Test
    void deberiaEditarTutorYResponderOk() throws Exception {
        when(tutorService.editarTutor(eq(5L), any(EditarTutorEmpresarialRequest.class), nullable(CustomUserDetails.class)))
                .thenReturn(tutorResponseEditado());

        mockMvc.perform(put("/tutores/{id}", 5L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Laura Tutor Editada",
                                  "telefono": "3110000000",
                                  "cargo": "Gerente TI",
                                  "area": "Tecnologia",
                                  "telefonoCorporativo": "6067654321",
                                  "esResponsablePrincipal": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Tutor actualizado"))
                .andExpect(jsonPath("$.datos.cargo").value("Gerente TI"));
    }

    @Test
    void deberiaResponderConflictCuandoEmpresaEstaDesactivada() throws Exception {
        when(tutorService.crearTutor(any(CrearTutorEmpresarialRequest.class), nullable(CustomUserDetails.class)))
                .thenThrow(new OperacionNoPermitidaException("No se pueden crear tutores para una empresa desactivada."));

        mockMvc.perform(post("/tutores")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Laura Tutor",
                                  "correo": "laura.tutor@empresa.com",
                                  "empresaId": 8,
                                  "cargo": "Lider de Desarrollo"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("No se pueden crear tutores para una empresa desactivada."));
    }

    @Test
    void deberiaDesactivarTutorYResponderOk() throws Exception {
        mockMvc.perform(patch("/tutores/{id}/desactivar", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Tutor desactivado"));

        verify(tutorService).desactivarTutor(eq(5L), nullable(CustomUserDetails.class));
    }

    private TutorEmpresarialResponse tutorResponse() {
        return TutorEmpresarialResponse.builder()
                .id(5L)
                .usuarioId(15L)
                .nombre("Laura Tutor")
                .correo("laura.tutor@empresa.com")
                .telefono("3001234567")
                .empresaId(8L)
                .empresaRazonSocial("Soft CUE SAS")
                .cargo("Lider de Desarrollo")
                .area("TI")
                .telefonoCorporativo("6061234567")
                .esResponsablePrincipal(true)
                .activo(true)
                .build();
    }

    private TutorEmpresarialResponse tutorResponseEditado() {
        TutorEmpresarialResponse response = tutorResponse();
        response.setNombre("Laura Tutor Editada");
        response.setCargo("Gerente TI");
        response.setArea("Tecnologia");
        response.setTelefono("3110000000");
        response.setTelefonoCorporativo("6067654321");
        response.setEsResponsablePrincipal(false);
        return response;
    }
}
