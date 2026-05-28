package co.edu.cue.practicas.service.programa;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.CrearProgramaRequest;
import co.edu.cue.practicas.dto.response.ProgramaResponse;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.Facultad;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.repository.facultad.FacultadRepository;
import co.edu.cue.practicas.repository.programa.ProgramaRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgramaServiceTest {

    @Mock
    private ProgramaRepository programaRepository;

    @Mock
    private FacultadRepository facultadRepository;

    @Mock
    private AuditoriaLogger auditoriaLogger;

    private ProgramaService programaService;
    private CustomUserDetails admin;

    @BeforeEach
    void configurar() {
        programaService = new ProgramaService(
                programaRepository,
                facultadRepository,
                auditoriaLogger,
                new ObjectMapper());
        admin = DatosDePrueba.userDetails(DatosDePrueba.administradorDti());
    }

    @Test
    void deberiaCrearProgramaConRequisitosCuandoFacultadExiste() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        CrearProgramaRequest request = DatosDePrueba.crearProgramaRequest(1L);

        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));
        when(programaRepository.existsByNombreIgnoreCaseAndFacultad_Id("Ingenieria de Sistemas", 1L))
                .thenReturn(false);
        when(programaRepository.save(any(Programa.class))).thenAnswer(invocation -> {
            Programa programa = invocation.getArgument(0);
            programa.setId(20L);
            return programa;
        });

        ProgramaResponse response = programaService.crearPrograma(request, admin);

        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getNombre()).isEqualTo("Ingenieria de Sistemas");
        assertThat(response.getFacultadId()).isEqualTo(1L);
        assertThat(response.getNumeroTotalPracticas()).isEqualTo(2);
        assertThat(response.getRequisitos()).hasSize(1);
        assertThat(response.getRequisitos().getFirst().getCreditosMinimos()).isEqualTo(80);
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaRechazarProgramaDuplicadoEnLaMismaFacultad() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        CrearProgramaRequest request = DatosDePrueba.crearProgramaRequest(1L);

        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));
        when(programaRepository.existsByNombreIgnoreCaseAndFacultad_Id("Ingenieria de Sistemas", 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> programaService.crearPrograma(request, admin))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("Ya existe");

        verify(programaRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarNoEncontradoCuandoFacultadNoExisteAlCrearPrograma() {
        CrearProgramaRequest request = DatosDePrueba.crearProgramaRequest(99L);
        when(facultadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programaService.crearPrograma(request, admin))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Facultad no encontrada");

        verify(programaRepository, never()).save(any());
    }

    @Test
    void deberiaDesactivarProgramaExistente() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        Programa programa = DatosDePrueba.programa(2L, "Sistemas", facultad);
        when(programaRepository.findById(2L)).thenReturn(Optional.of(programa));

        programaService.desactivarPrograma(2L, admin);

        assertThat(programa.isActivo()).isFalse();
        verify(programaRepository).save(programa);
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaListarProgramasActivosPorFacultad() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        Programa programa = DatosDePrueba.programa(2L, "Sistemas", facultad);
        when(programaRepository.findByFacultad_IdAndActivoTrue(1L)).thenReturn(List.of(programa));

        List<ProgramaResponse> response = programaService.listarPorFacultad(1L);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getNombre()).isEqualTo("Sistemas");
    }

    @Test
    void deberiaListarProgramasActivosPaginados() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        Programa programa = DatosDePrueba.programa(2L, "Sistemas", facultad);
        PageRequest pageable = PageRequest.of(0, 10);
        when(programaRepository.findByActivoTrue(pageable))
                .thenReturn(new PageImpl<>(List.of(programa), pageable, 1));

        Page<ProgramaResponse> response = programaService.listar(pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().getFacultadNombre()).isEqualTo("Ingenieria");
    }
}
