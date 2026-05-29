package co.edu.cue.practicas.service.vacante;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.CrearVacanteRequest;
import co.edu.cue.practicas.dto.request.DecisionVacanteRequest;
import co.edu.cue.practicas.dto.request.EditarVacanteRequest;
import co.edu.cue.practicas.dto.response.BitacoraVacanteResponse;
import co.edu.cue.practicas.dto.response.VacanteResponse;
import co.edu.cue.practicas.event.VacanteAprobadaEvent;
import co.edu.cue.practicas.event.VacanteCreadaEvent;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.BitacoraVacante;
import co.edu.cue.practicas.model.entity.Empresa;
import co.edu.cue.practicas.model.entity.Facultad;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.model.entity.TutorEmpresarial;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.entity.Vacante;
import co.edu.cue.practicas.model.enums.EstadoVacante;
import co.edu.cue.practicas.model.enums.JornadaVacante;
import co.edu.cue.practicas.model.enums.ModalidadVacante;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.repository.empresa.EmpresaRepository;
import co.edu.cue.practicas.repository.programa.ProgramaRepository;
import co.edu.cue.practicas.repository.tutor.TutorEmpresarialRepository;
import co.edu.cue.practicas.repository.vacante.BitacoraVacanteRepository;
import co.edu.cue.practicas.repository.vacante.VacanteRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VacanteServiceTest {

    @Mock
    private VacanteRepository vacanteRepository;

    @Mock
    private BitacoraVacanteRepository bitacoraVacanteRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private ProgramaRepository programaRepository;

    @Mock
    private TutorEmpresarialRepository tutorRepository;

    @Mock
    private VacanteAccessProxy accessProxy;

    @Mock
    private AuditoriaLogger auditoriaLogger;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private VacanteService vacanteService;
    private CustomUserDetails admin;
    private Empresa empresa;
    private Programa programa;
    private TutorEmpresarial tutor;

    @BeforeEach
    void configurar() {
        vacanteService = new VacanteService(
                vacanteRepository,
                bitacoraVacanteRepository,
                empresaRepository,
                programaRepository,
                tutorRepository,
                accessProxy,
                auditoriaLogger,
                eventPublisher,
                new ObjectMapper());
        admin = DatosDePrueba.userDetails(DatosDePrueba.administradorDti());
        empresa = empresa(true);
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        programa = DatosDePrueba.programa(2L, "Ingenieria de Sistemas", facultad);
        tutor = tutor(empresa);
    }

    @Test
    void deberiaCrearVacantePendienteYRegistrarBitacora() {
        CrearVacanteRequest request = crearVacanteRequest();
        when(empresaRepository.findById(8L)).thenReturn(Optional.of(empresa));
        when(programaRepository.findById(2L)).thenReturn(Optional.of(programa));
        when(tutorRepository.findById(5L)).thenReturn(Optional.of(tutor));
        when(vacanteRepository.save(any(Vacante.class))).thenAnswer(invocation -> {
            Vacante vacante = invocation.getArgument(0);
            vacante.setId(20L);
            return vacante;
        });

        VacanteResponse response = vacanteService.crearVacante(request, admin);

        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getEstado()).isEqualTo(EstadoVacante.PENDIENTE);
        assertThat(response.getEmpresaId()).isEqualTo(8L);
        assertThat(response.getTutorId()).isEqualTo(5L);

        ArgumentCaptor<Vacante> vacanteCaptor = ArgumentCaptor.forClass(Vacante.class);
        verify(vacanteRepository).save(vacanteCaptor.capture());
        Vacante guardada = vacanteCaptor.getValue();
        assertThat(guardada.getTitulo()).isEqualTo("Practicante Backend Java");
        assertThat(guardada.getCupos()).isEqualTo(2);
        assertThat(guardada.isRemunerada()).isTrue();
        assertThat(guardada.getEstado()).isEqualTo(EstadoVacante.PENDIENTE);

        verify(bitacoraVacanteRepository).save(any(BitacoraVacante.class));
        verify(eventPublisher).publishEvent(any(VacanteCreadaEvent.class));
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaRechazarCreacionParaEmpresaDesactivada() {
        CrearVacanteRequest request = crearVacanteRequest();
        when(empresaRepository.findById(8L)).thenReturn(Optional.of(empresa(false)));

        assertThatThrownBy(() -> vacanteService.crearVacante(request, admin))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("empresa desactivada");

        verify(vacanteRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deberiaRechazarCreacionCuandoTutorNoPerteneceALaEmpresa() {
        CrearVacanteRequest request = crearVacanteRequest();
        when(empresaRepository.findById(8L)).thenReturn(Optional.of(empresa));
        when(programaRepository.findById(2L)).thenReturn(Optional.of(programa));
        when(tutorRepository.findById(5L)).thenReturn(Optional.of(tutor(empresaConId(99L))));

        assertThatThrownBy(() -> vacanteService.crearVacante(request, admin))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("El tutor no pertenece");

        verify(vacanteRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarNoEncontradoCuandoProgramaNoExisteAlCrear() {
        CrearVacanteRequest request = crearVacanteRequest();
        when(empresaRepository.findById(8L)).thenReturn(Optional.of(empresa));
        when(programaRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vacanteService.crearVacante(request, admin))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Programa no encontrado: 2");

        verify(vacanteRepository, never()).save(any());
    }

    @Test
    void deberiaAprobarVacantePendiente() {
        Vacante vacante = vacante(EstadoVacante.PENDIENTE);
        when(vacanteRepository.findById(20L)).thenReturn(Optional.of(vacante));
        when(vacanteRepository.save(vacante)).thenReturn(vacante);

        VacanteResponse response = vacanteService.aprobarVacante(20L, decision("Cumple requisitos"), admin);

        assertThat(response.getEstado()).isEqualTo(EstadoVacante.DISPONIBLE);
        assertThat(vacante.getAprobador()).isSameAs(admin.getUsuario());
        assertThat(vacante.getFechaDecision()).isNotNull();
        verify(bitacoraVacanteRepository).save(any(BitacoraVacante.class));
        verify(eventPublisher).publishEvent(any(VacanteAprobadaEvent.class));
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaRechazarSolicitudSinMotivoAntesDeConsultarRepositorio() {
        assertThatThrownBy(() -> vacanteService.rechazarVacante(20L, decision(" "), admin))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("motivo del rechazo");

        verify(vacanteRepository, never()).findById(20L);
        verify(vacanteRepository, never()).save(any());
    }

    @Test
    void deberiaImpedirCerrarVacantePendiente() {
        Vacante vacante = vacante(EstadoVacante.PENDIENTE);
        when(vacanteRepository.findById(20L)).thenReturn(Optional.of(vacante));

        assertThatThrownBy(() -> vacanteService.cerrarVacante(20L, decision("Cierre manual"), admin))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("No se puede cerrar");

        assertThat(vacante.getEstado()).isEqualTo(EstadoVacante.PENDIENTE);
        verify(vacanteRepository, never()).save(any());
    }

    @Test
    void deberiaEditarVacanteRechazadaYReabrirlaComoPendiente() {
        Vacante vacante = vacante(EstadoVacante.RECHAZADA);
        vacante.setMotivoRechazo("Faltan requisitos");
        vacante.setAprobador(admin.getUsuario());
        when(vacanteRepository.findById(20L)).thenReturn(Optional.of(vacante));
        when(vacanteRepository.save(vacante)).thenReturn(vacante);

        VacanteResponse response = vacanteService.editarVacante(20L, editarVacanteRequest(), admin);

        assertThat(response.getEstado()).isEqualTo(EstadoVacante.PENDIENTE);
        assertThat(vacante.getMotivoRechazo()).isNull();
        assertThat(vacante.getAprobador()).isNull();
        assertThat(vacante.getTitulo()).isEqualTo("Practicante Backend Java Editada");
        verify(bitacoraVacanteRepository).save(any(BitacoraVacante.class));
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaListarVacantesDelegandoFiltroAlProxy() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(accessProxy.listarSegunRol(admin, EstadoVacante.DISPONIBLE, pageable))
                .thenReturn(new PageImpl<>(List.of(vacante(EstadoVacante.DISPONIBLE)), pageable, 1));

        Page<VacanteResponse> response = vacanteService.listar(EstadoVacante.DISPONIBLE, pageable, admin);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().getEstado()).isEqualTo(EstadoVacante.DISPONIBLE);
    }

    @Test
    void deberiaConsultarHistorialDespuesDeValidarAcceso() {
        Vacante vacante = vacante(EstadoVacante.DISPONIBLE);
        BitacoraVacante bitacora = BitacoraVacante.builder()
                .id(1L)
                .vacante(vacante)
                .usuario(admin.getUsuario())
                .estadoAnterior(EstadoVacante.PENDIENTE)
                .estadoNuevo(EstadoVacante.DISPONIBLE)
                .motivo("Cumple requisitos")
                .build();
        when(vacanteRepository.findById(20L)).thenReturn(Optional.of(vacante));
        when(bitacoraVacanteRepository.findByVacante_IdOrderByFechaHoraDesc(20L))
                .thenReturn(List.of(bitacora));

        List<BitacoraVacanteResponse> response = vacanteService.historial(20L, admin);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getEstadoNuevo()).isEqualTo(EstadoVacante.DISPONIBLE);
        verify(accessProxy).verificarAccesoLectura(admin, vacante);
    }

    private CrearVacanteRequest crearVacanteRequest() {
        CrearVacanteRequest request = new CrearVacanteRequest();
        request.setTitulo("Practicante Backend Java");
        request.setDescripcion("Apoyo en desarrollo de servicios");
        request.setEmpresaId(8L);
        request.setTutorId(5L);
        request.setProgramaId(2L);
        request.setModalidad(ModalidadVacante.HIBRIDO);
        request.setJornada(JornadaVacante.MEDIO_TIEMPO);
        request.setCiudad("Armenia");
        request.setCupos(2);
        request.setDuracionMeses(6);
        request.setRemunerada(true);
        request.setValorRemuneracion(1300000.0);
        request.setFechaInicio(LocalDate.of(2026, 6, 15));
        request.setFechaFin(LocalDate.of(2026, 12, 15));
        request.setRequisitos("Java basico");
        request.setResponsabilidades("Construir endpoints");
        return request;
    }

    private EditarVacanteRequest editarVacanteRequest() {
        EditarVacanteRequest request = new EditarVacanteRequest();
        request.setTitulo("Practicante Backend Java Editada");
        request.setDescripcion("Apoyo en desarrollo de APIs");
        request.setModalidad(ModalidadVacante.REMOTO);
        request.setJornada(JornadaVacante.MEDIO_TIEMPO);
        request.setCiudad("Pereira");
        request.setCupos(3);
        request.setDuracionMeses(6);
        request.setRemunerada(false);
        request.setFechaInicio(LocalDate.of(2026, 6, 20));
        request.setFechaFin(LocalDate.of(2026, 12, 20));
        request.setRequisitos("Java y SQL");
        request.setResponsabilidades("Construir y probar endpoints");
        return request;
    }

    private DecisionVacanteRequest decision(String motivo) {
        DecisionVacanteRequest request = new DecisionVacanteRequest();
        request.setMotivo(motivo);
        return request;
    }

    private Vacante vacante(EstadoVacante estado) {
        return Vacante.builder()
                .id(20L)
                .titulo("Practicante Backend Java")
                .descripcion("Apoyo en desarrollo de servicios")
                .empresa(empresa)
                .tutor(tutor)
                .programa(programa)
                .modalidad(ModalidadVacante.HIBRIDO)
                .jornada(JornadaVacante.MEDIO_TIEMPO)
                .ciudad("Armenia")
                .cupos(2)
                .cuposOcupados(0)
                .duracionMeses(6)
                .remunerada(true)
                .valorRemuneracion(1300000.0)
                .fechaInicio(LocalDate.of(2026, 6, 15))
                .fechaFin(LocalDate.of(2026, 12, 15))
                .requisitos("Java basico")
                .responsabilidades("Construir endpoints")
                .estado(estado)
                .build();
    }

    private TutorEmpresarial tutor(Empresa empresa) {
        return TutorEmpresarial.builder()
                .id(5L)
                .empresa(empresa)
                .usuario(Usuario.builder()
                        .id(15L)
                        .nombre("Laura Tutor")
                        .correo("laura.tutor@empresa.com")
                        .passwordHash("hash")
                        .rol(Rol.TUTOR_EMPRESARIAL)
                        .activo(true)
                        .build())
                .cargo("Lider de Desarrollo")
                .activo(true)
                .build();
    }

    private Empresa empresa(boolean activo) {
        return empresaConId(8L, activo);
    }

    private Empresa empresaConId(Long id) {
        return empresaConId(id, true);
    }

    private Empresa empresaConId(Long id, boolean activo) {
        return Empresa.builder()
                .id(id)
                .nit("900123456-7")
                .razonSocial("Soft CUE SAS")
                .correoContacto("contacto@softcue.com")
                .activo(activo)
                .build();
    }
}
