package co.edu.cue.practicas.service.reporte;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.dto.request.FiltrosReporteEstadoRequest;
import co.edu.cue.practicas.dto.response.ReporteEstadoResponse;
import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoEstudiante;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.FormatoExport;
import co.edu.cue.practicas.model.enums.ResultadoNotaFinal;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.repository.evaluacion.NotaFinalRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteEstadoServiceTest {

    @Mock private PracticaRepository practicaRepository;
    @Mock private NotaFinalRepository notaFinalRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private ReporteEstadoService service;

    @BeforeEach
    void configurar() {
        service = new ReporteEstadoService(
                practicaRepository, notaFinalRepository, usuarioRepository,
                List.of(new ExportadorCsv(), new ExportadorPdfStub()));
    }

    @Test
    void direccionDeberiaVerTotalesGlobalesSinFiltros() {
        CustomUserDetails direccion = DatosDePrueba.userDetails(
                DatosDePrueba.usuario(50L, "Dirección", "dir@cue.edu.co", Rol.DIRECCION));

        when(usuarioRepository.countByRolAndEstadoEstudiante(Rol.ESTUDIANTE, EstadoEstudiante.APTO))
                .thenReturn(15L);
        when(practicaRepository.countByEstado(EstadoPractica.EN_CURSO)).thenReturn(20L);
        when(notaFinalRepository.countByResultado(ResultadoNotaFinal.APROBADO)).thenReturn(8L);
        when(notaFinalRepository.countByResultado(ResultadoNotaFinal.REPROBADO)).thenReturn(2L);

        ReporteEstadoResponse r = service.generar(new FiltrosReporteEstadoRequest(), direccion);

        assertThat(r.getAptosSinIniciar()).isEqualTo(15);
        assertThat(r.getEnPractica()).isEqualTo(20);
        assertThat(r.getCompletados()).isEqualTo(8);
        assertThat(r.getReprobados()).isEqualTo(2);
        assertThat(r.getTotal()).isEqualTo(45);
    }

    @Test
    void coordinadorPracticasDeberiaQuedarRestringidoASuPrograma() {
        var facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        Programa programa = DatosDePrueba.programa(2L, "Sistemas", facultad);
        Usuario coord = DatosDePrueba.coordinadorPracticas();
        coord.setPrograma(programa);
        CustomUserDetails details = DatosDePrueba.userDetails(coord);

        when(usuarioRepository.countByRolAndEstadoEstudianteAndPrograma_Id(
                Rol.ESTUDIANTE, EstadoEstudiante.APTO, 2L)).thenReturn(5L);
        when(practicaRepository.countByPrograma_IdAndEstado(2L, EstadoPractica.EN_CURSO)).thenReturn(7L);
        when(notaFinalRepository.countByPractica_Programa_IdAndResultado(2L, ResultadoNotaFinal.APROBADO))
                .thenReturn(3L);
        when(notaFinalRepository.countByPractica_Programa_IdAndResultado(2L, ResultadoNotaFinal.REPROBADO))
                .thenReturn(1L);

        ReporteEstadoResponse r = service.generar(new FiltrosReporteEstadoRequest(), details);

        assertThat(r.getFiltros().getProgramaId()).isEqualTo(2L);
        assertThat(r.getAptosSinIniciar()).isEqualTo(5);
        assertThat(r.getCompletados()).isEqualTo(3);
    }

    @Test
    void coordinadorPracticasDeberiaRechazarFiltroDeOtroPrograma() {
        var facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        Programa programa = DatosDePrueba.programa(2L, "Sistemas", facultad);
        Usuario coord = DatosDePrueba.coordinadorPracticas();
        coord.setPrograma(programa);
        CustomUserDetails details = DatosDePrueba.userDetails(coord);

        FiltrosReporteEstadoRequest req = new FiltrosReporteEstadoRequest();
        req.setProgramaId(999L);

        assertThatThrownBy(() -> service.generar(req, details))
                .isInstanceOf(AccesoNoAutorizadoException.class);
    }

    @Test
    void exportarCsvDeberiaRetornarBytesUtf8ConFormatoSemicolon() {
        CustomUserDetails direccion = DatosDePrueba.userDetails(
                DatosDePrueba.usuario(50L, "Dirección", "dir@cue.edu.co", Rol.DIRECCION));

        when(usuarioRepository.countByRolAndEstadoEstudiante(any(), any())).thenReturn(1L);
        when(practicaRepository.countByEstado(any())).thenReturn(2L);
        when(notaFinalRepository.countByResultado(any())).thenReturn(3L);

        ReporteEstadoService.ExportResult r = service.exportar(
                new FiltrosReporteEstadoRequest(), FormatoExport.CSV, direccion);

        String contenido = new String(r.bytes(), java.nio.charset.StandardCharsets.UTF_8);
        assertThat(contenido).contains("Reporte de Estado");
        assertThat(contenido).contains("Aptos sin iniciar;1");
        assertThat(r.contentType()).startsWith("text/csv");
        assertThat(r.fileName()).endsWith(".csv");
    }
}
