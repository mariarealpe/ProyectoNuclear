package co.edu.cue.practicas.security.jwt;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EtiquetaCargo;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void configurar() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret",
                "clave-super-secreta-para-pruebas-unitarias-de-jwt-con-mas-de-64-caracteres");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 60_000L);
    }

    @Test
    void deberiaGenerarTokenValidoYExtraerClaimsPrincipales() {
        Usuario usuario = DatosDePrueba.usuario(
                8L,
                "Secretaria Academica",
                "secretaria@cue.edu.co",
                Rol.COORDINACION_ACADEMICA);
        usuario.setEtiquetaCargo(EtiquetaCargo.SECRETARIA);

        String token = jwtUtil.generarToken(new CustomUserDetails(usuario));

        assertThat(jwtUtil.validarToken(token)).isTrue();
        assertThat(jwtUtil.extraerCorreo(token)).isEqualTo("secretaria@cue.edu.co");
        assertThat(jwtUtil.extraerRol(token)).isEqualTo(Rol.COORDINACION_ACADEMICA);
        assertThat(jwtUtil.extraerEtiquetaCargo(token)).isEqualTo(EtiquetaCargo.SECRETARIA);
    }

    @Test
    void deberiaRetornarFalseCuandoTokenEsInvalido() {
        assertThat(jwtUtil.validarToken("token.invalido")).isFalse();
    }

    @Test
    void deberiaRetornarFalseCuandoTokenEstaExpirado() {
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", -1_000L);
        Usuario usuario = DatosDePrueba.usuario(9L, "Estudiante", "estudiante@cue.edu.co", Rol.ESTUDIANTE);

        String token = jwtUtil.generarToken(new CustomUserDetails(usuario));

        assertThat(jwtUtil.validarToken(token)).isFalse();
    }
}
