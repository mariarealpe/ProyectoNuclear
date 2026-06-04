package co.edu.cue.practicas.service.cierre;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.EjecutarCierrePracticaRequest;
import co.edu.cue.practicas.dto.response.ChecklistCierreResponse;
import co.edu.cue.practicas.dto.response.CierrePracticaResponse;
import co.edu.cue.practicas.event.CierrePracticaEvent;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.entity.DocumentoPractica;
import co.edu.cue.practicas.model.entity.NotaFinal;
import co.edu.cue.practicas.model.entity.NotificacionHistorial;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.ResultadoNotaFinal;
import co.edu.cue.practicas.model.enums.TipoEventoNotificacion;
import co.edu.cue.practicas.repository.evaluacion.NotaFinalRepository;
import co.edu.cue.practicas.repository.notificacion.NotificacionHistorialRepository;
import co.edu.cue.practicas.repository.practica.DocumentoPracticaRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.notificacion.PlantillaNotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CierrePracticaServiceTest {

    @Mock private PracticaRepository practicaRepository;
    @Mock private NotaFinalRepository notaFinalRepository;
    @Mock private DocumentoPracticaRepository documentoRepository;
    @Mock private NotificacionHistorialRepository notificacionRepository;
    @Mock private ChecklistCierreService checklistService;
    @Mock private PlantillaNotificacionService plantillaService;
    @Mock private AuditoriaLogger auditoriaLogger;
    @Mock private ApplicationEventPublisher eventPublisher;

    private CierrePracticaService service;

    private Practica practica;
    private NotaFinal notaFinal;
    private CustomUserDetails coordinador;

    @BeforeEach
    void configurar() {
        service = new CierrePracticaService(
                practicaRepository, notaFinalRepository, documentoRepository,
                notificacionRepository, checklistService, plantillaService,
                auditoriaLogger, eventPublisher);

        var facultad   = DatosDePrueba.facultad(1L, "Ingenieria");
        var programa   = DatosDePrueba.programa(2L, "Sistemas", facultad);
        var docente    = DatosDePrueba.docenteAsesor();
        var tutor      = DatosDePrueba.tutorEmpresarial();
        var estudiante = DatosDePrueba.usuario(5L, "Estudiante A", "est@cue.edu.co",
                co.edu.cue.practicas.model.enums.Rol.ESTUDIANTE);
        Usuario coord  = DatosDePrueba.coordinadorPracticas();
        practica    = DatosDePrueba.practica(7L, estudiante, programa, docente, tutor);
        notaFinal   = DatosDePrueba.notaFinal(99L, practica, coord);
        coordinador = DatosDePrueba.userDetails(coord);
    }

    private EjecutarCierrePracticaRequest requestConfirmado() {
        EjecutarCierrePracticaRequest r = new EjecutarCierrePracticaRequest();
        r.setConfirmar(true);
        return r;
    }

    private ChecklistCierreResponse checklistCompleto() {
        return ChecklistCierreResponse.builder()
                .practicaId(7L).items(java.util.List.of())
                .puedeEjecutarCierre(true).totalItems(8).itemsCompletados(8).build();
    }

    @Test
    void deberiaEjecutarCierreCuandoChecklistEstaCompletoYConfirmado() {
        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(checklistService.evaluarChecklist(7L)).thenReturn(checklistCompleto());
        when(notaFinalRepository.findByPractica_Id(7L)).thenReturn(Optional.of(notaFinal));
        when(practicaRepository.save(any(Practica.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notaFinalRepository.save(any(NotaFinal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(plantillaService.renderizarParaEvento(any(), any()))
                .thenReturn(new PlantillaNotificacionService.RenderResult(
                        "Cierre", "<p>resultado</p>", false, 3));

        CierrePracticaResponse response = service.ejecutarCierre(7L, requestConfirmado(), coordinador);

        assertThat(response.getEstadoPractica()).isEqualTo(EstadoPractica.FINALIZADA);
        assertThat(response.getResultado()).isEqualTo(ResultadoNotaFinal.APROBADO);
        assertThat(response.getEtiquetaResultado()).contains("Completada");
        assertThat(practica.getNotasCerradas()).isTrue();
        assertThat(notaFinal.getCerrada()).isTrue();
        verify(eventPublisher).publishEvent(any(CierrePracticaEvent.class));
        verify(notificacionRepository, atLeastOnce()).save(any(NotificacionHistorial.class));
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaEtiquetarComoReprobadaCuandoNotaFinalEsReprobado() {
        notaFinal.setResultado(ResultadoNotaFinal.REPROBADO);
        notaFinal.setNota(2.5);
        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(checklistService.evaluarChecklist(7L)).thenReturn(checklistCompleto());
        when(notaFinalRepository.findByPractica_Id(7L)).thenReturn(Optional.of(notaFinal));
        when(practicaRepository.save(any(Practica.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notaFinalRepository.save(any(NotaFinal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(plantillaService.renderizarParaEvento(any(), any())).thenReturn(null);

        CierrePracticaResponse response = service.ejecutarCierre(7L, requestConfirmado(), coordinador);

        assertThat(response.getResultado()).isEqualTo(ResultadoNotaFinal.REPROBADO);
        assertThat(response.getEtiquetaResultado()).contains("Reprobada");
    }

    @Test
    void deberiaFallarSiNoSeConfirmaExplicitamente() {
        EjecutarCierrePracticaRequest req = new EjecutarCierrePracticaRequest();
        req.setConfirmar(false);

        assertThatThrownBy(() -> service.ejecutarCierre(7L, req, coordinador))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("confirmación explícita");

        verify(practicaRepository, never()).findById(any());
    }

    @Test
    void deberiaFallarSiPracticaNoEstaEnCurso() {
        practica.setEstado(EstadoPractica.FINALIZADA);
        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));

        assertThatThrownBy(() -> service.ejecutarCierre(7L, requestConfirmado(), coordinador))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("EN_CURSO");
    }

    @Test
    void deberiaFallarSiChecklistIncompleto() {
        ChecklistCierreResponse incompleto = ChecklistCierreResponse.builder()
                .practicaId(7L).items(java.util.List.of())
                .puedeEjecutarCierre(false).totalItems(8).itemsCompletados(5).build();

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(checklistService.evaluarChecklist(7L)).thenReturn(incompleto);

        assertThatThrownBy(() -> service.ejecutarCierre(7L, requestConfirmado(), coordinador))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("faltan 3");
    }

    @Test
    void deberiaFallarSiNoExisteNotaFinal() {
        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(checklistService.evaluarChecklist(7L)).thenReturn(checklistCompleto());
        when(notaFinalRepository.findByPractica_Id(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.ejecutarCierre(7L, requestConfirmado(), coordinador))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("nota final");
    }

    @Test
    void deberiaGuardarActaCuandoSeProveeURL() {
        EjecutarCierrePracticaRequest req = requestConfirmado();
        req.setUrlActaCierre("https://example.com/acta.pdf");
        req.setNombreActaCierre("acta.pdf");

        when(practicaRepository.findById(7L)).thenReturn(Optional.of(practica));
        when(checklistService.evaluarChecklist(7L)).thenReturn(checklistCompleto());
        when(notaFinalRepository.findByPractica_Id(7L)).thenReturn(Optional.of(notaFinal));
        when(practicaRepository.save(any(Practica.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notaFinalRepository.save(any(NotaFinal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(plantillaService.renderizarParaEvento(any(), any())).thenReturn(null);

        service.ejecutarCierre(7L, req, coordinador);

        verify(documentoRepository).save(any(DocumentoPractica.class));
    }
}
