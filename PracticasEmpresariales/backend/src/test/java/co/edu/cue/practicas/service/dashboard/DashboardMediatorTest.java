package co.edu.cue.practicas.service.dashboard;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.dto.response.DashboardResponse;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EtiquetaCargo;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardMediatorTest {

    private final DashboardMediator dashboardMediator = new DashboardMediator();

    @ParameterizedTest
    @MethodSource("dashboardsPorRol")
    void deberiaResolverDashboardConSeccionesYPermisosSegunRol(
            Rol rol,
            boolean soloLectura,
            int cantidadSecciones) {

        CustomUserDetails userDetails = DatosDePrueba.userDetails(
                DatosDePrueba.usuario(1L, "Usuario " + rol.name(), "user@cue.edu.co", rol));

        DashboardResponse response = dashboardMediator.resolverDashboard(userDetails);

        assertThat(response.getRol()).isEqualTo(rol);
        assertThat(response.isSoloLectura()).isEqualTo(soloLectura);
        assertThat(response.getSecciones()).hasSize(cantidadSecciones);
        assertThat(response.getNombreUsuario()).contains("Usuario");
    }

    @Test
    void deberiaIncluirEtiquetaCargoEnDashboardDeCoordinacionAcademica() {
        Usuario usuario = DatosDePrueba.usuario(
                2L,
                "Coordinadora",
                "coord@cue.edu.co",
                Rol.COORDINACION_ACADEMICA);
        usuario.setEtiquetaCargo(EtiquetaCargo.SECRETARIA);

        DashboardResponse response = dashboardMediator.resolverDashboard(new CustomUserDetails(usuario));

        assertThat(response.getEtiquetaCargo()).isEqualTo(EtiquetaCargo.SECRETARIA);
        assertThat(response.getSecciones())
                .extracting(seccion -> seccion.get("id"))
                .contains("estudiantes-no-apto", "estudiantes-apto");
    }

    private static Stream<Arguments> dashboardsPorRol() {
        return Stream.of(
                Arguments.of(Rol.ADMIN_DTI, false, 4),
                Arguments.of(Rol.COORDINACION_ACADEMICA, false, 3),
                Arguments.of(Rol.COORDINADOR_PRACTICAS, false, 5),
                Arguments.of(Rol.DOCENTE_ASESOR, false, 3),
                Arguments.of(Rol.TUTOR_EMPRESARIAL, false, 3),
                Arguments.of(Rol.ESTUDIANTE, false, 3),
                Arguments.of(Rol.DIRECCION, true, 3)
        );
    }
}
