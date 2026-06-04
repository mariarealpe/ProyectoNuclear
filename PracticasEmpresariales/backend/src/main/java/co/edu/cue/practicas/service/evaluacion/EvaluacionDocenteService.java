package co.edu.cue.practicas.service.evaluacion;

import co.edu.cue.practicas.audit.ModuloAuditoria;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.RegistrarEvaluacionDocenteRequest;
import co.edu.cue.practicas.dto.response.EvaluacionDocenteResponse;
import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.BitacoraAuditoria;
import co.edu.cue.practicas.model.entity.ConfiguracionPrograma;
import co.edu.cue.practicas.model.entity.EvaluacionDocente;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.ResultadoEvaluacion;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoAccion;
import co.edu.cue.practicas.repository.configuracion.ConfiguracionProgramaRepository;
import co.edu.cue.practicas.repository.evaluacion.EvaluacionDocenteRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.security.annotation.RequiereRol;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * RF-08-01 — Registro de nota del Docente Asesor.
 *
 * Solo el Docente Asesor formalmente asignado a la práctica puede registrar
 * o modificar su evaluación. La nota se valida contra ConfiguracionPrograma
 * del programa correspondiente y determina APROBADO o DESAPROBADO.
 */
@Service
@RequiredArgsConstructor
public class EvaluacionDocenteService {

    private final EvaluacionDocenteRepository evaluacionRepository;
    private final PracticaRepository practicaRepository;
    private final ConfiguracionProgramaRepository configuracionRepository;
    private final AuditoriaLogger auditoriaLogger;
    private final ObjectMapper objectMapper;

    /**
     * Registra la evaluación del Docente Asesor para una práctica EN_CURSO.
     * Solo puede ejecutarlo el docente asignado en Practica.docenteAsesor.
     * Solo se permite una evaluación por práctica.
     */
    @RequiereRol(roles = {Rol.DOCENTE_ASESOR})
    @Transactional
    public EvaluacionDocenteResponse registrar(
            Long practicaId,
            RegistrarEvaluacionDocenteRequest request,
            CustomUserDetails docente) {

        Practica practica = buscarPractica(practicaId);
        validarPracticaEnCurso(practica);
        validarDocenteAsignado(practica, docente);

        if (evaluacionRepository.existsByPractica_Id(practicaId)) {
            throw new OperacionNoPermitidaException(
                    "Ya existe una evaluación registrada para la práctica " + practicaId);
        }

        double[] rangos = cargarRangos(practica);
        double notaMaxima  = rangos[0];
        double notaMinima  = rangos[1];

        validarRangoNota(request.getNota(), notaMaxima);

        ResultadoEvaluacion resultado = calcularResultado(request.getNota(), notaMinima);

        EvaluacionDocente evaluacion = EvaluacionDocente.builder()
                .practica(practica)
                .docente(docente.getUsuario())
                .nota(request.getNota())
                .resultado(resultado)
                .observaciones(request.getObservaciones())
                .build();

        evaluacion = evaluacionRepository.save(evaluacion);

        auditoriaLogger.registrar(iniciarAuditoria(docente)
                .modulo(ModuloAuditoria.EVALUACIONES_DOCENTE)
                .tipoAccion(TipoAccion.CREAR)
                .registroAfectadoId(evaluacion.getId())
                .registroAfectadoTipo("EvaluacionDocente")
                .valoresNuevos(toJson(Map.of(
                        "practicaId", practicaId,
                        "nota", request.getNota(),
                        "resultado", resultado)))
                .exitoso(true));

        return EvaluacionDocenteResponse.desde(evaluacion);
    }

    /**
     * Actualiza la evaluación existente mientras la práctica siga EN_CURSO.
     * Solo puede ejecutarlo el mismo docente que la registró.
     */
    @RequiereRol(roles = {Rol.DOCENTE_ASESOR})
    @Transactional
    public EvaluacionDocenteResponse actualizar(
            Long evaluacionId,
            RegistrarEvaluacionDocenteRequest request,
            CustomUserDetails docente) {

        EvaluacionDocente evaluacion = evaluacionRepository.findById(evaluacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Evaluación no encontrada: " + evaluacionId));

        if (!evaluacion.getDocente().getId().equals(docente.getUsuario().getId())) {
            throw new AccesoNoAutorizadoException(
                    "Solo el docente que registró la evaluación puede modificarla");
        }

        validarPracticaEnCurso(evaluacion.getPractica());

        double[] rangos = cargarRangos(evaluacion.getPractica());
        double notaMaxima = rangos[0];
        double notaMinima = rangos[1];

        validarRangoNota(request.getNota(), notaMaxima);

        String antes = toJson(Map.of(
                "nota", evaluacion.getNota(),
                "resultado", evaluacion.getResultado(),
                "observaciones", evaluacion.getObservaciones()));

        ResultadoEvaluacion resultado = calcularResultado(request.getNota(), notaMinima);
        evaluacion.setNota(request.getNota());
        evaluacion.setResultado(resultado);
        evaluacion.setObservaciones(request.getObservaciones());
        evaluacion = evaluacionRepository.save(evaluacion);

        auditoriaLogger.registrar(iniciarAuditoria(docente)
                .modulo(ModuloAuditoria.EVALUACIONES_DOCENTE)
                .tipoAccion(TipoAccion.EDITAR)
                .registroAfectadoId(evaluacion.getId())
                .registroAfectadoTipo("EvaluacionDocente")
                .valoresAnteriores(antes)
                .valoresNuevos(toJson(Map.of(
                        "nota", request.getNota(),
                        "resultado", resultado,
                        "observaciones", request.getObservaciones())))
                .exitoso(true));

        return EvaluacionDocenteResponse.desde(evaluacion);
    }

    /**
     * Obtiene la evaluación docente de una práctica.
     * Accesible para DOCENTE_ASESOR (propias), COORDINADOR_PRACTICAS y ADMIN_DTI.
     */
    @Transactional(readOnly = true)
    public EvaluacionDocenteResponse obtenerPorPractica(Long practicaId) {
        return evaluacionRepository.findByPractica_Id(practicaId)
                .map(EvaluacionDocenteResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe evaluación para la práctica: " + practicaId));
    }

    /**
     * Obtiene una evaluación por su ID.
     */
    @Transactional(readOnly = true)
    public EvaluacionDocenteResponse obtenerPorId(Long evaluacionId) {
        return evaluacionRepository.findById(evaluacionId)
                .map(EvaluacionDocenteResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Evaluación no encontrada: " + evaluacionId));
    }

    // =========================================================================
    // Métodos privados
    // =========================================================================

    private Practica buscarPractica(Long practicaId) {
        return practicaRepository.findById(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Práctica no encontrada: " + practicaId));
    }

    private void validarPracticaEnCurso(Practica practica) {
        if (practica.getEstado() != EstadoPractica.EN_CURSO) {
            throw new OperacionNoPermitidaException(
                    "Solo se pueden registrar evaluaciones en prácticas EN_CURSO. " +
                    "Estado actual: " + practica.getEstado());
        }
    }

    private void validarDocenteAsignado(Practica practica, CustomUserDetails docente) {
        if (practica.getDocenteAsesor() == null ||
            !practica.getDocenteAsesor().getId().equals(docente.getUsuario().getId())) {
            throw new AccesoNoAutorizadoException(
                    "Solo el Docente Asesor asignado a la práctica puede registrar la evaluación");
        }
    }

    /**
     * Carga notaMaxima y notaMinimaAprobacion desde ConfiguracionPrograma.
     * Si el programa no tiene configuración, usa los valores por defecto del dominio.
     *
     * @return double[0] = notaMaxima, double[1] = notaMinimaAprobacion
     */
    private double[] cargarRangos(Practica practica) {
        return configuracionRepository
                .findByPrograma_Id(practica.getPrograma().getId())
                .map(c -> new double[]{c.getNotaMaxima(), c.getNotaMinimaAprobacion()})
                .orElse(new double[]{5.0, 3.0});
    }

    private void validarRangoNota(double nota, double notaMaxima) {
        if (nota < 0.0 || nota > notaMaxima) {
            throw new OperacionNoPermitidaException(String.format(
                    "La nota debe estar entre 0.0 y %.1f", notaMaxima));
        }
    }

    private ResultadoEvaluacion calcularResultado(double nota, double notaMinima) {
        return nota >= notaMinima ? ResultadoEvaluacion.APROBADO : ResultadoEvaluacion.DESAPROBADO;
    }

    private BitacoraAuditoria.BitacoraAuditoriaBuilder iniciarAuditoria(CustomUserDetails actor) {
        return BitacoraAuditoria.builder()
                .usuario(actor.getUsuario())
                .nombreUsuario(actor.getNombre())
                .rolUsuario(actor.getRol())
                .etiquetaCargoUsuario(actor.getEtiquetaCargo());
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
