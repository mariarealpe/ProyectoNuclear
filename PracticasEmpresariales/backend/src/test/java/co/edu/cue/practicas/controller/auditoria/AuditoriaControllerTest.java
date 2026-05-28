package co.edu.cue.practicas.controller.auditoria;

import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.model.entity.BitacoraAuditoria;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoAccion;
import co.edu.cue.practicas.repository.auditoria.BitacoraAuditoriaRepository;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuditoriaControllerTest {

    @Mock
    private BitacoraAuditoriaRepository bitacoraRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuditoriaController(bitacoraRepository))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    void deberiaFiltrarBitacoraYResponderPaginaDeResultados() throws Exception {
        BitacoraAuditoria entrada = BitacoraAuditoria.builder()
                .nombreUsuario("Admin DTI")
                .rolUsuario(Rol.ADMIN_DTI)
                .modulo("AUTH")
                .tipoAccion(TipoAccion.LOGIN_EXITOSO)
                .ipOrigen("127.0.0.1")
                .exitoso(true)
                .build();
        when(bitacoraRepository.filtrar(
                eq(1L),
                eq(TipoAccion.LOGIN_EXITOSO),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq("AUTH"),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entrada), PageRequest.of(0, 50), 1));

        mockMvc.perform(get("/auditoria")
                        .param("usuarioId", "1")
                        .param("tipoAccion", "LOGIN_EXITOSO")
                        .param("desde", "2026-05-01T00:00:00")
                        .param("hasta", "2026-05-31T23:59:59")
                        .param("modulo", "AUTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos.content[0].nombreUsuario").value("Admin DTI"))
                .andExpect(jsonPath("$.datos.content[0].tipoAccion").value("LOGIN_EXITOSO"));
    }
}
