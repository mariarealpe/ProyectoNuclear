package co.edu.cue.practicas.service.evaluacion;

import co.edu.cue.practicas.audit.ModuloAuditoria;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.RegistrarNotaFinalRequest;
import co.edu.cue.practicas.dto.response.NotaFinalResponse;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.BitacoraAuditoria;
import co.edu.cue.practicas.model.entity.EvaluacionDocente;
import co.edu.cue.practicas.model.entity.EvaluacionTutor;
import co.edu.cue.practicas.model.entity.NotaFinal;
import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import co.edu.cue.practicas.model.enums.ResultadoNotaFinal;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoAccion;
import co.edu.cue.practicas.repository.configuracion.ConfiguracionProgramaRepository;
import co.edu.cue.practicas.repository.evaluacion.EvaluacionDocenteRepository;
import co.edu.cue.practicas.repository.evaluacion.EvaluacionTutorRepository;
import co.edu.cue.practicas.repository.evaluacion.NotaFinalRepository;
import co.edu.cue.practicas.repository.practica.PracticaRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.security.annotation.RequiereRol;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * RF-08-04 — Registro y cierre de la nota final por el Coordinador.
 *
 * Reglas:
 *  - Solo COORDINADOR_PRACTICAS puede registrar / actualizar / cerrar.
 *  - El registro requiere que existan las notas del Docente Asesor y del Tutor
 *    Empresarial (se exponen como referencia, pero el Coordinador ingresa el
 *    valor manualmente — el sistema NO calcula promedios).
 *  - La práctica debe estar EN_CURSO al momento del registro/actualización.
 *  - Tras ejecutar el cierre, NotaFinal.cerrada = true y Practica.notasCerradas
 *    = true; los servicios de evaluación (docente, tutor) y este propio rechazan
 *    cualquier modificación posterior (Patrón Proxy).
 *
 * Patrones aplicados:
 *  - Strategy: la política de aprobación (notaMinima por programa) se carga
 *    dinámicamente desde ConfiguracionPrograma.
 *  - Observer: la auditoría y los listeners aguas abajo (dashboard) se enteran
 *    del registro y del cierre.
 *  - Proxy: la inmutabilidad post-cierre se enforcea aquí y en los servicios
 *    docente / tutor.
 */
@Service
@RequiredArgsConstructor
public class NotaFinalService {

    private final NotaFinalRepository notaFinalRepository;
    private final EvaluacionDocenteRepository evaluacionDocenteRepository;
    private final EvaluacionTutorRepository evaluacionTutorRepository;
    private final PracticaRepository practicaRepository;
    private final ConfiguracionProgramaRepository configuracionRepository;
    private final AuditoriaLogger auditoriaLogger;
    private final ObjectMapper objectMapper;

    /**
     * Registra la nota final del Coordinador. Requiere que existan las notas de
     * docente y tutor (regla de negocio del RF-08-04: ambas son referencia obligatoria).
     */
    @RequiereRol(roles = {Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public NotaFinalResponse registrar(
            Long practicaId,
            RegistrarNotaFinalRequest request,
            CustomUserDetails coordinador) {

        Practica practica = buscarPractica(practicaId);
        validarPracticaEnCurso(practica);
        validarNotasNoCerradas(practica);

        if (notaFinalRepository.existsByPractica_Id(practicaId)) {
            throw new OperacionNoPermitidaException(
                    "Ya existe una nota final registrada para la práctica " + practicaId);
        }

        Optional<EvaluacionDocente> evDocente = evaluacionDocenteRepository.findByPractica_Id(practicaId);
        Optional<EvaluacionTutor> evTutor = evaluacionTutorRepository.findByPractica_Id(practicaId);

        if (evDocente.isEmpty()) {
            throw new OperacionNoPermitidaException(
                    "No se puede registrar la nota final: falta la evaluación del Docente Asesor");
        }
        if (evTutor.isEmpty()) {
            throw new OperacionNoPermitidaException(
                    "No se puede registrar la nota final: falta la evaluación del Tutor Empresarial");
        }

        double[] rangos = cargarRangos(practica);
        double notaMaxima = rangos[0];
        double notaMinima = rangos[1];

        validarRangoNota(request.getNota(), notaMaxima);
        ResultadoNotaFinal resultado = calcularResultado(request.getNota(), notaMinima);

        NotaFinal notaFinal = NotaFinal.builder()
                .practica(practica)
                .coordinador(coordinador.getUsuario())
                .nota(request.getNota())
                .resultado(resultado)
                .observaciones(request.getObservaciones())
                .cerrada(false)
                .build();

        notaFinal = notaFinalRepository.save(notaFinal);

        auditoriaLogger.registrar(iniciarAuditoria(coordinador)
                .modulo(ModuloAuditoria.NOTA_FINAL)
                .tipoAccion(TipoAccion.CREAR)
                .registroAfectadoId(notaFinal.getId())
                .registroAfectadoTipo("NotaFinal")
                .valoresNuevos(toJson(Map.of(
                        "practicaId", practicaId,
                        "nota", request.getNota(),
                        "resultado", resultado)))
                .exitoso(true));

        return construirResponse(notaFinal, evDocente.orElse(null), evTutor.orElse(null));
    }

    /**
     * Actualiza la nota final mientras NO se haya cerrado.
     */
    @RequiereRol(roles = {Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public NotaFinalResponse actualizar(
            Long notaFinalId,
            RegistrarNotaFinalRequest request,
            CustomUserDetails coordinador) {

        NotaFinal nota = buscar(notaFinalId);
        validarPracticaEnCurso(nota.getPractica());

        if (Boolean.TRUE.equals(nota.getCerrada())
                || Boolean.TRUE.equals(nota.getPractica().getNotasCerradas())) {
            throw new OperacionNoPermitidaException(
                    "La nota final ya fue cerrada y es inmutable");
        }

        double[] rangos = cargarRangos(nota.getPractica());
        double notaMaxima = rangos[0];
        double notaMinima = rangos[1];

        validarRangoNota(request.getNota(), notaMaxima);
        ResultadoNotaFinal resultado = calcularResultado(request.getNota(), notaMinima);

        Map<String, Object> antes = new HashMap<>();
        antes.put("nota", nota.getNota());
        antes.put("resultado", nota.getResultado());
        antes.put("observaciones", nota.getObservaciones());

        nota.setNota(request.getNota());
        nota.setResultado(resultado);
        nota.setObservaciones(request.getObservaciones());
        nota = notaFinalRepository.save(nota);

        auditoriaLogger.registrar(iniciarAuditoria(coordinador)
                .modulo(ModuloAuditoria.NOTA_FINAL)
                .tipoAccion(TipoAccion.EDITAR)
                .registroAfectadoId(nota.getId())
                .registroAfectadoTipo("NotaFinal")
                .valoresAnteriores(toJson(antes))
                .valoresNuevos(toJson(Map.of(
                        "nota", request.getNota(),
                        "resultado", resultado)))
                .exitoso(true));

        return construirResponse(nota,
                evaluacionDocenteRepository.findByPractica_Id(nota.getPractica().getId()).orElse(null),
                evaluacionTutorRepository.findByPractica_Id(nota.getPractica().getId()).orElse(null));
    }

    /**
     * Cierra el proceso de evaluación: marca la nota final como inmutable y
     * sincroniza Practica.notasCerradas para bloquear cambios en docente/tutor.
     * Es una transición irreversible (Proxy).
     */
    @RequiereRol(roles = {Rol.COORDINADOR_PRACTICAS})
    @Transactional
    public NotaFinalResponse cerrar(Long notaFinalId, CustomUserDetails coordinador) {
        NotaFinal nota = buscar(notaFinalId);

        if (Boolean.TRUE.equals(nota.getCerrada())) {
            throw new OperacionNoPermitidaException("La nota final ya está cerrada");
        }

        nota.setCerrada(true);
        nota.setCerradaEn(LocalDateTime.now());
        nota = notaFinalRepository.save(nota);

        Practica practica = nota.getPractica();
        practica.setNotasCerradas(true);
        practicaRepository.save(practica);

        auditoriaLogger.registrar(iniciarAuditoria(coordinador)
                .modulo(ModuloAuditoria.NOTA_FINAL)
                .tipoAccion(TipoAccion.CAMBIO_ESTADO)
                .registroAfectadoId(nota.getId())
                .registroAfectadoTipo("NotaFinal")
                .valoresNuevos(toJson(Map.of(
                        "cerrada", true,
                        "practicaId", practica.getId(),
                        "notasCerradas", true)))
                .exitoso(true));

        return construirResponse(nota,
                evaluacionDocenteRepository.findByPractica_Id(practica.getId()).orElse(null),
                evaluacionTutorRepository.findByPractica_Id(practica.getId()).orElse(null));
    }

    @Transactional(readOnly = true)
    public NotaFinalResponse obtenerPorPractica(Long practicaId) {
        NotaFinal nota = notaFinalRepository.findByPractica_Id(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe nota final para la práctica: " + practicaId));
        return construirResponse(nota,
                evaluacionDocenteRepository.findByPractica_Id(practicaId).orElse(null),
                evaluacionTutorRepository.findByPractica_Id(practicaId).orElse(null));
    }

    @Transactional(readOnly = true)
    public NotaFinalResponse obtenerPorId(Long id) {
        NotaFinal nota = buscar(id);
        return construirResponse(nota,
                evaluacionDocenteRepository.findByPractica_Id(nota.getPractica().getId()).orElse(null),
                evaluacionTutorRepository.findByPractica_Id(nota.getPractica().getId()).orElse(null));
    }

    // =========================================================================
    // Métodos privados
    // =========================================================================

    private NotaFinal buscar(Long id) {
        return notaFinalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Nota final no encontrada: " + id));
    }

    private Practica buscarPractica(Long practicaId) {
        return practicaRepository.findById(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Práctica no encontrada: " + practicaId));
    }

    private void validarPracticaEnCurso(Practica practica) {
        if (practica.getEstado() != EstadoPractica.EN_CURSO) {
            throw new OperacionNoPermitidaException(
                    "Solo se puede registrar la nota final en prácticas EN_CURSO. " +
                    "Estado actual: " + practica.getEstado());
        }
    }

    private void validarNotasNoCerradas(Practica practica) {
        if (Boolean.TRUE.equals(practica.getNotasCerradas())) {
            throw new OperacionNoPermitidaException(
                    "El proceso de evaluación ya fue cerrado para esta práctica");
        }
    }

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

    private ResultadoNotaFinal calcularResultado(double nota, double notaMinima) {
        return nota >= notaMinima ? ResultadoNotaFinal.APROBADO : ResultadoNotaFinal.REPROBADO;
    }

    private NotaFinalResponse construirResponse(
            NotaFinal nota, EvaluacionDocente evDocente, EvaluacionTutor evTutor) {

        return NotaFinalResponse.desde(
                nota,
                evDocente != null ? evDocente.getNota() : null,
                evDocente != null ? evDocente.getResultado() : null,
                evTutor != null ? evTutor.getNota() : null,
                evTutor != null ? evTutor.getResultado() : null
        );
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
