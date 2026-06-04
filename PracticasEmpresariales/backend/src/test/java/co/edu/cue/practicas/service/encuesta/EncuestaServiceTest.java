package co.edu.cue.practicas.service.encuesta;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.GuardarRespuestasEncuestaRequest;
import co.edu.cue.practicas.dto.response.EncuestaResponse;
import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.Encuesta;
import co.edu.cue.practicas.model.entity.NotificacionHistorial;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoEncuesta;
import co.edu.cue.practicas.model.enums.TipoEncuesta;
import co.edu.cue.practicas.model.enums.TipoEventoNotificacion;
import co.edu.cue.practicas.repository.encuesta.EncuestaRepository;
import co.edu.cue.practicas.repository.notificacion.NotificacionHistorialRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.notificacion.PlantillaNotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncuestaServiceTest {

    @Mock private EncuestaRepository encuestaRepository;
    @Mock private PracticaRepository practicaRepository;
    @Mock private NotificacionHistorialRepository notificacionRepository;
    @Mock private PlantillaNotificacionService plantillaService;
    @Mock private AuditoriaLogger auditoriaLogger;

    private EncuestaService service;

    private Usuario estudiante;
    private Usuario tutor;
    private Practica practica;
    private CustomUserDetails tutorDetails;
    private CustomUserDetails estudianteDetails;

    @BeforeEach
    void configurar() {
        service = new EncuestaService(
                encuestaRepository, practicaRepository, notificacionRepository,
                plantillaService, auditoriaLogger);

        var facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        var programa = DatosDePrueba.programa(2L, "Sistemas", facultad);
        var docente  = DatosDePrueba.docenteAsesor();
        tutor        = DatosDePrueba.tutorEmpresarial();
        estudiante   = DatosDePrueba.usuario(5L, "Estudiante A", "est@cue.edu.co",
                co.edu.cue.practicas.model.enums.Rol.ESTUDIANTE);
        practica     = DatosDePrueba.practica(7L, estudiante, programa, docente, tutor);
        tutorDetails      = DatosDePrueba.userDetails(tutor);
        estudianteDetails = DatosDePrueba.userDetails(estudiante);
    }

    // =========================================================================
    // iniciarFaseCierre()
    // =========================================================================

    @Test
    void deberiaCrearTresEncuestasAlIniciarFaseDeCierre() {
        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(encuestaRepository.findByPractica_IdAndTipo(any(), any())).thenReturn(Optional.empty());
        when(encuestaRepository.save(any(Encuesta.class))).thenAnswer(inv -> {
            Encuesta e = inv.getArgument(0);
            e.setId((long) (Math.random() * 1000));
            return e;
        });
        when(plantillaService.renderizarParaEvento(any(), any()))
                .thenReturn(new PlantillaNotificacionService.RenderResult(
                        "asunto", "<p>cuerpo</p>", true, 3));

        List<EncuestaResponse> creadas = service.iniciarFaseCierre(7L);

        assertThat(creadas).hasSize(3);
        assertThat(creadas.stream().map(EncuestaResponse::getTipo).toList())
                .containsExactlyInAnyOrder(
                        TipoEncuesta.TUTOR_SATISFACCION,
                        TipoEncuesta.ESTUDIANTE_SATISFACCION,
                        TipoEncuesta.ESTUDIANTE_AUTOEVALUACION);
        verify(notificacionRepository, org.mockito.Mockito.times(3))
                .save(any(NotificacionHistorial.class));
    }

    @Test
    void deberiaSerIdempotenteAlIniciarFaseDeCierre() {
        Encuesta existente = Encuesta.builder()
                .id(99L).practica(practica).tipo(TipoEncuesta.TUTOR_SATISFACCION)
                .destinatario(tutor).estado(EstadoEncuesta.PENDIENTE).build();

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(encuestaRepository.findByPractica_IdAndTipo(7L, TipoEncuesta.TUTOR_SATISFACCION))
                .thenReturn(Optional.of(existente));
        when(encuestaRepository.findByPractica_IdAndTipo(7L, TipoEncuesta.ESTUDIANTE_SATISFACCION))
                .thenReturn(Optional.empty());
        when(encuestaRepository.findByPractica_IdAndTipo(7L, TipoEncuesta.ESTUDIANTE_AUTOEVALUACION))
                .thenReturn(Optional.empty());
        when(encuestaRepository.save(any(Encuesta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(plantillaService.renderizarParaEvento(any(), any())).thenReturn(null);

        service.iniciarFaseCierre(7L);

        // Solo 2 saves (las 2 que no existían). La de tutor no se recrea.
        verify(encuestaRepository, org.mockito.Mockito.times(2)).save(any(Encuesta.class));
    }

    @Test
    void deberiaFallarSiTutorNoEstaAsignadoEnLaPractica() {
        practica.setTutorEmpresarial(null);
        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(encuestaRepository.findByPractica_IdAndTipo(7L, TipoEncuesta.TUTOR_SATISFACCION))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.iniciarFaseCierre(7L))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("destinatario no asignado");
    }

    // =========================================================================
    // guardarBorradorTutor()
    // =========================================================================

    @Test
    void deberiaGuardarBorradorDelTutor() {
        Encuesta encuesta = encuestaTutor(EstadoEncuesta.PENDIENTE);
        when(encuestaRepository.findById(100L)).thenReturn(Optional.of(encuesta));
        when(encuestaRepository.save(any(Encuesta.class))).thenAnswer(inv -> inv.getArgument(0));

        GuardarRespuestasEncuestaRequest req = new GuardarRespuestasEncuestaRequest();
        req.setRespuestasJson("{\"q1\":\"parcial\"}");

        EncuestaResponse response = service.guardarBorradorTutor(100L, req, tutorDetails);

        assertThat(response.getEstado()).isEqualTo(EstadoEncuesta.EN_BORRADOR);
        assertThat(response.getRespuestasJson()).isEqualTo("{\"q1\":\"parcial\"}");
    }

    @Test
    void deberiaRechazarBorradorEnEncuestaDeEstudiante() {
        Encuesta encuesta = encuestaEstudiante(TipoEncuesta.ESTUDIANTE_SATISFACCION, EstadoEncuesta.PENDIENTE);
        when(encuestaRepository.findById(200L)).thenReturn(Optional.of(encuesta));
        GuardarRespuestasEncuestaRequest req = new GuardarRespuestasEncuestaRequest();
        req.setRespuestasJson("{}");

        assertThatThrownBy(() -> service.guardarBorradorTutor(200L, req, tutorDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("Tutor admite");
    }

    @Test
    void deberiaRechazarBorradorDeOtroTutor() {
        Usuario otro = DatosDePrueba.usuario(999L, "Otro Tutor", "otro@cue.edu.co",
                co.edu.cue.practicas.model.enums.Rol.TUTOR_EMPRESARIAL);
        CustomUserDetails otroDetails = DatosDePrueba.userDetails(otro);
        Encuesta encuesta = encuestaTutor(EstadoEncuesta.PENDIENTE);

        when(encuestaRepository.findById(100L)).thenReturn(Optional.of(encuesta));
        GuardarRespuestasEncuestaRequest req = new GuardarRespuestasEncuestaRequest();
        req.setRespuestasJson("{}");

        assertThatThrownBy(() -> service.guardarBorradorTutor(100L, req, otroDetails))
                .isInstanceOf(AccesoNoAutorizadoException.class);
    }

    // =========================================================================
    // completar()
    // =========================================================================

    @Test
    void deberiaCompletarEncuestaDeEstudiante() {
        Encuesta encuesta = encuestaEstudiante(TipoEncuesta.ESTUDIANTE_SATISFACCION, EstadoEncuesta.PENDIENTE);
        when(encuestaRepository.findById(200L)).thenReturn(Optional.of(encuesta));
        when(encuestaRepository.save(any(Encuesta.class))).thenAnswer(inv -> inv.getArgument(0));
        GuardarRespuestasEncuestaRequest req = new GuardarRespuestasEncuestaRequest();
        req.setRespuestasJson("{\"q1\":\"final\"}");

        EncuestaResponse response = service.completar(200L, req, estudianteDetails);

        assertThat(response.getEstado()).isEqualTo(EstadoEncuesta.COMPLETADA);
        assertThat(response.getCompletadaEn()).isNotNull();
    }

    @Test
    void deberiaFallarCompletarSiYaEstaCompletada() {
        Encuesta encuesta = encuestaEstudiante(TipoEncuesta.ESTUDIANTE_SATISFACCION, EstadoEncuesta.COMPLETADA);
        when(encuestaRepository.findById(200L)).thenReturn(Optional.of(encuesta));

        GuardarRespuestasEncuestaRequest req = new GuardarRespuestasEncuestaRequest();
        req.setRespuestasJson("{}");

        assertThatThrownBy(() -> service.completar(200L, req, estudianteDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("inmutable");
    }

    // =========================================================================
    // enviarRecordatorio()
    // =========================================================================

    @Test
    void deberiaEnviarRecordatorioSiNoSeEnvioHoy() {
        Encuesta encuesta = encuestaTutor(EstadoEncuesta.PENDIENTE);
        when(encuestaRepository.findById(100L)).thenReturn(Optional.of(encuesta));
        when(encuestaRepository.save(any(Encuesta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(plantillaService.renderizarParaEvento(any(), any()))
                .thenReturn(new PlantillaNotificacionService.RenderResult(
                        "asunto", "<p>cuerpo</p>", false, 3));

        EncuestaResponse response = service.enviarRecordatorio(100L, tutorDetails);

        assertThat(response.getUltimoRecordatorioEn()).isNotNull();
        verify(notificacionRepository).save(any(NotificacionHistorial.class));
    }

    @Test
    void deberiaRechazarRecordatorioSiYaSeEnvioHoy() {
        Encuesta encuesta = encuestaTutor(EstadoEncuesta.PENDIENTE);
        encuesta.setUltimoRecordatorioEn(LocalDateTime.now());
        when(encuestaRepository.findById(100L)).thenReturn(Optional.of(encuesta));

        assertThatThrownBy(() -> service.enviarRecordatorio(100L, tutorDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("hoy");

        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void deberiaRechazarRecordatorioSiYaEstaCompletada() {
        Encuesta encuesta = encuestaTutor(EstadoEncuesta.COMPLETADA);
        when(encuestaRepository.findById(100L)).thenReturn(Optional.of(encuesta));

        assertThatThrownBy(() -> service.enviarRecordatorio(100L, tutorDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("completada");
    }

    @Test
    void deberiaFallarSiEncuestaNoExiste() {
        when(encuestaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(999L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Encuesta encuestaTutor(EstadoEncuesta estado) {
        return Encuesta.builder()
                .id(100L)
                .practica(practica)
                .tipo(TipoEncuesta.TUTOR_SATISFACCION)
                .destinatario(tutor)
                .estado(estado)
                .build();
    }

    private Encuesta encuestaEstudiante(TipoEncuesta tipo, EstadoEncuesta estado) {
        return Encuesta.builder()
                .id(200L)
                .practica(practica)
                .tipo(tipo)
                .destinatario(estudiante)
                .estado(estado)
                .build();
    }
}
