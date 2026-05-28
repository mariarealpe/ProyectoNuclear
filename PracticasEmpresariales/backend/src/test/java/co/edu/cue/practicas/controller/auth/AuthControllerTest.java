package co.edu.cue.practicas.controller.auth;

import co.edu.cue.practicas.dto.request.CambiarPasswordRequest;
import co.edu.cue.practicas.dto.request.LoginRequest;
import co.edu.cue.practicas.dto.response.LoginResponse;
import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.auth.AuthService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    void deberiaResponderOkCuandoLoginEsExitoso() throws Exception {
        LoginResponse response = LoginResponse.builder()
                .token("jwt-token")
                .tipo("Bearer")
                .usuarioId(1L)
                .nombre("Admin DTI")
                .correo("admin@cue.edu.co")
                .rol(Rol.ADMIN_DTI)
                .primerIngreso(false)
                .build();
        when(authService.login(any(LoginRequest.class), anyString())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": "admin@cue.edu.co",
                                  "password": "Admin2026!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Login exitoso"))
                .andExpect(jsonPath("$.datos.token").value("jwt-token"))
                .andExpect(jsonPath("$.datos.rol").value("ADMIN_DTI"));

        verify(authService).login(any(LoginRequest.class), anyString());
    }

    @Test
    void deberiaResponderBadRequestCuandoCorreoTieneFormatoInvalido() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": "correo-invalido",
                                  "password": "Admin2026!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.datos.correo").exists());
    }

    @Test
    void deberiaResponderForbiddenCuandoCredencialesSonInvalidas() throws Exception {
        when(authService.login(any(LoginRequest.class), anyString()))
                .thenThrow(new AccesoNoAutorizadoException("Credenciales incorrectas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": "admin@cue.edu.co",
                                  "password": "bad"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Credenciales incorrectas"));
    }

    @Test
    void deberiaResponderOkCuandoCambioPasswordEsValido() throws Exception {
        mockMvc.perform(post("/auth/cambiar-password")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "passwordActual": "Actual123",
                                  "passwordNueva": "Nueva12345",
                                  "passwordConfirmacion": "Nueva12345"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").exists());

        verify(authService).cambiarPassword(
                any(CambiarPasswordRequest.class),
                nullable(CustomUserDetails.class));
    }

    @Test
    void deberiaResponderBadRequestCuandoNuevaPasswordEsMuyCorta() throws Exception {
        mockMvc.perform(post("/auth/cambiar-password")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "passwordActual": "Actual123",
                                  "passwordNueva": "corta",
                                  "passwordConfirmacion": "corta"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.datos.passwordNueva").exists());
    }
}
