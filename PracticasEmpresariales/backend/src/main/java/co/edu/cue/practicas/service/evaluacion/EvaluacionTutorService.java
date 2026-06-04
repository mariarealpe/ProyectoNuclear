package co.edu.cue.practicas.service.evaluacion;

import co.edu.cue.practicas.audit.ModuloAuditoria;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.RegistrarEvaluacionTutorRequest;
import co.edu.cue.practicas.dto.response.EvaluacionTutorResponse;
import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.BitacoraAuditoria;
import co.edu.cue.practicas.model.entity.EvaluacionTutor;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.ResultadoEvaluacion;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoAccion;
import co.edu.cue.practicas.repository.configuracion.ConfiguracionProgramaRepository;
import co.edu.cue.practicas.repository.evaluacion.EvaluacionTutorRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.security.annotation.RequiereRol;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * RF-08-02 — Registro de nota del Tutor Empresarial.
 *
 * Patrones:
 *  - Template Method: mismo flujo de validación y cálculo que el docente.
 *  - Adapter: el portal externo del tutor consume esta API interna.
 *  - Proxy: tras el cierre del Coordinador (Practica.notasCerradas = true),
 *    cualquier intento de modificación es rechazado.
 *
 * Solo el Tutor Empresarial formalmente asignado a la práctica
 * (Practica.tutorEmpresarial) puede registrar o modificar su evaluación.
 */
@Service
@RequiredArgsConstructor
public class EvaluacionTutorService {

    private final EvaluacionTutorRepository evaluacionRepository;
    private final PracticaRepository practicaRepository;
    private final ConfiguracionProgramaRepository configuracionRepository;
    private final AuditoriaLogger auditoriaLogger;
    private final ObjectMapper objectMapper;

    /**
     * Registra la evaluación del Tutor Empresarial para una práctica EN_CURSO.
     * Solo puede ejecutarlo el tutor asignado en Practica.tutorEmpresarial.
     * Solo se permite una evaluación por práctica.
     */
    @RequiereRol(roles = {Rol.TUTOR_EMPRESARIAL})
    @Transactional
    public EvaluacionTutorResponse registrar(
            Long practicaId,
            RegistrarEvaluacionTutorRequest request,
            CustomUserDetails tutor) {

        Practica practica = buscarPractica(practicaId);
        validarPracticaEnCurso(practica);
        validarNotasNoCerradas(practica);
        validarTutorAsignado(practica, tutor);

        if (evaluacionRepository.existsByPractica_Id(practicaId)) {
            throw new OperacionNoPermitidaException(
                    "Ya existe una evaluación de tutor registrada para la práctica " + practicaId);
        }

        double[] rangos = cargarRangos(practica);
        double notaMaxima = rangos[0];
        double notaMinima = rangos[1];

        validarRangoNota(request.getNota(), notaMaxima);

        ResultadoEvaluacion resultado = calcularResultado(request.getNota(), notaMinima);

        EvaluacionTutor evaluacion = EvaluacionTutor.builder()
                .practica(practica)
                .tutor(tutor.getUsuario())
                .nota(request.getNota())
                .resultado(resultado)
                .observaciones(request.getObservaciones())
                .build();

        evaluacion = evaluacionRepository.save(evaluacion);

        auditoriaLogger.registrar(iniciarAuditoria(tutor)
                .modulo(ModuloAuditoria.EVALUACIONES_TUTOR)
                .tipoAccion(TipoAccion.CREAR)
                .registroAfectadoId(evaluacion.getId())
                .registroAfectadoTipo("EvaluacionTutor")
                .valoresNuevos(toJson(Map.of(
                        "practicaId", practicaId,
                        "nota", request.getNota(),
                        "resultado", resultado)))
                .exitoso(true));

        return EvaluacionTutorResponse.desde(evaluacion);
    }

    /**
     * Actualiza la evaluación existente mientras la práctica siga EN_CURSO
     * y el Coordinador no haya cerrado las notas.
     * Solo puede ejecutarlo el mismo tutor que la registró.
     */
    @RequiereRol(roles = {Rol.TUTOR_EMPRESARIAL})
    @Transactional
    public EvaluacionTutorResponse actualizar(
            Long evaluacionId,
            RegistrarEvaluacionTutorRequest request,
            CustomUserDetails tutor) {

        EvaluacionTutor evaluacion = evaluacionRepository.findById(evaluacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Evaluación no encontrada: " + evaluacionId));

        if (!evaluacion.getTutor().getId().equals(tutor.getUsuario().getId())) {
            throw new AccesoNoAutorizadoException(
                    "Solo el tutor que registró la evaluación puede modificarla");
        }

        validarPracticaEnCurso(evaluacion.getPractica());
        validarNotasNoCerradas(evaluacion.getPractica());

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

        auditoriaLogger.registrar(iniciarAuditoria(tutor)
                .modulo(ModuloAuditoria.EVALUACIONES_TUTOR)
                .tipoAccion(TipoAccion.EDITAR)
                .registroAfectadoId(evaluacion.getId())
                .registroAfectadoTipo("EvaluacionTutor")
                .valoresAnteriores(antes)
                .valoresNuevos(toJson(Map.of(
                        "nota", request.getNota(),
                        "resultado", resultado,
                        "observaciones", request.getObservaciones())))
                .exitoso(true));

        return EvaluacionTutorResponse.desde(evaluacion);
    }

    /**
     * Obtiene la evaluación del tutor de una práctica.
     * Accesible para TUTOR_EMPRESARIAL (propias), COORDINADOR_PRACTICAS y ADMIN_DTI.
     */
    @Transactional(readOnly = true)
    public EvaluacionTutorResponse obtenerPorPractica(Long practicaId) {
        return evaluacionRepository.findByPractica_Id(practicaId)
                .map(EvaluacionTutorResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe evaluación de tutor para la práctica: " + practicaId));
    }

    @Transactional(readOnly = true)
    public EvaluacionTutorResponse obtenerPorId(Long evaluacionId) {
        return evaluacionRepository.findById(evaluacionId)
                .map(EvaluacionTutorResponse::desde)
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

    private void validarNotasNoCerradas(Practica practica) {
        if (Boolean.TRUE.equals(practica.getNotasCerradas())) {
            throw new OperacionNoPermitidaException(
                    "La evaluación no se puede modificar: el Coordinador ya cerró el proceso de evaluación");
        }
    }

    private void validarTutorAsignado(Practica practica, CustomUserDetails tutor) {
        if (practica.getTutorEmpresarial() == null ||
            !practica.getTutorEmpresarial().getId().equals(tutor.getUsuario().getId())) {
            throw new AccesoNoAutorizadoException(
                    "Solo el Tutor Empresarial asignado a la práctica puede registrar la evaluación");
        }
    }

    /**
     * Carga notaMaxima y notaMinimaAprobacion desde ConfiguracionPrograma.
     * Si el programa no tiene configuración, usa los valores por defecto del dominio.
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
