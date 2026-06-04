package co.edu.cue.practicas.service.practica;

import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.entity.Asignacion;
import co.edu.cue.practicas.model.entity.DocumentoPractica;
import co.edu.cue.practicas.model.entity.FirmaDocumento;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.entity.Vacante;
import co.edu.cue.practicas.model.enums.EstadoAsignacion;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoDocumento;
import co.edu.cue.practicas.model.enums.TipoFirmante;
import co.edu.cue.practicas.model.enums.TipoNotificacion;
import co.edu.cue.practicas.repository.asignacion.AsignacionRepository;
import co.edu.cue.practicas.repository.practica.DocumentoPracticaRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import co.edu.cue.practicas.service.asignacion.AsignacionService;
import co.edu.cue.practicas.service.notificacion.NotificacionSprint3Service;
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
class PracticaSprint3ServiceTest {

    @Mock
    private PracticaRepository practicaRepository;

    @Mock
    private AsignacionRepository asignacionRepository;

    @Mock
    private DocumentoPracticaRepository documentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AsignacionService asignacionService;

    @Mock
    private NotificacionSprint3Service notificacionService;

    private PracticaSprint3Service practicaService;

    @BeforeEach
    void configurar() {
        practicaService = new PracticaSprint3Service(practicaRepository, asignacionRepository, documentoRepository, usuarioRepository, asignacionService, notificacionService, new PracticaSprint3Mapper());
    }

    @Test
    void deberiaPrepararPracticaDesdeAsignacionAsignada() {
        Asignacion asignacion = asignacion(EstadoAsignacion.ASIGNADA);
        Usuario coordinador = usuario(30L, "Coordinador Practicas", Rol.COORDINADOR_PRACTICAS);
        when(asignacionService.buscar(99L)).thenReturn(asignacion);
        when(usuarioRepository.findById(30L)).thenReturn(Optional.of(coordinador));
        when(practicaRepository.existsByEstudiante_IdAndEstado(10L, EstadoPractica.EN_CURSO)).thenReturn(false);
        when(practicaRepository.save(any(Practica.class))).thenAnswer(invocation -> {
            Practica practica = invocation.getArgument(0);
            practica.setId(50L);
            return practica;
        });

        Map<String, Object> response = practicaService.prepararDesdeAsignacion(99L, 30L);

        assertThat(response.get("id")).isEqualTo(50L);
        assertThat(response.get("estado")).isEqualTo(EstadoPractica.PENDIENTE_ASIGNACION);
        assertThat(response.get("nombre")).isEqualTo("Desarrollador Java");
        assertThat(asignacion.getEstado()).isEqualTo(EstadoAsignacion.EN_VINCULACION);
        assertThat(asignacion.getFechaVinculacion()).isNotNull();
        verify(asignacionService).registrarCambio(
                eq(asignacion),
                eq(EstadoAsignacion.ASIGNADA),
                eq(EstadoAsignacion.EN_VINCULACION),
                eq("Practica preparada para documentos de vinculacion"),
                eq(coordinador));
    }

    @Test
    void deberiaRechazarPreparacionCuandoAsignacionNoEstaAsignada() {
        Asignacion asignacion = asignacion(EstadoAsignacion.EN_CURSO);
        Usuario coordinador = usuario(30L, "Coordinador Practicas", Rol.COORDINADOR_PRACTICAS);
        when(asignacionService.buscar(99L)).thenReturn(asignacion);
        when(usuarioRepository.findById(30L)).thenReturn(Optional.of(coordinador));

        assertThatThrownBy(() -> practicaService.prepararDesdeAsignacion(99L, 30L))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("ASIGNADA");

        verify(practicaRepository, never()).save(any());
        verify(asignacionService, never()).registrarCambio(any(), any(), any(), any(), any());
    }

    @Test
    void deberiaConfirmarVinculacionConConvenioFirmado() {
        Practica practica = practica(EstadoPractica.PENDIENTE_ASIGNACION);
        Asignacion asignacion = asignacion(EstadoAsignacion.EN_VINCULACION);
        Usuario usuario = usuario(30L, "Coordinador Practicas", Rol.COORDINADOR_PRACTICAS);
        Usuario docenteAsesor = usuario(40L, "Docente Asesor", Rol.DOCENTE_ASESOR);
        LocalDateTime inicio = LocalDateTime.parse("2026-06-10T08:00:00");
        LocalDateTime fin = LocalDateTime.parse("2026-12-10T17:00:00");
        when(practicaRepository.findById(50L)).thenReturn(Optional.of(practica));
        when(usuarioRepository.findById(30L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findById(40L)).thenReturn(Optional.of(docenteAsesor));
        when(asignacionRepository.findFirstByEstudiante_IdOrderByFechaAsignacionDesc(10L))
                .thenReturn(Optional.of(asignacion));
        when(documentoRepository.findByPractica_IdAndTipo(50L, TipoDocumento.CONVENIO))
                .thenReturn(List.of(convenioFirmado(practica)));
        when(practicaRepository.save(any(Practica.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> response = practicaService.confirmarVinculacion(50L, 30L, 40L, inicio, fin);

        assertThat(response.get("estado")).isEqualTo(EstadoPractica.EN_CURSO);
        assertThat(practica.getEstado()).isEqualTo(EstadoPractica.EN_CURSO);
        assertThat(asignacion.getEstado()).isEqualTo(EstadoAsignacion.EN_CURSO);
        assertThat(asignacion.getFechaInicio()).isEqualTo(inicio);
        assertThat(asignacion.getFechaFin()).isEqualTo(fin);
        verify(asignacionService).registrarCambio(
                eq(asignacion),
                eq(EstadoAsignacion.EN_VINCULACION),
                eq(EstadoAsignacion.EN_CURSO),
                eq("Vinculacion confirmada y practica activada"),
                eq(usuario));
        verify(notificacionService).registrar(
                eq(10L),
                eq(TipoNotificacion.VINCULACION_CONFIRMADA),
                eq("Practica en curso"),
                eq("Tu vinculacion fue confirmada. La practica queda EN_CURSO."),
                eq(99L),
                eq(50L));
    }

    @Test
    void deberiaRechazarConfirmacionSinConvenio() {
        Practica practica = practica(EstadoPractica.PENDIENTE_ASIGNACION);
        Asignacion asignacion = asignacion(EstadoAsignacion.EN_VINCULACION);
        Usuario usuario = usuario(30L, "Coordinador Practicas", Rol.COORDINADOR_PRACTICAS);
        when(practicaRepository.findById(50L)).thenReturn(Optional.of(practica));
        when(usuarioRepository.findById(30L)).thenReturn(Optional.of(usuario));
        when(asignacionRepository.findFirstByEstudiante_IdOrderByFechaAsignacionDesc(10L))
                .thenReturn(Optional.of(asignacion));
        when(documentoRepository.findByPractica_IdAndTipo(50L, TipoDocumento.CONVENIO))
                .thenReturn(List.of());

        assertThatThrownBy(() -> practicaService.confirmarVinculacion(50L, 30L, 40L, null, null))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("Debe existir un convenio");

        verify(practicaRepository, never()).save(any());
        verify(notificacionService, never()).registrar(any(), any(), any(), any(), any(), any());
    }

    @Test
    void deberiaRechazarConfirmacionConConvenioSinFirmasCompletas() {
        Practica practica = practica(EstadoPractica.PENDIENTE_ASIGNACION);
        Asignacion asignacion = asignacion(EstadoAsignacion.EN_VINCULACION);
        Usuario usuario = usuario(30L, "Coordinador Practicas", Rol.COORDINADOR_PRACTICAS);
        DocumentoPractica convenio = DocumentoPractica.builder()
                .id(70L)
                .practica(practica)
                .tipo(TipoDocumento.CONVENIO)
                .build();
        when(practicaRepository.findById(50L)).thenReturn(Optional.of(practica));
        when(usuarioRepository.findById(30L)).thenReturn(Optional.of(usuario));
        when(asignacionRepository.findFirstByEstudiante_IdOrderByFechaAsignacionDesc(10L))
                .thenReturn(Optional.of(asignacion));
        when(documentoRepository.findByPractica_IdAndTipo(50L, TipoDocumento.CONVENIO))
                .thenReturn(List.of(convenio));

        assertThatThrownBy(() -> practicaService.confirmarVinculacion(50L, 30L, 40L, null, null))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("3 firmas confirmadas");

        verify(practicaRepository, never()).save(any());
        verify(notificacionService, never()).registrar(any(), any(), any(), any(), any(), any());
    }

    private DocumentoPractica convenioFirmado(Practica practica) {
        DocumentoPractica convenio = DocumentoPractica.builder()
                .id(70L)
                .practica(practica)
                .tipo(TipoDocumento.CONVENIO)
                .build();
        convenio.setFirmas(List.of(
                firma(convenio, TipoFirmante.COORDINADOR, 30L),
                firma(convenio, TipoFirmante.TUTOR_EMPRESARIAL, 60L),
                firma(convenio, TipoFirmante.ESTUDIANTE, 10L)));
        return convenio;
    }

    private FirmaDocumento firma(DocumentoPractica documento, TipoFirmante tipo, Long usuarioId) {
        return FirmaDocumento.builder()
                .documento(documento)
                .tipo(tipo)
                .usuario(usuario(usuarioId, "Firmante " + usuarioId, Rol.ESTUDIANTE))
                .fechaFirma(LocalDateTime.now())
                .build();
    }

    private Asignacion asignacion(EstadoAsignacion estado) {
        return Asignacion.builder()
                .id(99L)
                .estudiante(usuario(10L, "Estudiante Uno", Rol.ESTUDIANTE))
                .coordinador(usuario(30L, "Coordinador Practicas", Rol.COORDINADOR_PRACTICAS))
                .vacante(Vacante.builder()
                        .id(20L)
                        .titulo("Desarrollador Java")
                        .descripcion("Construccion de servicios backend")
                        .programa(programa())
                        .build())
                .estado(estado)
                .build();
    }

    private Practica practica(EstadoPractica estado) {
        return Practica.builder()
                .id(50L)
                .estudiante(usuario(10L, "Estudiante Uno", Rol.ESTUDIANTE))
                .programa(programa())
                .numeroPractica(1)
                .nombre("Desarrollador Java")
                .estado(estado)
                .build();
    }

    private Programa programa() {
        return Programa.builder()
                .id(2L)
                .nombre("Ingenieria de Sistemas")
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
