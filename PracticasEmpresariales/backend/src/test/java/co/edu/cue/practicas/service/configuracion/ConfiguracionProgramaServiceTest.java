package co.edu.cue.practicas.service.configuracion;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.ActualizarConfiguracionProgramaRequest;
import co.edu.cue.practicas.dto.response.ConfiguracionProgramaResponse;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.ConfiguracionPrograma;
import co.edu.cue.practicas.model.entity.Facultad;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.repository.configuracion.ConfiguracionProgramaRepository;
import co.edu.cue.practicas.repository.programa.ProgramaRepository;
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
class ConfiguracionProgramaServiceTest {

    @Mock
    private ConfiguracionProgramaRepository configuracionProgramaRepository;

    @Mock
    private ProgramaRepository programaRepository;

    @Mock
    private AuditoriaLogger auditoriaLogger;

    private ConfiguracionProgramaService configuracionProgramaService;
    private CustomUserDetails admin;

    @BeforeEach
    void configurar() {
        configuracionProgramaService = new ConfiguracionProgramaService(
                configuracionProgramaRepository,
                programaRepository,
                auditoriaLogger,
                new ObjectMapper());
        admin = DatosDePrueba.userDetails(DatosDePrueba.administradorDti());
    }

    @Test
    void deberiaObtenerConfiguracionExistentePorPrograma() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        Programa programa = DatosDePrueba.programa(2L, "Sistemas", facultad);
        ConfiguracionPrograma configuracion = DatosDePrueba.configuracionPrograma(10L, programa);

        when(programaRepository.findById(2L)).thenReturn(Optional.of(programa));
        when(configuracionProgramaRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(configuracion));

        ConfiguracionProgramaResponse response = configuracionProgramaService.obtenerPorPrograma(2L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getProgramaId()).isEqualTo(2L);
        assertThat(response.getNotaMinimaAprobacion()).isEqualTo(3.2);
        assertThat(response.getNumeroTotalPracticas()).isEqualTo(2);
    }

    @Test
    void deberiaRetornarConfiguracionPorDefectoCuandoNoExiste() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        Programa programa = DatosDePrueba.programa(2L, "Sistemas", facultad);

        when(programaRepository.findById(2L)).thenReturn(Optional.of(programa));
        when(configuracionProgramaRepository.findByPrograma_Id(2L)).thenReturn(Optional.empty());

        ConfiguracionProgramaResponse response = configuracionProgramaService.obtenerPorPrograma(2L);

        assertThat(response.getId()).isNull();
        assertThat(response.getProgramaNombre()).isEqualTo("Sistemas");
        assertThat(response.getNotaMinimaAprobacion()).isEqualTo(3.5);
        assertThat(response.getNumeroTotalPracticas()).isEqualTo(2);
        verify(configuracionProgramaRepository, never()).save(any());
    }

    @Test
    void deberiaActualizarConfiguracionExistente() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        Programa programa = DatosDePrueba.programa(2L, "Sistemas", facultad);
        ConfiguracionPrograma configuracion = DatosDePrueba.configuracionPrograma(10L, programa);
        ActualizarConfiguracionProgramaRequest request = DatosDePrueba.actualizarConfiguracionProgramaRequest();
        request.setDiasInactividadAlerta(9);
        request.setMaximoAsignacionesSimultaneas(2);

        when(programaRepository.findById(2L)).thenReturn(Optional.of(programa));
        when(configuracionProgramaRepository.findByPrograma_Id(2L)).thenReturn(Optional.of(configuracion));
        when(configuracionProgramaRepository.save(configuracion)).thenReturn(configuracion);

        ConfiguracionProgramaResponse response =
                configuracionProgramaService.actualizarPorPrograma(2L, request, admin);

        assertThat(response.getDiasInactividadAlerta()).isEqualTo(9);
        assertThat(response.getMaximoAsignacionesSimultaneas()).isEqualTo(2);
        assertThat(response.getCorreoRemitente()).isEqualTo("practicas@cue.edu.co");
        verify(configuracionProgramaRepository).save(configuracion);
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaCrearConfiguracionCuandoNoExisteAlActualizar() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        Programa programa = DatosDePrueba.programa(2L, "Sistemas", facultad);
        ActualizarConfiguracionProgramaRequest request = DatosDePrueba.actualizarConfiguracionProgramaRequest();

        when(programaRepository.findById(2L)).thenReturn(Optional.of(programa));
        when(configuracionProgramaRepository.findByPrograma_Id(2L)).thenReturn(Optional.empty());
        when(configuracionProgramaRepository.save(any(ConfiguracionPrograma.class))).thenAnswer(invocation -> {
            ConfiguracionPrograma configuracion = invocation.getArgument(0);
            configuracion.setId(11L);
            return configuracion;
        });

        ConfiguracionProgramaResponse response =
                configuracionProgramaService.actualizarPorPrograma(2L, request, admin);

        assertThat(response.getId()).isEqualTo(11L);
        assertThat(response.getProgramaId()).isEqualTo(2L);
        assertThat(response.getNotaMaxima()).isEqualTo(5.0);
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaLanzarNoEncontradoCuandoProgramaNoExiste() {
        ActualizarConfiguracionProgramaRequest request = DatosDePrueba.actualizarConfiguracionProgramaRequest();
        when(programaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configuracionProgramaService.actualizarPorPrograma(99L, request, admin))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Programa no encontrado");

        verify(configuracionProgramaRepository, never()).save(any());
    }

    @Test
    void deberiaRechazarNotaMinimaMayorANotaMaxima() {
        ActualizarConfiguracionProgramaRequest request = DatosDePrueba.actualizarConfiguracionProgramaRequest();
        request.setNotaMinimaAprobacion(5.1);
        request.setNotaMaxima(5.0);

        assertThatThrownBy(() -> configuracionProgramaService.actualizarPorPrograma(2L, request, admin))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("nota mínima");

        verify(programaRepository, never()).findById(any());
        verify(configuracionProgramaRepository, never()).save(any());
    }
}
