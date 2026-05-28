package co.edu.cue.practicas.controller.usuario;

import co.edu.cue.practicas.dto.request.CrearUsuarioRequest;
import co.edu.cue.practicas.dto.response.UsuarioResponse;
import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.enums.EstadoEstudiante;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.usuario.UsuarioService;
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
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UsuarioController(usuarioService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    void deberiaCrearUsuarioYResponderCreated() throws Exception {
        UsuarioResponse response = usuarioResponse();
        when(usuarioService.crearUsuario(any(CrearUsuarioRequest.class), nullable(CustomUserDetails.class)))
                .thenReturn(response);

        mockMvc.perform(post("/usuarios")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Usuario Nuevo",
                                  "correo": "nuevo@cue.edu.co",
                                  "rol": "ESTUDIANTE",
                                  "telefono": "3001234567",
                                  "facultadId": 1,
                                  "programaId": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Usuario creado exitosamente"))
                .andExpect(jsonPath("$.datos.id").value(10))
                .andExpect(jsonPath("$.datos.estadoEstudiante").value("NO_APTO"));
    }

    @Test
    void deberiaResponderBadRequestCuandoCrearUsuarioNoTieneNombre() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": "nuevo@cue.edu.co",
                                  "rol": "ESTUDIANTE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.datos.nombre").exists());

        verify(usuarioService, never()).crearUsuario(any(), any());
    }

    @Test
    void deberiaListarUsuariosPaginados() throws Exception {
        when(usuarioService.listarUsuarios(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(usuarioResponse()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/usuarios")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos.content[0].correo").value("nuevo@cue.edu.co"));
    }

    @Test
    void deberiaResponderNotFoundCuandoUsuarioNoExiste() throws Exception {
        when(usuarioService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Usuario no encontrado con id: 99"));

        mockMvc.perform(get("/usuarios/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Usuario no encontrado con id: 99"));
    }

    @Test
    void deberiaResponderConflictCuandoNoSePuedeDesactivarUsuario() throws Exception {
        doThrow(new OperacionNoPermitidaException("No se puede desactivar al unico Administrador DTI activo"))
                .when(usuarioService)
                .desactivarUsuario(eq(1L), nullable(CustomUserDetails.class));

        mockMvc.perform(patch("/usuarios/{id}/desactivar", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("No se puede desactivar al unico Administrador DTI activo"));
    }

    private UsuarioResponse usuarioResponse() {
        return UsuarioResponse.builder()
                .id(10L)
                .nombre("Usuario Nuevo")
                .correo("nuevo@cue.edu.co")
                .telefono("3001234567")
                .rol(Rol.ESTUDIANTE)
                .activo(true)
                .primerIngreso(true)
                .facultadId(1L)
                .facultadNombre("Ingenieria")
                .programaId(2L)
                .programaNombre("Sistemas")
                .estadoEstudiante(EstadoEstudiante.NO_APTO)
                .build();
    }
}
