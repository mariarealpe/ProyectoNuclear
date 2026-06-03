package co.edu.cue.practicas.service.seguimiento;

import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.model.entity.SeguimientoPractica;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.EstadoSeguimiento;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoNotificacion;
import co.edu.cue.practicas.repository.practica.SeguimientoPracticaRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import co.edu.cue.practicas.service.notificacion.NotificacionSprint3Service;
import co.edu.cue.practicas.service.practica.PracticaSprint3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeguimientoPracticaServiceTest {

    @Mock
    private SeguimientoPracticaRepository seguimientoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PracticaSprint3Service practicaService;

    @Mock
    private NotificacionSprint3Service notificacionService;

    private SeguimientoPracticaService seguimientoService;

    @BeforeEach
    void configurar() {
        seguimientoService = new SeguimientoPracticaService(
                seguimientoRepository,
                usuarioRepository,
                practicaService,
                notificacionService);
    }

    @Test
    void deberiaCrearSeguimientoCuandoPracticaEstaEnCursoYSemanaNoExiste() {
        Practica practica = practica(EstadoPractica.EN_CURSO);
        Usuario estudiante = practica.getEstudiante();
        when(practicaService.buscar(50L)).thenReturn(practica);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(estudiante));
        when(seguimientoRepository.findByPractica_IdAndSemana(50L, 3)).thenReturn(Optional.empty());
        when(seguimientoRepository.save(any(SeguimientoPractica.class))).thenAnswer(invocation -> {
            SeguimientoPractica seguimiento = invocation.getArgument(0);
            seguimiento.setId(120L);
            return seguimiento;
        });

        Map<String, Object> response = seguimientoService.crear(
                50L,
                10L,
                3,
                "Desarrollo API",
                "Modulo completado",
                "Sin bloqueos");

        assertThat(response.get("id")).isEqualTo(120L);
        assertThat(response.get("estado")).isEqualTo(EstadoSeguimiento.PENDIENTE);
        assertThat(response.get("version")).isEqualTo(1);
        assertThat(response.get("cargadoPorId")).isEqualTo(10L);
    }

    @Test
    void deberiaRechazarSeguimientoDuplicadoParaLaSemana() {
        Practica practica = practica(EstadoPractica.EN_CURSO);
        Usuario estudiante = practica.getEstudiante();
        when(practicaService.buscar(50L)).thenReturn(practica);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(estudiante));
        when(seguimientoRepository.findByPractica_IdAndSemana(50L, 3))
                .thenReturn(Optional.of(seguimiento(EstadoSeguimiento.PENDIENTE, 3)));

        assertThatThrownBy(() -> seguimientoService.crear(
                50L,
                10L,
                3,
                "Desarrollo API",
                "Modulo completado",
                null))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("Ya existe seguimiento");

        verify(seguimientoRepository, never()).save(any());
    }

    @Test
    void deberiaAprobarSeguimientoConDocenteAsesor() {
        SeguimientoPractica seguimiento = seguimiento(EstadoSeguimiento.PENDIENTE, 3);
        Usuario docente = usuario(40L, "Docente Asesor", Rol.DOCENTE_ASESOR);
        when(seguimientoRepository.findById(120L)).thenReturn(Optional.of(seguimiento));
        when(usuarioRepository.findById(40L)).thenReturn(Optional.of(docente));
        when(seguimientoRepository.save(any(SeguimientoPractica.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> response = seguimientoService.revisar(
                120L,
                40L,
                EstadoSeguimiento.APROBADO,
                "Buen avance");

        assertThat(response.get("estado")).isEqualTo(EstadoSeguimiento.APROBADO);
        assertThat(response.get("docenteAsesorId")).isEqualTo(40L);
        assertThat(response.get("observacionesDocente")).isEqualTo("Buen avance");
        assertThat(seguimiento.getFechaRevision()).isNotNull();
        verify(notificacionService, never()).registrar(any(), any(), any(), any(), any(), any());
    }

    @Test
    void deberiaRechazarSeguimientoYNotificarAlEstudiante() {
        SeguimientoPractica seguimiento = seguimiento(EstadoSeguimiento.PENDIENTE, 3);
        Usuario docente = usuario(40L, "Docente Asesor", Rol.DOCENTE_ASESOR);
        when(seguimientoRepository.findById(120L)).thenReturn(Optional.of(seguimiento));
        when(usuarioRepository.findById(40L)).thenReturn(Optional.of(docente));
        when(seguimientoRepository.save(any(SeguimientoPractica.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> response = seguimientoService.revisar(
                120L,
                40L,
                EstadoSeguimiento.RECHAZADO,
                "Debe agregar evidencias");

        assertThat(response.get("estado")).isEqualTo(EstadoSeguimiento.RECHAZADO);
        verify(notificacionService).registrar(
                eq(10L),
                eq(TipoNotificacion.SEGUIMIENTO_RECHAZADO),
                eq("Seguimiento rechazado"),
                eq("Debe agregar evidencias"),
                isNull(),
                eq(50L));
    }

    @Test
    void deberiaRechazarRevisionPendiente() {
        SeguimientoPractica seguimiento = seguimiento(EstadoSeguimiento.PENDIENTE, 3);
        Usuario docente = usuario(40L, "Docente Asesor", Rol.DOCENTE_ASESOR);
        when(seguimientoRepository.findById(120L)).thenReturn(Optional.of(seguimiento));
        when(usuarioRepository.findById(40L)).thenReturn(Optional.of(docente));

        assertThatThrownBy(() -> seguimientoService.revisar(
                120L,
                40L,
                EstadoSeguimiento.PENDIENTE,
                null))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("aprobar o rechazar");

        verify(seguimientoRepository, never()).save(any());
    }

    @Test
    void deberiaCorregirSeguimientoRechazadoDeSemanaReciente() {
        SeguimientoPractica seguimiento = seguimiento(EstadoSeguimiento.RECHAZADO, 3);
        seguimiento.setObservacionesDocente("Debe ampliar actividades");
        seguimiento.setDocenteAsesor(usuario(40L, "Docente Asesor", Rol.DOCENTE_ASESOR));
        seguimiento.setFechaRevision(LocalDateTime.now());
        when(seguimientoRepository.findById(120L)).thenReturn(Optional.of(seguimiento));
        when(seguimientoRepository.findFirstByPractica_IdOrderBySemanaDesc(50L))
                .thenReturn(Optional.of(seguimiento));
        when(seguimientoRepository.save(any(SeguimientoPractica.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> response = seguimientoService.corregirRechazado(
                120L,
                "Actividades corregidas",
                "Logros corregidos",
                null);

        assertThat(response.get("estado")).isEqualTo(EstadoSeguimiento.PENDIENTE);
        assertThat(response.get("version")).isEqualTo(2);
        assertThat(response.get("observacionesDocente")).isNull();
        assertThat(response.get("docenteAsesorId")).isNull();
    }

    @Test
    void deberiaRechazarCorreccionSiNoEsSemanaReciente() {
        SeguimientoPractica seguimiento = seguimiento(EstadoSeguimiento.RECHAZADO, 2);
        SeguimientoPractica semanaReciente = seguimiento(EstadoSeguimiento.PENDIENTE, 3);
        when(seguimientoRepository.findById(120L)).thenReturn(Optional.of(seguimiento));
        when(seguimientoRepository.findFirstByPractica_IdOrderBySemanaDesc(50L))
                .thenReturn(Optional.of(semanaReciente));

        assertThatThrownBy(() -> seguimientoService.corregirRechazado(
                120L,
                "Actividades corregidas",
                "Logros corregidos",
                null))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("semana mas reciente");

        verify(seguimientoRepository, never()).save(any());
    }

    @Test
    void deberiaListarSeguimientosPorPracticaOrdenados() {
        when(seguimientoRepository.findByPractica_IdOrderBySemanaAsc(50L))
                .thenReturn(List.of(seguimiento(EstadoSeguimiento.PENDIENTE, 1)));

        List<Map<String, Object>> response = seguimientoService.listarPorPractica(50L);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().get("semana")).isEqualTo(1);
        verify(seguimientoRepository).findByPractica_IdOrderBySemanaAsc(50L);
    }

    private SeguimientoPractica seguimiento(EstadoSeguimiento estado, Integer semana) {
        return SeguimientoPractica.builder()
                .id(120L)
                .practica(practica(EstadoPractica.EN_CURSO))
                .semana(semana)
                .actividades("Desarrollo API")
                .logros("Modulo completado")
                .dificultades("Sin bloqueos")
                .estado(estado)
                .cargadoPor(usuario(10L, "Estudiante Uno", Rol.ESTUDIANTE))
                .build();
    }

    private Practica practica(EstadoPractica estado) {
        return Practica.builder()
                .id(50L)
                .estudiante(usuario(10L, "Estudiante Uno", Rol.ESTUDIANTE))
                .programa(Programa.builder().id(2L).nombre("Ingenieria de Sistemas").build())
                .numeroPractica(1)
                .nombre("Practica de desarrollo")
                .estado(estado)
                .build();
    }

    private Usuario usuario(Long id, String nombre, Rol rol) {
        return Usuario.builder()
                .id(id)
                .nombre(nombre)
                .correo("usuario" + id + "@cue.edu.co")
                .passwordHash("hash")
                .rol(rol)
                .build();
    }
}
