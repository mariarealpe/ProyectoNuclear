package co.edu.cue.practicas.security.filter;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.security.annotation.RequiereRol;
import co.edu.cue.practicas.security.annotation.SoloLectura;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScopeValidationAspectTest {

    @Mock
    private AuditoriaLogger auditoriaLogger;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private ScopeValidationAspect aspect;

    @BeforeEach
    void configurar() {
        aspect = new ScopeValidationAspect(auditoriaLogger);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deberiaPermitirEjecucionCuandoUsuarioTieneRolRequerido() throws Throwable {
        autenticarComo(Rol.ADMIN_DTI);
        prepararMetodo("soloAdmin");
        when(joinPoint.proceed()).thenReturn("ejecutado");

        Object resultado = aspect.validarRol(joinPoint);

        assertThat(resultado).isEqualTo("ejecutado");
        verify(auditoriaLogger, never()).registrarAccesoNegado(any(), any(), any());
    }

    @Test
    void deberiaBloquearEjecucionCuandoUsuarioNoTieneRolRequerido() throws Throwable {
        autenticarComo(Rol.ESTUDIANTE);
        prepararMetodo("soloAdmin");

        assertThatThrownBy(() -> aspect.validarRol(joinPoint))
                .isInstanceOf(AccesoNoAutorizadoException.class)
                .hasMessageContaining("Acceso denegado");

        verify(auditoriaLogger).registrarAccesoNegado(
                any(Usuario.class),
                eq("Service.soloAdmin"),
                eq("desconocida"));
        verify(joinPoint, never()).proceed();
    }

    @Test
    void deberiaBloquearCuandoNoHayUsuarioAutenticado() throws Throwable {
        prepararMetodo("soloAdmin");

        assertThatThrownBy(() -> aspect.validarRol(joinPoint))
                .isInstanceOf(AccesoNoAutorizadoException.class)
                .hasMessageContaining("No autenticado");

        verify(auditoriaLogger, never()).registrarAccesoNegado(any(), any(), any());
        verify(joinPoint, never()).proceed();
    }

    @Test
    void deberiaBloquearOperacionesDeEscrituraParaRolDireccion() throws Throwable {
        autenticarComo(Rol.DIRECCION);
        prepararMetodo("operacionEscritura");

        assertThatThrownBy(() -> aspect.bloquearEscrituraDireccion(joinPoint))
                .isInstanceOf(AccesoNoAutorizadoException.class)
                .hasMessageContaining("solo tiene acceso de lectura");

        verify(auditoriaLogger).registrarAccesoNegado(
                any(Usuario.class),
                eq("Service.operacionEscritura"),
                eq("desconocida"));
        verify(joinPoint, never()).proceed();
    }

    @Test
    void deberiaPermitirOperacionesDeEscrituraParaRolDistintoADireccion() throws Throwable {
        autenticarComo(Rol.ADMIN_DTI);
        when(joinPoint.proceed()).thenReturn("ok");

        Object resultado = aspect.bloquearEscrituraDireccion(joinPoint);

        assertThat(resultado).isEqualTo("ok");
    }

    private void autenticarComo(Rol rol) {
        CustomUserDetails userDetails = DatosDePrueba.userDetails(
                DatosDePrueba.usuario(1L, "Usuario", "usuario@cue.edu.co", rol));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, List.of()));
    }

    private void prepararMetodo(String nombreMetodo) throws NoSuchMethodException {
        Method method = ServicioProtegido.class.getMethod(nombreMetodo);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
    }

    static class ServicioProtegido {

        @RequiereRol(roles = {Rol.ADMIN_DTI})
        public String soloAdmin() {
            return "soloAdmin";
        }

        @SoloLectura
        public String operacionEscritura() {
            return "operacionEscritura";
        }
    }
}
