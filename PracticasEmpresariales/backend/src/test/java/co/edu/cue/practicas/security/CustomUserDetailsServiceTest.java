package co.edu.cue.practicas.security;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void deberiaCargarUsuarioActivoPorCorreo() {
        Usuario usuario = DatosDePrueba.usuario(1L, "Admin", "admin@cue.edu.co", Rol.ADMIN_DTI);
        when(usuarioRepository.findByCorreoAndActivoTrue("admin@cue.edu.co")).thenReturn(Optional.of(usuario));

        CustomUserDetailsService service = new CustomUserDetailsService(usuarioRepository);

        CustomUserDetails userDetails = (CustomUserDetails) service.loadUserByUsername("admin@cue.edu.co");

        assertThat(userDetails.getUsername()).isEqualTo("admin@cue.edu.co");
        assertThat(userDetails.getRol()).isEqualTo(Rol.ADMIN_DTI);
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void deberiaLanzarExcepcionCuandoUsuarioNoExisteOEstaInactivo() {
        when(usuarioRepository.findByCorreoAndActivoTrue("nadie@cue.edu.co")).thenReturn(Optional.empty());
        CustomUserDetailsService service = new CustomUserDetailsService(usuarioRepository);

        assertThatThrownBy(() -> service.loadUserByUsername("nadie@cue.edu.co"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }
}
