package co.edu.cue.practicas.service.usuario;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.CrearUsuarioRequest;
import co.edu.cue.practicas.dto.request.EditarUsuarioRequest;
import co.edu.cue.practicas.dto.response.UsuarioResponse;
import co.edu.cue.practicas.event.UsuarioCreadoEvent;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.Facultad;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoEstudiante;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.repository.facultad.FacultadRepository;
import co.edu.cue.practicas.repository.programa.ProgramaRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FacultadRepository facultadRepository;

    @Mock
    private ProgramaRepository programaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditoriaLogger auditoriaLogger;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private UsuarioService usuarioService;
    private CustomUserDetails admin;

    @BeforeEach
    void configurar() {
        usuarioService = new UsuarioService(
                usuarioRepository,
                facultadRepository,
                programaRepository,
                passwordEncoder,
                auditoriaLogger,
                eventPublisher,
                new ObjectMapper());
        admin = DatosDePrueba.userDetails(DatosDePrueba.administradorDti());
    }

    @Test
    void deberiaCrearEstudianteConEstadoNoAptoYPublicarEvento() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        Programa programa = DatosDePrueba.programa(2L, "Sistemas", facultad);
        CrearUsuarioRequest request = DatosDePrueba.crearUsuarioRequest(Rol.ESTUDIANTE);

        when(usuarioRepository.existsByCorreo("nuevo@cue.edu.co")).thenReturn(false);
        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));
        when(programaRepository.findById(2L)).thenReturn(Optional.of(programa));
        when(passwordEncoder.encode(anyString())).thenReturn("hash-temporal");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(99L);
            return usuario;
        });

        UsuarioResponse response = usuarioService.crearUsuario(request, admin);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getRol()).isEqualTo(Rol.ESTUDIANTE);
        assertThat(response.getEstadoEstudiante()).isEqualTo(EstadoEstudiante.NO_APTO);
        assertThat(response.getFacultadId()).isEqualTo(1L);
        assertThat(response.getProgramaId()).isEqualTo(2L);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario guardado = usuarioCaptor.getValue();
        assertThat(guardado.getPasswordHash()).isEqualTo("hash-temporal");
        assertThat(guardado.isActivo()).isTrue();
        assertThat(guardado.isPrimerIngreso()).isTrue();

        verify(eventPublisher).publishEvent(any(UsuarioCreadoEvent.class));
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaRechazarCreacionCuandoCorreoYaExiste() {
        CrearUsuarioRequest request = DatosDePrueba.crearUsuarioRequest(Rol.ADMIN_DTI);
        when(usuarioRepository.existsByCorreo("nuevo@cue.edu.co")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crearUsuario(request, admin))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("correo ya");

        verify(usuarioRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deberiaExigirEtiquetaCargoParaCoordinacionAcademica() {
        CrearUsuarioRequest request = DatosDePrueba.crearUsuarioRequest(Rol.COORDINACION_ACADEMICA);
        request.setEtiquetaCargo(null);
        when(usuarioRepository.existsByCorreo("nuevo@cue.edu.co")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.crearUsuario(request, admin))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("etiqueta de cargo");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarNoEncontradoCuandoProgramaNoExisteAlCrearUsuario() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");
        CrearUsuarioRequest request = DatosDePrueba.crearUsuarioRequest(Rol.ESTUDIANTE);

        when(usuarioRepository.existsByCorreo("nuevo@cue.edu.co")).thenReturn(false);
        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));
        when(programaRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.crearUsuario(request, admin))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Programa no encontrado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deberiaEditarUsuarioExistenteYRegistrarAuditoria() {
        Usuario usuario = DatosDePrueba.usuario(10L, "Nombre Original", "user@cue.edu.co", Rol.ESTUDIANTE);
        EditarUsuarioRequest request = DatosDePrueba.editarUsuarioRequest();

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        UsuarioResponse response = usuarioService.editarUsuario(10L, request, admin);

        assertThat(response.getNombre()).isEqualTo("Nombre Editado");
        assertThat(usuario.getTelefono()).isEqualTo("3100000000");
        assertThat(usuario.getFotoPerfil()).isEqualTo("https://cdn.example.com/foto.png");
        verify(usuarioRepository).save(usuario);
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaLanzarNoEncontradoCuandoUsuarioNoExisteAlEditar() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.editarUsuario(999L, DatosDePrueba.editarUsuarioRequest(), admin))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void deberiaDesactivarUsuarioCuandoNoEsElUnicoAdministradorDti() {
        Usuario usuario = DatosDePrueba.usuario(3L, "Admin Dos", "admin2@cue.edu.co", Rol.ADMIN_DTI);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.countByRolAndActivoTrue(Rol.ADMIN_DTI)).thenReturn(2L);

        usuarioService.desactivarUsuario(3L, admin);

        assertThat(usuario.isActivo()).isFalse();
        verify(usuarioRepository).save(usuario);
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaImpedirDesactivarUnicoAdministradorDtiActivo() {
        Usuario usuario = DatosDePrueba.usuario(3L, "Admin Unico", "admin@cue.edu.co", Rol.ADMIN_DTI);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.countByRolAndActivoTrue(Rol.ADMIN_DTI)).thenReturn(1L);

        assertThatThrownBy(() -> usuarioService.desactivarUsuario(3L, admin))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("Administrador DTI");

        assertThat(usuario.isActivo()).isTrue();
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deberiaActivarUsuarioExistente() {
        Usuario usuario = DatosDePrueba.usuario(4L, "Usuario Inactivo", "inactivo@cue.edu.co", Rol.ESTUDIANTE);
        usuario.setActivo(false);
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(usuario));

        usuarioService.activarUsuario(4L, admin);

        assertThat(usuario.isActivo()).isTrue();
        verify(usuarioRepository).save(usuario);
        verify(auditoriaLogger).registrar(any());
    }
}
