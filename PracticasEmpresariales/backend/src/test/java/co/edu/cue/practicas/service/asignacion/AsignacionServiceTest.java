package co.edu.cue.practicas.service.asignacion;

import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.entity.Asignacion;
import co.edu.cue.practicas.model.entity.CambioEstadoAsignacion;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.entity.Vacante;
import co.edu.cue.practicas.model.enums.EstadoAsignacion;
import co.edu.cue.practicas.model.enums.EstadoEstudiante;
import co.edu.cue.practicas.model.enums.EstadoVacante;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoNotificacion;
import co.edu.cue.practicas.repository.asignacion.AsignacionRepository;
import co.edu.cue.practicas.repository.asignacion.CambioEstadoAsignacionRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import co.edu.cue.practicas.repository.vacante.VacanteRepository;
import co.edu.cue.practicas.service.notificacion.NotificacionSprint3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsignacionServiceTest {

    @Mock
    private AsignacionRepository asignacionRepository;

    @Mock
    private CambioEstadoAsignacionRepository cambioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VacanteRepository vacanteRepository;

    @Mock
    private NotificacionSprint3Service notificacionService;

    private AsignacionService asignacionService;

    @BeforeEach
    void configurar() {
        asignacionService = new AsignacionService(
                asignacionRepository,
                cambioRepository,
                usuarioRepository,
                vacanteRepository,
                notificacionService);
    }

    @Test
    void deberiaCrearAsignacionCuandoEstudianteAptoYVacanteDisponible() {
        Usuario estudiante = estudiante(EstadoEstudiante.APTO);
        Usuario coordinador = usuario(30L, "Coordinador Practicas", Rol.COORDINADOR_PRACTICAS);
        Vacante vacante = vacante(EstadoVacante.DISPONIBLE, 1, 0);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(estudiante));
        when(usuarioRepository.findById(30L)).thenReturn(Optional.of(coordinador));
        when(vacanteRepository.findById(20L)).thenReturn(Optional.of(vacante));
        when(asignacionRepository.existsByEstudiante_IdAndEstadoIn(eq(10L), any())).thenReturn(false);
        when(asignacionRepository.save(any(Asignacion.class))).thenAnswer(invocation -> {
            Asignacion asignacion = invocation.getArgument(0);
            asignacion.setId(99L);
            return asignacion;
        });

        Map<String, Object> response = asignacionService.crear(10L, 20L, 30L);

        assertThat(response.get("id")).isEqualTo(99L);
        assertThat(response.get("estado")).isEqualTo(EstadoAsignacion.ASIGNADA);
        assertThat(vacante.getCuposOcupados()).isEqualTo(1);
        assertThat(vacante.getEstado()).isEqualTo(EstadoVacante.CERRADA);
        verify(cambioRepository).save(any(CambioEstadoAsignacion.class));
        verify(notificacionService).registrar(
                eq(10L),
                eq(TipoNotificacion.ASIGNACION_CREADA),
                eq("Nueva asignacion de practica"),
                contains("Desarrollador Java"),
                eq(99L),
                isNull());
    }

    @Test
    void deberiaRechazarCreacionCuandoEstudianteNoEstaApto() {
        Usuario estudiante = estudiante(EstadoEstudiante.NO_APTO);
        Usuario coordinador = usuario(30L, "Coordinador Practicas", Rol.COORDINADOR_PRACTICAS);
        Vacante vacante = vacante(EstadoVacante.DISPONIBLE, 2, 0);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(estudiante));
        when(usuarioRepository.findById(30L)).thenReturn(Optional.of(coordinador));
        when(vacanteRepository.findById(20L)).thenReturn(Optional.of(vacante));

        assertThatThrownBy(() -> asignacionService.crear(10L, 20L, 30L))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("APTO");

        verify(asignacionRepository, never()).save(any());
        verify(cambioRepository, never()).save(any());
        verify(notificacionService, never()).registrar(any(), any(), any(), any(), any(), any());
    }

    @Test
    void deberiaCancelarAsignacionLiberandoCupoYNotificando() {
        Asignacion asignacion = asignacion(EstadoAsignacion.ASIGNADA);
        asignacion.getVacante().setEstado(EstadoVacante.CERRADA);
        asignacion.getVacante().setCupos(2);
        asignacion.getVacante().setCuposOcupados(2);
        Usuario usuario = usuario(30L, "Coordinador Practicas", Rol.COORDINADOR_PRACTICAS);
        when(asignacionRepository.findById(99L)).thenReturn(Optional.of(asignacion));
        when(usuarioRepository.findById(30L)).thenReturn(Optional.of(usuario));
        when(asignacionRepository.save(any(Asignacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> response = asignacionService.cambiarEstado(
                99L,
                EstadoAsignacion.CANCELADA,
                30L,
                "La empresa cancelo la vacante");

        assertThat(response.get("estado")).isEqualTo(EstadoAsignacion.CANCELADA);
        assertThat(response.get("motivoCancelacion")).isEqualTo("La empresa cancelo la vacante");
        assertThat(asignacion.getVacante().getCuposOcupados()).isEqualTo(1);
        assertThat(asignacion.getVacante().getEstado()).isEqualTo(EstadoVacante.DISPONIBLE);
        verify(cambioRepository).save(any(CambioEstadoAsignacion.class));
        verify(notificacionService).registrar(
                eq(10L),
                eq(TipoNotificacion.ASIGNACION_CANCELADA),
                eq("Asignacion cancelada"),
                eq("La empresa cancelo la vacante"),
                eq(99L),
                isNull());
    }

    @Test
    void deberiaRechazarTransicionNoPermitida() {
        Asignacion asignacion = asignacion(EstadoAsignacion.EN_VINCULACION);
        Usuario usuario = usuario(30L, "Coordinador Practicas", Rol.COORDINADOR_PRACTICAS);
        when(asignacionRepository.findById(99L)).thenReturn(Optional.of(asignacion));
        when(usuarioRepository.findById(30L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> asignacionService.cambiarEstado(
                99L,
                EstadoAsignacion.CANCELADA,
                30L,
                "Cancelacion tardia"))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("Transicion de estado no permitida");

        verify(asignacionRepository, never()).save(any());
        verify(cambioRepository, never()).save(any());
        verify(notificacionService, never()).registrar(any(), any(), any(), any(), any(), any());
    }

    @Test
    void deberiaListarPorCoordinadorYEstado() {
        Asignacion asignacion = asignacion(EstadoAsignacion.ASIGNADA);
        PageRequest pageable = PageRequest.of(0, 10);
        when(asignacionRepository.findByCoordinador_IdAndEstado(30L, EstadoAsignacion.ASIGNADA, pageable))
                .thenReturn(new PageImpl<>(List.of(asignacion), pageable, 1));

        Page<Map<String, Object>> response = asignacionService.listar(
                EstadoAsignacion.ASIGNADA,
                30L,
                pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().get("vacante")).isEqualTo("Desarrollador Java");
        verify(asignacionRepository).findByCoordinador_IdAndEstado(30L, EstadoAsignacion.ASIGNADA, pageable);
    }

    private Asignacion asignacion(EstadoAsignacion estado) {
        return Asignacion.builder()
                .id(99L)
                .estudiante(estudiante(EstadoEstudiante.APTO))
                .coordinador(usuario(30L, "Coordinador Practicas", Rol.COORDINADOR_PRACTICAS))
                .vacante(vacante(EstadoVacante.DISPONIBLE, 2, 1))
                .estado(estado)
                .build();
    }

    private Usuario estudiante(EstadoEstudiante estado) {
        return usuario(10L, "Estudiante Uno", Rol.ESTUDIANTE, estado);
    }

    private Usuario usuario(Long id, String nombre, Rol rol) {
        return usuario(id, nombre, rol, EstadoEstudiante.NO_APTO);
    }

    private Usuario usuario(Long id, String nombre, Rol rol, EstadoEstudiante estadoEstudiante) {
        return Usuario.builder()
                .id(id)
                .nombre(nombre)
                .correo("usuario" + id + "@cue.edu.co")
                .passwordHash("hash")
                .rol(rol)
                .estadoEstudiante(estadoEstudiante)
                .activo(true)
                .build();
    }

    private Vacante vacante(EstadoVacante estado, int cupos, int cuposOcupados) {
        return Vacante.builder()
                .id(20L)
                .titulo("Desarrollador Java")
                .descripcion("Construccion de servicios backend")
                .estado(estado)
                .cupos(cupos)
                .cuposOcupados(cuposOcupados)
                .build();
    }
}
