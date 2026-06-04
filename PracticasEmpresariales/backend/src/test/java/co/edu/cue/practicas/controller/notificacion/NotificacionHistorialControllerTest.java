package co.edu.cue.practicas.controller.notificacion;

import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.enums.EstadoNotificacion;
import co.edu.cue.practicas.model.enums.TipoNotificacion;
import co.edu.cue.practicas.service.notificacion.NotificacionSprint3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
class NotificacionHistorialControllerTest {

    @Mock
    private NotificacionSprint3Service notificacionService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new NotificacionHistorialController(notificacionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void deberiaRegistrarNotificacionYResponderCreated() throws Exception {
        when(notificacionService.registrar(
                10L,
                TipoNotificacion.ASIGNACION_CREADA,
                "Nueva asignacion",
                "Tiene una nueva asignacion",
                99L,
                null))
                .thenReturn(notificacionMap());

        mockMvc.perform(post("/notificaciones")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioDestinoId": 10,
                                  "tipo": "ASIGNACION_CREADA",
                                  "asunto": "Nueva asignacion",
                                  "cuerpo": "Tiene una nueva asignacion",
                                  "asignacionId": 99
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Notificacion registrada"))
                .andExpect(jsonPath("$.datos.id").value(77))
                .andExpect(jsonPath("$.datos.estado").value("PENDIENTE"));
    }

    @Test
    void deberiaMarcarFallidaSinBodyYResponderOk() throws Exception {
        Map<String, Object> fallida = Map.of(
                "id", 77L,
                "estado", EstadoNotificacion.FALLIDO,
                "errorMensaje", "Fallo registrado desde Postman");
        when(notificacionService.marcarFallida(77L, null)).thenReturn(fallida);

        mockMvc.perform(patch("/notificaciones/{id}/fallida", 77L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Notificacion marcada como fallida"))
                .andExpect(jsonPath("$.datos.estado").value("FALLIDO"));

        verify(notificacionService).marcarFallida(77L, null);
    }

    @Test
    void deberiaResponderNotFoundCuandoNoExisteAlMarcarEnviada() throws Exception {
        when(notificacionService.marcarEnviada(404L))
                .thenThrow(new RecursoNoEncontradoException("Notificacion no encontrada"));

        mockMvc.perform(patch("/notificaciones/{id}/enviada", 404L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Notificacion no encontrada"));
    }

    @Test
    void deberiaListarNotificacionesPorUsuario() throws Exception {
        when(notificacionService.listarPorUsuario(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notificacionMap()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/notificaciones/usuario/{usuarioId}", 10L)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos.content[0].correoDestino").value("estudiante@cue.edu.co"));
    }

    @Test
    void deberiaListarPendientesYReintentos() throws Exception {
        when(notificacionService.listarPendientes()).thenReturn(List.of(notificacionMap()));
        when(notificacionService.listarReintentosPendientes())
                .thenReturn(List.of(Map.of("id", 78L, "estado", EstadoNotificacion.FALLIDO)));

        mockMvc.perform(get("/notificaciones/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos[0].estado").value("PENDIENTE"));

        mockMvc.perform(get("/notificaciones/reintentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos[0].estado").value("FALLIDO"));
    }

    @Test
    void deberiaResponderErrorInternoCuandoTipoNoExiste() throws Exception {
        mockMvc.perform(post("/notificaciones")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioDestinoId": 10,
                                  "tipo": "NO_EXISTE",
                                  "asunto": "Nueva asignacion",
                                  "cuerpo": "Tiene una nueva asignacion"
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.exitoso").value(false));

        verify(notificacionService, never()).registrar(any(), any(), any(), any(), isNull(), isNull());
    }

    private Map<String, Object> notificacionMap() {
        return Map.of(
                "id", 77L,
                "usuarioDestinoId", 10L,
                "correoDestino", "estudiante@cue.edu.co",
                "tipo", TipoNotificacion.ASIGNACION_CREADA,
                "asunto", "Nueva asignacion",
                "estado", EstadoNotificacion.PENDIENTE,
                "reintentos", 0,
                "asignacionId", 99L);
    }
}
