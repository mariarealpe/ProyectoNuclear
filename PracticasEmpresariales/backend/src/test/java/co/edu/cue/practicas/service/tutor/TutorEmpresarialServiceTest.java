package co.edu.cue.practicas.service.tutor;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.CrearTutorEmpresarialRequest;
import co.edu.cue.practicas.dto.request.EditarTutorEmpresarialRequest;
import co.edu.cue.practicas.dto.response.TutorEmpresarialResponse;
import co.edu.cue.practicas.event.UsuarioCreadoEvent;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.Empresa;
import co.edu.cue.practicas.model.entity.TutorEmpresarial;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.repository.empresa.EmpresaRepository;
import co.edu.cue.practicas.repository.tutor.TutorEmpresarialRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TutorEmpresarialServiceTest {

    @Mock
    private TutorEmpresarialRepository tutorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditoriaLogger auditoriaLogger;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TutorEmpresarialService tutorService;
    private CustomUserDetails admin;

    @BeforeEach
    void configurar() {
        tutorService = new TutorEmpresarialService(
                tutorRepository,
                usuarioRepository,
                empresaRepository,
                passwordEncoder,
                auditoriaLogger,
                eventPublisher,
                new ObjectMapper());
        admin = DatosDePrueba.userDetails(DatosDePrueba.administradorDti());
    }

    @Test
    void deberiaCrearTutorConUsuarioTemporalYPublicarEvento() {
        CrearTutorEmpresarialRequest request = crearTutorRequest();
        Empresa empresa = empresa(true);

        when(usuarioRepository.existsByCorreo("laura.tutor@empresa.com")).thenReturn(false);
        when(empresaRepository.findById(8L)).thenReturn(Optional.of(empresa));
        when(passwordEncoder.encode(anyString())).thenReturn("hash-temporal");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(15L);
            return usuario;
        });
        when(tutorRepository.save(any(TutorEmpresarial.class))).thenAnswer(invocation -> {
            TutorEmpresarial tutor = invocation.getArgument(0);
            tutor.setId(5L);
            return tutor;
        });

        TutorEmpresarialResponse response = tutorService.crearTutor(request, admin);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getUsuarioId()).isEqualTo(15L);
        assertThat(response.getEmpresaId()).isEqualTo(8L);
        assertThat(response.getCargo()).isEqualTo("Lider de Desarrollo");

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario usuarioGuardado = usuarioCaptor.getValue();
        assertThat(usuarioGuardado.getRol()).isEqualTo(Rol.TUTOR_EMPRESARIAL);
        assertThat(usuarioGuardado.getPasswordHash()).isEqualTo("hash-temporal");
        assertThat(usuarioGuardado.isPrimerIngreso()).isTrue();

        ArgumentCaptor<TutorEmpresarial> tutorCaptor = ArgumentCaptor.forClass(TutorEmpresarial.class);
        verify(tutorRepository).save(tutorCaptor.capture());
        assertThat(tutorCaptor.getValue().getEmpresa()).isSameAs(empresa);

        verify(eventPublisher).publishEvent(any(UsuarioCreadoEvent.class));
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaRechazarCreacionCuandoCorreoYaExiste() {
        CrearTutorEmpresarialRequest request = crearTutorRequest();
        when(usuarioRepository.existsByCorreo("laura.tutor@empresa.com")).thenReturn(true);

        assertThatThrownBy(() -> tutorService.crearTutor(request, admin))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("correo ya");

        verify(usuarioRepository, never()).save(any());
        verify(tutorRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarNoEncontradoCuandoEmpresaNoExisteAlCrear() {
        CrearTutorEmpresarialRequest request = crearTutorRequest();
        when(usuarioRepository.existsByCorreo("laura.tutor@empresa.com")).thenReturn(false);
        when(empresaRepository.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tutorService.crearTutor(request, admin))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Empresa no encontrada: 8");

        verify(usuarioRepository, never()).save(any());
        verify(tutorRepository, never()).save(any());
    }

    @Test
    void deberiaRechazarCreacionParaEmpresaDesactivada() {
        CrearTutorEmpresarialRequest request = crearTutorRequest();
        when(usuarioRepository.existsByCorreo("laura.tutor@empresa.com")).thenReturn(false);
        when(empresaRepository.findById(8L)).thenReturn(Optional.of(empresa(false)));

        assertThatThrownBy(() -> tutorService.crearTutor(request, admin))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("empresa desactivada");

        verify(usuarioRepository, never()).save(any());
        verify(tutorRepository, never()).save(any());
    }

    @Test
    void deberiaEditarTutorYSincronizarUsuario() {
        TutorEmpresarial tutor = tutor();
        EditarTutorEmpresarialRequest request = editarTutorRequest();
        when(tutorRepository.findById(5L)).thenReturn(Optional.of(tutor));

        TutorEmpresarialResponse response = tutorService.editarTutor(5L, request, admin);

        assertThat(response.getNombre()).isEqualTo("Laura Tutor Editada");
        assertThat(response.getCargo()).isEqualTo("Gerente TI");
        assertThat(tutor.getUsuario().getTelefono()).isEqualTo("3110000000");
        assertThat(tutor.getArea()).isEqualTo("Tecnologia");
        verify(usuarioRepository).save(tutor.getUsuario());
        verify(tutorRepository).save(tutor);
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaDesactivarTutorYUsuarioAsociado() {
        TutorEmpresarial tutor = tutor();
        when(tutorRepository.findById(5L)).thenReturn(Optional.of(tutor));

        tutorService.desactivarTutor(5L, admin);

        assertThat(tutor.isActivo()).isFalse();
        assertThat(tutor.getUsuario().isActivo()).isFalse();
        verify(tutorRepository).save(tutor);
        verify(usuarioRepository).save(tutor.getUsuario());
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaActivarTutorYUsuarioAsociado() {
        TutorEmpresarial tutor = tutor();
        tutor.setActivo(false);
        tutor.getUsuario().setActivo(false);
        when(tutorRepository.findById(5L)).thenReturn(Optional.of(tutor));

        tutorService.activarTutor(5L, admin);

        assertThat(tutor.isActivo()).isTrue();
        assertThat(tutor.getUsuario().isActivo()).isTrue();
        verify(tutorRepository).save(tutor);
        verify(usuarioRepository).save(tutor.getUsuario());
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaListarTutoresPorEmpresa() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(tutorRepository.findByEmpresa_Id(8L, pageable))
                .thenReturn(new PageImpl<>(List.of(tutor()), pageable, 1));

        Page<TutorEmpresarialResponse> response = tutorService.listar(8L, pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().getEmpresaId()).isEqualTo(8L);
    }

    private CrearTutorEmpresarialRequest crearTutorRequest() {
        CrearTutorEmpresarialRequest request = new CrearTutorEmpresarialRequest();
        request.setNombre("Laura Tutor");
        request.setCorreo("laura.tutor@empresa.com");
        request.setTelefono("3001234567");
        request.setEmpresaId(8L);
        request.setCargo("Lider de Desarrollo");
        request.setArea("TI");
        request.setTelefonoCorporativo("6061234567");
        request.setEsResponsablePrincipal(true);
        return request;
    }

    private EditarTutorEmpresarialRequest editarTutorRequest() {
        EditarTutorEmpresarialRequest request = new EditarTutorEmpresarialRequest();
        request.setNombre("Laura Tutor Editada");
        request.setTelefono("3110000000");
        request.setCargo("Gerente TI");
        request.setArea("Tecnologia");
        request.setTelefonoCorporativo("6067654321");
        request.setEsResponsablePrincipal(false);
        return request;
    }

    private TutorEmpresarial tutor() {
        return TutorEmpresarial.builder()
                .id(5L)
                .usuario(Usuario.builder()
                        .id(15L)
                        .nombre("Laura Tutor")
                        .correo("laura.tutor@empresa.com")
                        .passwordHash("hash")
                        .telefono("3001234567")
                        .rol(Rol.TUTOR_EMPRESARIAL)
                        .activo(true)
                        .primerIngreso(true)
                        .build())
                .empresa(empresa(true))
                .cargo("Lider de Desarrollo")
                .area("TI")
                .telefonoCorporativo("6061234567")
                .esResponsablePrincipal(true)
                .activo(true)
                .build();
    }

    private Empresa empresa(boolean activo) {
        return Empresa.builder()
                .id(8L)
                .nit("900123456-7")
                .razonSocial("Soft CUE SAS")
                .correoContacto("contacto@softcue.com")
                .activo(activo)
                .build();
    }
}
