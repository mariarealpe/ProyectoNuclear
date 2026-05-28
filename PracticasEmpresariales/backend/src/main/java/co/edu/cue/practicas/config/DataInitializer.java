package co.edu.cue.practicas.config;

import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EstadoEstudiante;
import co.edu.cue.practicas.model.enums.EtiquetaCargo;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Siembra el primer Administrador DTI al arrancar el sistema, y además
 * crea un usuario de PRUEBA por cada rol del enum Rol con contraseña
 * conocida para facilitar las demos y pruebas manuales.
 *
 * Los usuarios solo se crean si no existe ninguno con ese correo,
 * por lo que es seguro ejecutarlo en cada arranque sin duplicarlos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin.correo}")
    private String correoDti;

    @Value("${app.init.admin.password}")
    private String passwordDti;

    /** Contraseña común para todos los usuarios de prueba (no para el DTI). */
    private static final String PASSWORD_PRUEBA = "Test2026!";

    @Override
    public void run(String... args) {
        crearAdminDtiSiNoExiste();
        crearUsuariosDePrueba();
    }

    private void crearAdminDtiSiNoExiste() {
        if (usuarioRepository.existsByCorreo(correoDti)) return;

        Usuario dti = Usuario.builder()
                .nombre("Administrador DTI")
                .correo(correoDti)
                .passwordHash(passwordEncoder.encode(passwordDti))
                .rol(Rol.ADMIN_DTI)
                .activo(true)
                .primerIngreso(false)
                .build();
        usuarioRepository.save(dti);

        log.info("=======================================================");
        log.info("  USUARIO DTI INICIAL CREADO");
        log.info("  Correo : {}", correoDti);
        log.info("  CAMBIA app.init.admin.password EN PRODUCCIÓN");
        log.info("=======================================================");
    }

    /**
     * Crea un usuario de prueba por cada rol con contraseña conocida.
     * primerIngreso=false para que el login lleve directamente al dashboard
     * sin forzar cambio de contraseña.
     */
    private void crearUsuariosDePrueba() {
        sembrar("coordinacion@cue.edu.co",  "Coordinación Académica (Prueba)",
                Rol.COORDINACION_ACADEMICA, EtiquetaCargo.COORDINACION_ACADEMICA);

        sembrar("secretaria@cue.edu.co",    "Secretaría Académica (Prueba)",
                Rol.COORDINACION_ACADEMICA, EtiquetaCargo.SECRETARIA);

        sembrar("coordpracticas@cue.edu.co","Coordinador de Prácticas (Prueba)",
                Rol.COORDINADOR_PRACTICAS, null);

        sembrar("docente@cue.edu.co",       "Docente Asesor (Prueba)",
                Rol.DOCENTE_ASESOR, null);

        sembrar("tutor@cue.edu.co",         "Tutor Empresarial (Prueba)",
                Rol.TUTOR_EMPRESARIAL, null);

        sembrar("estudiante@cue.edu.co",    "Estudiante Practicante (Prueba)",
                Rol.ESTUDIANTE, null);

        sembrar("direccion@cue.edu.co",     "Dirección (Prueba)",
                Rol.DIRECCION, null);
    }

    private void sembrar(String correo, String nombre, Rol rol, EtiquetaCargo etiqueta) {
        if (usuarioRepository.existsByCorreo(correo)) return;

        Usuario.UsuarioBuilder builder = Usuario.builder()
                .nombre(nombre)
                .correo(correo)
                .passwordHash(passwordEncoder.encode(PASSWORD_PRUEBA))
                .rol(rol)
                .etiquetaCargo(etiqueta)
                .activo(true)
                .primerIngreso(false);

        if (rol == Rol.ESTUDIANTE) {
            builder.estadoEstudiante(EstadoEstudiante.APTO)
                    .creditosAprobados(120)
                    .promedioAcumulado(4.2);
        }

        usuarioRepository.save(builder.build());
        log.info("Usuario de prueba creado: {} ({}{})",
                correo, rol, etiqueta != null ? " / " + etiqueta : "");
    }
}
