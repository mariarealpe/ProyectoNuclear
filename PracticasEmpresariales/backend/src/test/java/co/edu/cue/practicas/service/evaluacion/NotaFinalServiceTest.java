package co.edu.cue.practicas.service.evaluacion;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.RegistrarNotaFinalRequest;
import co.edu.cue.practicas.dto.response.NotaFinalResponse;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.ConfiguracionPrograma;
import co.edu.cue.practicas.model.entity.EvaluacionDocente;
import co.edu.cue.practicas.model.entity.EvaluacionTutor;
import co.edu.cue.practicas.model.entity.Facultad;
import co.edu.cue.practicas.model.entity.NotaFinal;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.ResultadoNotaFinal;
import co.edu.cue.practicas.repository.configuracion.ConfiguracionProgramaRepository;
import co.edu.cue.practicas.repository.evaluacion.EvaluacionDocenteRepository;
import co.edu.cue.practicas.repository.evaluacion.EvaluacionTutorRepository;
import co.edu.cue.practicas.repository.evaluacion.NotaFinalRepository;
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
class NotaFinalServiceTest {

    @Mock private NotaFinalRepository notaFinalRepository;
    @Mock private EvaluacionDocenteRepository evaluacionDocenteRepository;
    @Mock private EvaluacionTutorRepository evaluacionTutorRepository;
    @Mock private PracticaRepository practicaRepository;
    @Mock private ConfiguracionProgramaRepository configuracionRepository;
    @Mock private AuditoriaLogger auditoriaLogger;

    private NotaFinalService service;

    private Facultad facultad;
    private Programa programa;
    private Usuario docente;
    private Usuario tutor;
    private Usuario coordinador;
    private Usuario estudiante;
    private Practica practica;
    private ConfiguracionPrograma config;
    private EvaluacionDocente evDocente;
    private EvaluacionTutor evTutor;
    private CustomUserDetails coordDetails;

    @BeforeEach
    void configurar() {
        service = new NotaFinalService(
                notaFinalRepository,
                evaluacionDocenteRepository,
                evaluacionTutorRepository,
                practicaRepository,
                configuracionRepository,
                auditoriaLogger,
                new ObjectMapper());

        facultad    = DatosDePrueba.facultad(1L, "Ingenieria");
        programa    = DatosDePrueba.programa(2L, "Sistemas", facultad);
        docente     = DatosDePrueba.docenteAsesor();
        tutor       = DatosDePrueba.tutorEmpresarial();
        coordinador = DatosDePrueba.coordinadorPracticas();
        estudiante  = DatosDePrueba.usuario(5L, "Estudiante A", "est@cue.edu.co", co.edu.cue.practicas.model.enums.Rol.ESTUDIANTE);
        practica    = DatosDePrueba.practica(7L, estudiante, programa, docente, tutor);
        config      = DatosDePrueba.configuracionPrograma(3L, programa);
        evDocente   = DatosDePrueba.evaluacionDocente(40L, practica, docente);
        evTutor     = DatosDePrueba.evaluacionTutor(41L, practica, tutor);
        coordDetails = DatosDePrueba.userDetails(coordinador);
    }

    // =========================================================================
    // registrar()
    // =========================================================================

    @Test
    void deberiaRegistrarNotaFinalAprobadaConReferencias() {
        RegistrarNotaFinalRequest request =
                DatosDePrueba.registrarNotaFinalRequest(4.3, "Aprueba la práctica");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(notaFinalRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(evaluacionDocenteRepository.findByPractica_Id(7L)).thenReturn(Optional.of(evDocente));
        when(evaluacionTutorRepository.findByPractica_Id(7L)).thenReturn(Optional.of(evTutor));
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(config));
        when(notaFinalRepository.save(any(NotaFinal.class))).thenAnswer(inv -> {
            NotaFinal n = inv.getArgument(0);
            n.setId(99L);
            return n;
        });

        NotaFinalResponse response = service.registrar(7L, request, coordDetails);

        assertThat(response.getNota()).isEqualTo(4.3);
        assertThat(response.getResultado()).isEqualTo(ResultadoNotaFinal.APROBADO);
        assertThat(response.getNotaReferenciaDocente()).isEqualTo(evDocente.getNota());
        assertThat(response.getNotaReferenciaTutor()).isEqualTo(evTutor.getNota());
        assertThat(response.getCerrada()).isFalse();
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaRegistrarNotaFinalReprobadaSiInferiorAlMinimo() {
        RegistrarNotaFinalRequest request =
                DatosDePrueba.registrarNotaFinalRequest(2.8, "No alcanza el mínimo");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(notaFinalRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(evaluacionDocenteRepository.findByPractica_Id(7L)).thenReturn(Optional.of(evDocente));
        when(evaluacionTutorRepository.findByPractica_Id(7L)).thenReturn(Optional.of(evTutor));
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(config));
        when(notaFinalRepository.save(any(NotaFinal.class))).thenAnswer(inv -> inv.getArgument(0));

        NotaFinalResponse response = service.registrar(7L, request, coordDetails);

        assertThat(response.getResultado()).isEqualTo(ResultadoNotaFinal.REPROBADO);
    }

    @Test
    void deberiaFallarSiNoExisteEvaluacionDocente() {
        RegistrarNotaFinalRequest request =
                DatosDePrueba.registrarNotaFinalRequest(4.0, "obs");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(notaFinalRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(evaluacionDocenteRepository.findByPractica_Id(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(7L, request, coordDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("Docente");
        verify(notaFinalRepository, never()).save(any());
    }

    @Test
    void deberiaFallarSiNoExisteEvaluacionTutor() {
        RegistrarNotaFinalRequest request =
                DatosDePrueba.registrarNotaFinalRequest(4.0, "obs");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(notaFinalRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(evaluacionDocenteRepository.findByPractica_Id(7L)).thenReturn(Optional.of(evDocente));
        when(evaluacionTutorRepository.findByPractica_Id(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(7L, request, coordDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("Tutor");
        verify(notaFinalRepository, never()).save(any());
    }

    @Test
    void deberiaFallarSiNotasYaEstanCerradas() {
        practica.setNotasCerradas(true);
        RegistrarNotaFinalRequest request =
                DatosDePrueba.registrarNotaFinalRequest(4.0, "obs");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));

        assertThatThrownBy(() -> service.registrar(7L, request, coordDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("cerrado");
    }

    @Test
    void deberiaFallarSiYaExisteNotaFinal() {
        RegistrarNotaFinalRequest request =
                DatosDePrueba.registrarNotaFinalRequest(4.0, "obs");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(notaFinalRepository.existsByPractica_Id(7L)).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(7L, request, coordDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    void deberiaFallarSiPracticaNoEstaEnCurso() {
        practica.setEstado(EstadoPractica.FINALIZADA);
        RegistrarNotaFinalRequest request =
                DatosDePrueba.registrarNotaFinalRequest(4.0, "obs");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));

        assertThatThrownBy(() -> service.registrar(7L, request, coordDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("EN_CURSO");
    }

    @Test
    void deberiaFallarSiNotaFueraDeRango() {
        RegistrarNotaFinalRequest request =
                DatosDePrueba.registrarNotaFinalRequest(6.5, "obs");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(notaFinalRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(evaluacionDocenteRepository.findByPractica_Id(7L)).thenReturn(Optional.of(evDocente));
        when(evaluacionTutorRepository.findByPractica_Id(7L)).thenReturn(Optional.of(evTutor));
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(config));

        assertThatThrownBy(() -> service.registrar(7L, request, coordDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("entre 0.0");
    }

    // =========================================================================
    // actualizar()
    // =========================================================================

    @Test
    void deberiaActualizarNotaFinalAntesDelCierre() {
        NotaFinal existente = DatosDePrueba.notaFinal(99L, practica, coordinador);
        RegistrarNotaFinalRequest request =
                DatosDePrueba.registrarNotaFinalRequest(3.0, "Recalculo");

        when(notaFinalRepository.findById(99L)).thenReturn(Optional.of(existente));
        when(configuracionRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(config));
        when(notaFinalRepository.save(any(NotaFinal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(evaluacionDocenteRepository.findByPractica_Id(7L)).thenReturn(Optional.of(evDocente));
        when(evaluacionTutorRepository.findByPractica_Id(7L)).thenReturn(Optional.of(evTutor));

        NotaFinalResponse response = service.actualizar(99L, request, coordDetails);

        assertThat(response.getNota()).isEqualTo(3.0);
        // config.notaMinimaAprobacion = 3.2 → 3.0 es REPROBADO
        assertThat(response.getResultado()).isEqualTo(ResultadoNotaFinal.REPROBADO);
    }

    @Test
    void deberiaFallarActualizacionSiNotaYaCerrada() {
        NotaFinal existente = DatosDePrueba.notaFinal(99L, practica, coordinador);
        existente.setCerrada(true);
        RegistrarNotaFinalRequest request =
                DatosDePrueba.registrarNotaFinalRequest(4.0, "Cambio");

        when(notaFinalRepository.findById(99L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.actualizar(99L, request, coordDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("inmutable");
    }

    // =========================================================================
    // cerrar()
    // =========================================================================

    @Test
    void deberiaCerrarNotaFinalYSincronizarPractica() {
        NotaFinal existente = DatosDePrueba.notaFinal(99L, practica, coordinador);

        when(notaFinalRepository.findById(99L)).thenReturn(Optional.of(existente));
        when(notaFinalRepository.save(any(NotaFinal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(practicaRepository.save(any(Practica.class))).thenAnswer(inv -> inv.getArgument(0));
        when(evaluacionDocenteRepository.findByPractica_Id(7L)).thenReturn(Optional.of(evDocente));
        when(evaluacionTutorRepository.findByPractica_Id(7L)).thenReturn(Optional.of(evTutor));

        NotaFinalResponse response = service.cerrar(99L, coordDetails);

        assertThat(response.getCerrada()).isTrue();
        assertThat(response.getCerradaEn()).isNotNull();
        assertThat(practica.getNotasCerradas()).isTrue();
        verify(practicaRepository).save(practica);
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaFallarCerrarSiYaEstaCerrada() {
        NotaFinal existente = DatosDePrueba.notaFinal(99L, practica, coordinador);
        existente.setCerrada(true);

        when(notaFinalRepository.findById(99L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.cerrar(99L, coordDetails))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("ya está cerrada");
    }

    // =========================================================================
    // obtener()
    // =========================================================================

    @Test
    void deberiaObtenerPorPracticaConReferenciasDeAmbas() {
        NotaFinal existente = DatosDePrueba.notaFinal(99L, practica, coordinador);
        when(notaFinalRepository.findByPractica_Id(7L)).thenReturn(Optional.of(existente));
        when(evaluacionDocenteRepository.findByPractica_Id(7L)).thenReturn(Optional.of(evDocente));
        when(evaluacionTutorRepository.findByPractica_Id(7L)).thenReturn(Optional.of(evTutor));

        NotaFinalResponse response = service.obtenerPorPractica(7L);

        assertThat(response.getNotaReferenciaDocente()).isEqualTo(evDocente.getNota());
        assertThat(response.getNotaReferenciaTutor()).isEqualTo(evTutor.getNota());
    }

    @Test
    void deberiaFallarSiNoExisteNotaFinal() {
        when(notaFinalRepository.findByPractica_Id(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorPractica(7L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
