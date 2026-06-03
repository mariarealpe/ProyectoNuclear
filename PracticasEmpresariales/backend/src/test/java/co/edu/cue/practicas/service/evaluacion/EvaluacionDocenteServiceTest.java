package co.edu.cue.practicas.service.evaluacion;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.RegistrarEvaluacionDocenteRequest;
import co.edu.cue.practicas.dto.response.EvaluacionDocenteResponse;
import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.ConfiguracionPrograma;
import co.edu.cue.practicas.model.entity.EvaluacionDocente;
import co.edu.cue.practicas.model.entity.Facultad;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.ResultadoEvaluacion;
import co.edu.cue.practicas.repository.configuracion.ConfiguracionProgramaRepository;
import co.edu.cue.practicas.repository.evaluacion.EvaluacionDocenteRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluacionDocenteServiceTest {

    @Mock private EvaluacionDocenteRepository evaluacionRepository;
    @Mock private PracticaRepository practicaRepository;
    @Mock private ConfiguracionProgramaRepository configuracionRepository;
    @Mock private AuditoriaLogger auditoriaLogger;

    private EvaluacionDocenteService service;

    private Facultad facultad;
    private Programa programa;
    private Usuario docente;
    private Usuario estudiante;
    private Practica practica;
    private ConfiguracionPrograma config;
    private CustomUserDetails docenteDetails;

    @BeforeEach
    void configurar() {
        service = new EvaluacionDocenteService(
                evaluacionRepository,
                practicaRepository,
                configuracionRepository,
                auditoriaLogger,
                new ObjectMapper());

        facultad    = DatosDePrueba.facultad(1L, "Ingenieria");
        programa    = DatosDePrueba.programa(2L, "Sistemas", facultad);
        docente     = DatosDePrueba.docenteAsesor();
        estudiante  = DatosDePrueba.usuario(5L, "Estudiante A", "est@cue.edu.co", co.edu.cue.practicas.model.enums.Rol.ESTUDIANTE);
        practica    = DatosDePrueba.practica(7L, estudiante, programa, docente);
        config      = DatosDePrueba.configuracionPrograma(3L, programa);
        docenteDetails = DatosDePrueba.userDetails(docente);
    }

    // =========================================================================
    // registrar() — casos exitosos
    // =========================================================================

    @Test
    void deberiaRegistrarEvaluacionAprobadaConNotaAlta() {
        RegistrarEvaluacionDocenteRequest request =
                DatosDePrueba.registrarEvaluacionRequest(4.5, "Excelente desempeño");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(evaluacionRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(config));
        when(evaluacionRepository.save(any(EvaluacionDocente.class))).thenAnswer(inv -> {
            EvaluacionDocente e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        EvaluacionDocenteResponse response = service.registrar(7L, request, docenteDetails);

        assertThat(response.getNota()).isEqualTo(4.5);
        assertThat(response.getResultado()).isEqualTo(ResultadoEvaluacion.APROBADO);
        assertThat(response.getObservaciones()).isEqualTo("Excelente desempeño");
        verify(evaluacionRepository).save(any(EvaluacionDocente.class));
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaRegistrarEvaluacionDesaprobadaConNotaBaja() {
        RegistrarEvaluacionDocenteRequest request =
                DatosDePrueba.registrarEvaluacionRequest(2.0, "No alcanzó los objetivos");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(evaluacionRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(config));
        when(evaluacionRepository.save(any(EvaluacionDocente.class))).thenAnswer(inv -> {
            EvaluacionDocente e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        EvaluacionDocenteResponse response = service.registrar(7L, request, docenteDetails);

        // notaMinimaAprobacion en config es 3.2; nota 2.0 < 3.2 → DESAPROBADO
        assertThat(response.getNota()).isEqualTo(2.0);
        assertThat(response.getResultado()).isEqualTo(ResultadoEvaluacion.DESAPROBADO);
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaResultarAprobadoEnNombreExactoDelUmbral() {
        // nota == notaMinimaAprobacion → APROBADO (límite incluido)
        double umbral = config.getNotaMinimaAprobacion(); // 3.2
        RegistrarEvaluacionDocenteRequest request =
                DatosDePrueba.registrarEvaluacionRequest(umbral, "Cumplimiento mínimo");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(evaluacionRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(config));
        when(evaluacionRepository.save(any(EvaluacionDocente.class))).thenAnswer(inv -> {
            EvaluacionDocente e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        EvaluacionDocenteResponse response = service.registrar(7L, request, docenteDetails);

        assertThat(response.getResultado()).isEqualTo(ResultadoEvaluacion.APROBADO);
    }

    @Test
    void deberiaUsarConfigPorDefectoCuandoNoExisteConfiguracion() {
        // Sin ConfiguracionPrograma → defaults: notaMaxima=5.0, notaMinima=3.0
        RegistrarEvaluacionDocenteRequest request =
                DatosDePrueba.registrarEvaluacionRequest(2.5, "Bajo rendimiento");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(evaluacionRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.empty());
        when(evaluacionRepository.save(any(EvaluacionDocente.class))).thenAnswer(inv -> {
            EvaluacionDocente e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        EvaluacionDocenteResponse response = service.registrar(7L, request, docenteDetails);

        // 2.5 < 3.0 (default) → DESAPROBADO
        assertThat(response.getResultado()).isEqualTo(ResultadoEvaluacion.DESAPROBADO);
    }

    // =========================================================================
    // registrar() — casos de error
    // =========================================================================

    @Test
    void deberiaLanzarNotFoundCuandoPracticaNoExiste() {
        when(practicaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(99L,
                DatosDePrueba.registrarEvaluacionRequest(4.0, "obs"), docenteDetails))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");

        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarErrorCuandoPracticaNoEstaEnCurso() {
        practica.setEstado(EstadoPractica.FINALIZADA);
        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));

        assertThatThrownBy(() -> service.registrar(7L,
                DatosDePrueba.registrarEvaluacionRequest(4.0, "obs"), docenteDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("EN_CURSO");

        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarErrorCuandoDocenteNoEsElAsignado() {
        Usuario otroDocente = DatosDePrueba.usuario(99L, "Otro Docente", "otro@cue.edu.co",
                co.edu.cue.practicas.model.enums.Rol.DOCENTE_ASESOR);
        CustomUserDetails otroDetails = DatosDePrueba.userDetails(otroDocente);

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));

        assertThatThrownBy(() -> service.registrar(7L,
                DatosDePrueba.registrarEvaluacionRequest(4.0, "obs"), otroDetails))
                .isInstanceOf(AccesoNoAutorizadoException.class);

        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarErrorCuandoYaExisteEvaluacion() {
        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(evaluacionRepository.existsByPractica_Id(7L)).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(7L,
                DatosDePrueba.registrarEvaluacionRequest(4.0, "obs"), docenteDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("Ya existe");

        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarErrorCuandoNotaEsMayorAlMaximo() {
        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(evaluacionRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(config));

        // notaMaxima en config es 5.0
        assertThatThrownBy(() -> service.registrar(7L,
                DatosDePrueba.registrarEvaluacionRequest(5.5, "obs"), docenteDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("5.0");

        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarErrorCuandoNotaEsNegativa() {
        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(evaluacionRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(config));

        assertThatThrownBy(() -> service.registrar(7L,
                DatosDePrueba.registrarEvaluacionRequest(-0.1, "obs"), docenteDetails))
                .isInstanceOf(OperacionNoPermitidaException.class);

        verify(evaluacionRepository, never()).save(any());
    }

    // =========================================================================
    // actualizar()
    // =========================================================================

    @Test
    void deberiaActualizarEvaluacionExistente() {
        EvaluacionDocente evaluacion = DatosDePrueba.evaluacionDocente(1L, practica, docente);
        RegistrarEvaluacionDocenteRequest request =
                DatosDePrueba.registrarEvaluacionRequest(3.0, "Revisado y corregido");

        when(evaluacionRepository.findById(1L)).thenReturn(Optional.of(evaluacion));
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(config));
        when(evaluacionRepository.save(evaluacion)).thenReturn(evaluacion);

        EvaluacionDocenteResponse response = service.actualizar(1L, request, docenteDetails);

        assertThat(response.getNota()).isEqualTo(3.0);
        // 3.0 < 3.2 (notaMinimaAprobacion del config) → DESAPROBADO
        assertThat(response.getResultado()).isEqualTo(ResultadoEvaluacion.DESAPROBADO);
        assertThat(response.getObservaciones()).isEqualTo("Revisado y corregido");
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaLanzarErrorAlActualizarSiPracticaNoEstaEnCurso() {
        practica.setEstado(EstadoPractica.FINALIZADA);
        EvaluacionDocente evaluacion = DatosDePrueba.evaluacionDocente(1L, practica, docente);

        when(evaluacionRepository.findById(1L)).thenReturn(Optional.of(evaluacion));

        assertThatThrownBy(() -> service.actualizar(1L,
                DatosDePrueba.registrarEvaluacionRequest(4.0, "obs"), docenteDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("cerrada");

        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarErrorAlActualizarSiDocenteNoEsDueno() {
        EvaluacionDocente evaluacion = DatosDePrueba.evaluacionDocente(1L, practica, docente);
        Usuario intruso = DatosDePrueba.usuario(55L, "Intruso", "intruso@cue.edu.co",
                co.edu.cue.practicas.model.enums.Rol.DOCENTE_ASESOR);

        when(evaluacionRepository.findById(1L)).thenReturn(Optional.of(evaluacion));

        assertThatThrownBy(() -> service.actualizar(1L,
                DatosDePrueba.registrarEvaluacionRequest(4.0, "obs"),
                DatosDePrueba.userDetails(intruso)))
                .isInstanceOf(AccesoNoAutorizadoException.class);

        verify(evaluacionRepository, never()).save(any());
    }

    // =========================================================================
    // obtenerPorPractica()
    // =========================================================================

    @Test
    void deberiaObtenerEvaluacionExistente() {
        EvaluacionDocente evaluacion = DatosDePrueba.evaluacionDocente(1L, practica, docente);
        when(evaluacionRepository.findByPractica_Id(7L)).thenReturn(Optional.of(evaluacion));

        EvaluacionDocenteResponse response = service.obtenerPorPractica(7L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNota()).isEqualTo(4.0);
        assertThat(response.getResultado()).isEqualTo(ResultadoEvaluacion.APROBADO);
    }

    @Test
    void deberiaLanzarNotFoundCuandoNoHayEvaluacion() {
        when(evaluacionRepository.findByPractica_Id(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorPractica(7L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("7");
    }
}
