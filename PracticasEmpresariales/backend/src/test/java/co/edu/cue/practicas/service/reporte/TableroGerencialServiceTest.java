package co.edu.cue.practicas.service.reporte;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.dto.response.TableroGerencialResponse;
import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.model.entity.Facultad;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.ResultadoNotaFinal;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.repository.empresa.EmpresaRepository;
import co.edu.cue.practicas.repository.evaluacion.NotaFinalRepository;
import co.edu.cue.practicas.repository.facultad.FacultadRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableroGerencialServiceTest {

    @Mock private PracticaRepository practicaRepository;
    @Mock private NotaFinalRepository notaFinalRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private FacultadRepository facultadRepository;

    private TableroGerencialService service;

    @BeforeEach
    void configurar() {
        service = new TableroGerencialService(
                practicaRepository, notaFinalRepository, empresaRepository, facultadRepository);
    }

    @Test
    void direccionDeberiaVerTodasLasFacultadesYTotalesGlobales() {
        CustomUserDetails direccion = DatosDePrueba.userDetails(
                DatosDePrueba.usuario(50L, "Dirección", "dir@cue.edu.co", Rol.DIRECCION));
        Facultad f1 = DatosDePrueba.facultad(1L, "Ingenieria");
        Facultad f2 = DatosDePrueba.facultad(2L, "Salud");

        Page<Facultad> page = new PageImpl<>(List.of(f1, f2));
        when(facultadRepository.findByActivaTrue(any())).thenReturn(page);
        when(practicaRepository.countByEstado(EstadoPractica.EN_CURSO)).thenReturn(40L);
        when(empresaRepository.countByActivoTrue()).thenReturn(12L);
        when(notaFinalRepository.countByResultado(ResultadoNotaFinal.APROBADO)).thenReturn(30L);
        when(notaFinalRepository.countByResultado(ResultadoNotaFinal.REPROBADO)).thenReturn(10L);
        when(notaFinalRepository.countByCerradaEnBetween(any(), any())).thenReturn(5L);
        when(notaFinalRepository.countByPractica_Programa_Facultad_IdAndResultado(any(), any()))
                .thenReturn(0L);
        when(practicaRepository.countByPrograma_Facultad_IdAndEstado(any(), any())).thenReturn(0L);

        TableroGerencialResponse r = service.obtener(
                LocalDateTime.now().minusDays(30), LocalDateTime.now(), direccion);

        assertThat(r.getTotalPracticantesActivos()).isEqualTo(40);
        assertThat(r.getEmpresasActivas()).isEqualTo(12);
        assertThat(r.getTotalAprobadas()).isEqualTo(30);
        assertThat(r.getTotalReprobadas()).isEqualTo(10);
        assertThat(r.getTasaAprobacionGlobal()).isEqualTo(75.0);
        assertThat(r.getPracticasCerradasEnPeriodo()).isEqualTo(5);
        assertThat(r.getPorFacultad()).hasSize(2);
    }

    @Test
    void coordinacionAcademicaDeberiaVerSoloSuFacultad() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        Usuario coord = DatosDePrueba.usuario(70L, "Coord Ing", "ing@cue.edu.co",
                Rol.COORDINACION_ACADEMICA);
        coord.setFacultad(facultad);
        CustomUserDetails details = DatosDePrueba.userDetails(coord);

        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));
        when(practicaRepository.countByPrograma_Facultad_IdAndEstado(1L, EstadoPractica.EN_CURSO))
                .thenReturn(25L);
        when(notaFinalRepository.countByPractica_Programa_Facultad_IdAndResultado(
                1L, ResultadoNotaFinal.APROBADO)).thenReturn(20L);
        when(notaFinalRepository.countByPractica_Programa_Facultad_IdAndResultado(
                1L, ResultadoNotaFinal.REPROBADO)).thenReturn(5L);

        TableroGerencialResponse r = service.obtener(null, null, details);

        assertThat(r.getTotalPracticantesActivos()).isEqualTo(25);
        assertThat(r.getEmpresasActivas()).isEqualTo(-1L); // no aplica con scope facultad
        assertThat(r.getPorFacultad()).hasSize(1);
        assertThat(r.getPorFacultad().get(0).getFacultadId()).isEqualTo(1L);
        assertThat(r.getTasaAprobacionGlobal()).isEqualTo(80.0);
    }

    @Test
    void coordinacionAcademicaSinFacultadDeberiaFallar() {
        Usuario coord = DatosDePrueba.usuario(70L, "Coord", "c@cue.edu.co",
                Rol.COORDINACION_ACADEMICA);
        CustomUserDetails details = DatosDePrueba.userDetails(coord);

        assertThatThrownBy(() -> service.obtener(null, null, details))
                .isInstanceOf(AccesoNoAutorizadoException.class);
    }

    @Test
    void tasaAprobacionDeberiaSerCeroCuandoNoHayCalificadas() {
        CustomUserDetails direccion = DatosDePrueba.userDetails(
                DatosDePrueba.usuario(50L, "Dirección", "dir@cue.edu.co", Rol.DIRECCION));

        when(facultadRepository.findByActivaTrue(any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(practicaRepository.countByEstado(any())).thenReturn(5L);
        when(empresaRepository.countByActivoTrue()).thenReturn(0L);
        when(notaFinalRepository.countByResultado(any())).thenReturn(0L);

        TableroGerencialResponse r = service.obtener(null, null, direccion);

        assertThat(r.getTasaAprobacionGlobal()).isZero();
    }
}
