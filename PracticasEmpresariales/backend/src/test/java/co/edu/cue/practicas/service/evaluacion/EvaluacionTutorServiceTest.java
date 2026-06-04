package co.edu.cue.practicas.service.evaluacion;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.RegistrarEvaluacionTutorRequest;
import co.edu.cue.practicas.dto.response.EvaluacionTutorResponse;
import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.ConfiguracionPrograma;
import co.edu.cue.practicas.model.entity.EvaluacionTutor;
import co.edu.cue.practicas.model.entity.Facultad;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.ResultadoEvaluacion;
import co.edu.cue.practicas.repository.configuracion.ConfiguracionProgramaRepository;
import co.edu.cue.practicas.repository.evaluacion.EvaluacionTutorRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluacionTutorServiceTest {

    @Mock private EvaluacionTutorRepository evaluacionRepository;
    @Mock private PracticaRepository practicaRepository;
    @Mock private ConfiguracionProgramaRepository configuracionRepository;
    @Mock private AuditoriaLogger auditoriaLogger;

    private EvaluacionTutorService service;

    private Facultad facultad;
    private Programa programa;
    private Usuario docente;
    private Usuario tutor;
    private Usuario estudiante;
    private Practica practica;
    private ConfiguracionPrograma config;
    private CustomUserDetails tutorDetails;

    @BeforeEach
    void configurar() {
        service = new EvaluacionTutorService(
                evaluacionRepository,
                practicaRepository,
                configuracionRepository,
                auditoriaLogger,
                new ObjectMapper());

        facultad   = DatosDePrueba.facultad(1L, "Ingenieria");
        programa   = DatosDePrueba.programa(2L, "Sistemas", facultad);
        docente    = DatosDePrueba.docenteAsesor();
        tutor      = DatosDePrueba.tutorEmpresarial();
        estudiante = DatosDePrueba.usuario(5L, "Estudiante A", "est@cue.edu.co", co.edu.cue.practicas.model.enums.Rol.ESTUDIANTE);
        practica   = DatosDePrueba.practica(7L, estudiante, programa, docente, tutor);
        config     = DatosDePrueba.configuracionPrograma(3L, programa);
        tutorDetails = DatosDePrueba.userDetails(tutor);
    }

    // =========================================================================
    // registrar()
    // =========================================================================

    @Test
    void deberiaRegistrarEvaluacionAprobadaConNotaAlta() {
        RegistrarEvaluacionTutorRequest request =
                DatosDePrueba.registrarEvaluacionTutorRequest(4.5, "Excelente desempeño en la empresa");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(evaluacionRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(config));
        when(evaluacionRepository.save(any(EvaluacionTutor.class))).thenAnswer(inv -> {
            EvaluacionTutor e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        EvaluacionTutorResponse response = service.registrar(7L, request, tutorDetails);

        assertThat(response.getNota()).isEqualTo(4.5);
        assertThat(response.getResultado()).isEqualTo(ResultadoEvaluacion.APROBADO);
        verify(evaluacionRepository).save(any(EvaluacionTutor.class));
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaRegistrarEvaluacionDesaprobadaConNotaBaja() {
        RegistrarEvaluacionTutorRequest request =
                DatosDePrueba.registrarEvaluacionTutorRequest(2.5, "Desempeño insuficiente");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(evaluacionRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(config));
        when(evaluacionRepository.save(any(EvaluacionTutor.class))).thenAnswer(inv -> inv.getArgument(0));

        EvaluacionTutorResponse response = service.registrar(7L, request, tutorDetails);

        assertThat(response.getResultado()).isEqualTo(ResultadoEvaluacion.DESAPROBADO);
    }

    @Test
    void deberiaFallarSiTutorNoEsAsignado() {
        Usuario otroTutor = DatosDePrueba.usuario(99L, "Otro tutor", "otro@cue.edu.co", co.edu.cue.practicas.model.enums.Rol.TUTOR_EMPRESARIAL);
        CustomUserDetails otroDetails = DatosDePrueba.userDetails(otroTutor);
        RegistrarEvaluacionTutorRequest request =
                DatosDePrueba.registrarEvaluacionTutorRequest(4.0, "Buen trabajo");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));

        assertThatThrownBy(() -> service.registrar(7L, request, otroDetails))
                .isInstanceOf(AccesoNoAutorizadoException.class);
        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    void deberiaFallarSiPracticaNoEstaEnCurso() {
        practica.setEstado(EstadoPractica.FINALIZADA);
        RegistrarEvaluacionTutorRequest request =
                DatosDePrueba.registrarEvaluacionTutorRequest(4.0, "OK");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));

        assertThatThrownBy(() -> service.registrar(7L, request, tutorDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("EN_CURSO");
    }

    @Test
    void deberiaFallarSiNotasYaEstanCerradas() {
        practica.setNotasCerradas(true);
        RegistrarEvaluacionTutorRequest request =
                DatosDePrueba.registrarEvaluacionTutorRequest(4.0, "OK");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));

        assertThatThrownBy(() -> service.registrar(7L, request, tutorDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("cerró");
    }

    @Test
    void deberiaFallarSiYaExisteEvaluacionParaLaPractica() {
        RegistrarEvaluacionTutorRequest request =
                DatosDePrueba.registrarEvaluacionTutorRequest(4.0, "OK");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(evaluacionRepository.existsByPractica_Id(7L)).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(7L, request, tutorDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    void deberiaFallarSiNotaFueraDeRango() {
        RegistrarEvaluacionTutorRequest request =
                DatosDePrueba.registrarEvaluacionTutorRequest(6.0, "Excedido");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(evaluacionRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(config));

        assertThatThrownBy(() -> service.registrar(7L, request, tutorDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("entre 0.0");
    }

    @Test
    void deberiaUsarRangosPorDefectoSiNoExisteConfiguracion() {
        RegistrarEvaluacionTutorRequest request =
                DatosDePrueba.registrarEvaluacionTutorRequest(3.5, "Aceptable");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(evaluacionRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.empty());
        when(evaluacionRepository.save(any(EvaluacionTutor.class))).thenAnswer(inv -> inv.getArgument(0));

        EvaluacionTutorResponse response = service.registrar(7L, request, tutorDetails);

        // Por defecto notaMinima = 3.0, así que 3.5 es APROBADO
        assertThat(response.getResultado()).isEqualTo(ResultadoEvaluacion.APROBADO);
    }

    // =========================================================================
    // actualizar()
    // =========================================================================

    @Test
    void deberiaActualizarEvaluacionExistente() {
        EvaluacionTutor existente = DatosDePrueba.evaluacionTutor(50L, practica, tutor);
        RegistrarEvaluacionTutorRequest request =
                DatosDePrueba.registrarEvaluacionTutorRequest(4.8, "Mejoró notablemente");

        when(evaluacionRepository.findById(50L)).thenReturn(Optional.of(existente));
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(config));
        when(evaluacionRepository.save(any(EvaluacionTutor.class))).thenAnswer(inv -> inv.getArgument(0));

        EvaluacionTutorResponse response = service.actualizar(50L, request, tutorDetails);

        assertThat(response.getNota()).isEqualTo(4.8);
        assertThat(response.getResultado()).isEqualTo(ResultadoEvaluacion.APROBADO);
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaFallarActualizacionDeOtroTutor() {
        Usuario otroTutor = DatosDePrueba.usuario(99L, "Otro", "otro@cue.edu.co", co.edu.cue.practicas.model.enums.Rol.TUTOR_EMPRESARIAL);
        EvaluacionTutor existente = DatosDePrueba.evaluacionTutor(50L, practica, otroTutor);
        RegistrarEvaluacionTutorRequest request =
                DatosDePrueba.registrarEvaluacionTutorRequest(4.0, "Cambio");

        when(evaluacionRepository.findById(50L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.actualizar(50L, request, tutorDetails))
                .isInstanceOf(AccesoNoAutorizadoException.class);
    }

    @Test
    void deberiaFallarActualizacionSiNotasYaCerradas() {
        practica.setNotasCerradas(true);
        EvaluacionTutor existente = DatosDePrueba.evaluacionTutor(50L, practica, tutor);
        RegistrarEvaluacionTutorRequest request =
                DatosDePrueba.registrarEvaluacionTutorRequest(4.0, "Cambio");

        when(evaluacionRepository.findById(50L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.actualizar(50L, request, tutorDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("cerró");
    }

    // =========================================================================
    // obtenerPorPractica()
    // =========================================================================

    @Test
    void deberiaObtenerEvaluacionPorPractica() {
        EvaluacionTutor existente = DatosDePrueba.evaluacionTutor(50L, practica, tutor);
        when(evaluacionRepository.findByPractica_Id(7L)).thenReturn(Optional.of(existente));

        EvaluacionTutorResponse response = service.obtenerPorPractica(7L);

        assertThat(response.getId()).isEqualTo(50L);
        assertThat(response.getTutorId()).isEqualTo(tutor.getId());
    }

    @Test
    void deberiaFallarSiNoExisteEvaluacionParaLaPractica() {
        when(evaluacionRepository.findByPractica_Id(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorPractica(7L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
