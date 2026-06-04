package co.edu.cue.practicas.service.asignacion;

import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.Asignacion;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.entity.Vacante;
import co.edu.cue.practicas.model.enums.EstadoAsignacion;
import co.edu.cue.practicas.model.enums.TipoNotificacion;
import co.edu.cue.practicas.repository.asignacion.AsignacionRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import co.edu.cue.practicas.repository.vacante.VacanteRepository;
import co.edu.cue.practicas.service.notificacion.NotificacionSprint3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * PATRON FACADE — GPE-157, GPE-158, GPE-159
 *
 * Centraliza el flujo de asignacion para pruebas en Postman:
 * crear asignacion, mover estados y consultar trazabilidad.
 * Delega validaciones, transiciones, trazabilidad y mapeo para respetar SRP
 * y evitar el antipatrÃ³n God Service.
 */
@Service
@RequiredArgsConstructor
public class AsignacionService {

    private final AsignacionRepository asignacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final VacanteRepository vacanteRepository;
    private final AsignacionValidator asignacionValidator;
    private final AsignacionEstadoService estadoService;
    private final AsignacionTrazabilidadService trazabilidadService;
    private final AsignacionMapper mapper;
    private final NotificacionSprint3Service notificacionService;

    @Transactional
    public Map<String, Object> crear(Long estudianteId, Long vacanteId, Long coordinadorId) {
        Usuario estudiante = usuarioRepository.findById(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado"));
        Usuario coordinador = usuarioRepository.findById(coordinadorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Coordinador no encontrado"));
        Vacante vacante = vacanteRepository.findById(vacanteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vacante no encontrada"));

        asignacionValidator.validarAsignacion(estudiante, coordinador, vacante);

        Asignacion asignacion = Asignacion.builder()
                .estudiante(estudiante)
                .vacante(vacante)
                .coordinador(coordinador)
                .estado(EstadoAsignacion.ASIGNADA)
                .build();

        estadoService.ocuparCupo(vacante);

        Asignacion guardada = asignacionRepository.save(asignacion);
        registrarCambio(guardada, EstadoAsignacion.ASIGNADA, EstadoAsignacion.ASIGNADA,
                "Asignacion creada por Coordinador de Practicas", coordinador);

        notificacionService.registrar(
                estudiante.getId(),
                TipoNotificacion.ASIGNACION_CREADA,
                "Nueva asignacion de practica",
                "Has sido asignado a la vacante: " + vacante.getTitulo(),
                guardada.getId(),
                null
        );

        return mapper.toMap(guardada);
    }

    @Transactional
    public Map<String, Object> cambiarEstado(Long asignacionId, EstadoAsignacion nuevoEstado, Long usuarioId, String motivo) {
        Asignacion asignacion = buscar(asignacionId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        EstadoAsignacion anterior = asignacion.getEstado();
        estadoService.aplicarTransicion(asignacion, nuevoEstado, motivo);

        if (nuevoEstado == EstadoAsignacion.CANCELADA) {
            notificacionService.registrar(
                    asignacion.getEstudiante().getId(),
                    TipoNotificacion.ASIGNACION_CANCELADA,
                    "Asignacion cancelada",
                    motivo == null ? "La asignacion fue cancelada." : motivo,
                    asignacion.getId(),
                    null
            );
        }

        registrarCambio(asignacion, anterior, nuevoEstado, motivo, usuario);
        return mapper.toMap(asignacionRepository.save(asignacion));
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> listar(EstadoAsignacion estado, Long coordinadorId, Pageable pageable) {
        Page<Asignacion> page;
        if (coordinadorId != null && estado != null) {
            page = asignacionRepository.findByCoordinador_IdAndEstado(coordinadorId, estado, pageable);
        } else if (coordinadorId != null) {
            page = asignacionRepository.findByCoordinador_Id(coordinadorId, pageable);
        } else {
            page = asignacionRepository.findAll(pageable);
        }
        return page.map(mapper::toMap);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtener(Long id) {
        return mapper.toMap(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> historial(Long asignacionId) {
        return trazabilidadService.historial(asignacionId)
                .stream()
                .map(mapper::cambioToMap)
                .toList();
    }

    public Asignacion buscar(Long id) {
        return asignacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asignacion no encontrada"));
    }

    public void registrarCambio(Asignacion asignacion,
                                EstadoAsignacion anterior,
                                EstadoAsignacion nuevo,
                                String motivo,
                                Usuario usuario) {
        trazabilidadService.registrarCambio(asignacion, anterior, nuevo, motivo, usuario);
    }
}
