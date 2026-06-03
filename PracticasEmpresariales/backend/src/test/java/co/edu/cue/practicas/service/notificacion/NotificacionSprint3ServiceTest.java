package co.edu.cue.practicas.service.notificacion;

import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.Asignacion;
import co.edu.cue.practicas.model.entity.NotificacionHistorial;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoNotificacion;
import co.edu.cue.practicas.model.enums.TipoNotificacion;
import co.edu.cue.practicas.repository.asignacion.AsignacionRepository;
import co.edu.cue.practicas.repository.notificacion.NotificacionHistorialRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionSprint3ServiceTest {

    @Mock
    private NotificacionHistorialRepository notificacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AsignacionRepository asignacionRepository;

    @Mock
    private PracticaRepository practicaRepository;

    private NotificacionSprint3Service notificacionService;

    @BeforeEach
    void configurar() {
        notificacionService = new NotificacionSprint3Service(
                notificacionRepository,
                usuarioRepository,
                asignacionRepository,
                practicaRepository);
    }

    @Test
    void deberiaRegistrarNotificacionPendienteConContexto() {
        Usuario usuario = usuario();
        Asignacion asignacion = Asignacion.builder().id(99L).build();
        Practica practica = Practica.builder().id(50L).build();
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(asignacionRepository.findById(99L)).thenReturn(Optional.of(asignacion));
        when(practicaRepository.findById(50L)).thenReturn(Optional.of(practica));
        when(notificacionRepository.save(any(NotificacionHistorial.class))).thenAnswer(invocation -> {
            NotificacionHistorial notificacion = invocation.getArgument(0);
            notificacion.setId(77L);
            return notificacion;
        });

        Map<String, Object> response = notificacionService.registrar(
                10L,
                TipoNotificacion.VINCULACION_CONFIRMADA,
                "Practica en curso",
                "La vinculacion fue confirmada",
                99L,
                50L);

        assertThat(response.get("id")).isEqualTo(77L);
        assertThat(response.get("correoDestino")).isEqualTo("estudiante@cue.edu.co");
        assertThat(response.get("estado")).isEqualTo(EstadoNotificacion.PENDIENTE);
        assertThat(response.get("asignacionId")).isEqualTo(99L);
        assertThat(response.get("practicaId")).isEqualTo(50L);

        ArgumentCaptor<NotificacionHistorial> captor = ArgumentCaptor.forClass(NotificacionHistorial.class);
        verify(notificacionRepository).save(captor.capture());
        assertThat(captor.getValue().getUsuarioDestino()).isEqualTo(usuario);
        assertThat(captor.getValue().getReintentos()).isZero();
    }

    @Test
    void deberiaLanzarNoEncontradoCuandoUsuarioDestinoNoExiste() {
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificacionService.registrar(
                404L,
                TipoNotificacion.OTRO,
                "Asunto",
                "Cuerpo",
                null,
                null))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Usuario destino no encontrado");

        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void deberiaMarcarEnviadaLimpiandoErroresYReintentos() {
        NotificacionHistorial notificacion = notificacion(EstadoNotificacion.FALLIDO);
        notificacion.setErrorMensaje("SMTP no disponible");
        notificacion.setProxReintento(LocalDateTime.now().plusMinutes(5));
        when(notificacionRepository.findById(77L)).thenReturn(Optional.of(notificacion));
        when(notificacionRepository.save(any(NotificacionHistorial.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> response = notificacionService.marcarEnviada(77L);

        assertThat(response.get("estado")).isEqualTo(EstadoNotificacion.ENVIADO);
        assertThat(response.get("errorMensaje")).isNull();
        assertThat(response.get("proxReintento")).isNull();
    }

    @Test
    void deberiaMarcarFallidaRegistrandoReintentoConMotivoPorDefecto() {
        NotificacionHistorial notificacion = notificacion(EstadoNotificacion.PENDIENTE);
        when(notificacionRepository.findById(77L)).thenReturn(Optional.of(notificacion));
        when(notificacionRepository.save(any(NotificacionHistorial.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> response = notificacionService.marcarFallida(77L, null);

        assertThat(response.get("estado")).isEqualTo(EstadoNotificacion.FALLIDO);
        assertThat(response.get("reintentos")).isEqualTo(1);
        assertThat(response.get("errorMensaje")).isEqualTo("Fallo registrado desde Postman");
        assertThat(response.get("proxReintento")).isNotNull();
    }

    @Test
    void deberiaListarPendientes() {
        when(notificacionRepository.findByEstado(EstadoNotificacion.PENDIENTE))
                .thenReturn(List.of(notificacion(EstadoNotificacion.PENDIENTE)));

        List<Map<String, Object>> response = notificacionService.listarPendientes();

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().get("estado")).isEqualTo(EstadoNotificacion.PENDIENTE);
        verify(notificacionRepository).findByEstado(EstadoNotificacion.PENDIENTE);
    }

    private NotificacionHistorial notificacion(EstadoNotificacion estado) {
        return NotificacionHistorial.builder()
                .id(77L)
                .usuarioDestino(usuario())
                .tipo(TipoNotificacion.ASIGNACION_CREADA)
                .correoDestino("estudiante@cue.edu.co")
                .asunto("Nueva asignacion")
                .cuerpo("Tiene una nueva asignacion")
                .estado(estado)
                .build();
    }

    private Usuario usuario() {
        return Usuario.builder()
                .id(10L)
                .nombre("Estudiante Uno")
                .correo("estudiante@cue.edu.co")
                .passwordHash("hash")
                .build();
    }
}
