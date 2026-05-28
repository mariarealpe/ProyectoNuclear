package co.edu.cue.practicas.service.auth;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.CambiarPasswordRequest;
import co.edu.cue.practicas.dto.request.LoginRequest;
import co.edu.cue.practicas.dto.response.LoginResponse;
import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.model.entity.BitacoraAuditoria;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoAccion;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.security.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditoriaLogger auditoriaLogger;

    @Mock
    private Authentication authentication;

    private AuthService authService;

    @BeforeEach
    void configurar() {
        authService = new AuthService(
                authenticationManager,
                jwtUtil,
                usuarioRepository,
                passwordEncoder,
                auditoriaLogger);
    }

    @Test
    void deberiaRetornarTokenYActualizarUltimoAccesoCuandoCredencialesSonValidas() {
        Usuario usuario = DatosDePrueba.usuario(7L, "Laura Perez", "laura@cue.edu.co", Rol.ADMIN_DTI);
        CustomUserDetails userDetails = DatosDePrueba.userDetails(usuario);
        LoginRequest request = DatosDePrueba.loginRequest("laura@cue.edu.co", "Password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generarToken(userDetails)).thenReturn("jwt-token");

        LoginResponse response = authService.login(request, "127.0.0.1");

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getTipo()).isEqualTo("Bearer");
        assertThat(response.getUsuarioId()).isEqualTo(7L);
        assertThat(response.getCorreo()).isEqualTo("laura@cue.edu.co");
        assertThat(usuario.getUltimoAcceso()).isNotNull();
        verify(usuarioRepository).save(usuario);

        ArgumentCaptor<BitacoraAuditoria.BitacoraAuditoriaBuilder> captor =
                ArgumentCaptor.forClass(BitacoraAuditoria.BitacoraAuditoriaBuilder.class);
        verify(auditoriaLogger).registrar(captor.capture());
        BitacoraAuditoria auditoria = captor.getValue().build();
        assertThat(auditoria.getTipoAccion()).isEqualTo(TipoAccion.LOGIN_EXITOSO);
        assertThat(auditoria.isExitoso()).isTrue();
        assertThat(auditoria.getIpOrigen()).isEqualTo("127.0.0.1");
    }

    @Test
    void deberiaRegistrarIntentoFallidoYLanzarAccesoNoAutorizadoCuandoCredencialesSonInvalidas() {
        LoginRequest request = DatosDePrueba.loginRequest("invalido@cue.edu.co", "bad");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login(request, "10.0.0.5"))
                .isInstanceOf(AccesoNoAutorizadoException.class)
                .hasMessageContaining("Credenciales");

        verify(usuarioRepository, never()).save(any());
        ArgumentCaptor<BitacoraAuditoria.BitacoraAuditoriaBuilder> captor =
                ArgumentCaptor.forClass(BitacoraAuditoria.BitacoraAuditoriaBuilder.class);
        verify(auditoriaLogger).registrar(captor.capture());
        BitacoraAuditoria auditoria = captor.getValue().build();
        assertThat(auditoria.getTipoAccion()).isEqualTo(TipoAccion.LOGIN_FALLIDO);
        assertThat(auditoria.isExitoso()).isFalse();
        assertThat(auditoria.getNombreUsuario()).isEqualTo("invalido@cue.edu.co");
    }

    @Test
    void deberiaCambiarPasswordCuandoActualEsCorrectaYConfirmacionCoincide() {
        Usuario usuario = DatosDePrueba.usuario(9L, "Carlos Ruiz", "carlos@cue.edu.co", Rol.ESTUDIANTE);
        usuario.setPasswordHash("hash-anterior");
        usuario.setPrimerIngreso(true);
        CustomUserDetails userDetails = DatosDePrueba.userDetails(usuario);
        CambiarPasswordRequest request =
                DatosDePrueba.cambiarPasswordRequest("Actual123", "Nueva12345", "Nueva12345");

        when(passwordEncoder.matches("Actual123", "hash-anterior")).thenReturn(true);
        when(passwordEncoder.encode("Nueva12345")).thenReturn("hash-nuevo");

        authService.cambiarPassword(request, userDetails);

        assertThat(usuario.getPasswordHash()).isEqualTo("hash-nuevo");
        assertThat(usuario.isPrimerIngreso()).isFalse();
        verify(usuarioRepository).save(usuario);
        verify(auditoriaLogger).registrar(any(BitacoraAuditoria.BitacoraAuditoriaBuilder.class));
    }

    @Test
    void deberiaRechazarCambioPasswordCuandoConfirmacionNoCoincide() {
        Usuario usuario = DatosDePrueba.usuario(9L, "Carlos Ruiz", "carlos@cue.edu.co", Rol.ESTUDIANTE);
        CambiarPasswordRequest request =
                DatosDePrueba.cambiarPasswordRequest("Actual123", "Nueva12345", "Otra12345");

        assertThatThrownBy(() -> authService.cambiarPassword(request, DatosDePrueba.userDetails(usuario)))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("no coinciden");

        verify(usuarioRepository, never()).save(any());
        verify(auditoriaLogger, never()).registrar(any());
    }

    @Test
    void deberiaRechazarCambioPasswordCuandoPasswordActualEsIncorrecta() {
        Usuario usuario = DatosDePrueba.usuario(9L, "Carlos Ruiz", "carlos@cue.edu.co", Rol.ESTUDIANTE);
        usuario.setPasswordHash("hash-anterior");
        CambiarPasswordRequest request =
                DatosDePrueba.cambiarPasswordRequest("Incorrecta", "Nueva12345", "Nueva12345");

        when(passwordEncoder.matches("Incorrecta", "hash-anterior")).thenReturn(false);

        assertThatThrownBy(() -> authService.cambiarPassword(request, DatosDePrueba.userDetails(usuario)))
                .isInstanceOf(AccesoNoAutorizadoException.class)
                .hasMessageContaining("actual es incorrecta");

        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository, never()).save(any());
    }
}
