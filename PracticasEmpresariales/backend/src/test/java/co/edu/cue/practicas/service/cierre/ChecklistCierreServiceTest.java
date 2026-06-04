package co.edu.cue.practicas.service.cierre;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.dto.response.ChecklistCierreResponse;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.DocumentoPractica;
import co.edu.cue.practicas.model.entity.Encuesta;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoEncuesta;
import co.edu.cue.practicas.model.enums.TipoDocumento;
import co.edu.cue.practicas.model.enums.TipoEncuesta;
import co.edu.cue.practicas.model.enums.TipoItemChecklist;
import co.edu.cue.practicas.repository.encuesta.EncuestaRepository;
import co.edu.cue.practicas.repository.evaluacion.EvaluacionDocenteRepository;
import co.edu.cue.practicas.repository.evaluacion.EvaluacionTutorRepository;
import co.edu.cue.practicas.repository.evaluacion.NotaFinalRepository;
import co.edu.cue.practicas.repository.practica.DocumentoPracticaRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.service.encuesta.EncuestaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChecklistCierreServiceTest {

    @Mock private PracticaRepository practicaRepository;
    @Mock private EvaluacionDocenteRepository evaluacionDocenteRepository;
    @Mock private EvaluacionTutorRepository evaluacionTutorRepository;
    @Mock private NotaFinalRepository notaFinalRepository;
    @Mock private EncuestaRepository encuestaRepository;
    @Mock private DocumentoPracticaRepository documentoRepository;
    @Mock private EncuestaService encuestaService;

    private ChecklistCierreService service;

    private Practica practica;
    private Usuario estudiante;
    private Usuario tutor;

    @BeforeEach
    void configurar() {
        service = new ChecklistCierreService(
                practicaRepository, evaluacionDocenteRepository, evaluacionTutorRepository,
                notaFinalRepository, encuestaRepository, documentoRepository, encuestaService);

        var facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        var programa = DatosDePrueba.programa(2L, "Sistemas", facultad);
        var docente  = DatosDePrueba.docenteAsesor();
        tutor        = DatosDePrueba.tutorEmpresarial();
        estudiante   = DatosDePrueba.usuario(5L, "Estudiante A", "est@cue.edu.co",
                co.edu.cue.practicas.model.enums.Rol.ESTUDIANTE);
        practica = DatosDePrueba.practica(7L, estudiante, programa, docente, tutor);
    }

    @Test
    void deberiaRetornarChecklistConTodosPendientesCuandoNadaSeHaCompletado() {
        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(encuestaRepository.findByPractica_Id(7L)).thenReturn(List.of());
        when(evaluacionDocenteRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(evaluacionTutorRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(notaFinalRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(documentoRepository.findByPractica_IdAndTipo(7L, TipoDocumento.CONVENIO)).thenReturn(List.of());
        when(documentoRepository.findByPractica_IdAndTipo(7L, TipoDocumento.CARTA_PRESENTACION)).thenReturn(List.of());
        when(documentoRepository.findByPractica_IdAndTipo(7L, TipoDocumento.PLAN)).thenReturn(List.of());
        when(documentoRepository.findByPractica_IdAndTipo(7L, TipoDocumento.INFORME_FINAL)).thenReturn(List.of());

        ChecklistCierreResponse response = service.evaluarChecklist(7L);

        assertThat(response.getItems()).hasSize(8);
        assertThat(response.getItemsCompletados()).isZero();
        assertThat(response.isPuedeEjecutarCierre()).isFalse();
    }

    @Test
    void deberiaHabilitarCierreCuandoTodosLosItemsEstanCompletados() {
        Encuesta tutorOk    = encuestaCompletada(TipoEncuesta.TUTOR_SATISFACCION, tutor);
        Encuesta satOk      = encuestaCompletada(TipoEncuesta.ESTUDIANTE_SATISFACCION, estudiante);
        Encuesta autoOk     = encuestaCompletada(TipoEncuesta.ESTUDIANTE_AUTOEVALUACION, estudiante);
        DocumentoPractica doc = DocumentoPractica.builder().id(1L).build();

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(encuestaRepository.findByPractica_Id(7L)).thenReturn(List.of(tutorOk, satOk, autoOk));
        when(evaluacionDocenteRepository.existsByPractica_Id(7L)).thenReturn(true);
        when(evaluacionTutorRepository.existsByPractica_Id(7L)).thenReturn(true);
        when(notaFinalRepository.existsByPractica_Id(7L)).thenReturn(true);
        when(documentoRepository.findByPractica_IdAndTipo(7L, TipoDocumento.CONVENIO)).thenReturn(List.of(doc));
        when(documentoRepository.findByPractica_IdAndTipo(7L, TipoDocumento.CARTA_PRESENTACION)).thenReturn(List.of(doc));
        when(documentoRepository.findByPractica_IdAndTipo(7L, TipoDocumento.PLAN)).thenReturn(List.of(doc));
        when(documentoRepository.findByPractica_IdAndTipo(7L, TipoDocumento.INFORME_FINAL)).thenReturn(List.of(doc));

        ChecklistCierreResponse response = service.evaluarChecklist(7L);

        assertThat(response.getItemsCompletados()).isEqualTo(8);
        assertThat(response.isPuedeEjecutarCierre()).isTrue();
    }

    @Test
    void deberiaMostrarEncuestaEnBorradorComoNoCompletada() {
        Encuesta tutorBorrador = encuesta(TipoEncuesta.TUTOR_SATISFACCION, tutor, EstadoEncuesta.EN_BORRADOR);
        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(encuestaRepository.findByPractica_Id(7L)).thenReturn(List.of(tutorBorrador));
        when(evaluacionDocenteRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(evaluacionTutorRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(notaFinalRepository.existsByPractica_Id(7L)).thenReturn(false);
        when(documentoRepository.findByPractica_IdAndTipo(7L, TipoDocumento.CONVENIO)).thenReturn(List.of());
        when(documentoRepository.findByPractica_IdAndTipo(7L, TipoDocumento.CARTA_PRESENTACION)).thenReturn(List.of());
        when(documentoRepository.findByPractica_IdAndTipo(7L, TipoDocumento.PLAN)).thenReturn(List.of());
        when(documentoRepository.findByPractica_IdAndTipo(7L, TipoDocumento.INFORME_FINAL)).thenReturn(List.of());

        ChecklistCierreResponse response = service.evaluarChecklist(7L);

        var encuestaTutorItem = response.getItems().stream()
                .filter(i -> i.getTipo() == TipoItemChecklist.ENCUESTA_TUTOR)
                .findFirst().orElseThrow();
        assertThat(encuestaTutorItem.getEstado()).isEqualTo("EN_BORRADOR");
        assertThat(encuestaTutorItem.isCompletado()).isFalse();
    }

    @Test
    void deberiaFallarRecordatorioEnItemQueNoEsEncuesta() {
        assertThatThrownBy(() -> service.enviarRecordatorioItem(7L, TipoItemChecklist.NOTA_DOCENTE, null))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("no soporta recordatorios");
    }

    @Test
    void deberiaFallarSiPracticaNoExiste() {
        when(practicaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.evaluarChecklist(999L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    private Encuesta encuestaCompletada(TipoEncuesta tipo, Usuario destinatario) {
        return encuesta(tipo, destinatario, EstadoEncuesta.COMPLETADA);
    }

    private Encuesta encuesta(TipoEncuesta tipo, Usuario destinatario, EstadoEncuesta estado) {
        return Encuesta.builder()
                .id((long) tipo.ordinal() + 10)
                .practica(practica)
                .tipo(tipo)
                .destinatario(destinatario)
                .estado(estado)
                .build();
    }
}
