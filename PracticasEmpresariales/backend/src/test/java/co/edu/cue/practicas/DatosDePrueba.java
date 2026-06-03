package co.edu.cue.practicas;

import co.edu.cue.practicas.dto.request.CambiarPasswordRequest;
import co.edu.cue.practicas.dto.request.ActualizarConfiguracionProgramaRequest;
import co.edu.cue.practicas.dto.request.CrearFacultadRequest;
import co.edu.cue.practicas.dto.request.CrearProgramaRequest;
import co.edu.cue.practicas.dto.request.CrearUsuarioRequest;
import co.edu.cue.practicas.dto.request.EditarUsuarioRequest;
import co.edu.cue.practicas.dto.request.LoginRequest;
import co.edu.cue.practicas.model.entity.ConfiguracionPrograma;
import co.edu.cue.practicas.model.entity.Facultad;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.EtiquetaCargo;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.security.CustomUserDetails;

import java.util.ArrayList;

public final class DatosDePrueba {

    private DatosDePrueba() {
    }

    public static Usuario usuario(Long id, String nombre, String correo, Rol rol) {
        return Usuario.builder()
                .id(id)
                .nombre(nombre)
                .correo(correo)
                .passwordHash("hash-password")
                .rol(rol)
                .activo(true)
                .primerIngreso(true)
                .build();
    }

    public static Usuario administradorDti() {
        return usuario(1L, "Admin DTI", "admin@cue.edu.co", Rol.ADMIN_DTI);
    }

    public static CustomUserDetails userDetails(Usuario usuario) {
        return new CustomUserDetails(usuario);
    }

    public static Facultad facultad(Long id, String nombre) {
        return Facultad.builder()
                .id(id)
                .nombre(nombre)
                .descripcion("Descripcion " + nombre)
                .activa(true)
                .programas(new ArrayList<>())
                .build();
    }

    public static Programa programa(Long id, String nombre, Facultad facultad) {
        Programa programa = Programa.builder()
                .id(id)
                .nombre(nombre)
                .descripcion("Descripcion " + nombre)
                .facultad(facultad)
                .numeroTotalPracticas(2)
                .promedioMinimoGeneral(3.5)
                .activo(true)
                .requisitos(new ArrayList<>())
                .build();
        if (facultad != null) {
            facultad.getProgramas().add(programa);
        }
        return programa;
    }

    public static ConfiguracionPrograma configuracionPrograma(Long id, Programa programa) {
        return ConfiguracionPrograma.builder()
                .id(id)
                .programa(programa)
                .diasInactividadAlerta(5)
                .notificacionesAutomaticas(true)
                .notaMinimaAprobacion(3.2)
                .notaMaxima(5.0)
                .numeroCortes(3)
                .maximoAsignacionesSimultaneas(1)
                .correoRemitente("noreply@cue.edu.co")
                .build();
    }

    public static ActualizarConfiguracionProgramaRequest actualizarConfiguracionProgramaRequest() {
        ActualizarConfiguracionProgramaRequest request = new ActualizarConfiguracionProgramaRequest();
        request.setDiasInactividadAlerta(6);
        request.setNotificacionesAutomaticas(true);
        request.setNotaMinimaAprobacion(3.0);
        request.setNotaMaxima(5.0);
        request.setNumeroCortes(3);
        request.setMaximoAsignacionesSimultaneas(1);
        request.setPlantillaCorreoAsignacion("<p>Asignación creada</p>");
        request.setPlantillaCorreoSeguimiento("<p>Seguimiento actualizado</p>");
        request.setPlantillaCorreoAlerta("<p>Alerta de inactividad</p>");
        request.setCorreoRemitente("practicas@cue.edu.co");
        return request;
    }

    public static LoginRequest loginRequest(String correo, String password) {
        LoginRequest request = new LoginRequest();
        request.setCorreo(correo);
        request.setPassword(password);
        return request;
    }

    public static CambiarPasswordRequest cambiarPasswordRequest(
            String actual,
            String nueva,
            String confirmacion) {

        CambiarPasswordRequest request = new CambiarPasswordRequest();
        request.setPasswordActual(actual);
        request.setPasswordNueva(nueva);
        request.setPasswordConfirmacion(confirmacion);
        return request;
    }

    public static CrearFacultadRequest crearFacultadRequest(String nombre) {
        CrearFacultadRequest request = new CrearFacultadRequest();
        request.setNombre(nombre);
        request.setDescripcion("Descripcion " + nombre);
        return request;
    }

    public static CrearProgramaRequest crearProgramaRequest(Long facultadId) {
        CrearProgramaRequest request = new CrearProgramaRequest();
        request.setNombre("Ingenieria de Sistemas");
        request.setDescripcion("Programa de ingenieria");
        request.setFacultadId(facultadId);
        request.setNumeroTotalPracticas(2);
        request.setPromedioMinimoGeneral(3.5);

        CrearProgramaRequest.RequisitoRequest requisito = new CrearProgramaRequest.RequisitoRequest();
        requisito.setNumeroPractica(1);
        requisito.setCreditosMinimos(80);
        requisito.setPromedioMinimo(3.2);
        requisito.setRequierePracticaAnteriorAprobada(false);
        requisito.setDocumentosRequeridos("Hoja de vida");
        request.setRequisitos(java.util.List.of(requisito));

        return request;
    }

    public static CrearUsuarioRequest crearUsuarioRequest(Rol rol) {
        CrearUsuarioRequest request = new CrearUsuarioRequest();
        request.setNombre("Usuario Nuevo");
        request.setCorreo("nuevo@cue.edu.co");
        request.setRol(rol);
        request.setTelefono("3001234567");
        request.setFacultadId(1L);
        request.setProgramaId(2L);
        if (Rol.COORDINACION_ACADEMICA.equals(rol)) {
            request.setEtiquetaCargo(EtiquetaCargo.SECRETARIA);
        }
        return request;
    }

    public static EditarUsuarioRequest editarUsuarioRequest() {
        EditarUsuarioRequest request = new EditarUsuarioRequest();
        request.setNombre("Nombre Editado");
        request.setTelefono("3100000000");
        request.setFotoPerfil("https://cdn.example.com/foto.png");
        return request;
    }
}
