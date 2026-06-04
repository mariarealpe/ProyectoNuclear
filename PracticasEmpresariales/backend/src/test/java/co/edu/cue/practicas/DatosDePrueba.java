package co.edu.cue.practicas;

import co.edu.cue.practicas.dto.request.CambiarPasswordRequest;
import co.edu.cue.practicas.dto.request.ActualizarConfiguracionProgramaRequest;
import co.edu.cue.practicas.dto.request.CrearFacultadRequest;
import co.edu.cue.practicas.dto.request.CrearProgramaRequest;
import co.edu.cue.practicas.dto.request.CrearUsuarioRequest;
import co.edu.cue.practicas.dto.request.EditarUsuarioRequest;
import co.edu.cue.practicas.dto.request.LoginRequest;
import co.edu.cue.practicas.dto.request.RegistrarEvaluacionDocenteRequest;
import co.edu.cue.practicas.dto.request.RegistrarEvaluacionTutorRequest;
import co.edu.cue.practicas.dto.request.RegistrarNotaFinalRequest;
import co.edu.cue.practicas.model.entity.ConfiguracionPrograma;
import co.edu.cue.practicas.model.entity.EvaluacionDocente;
import co.edu.cue.practicas.model.entity.EvaluacionTutor;
import co.edu.cue.practicas.model.entity.Facultad;
import co.edu.cue.practicas.model.entity.NotaFinal;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.ResultadoNotaFinal;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.EtiquetaCargo;
import co.edu.cue.practicas.model.enums.ResultadoEvaluacion;
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

    public static Usuario docenteAsesor() {
        return usuario(10L, "Docente Asesor", "docente@cue.edu.co", Rol.DOCENTE_ASESOR);
    }

    public static Usuario tutorEmpresarial() {
        return usuario(11L, "Tutor Empresarial", "tutor@cue.edu.co", Rol.TUTOR_EMPRESARIAL);
    }

    public static Usuario coordinadorPracticas() {
        return usuario(12L, "Coordinador Prácticas", "coordpracticas@cue.edu.co", Rol.COORDINADOR_PRACTICAS);
    }

    public static Practica practica(Long id, Usuario estudiante, Programa programa, Usuario docente) {
        return Practica.builder()
                .id(id)
                .estudiante(estudiante)
                .programa(programa)
                .docenteAsesor(docente)
                .numeroPractica(1)
                .nombre("Práctica Empresarial")
                .descripcion("Descripción de práctica")
                .estado(EstadoPractica.EN_CURSO)
                .build();
    }

    public static Practica practica(Long id, Usuario estudiante, Programa programa, Usuario docente, Usuario tutor) {
        Practica p = practica(id, estudiante, programa, docente);
        p.setTutorEmpresarial(tutor);
        return p;
    }

    public static EvaluacionDocente evaluacionDocente(Long id, Practica practica, Usuario docente) {
        return EvaluacionDocente.builder()
                .id(id)
                .practica(practica)
                .docente(docente)
                .nota(4.0)
                .resultado(ResultadoEvaluacion.APROBADO)
                .observaciones("Desempeño sobresaliente durante la práctica")
                .build();
    }

    public static RegistrarEvaluacionDocenteRequest registrarEvaluacionRequest(double nota, String observaciones) {
        RegistrarEvaluacionDocenteRequest request = new RegistrarEvaluacionDocenteRequest();
        request.setNota(nota);
        request.setObservaciones(observaciones);
        return request;
    }

    public static EvaluacionTutor evaluacionTutor(Long id, Practica practica, Usuario tutor) {
        return EvaluacionTutor.builder()
                .id(id)
                .practica(practica)
                .tutor(tutor)
                .nota(4.2)
                .resultado(ResultadoEvaluacion.APROBADO)
                .observaciones("Buen desempeño en la empresa")
                .build();
    }

    public static RegistrarEvaluacionTutorRequest registrarEvaluacionTutorRequest(double nota, String observaciones) {
        RegistrarEvaluacionTutorRequest request = new RegistrarEvaluacionTutorRequest();
        request.setNota(nota);
        request.setObservaciones(observaciones);
        return request;
    }

    public static NotaFinal notaFinal(Long id, Practica practica, Usuario coordinador) {
        return NotaFinal.builder()
                .id(id)
                .practica(practica)
                .coordinador(coordinador)
                .nota(4.0)
                .resultado(ResultadoNotaFinal.APROBADO)
                .observaciones("Aprueba la práctica")
                .cerrada(false)
                .build();
    }

    public static RegistrarNotaFinalRequest registrarNotaFinalRequest(double nota, String observaciones) {
        RegistrarNotaFinalRequest request = new RegistrarNotaFinalRequest();
        request.setNota(nota);
        request.setObservaciones(observaciones);
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
