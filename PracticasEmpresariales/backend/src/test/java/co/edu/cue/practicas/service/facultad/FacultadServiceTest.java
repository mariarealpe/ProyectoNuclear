package co.edu.cue.practicas.service.facultad;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.CrearFacultadRequest;
import co.edu.cue.practicas.dto.response.FacultadResponse;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.Facultad;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.repository.facultad.FacultadRepository;
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
class FacultadServiceTest {

    @Mock
    private FacultadRepository facultadRepository;

    @Mock
    private AuditoriaLogger auditoriaLogger;

    private FacultadService facultadService;
    private CustomUserDetails admin;

    @BeforeEach
    void configurar() {
        facultadService = new FacultadService(facultadRepository, auditoriaLogger, new ObjectMapper());
        admin = DatosDePrueba.userDetails(DatosDePrueba.administradorDti());
    }

    @Test
    void deberiaCrearFacultadCuandoNombreNoExiste() {
        CrearFacultadRequest request = DatosDePrueba.crearFacultadRequest("Ingenieria");
        when(facultadRepository.existsByNombreIgnoreCase("Ingenieria")).thenReturn(false);
        when(facultadRepository.save(any(Facultad.class))).thenAnswer(invocation -> {
            Facultad facultad = invocation.getArgument(0);
            facultad.setId(15L);
            return facultad;
        });

        FacultadResponse response = facultadService.crearFacultad(request, admin);

        assertThat(response.getId()).isEqualTo(15L);
        assertThat(response.getNombre()).isEqualTo("Ingenieria");
        assertThat(response.isActiva()).isTrue();
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaRechazarFacultadConNombreDuplicado() {
        CrearFacultadRequest request = DatosDePrueba.crearFacultadRequest("Ingenieria");
        when(facultadRepository.existsByNombreIgnoreCase("Ingenieria")).thenReturn(true);

        assertThatThrownBy(() -> facultadService.crearFacultad(request, admin))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("Ya existe");

        verify(facultadRepository, never()).save(any());
    }

    @Test
    void deberiaDesactivarFacultadSinProgramasActivos() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));

        facultadService.desactivarFacultad(1L, admin);

        assertThat(facultad.isActiva()).isFalse();
        verify(facultadRepository).save(facultad);
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaImpedirDesactivarFacultadConProgramasActivos() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        Programa programaActivo = DatosDePrueba.programa(2L, "Sistemas", facultad);
        programaActivo.setActivo(true);
        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));

        assertThatThrownBy(() -> facultadService.desactivarFacultad(1L, admin))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("programas activos");

        assertThat(facultad.isActiva()).isTrue();
        verify(facultadRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarNoEncontradoCuandoFacultadNoExiste() {
        when(facultadRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facultadService.obtenerPorId(404L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Facultad no encontrada");
    }

    @Test
    void deberiaListarFacultadesActivasMapeadas() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        PageRequest pageable = PageRequest.of(0, 10);
        when(facultadRepository.findByActivaTrue(pageable))
                .thenReturn(new PageImpl<>(List.of(facultad), pageable, 1));

        Page<FacultadResponse> response = facultadService.listar(pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().getNombre()).isEqualTo("Ingenieria");
    }
}
