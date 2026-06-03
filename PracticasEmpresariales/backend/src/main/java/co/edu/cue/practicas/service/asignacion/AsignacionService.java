package co.edu.cue.practicas.service.asignacion;

import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.Asignacion;
import co.edu.cue.practicas.model.entity.CambioEstadoAsignacion;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.entity.Vacante;
import co.edu.cue.practicas.model.enums.EstadoAsignacion;
import co.edu.cue.practicas.model.enums.EstadoEstudiante;
import co.edu.cue.practicas.model.enums.EstadoVacante;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoNotificacion;
import co.edu.cue.practicas.repository.asignacion.AsignacionRepository;
import co.edu.cue.practicas.repository.asignacion.CambioEstadoAsignacionRepository;
import co.edu.cue.practicas.repository.usuario.UsuarioRepository;
import co.edu.cue.practicas.repository.vacante.VacanteRepository;
import co.edu.cue.practicas.service.notificacion.NotificacionSprint3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PATRON FACADE — GPE-157, GPE-158, GPE-159
 *
 * Centraliza el flujo de asignacion para pruebas en Postman:
 * crear asignacion, mover estados y consultar trazabilidad.
 * El detalle de repositorios y cambios de estado queda oculto al controller.
 */
@Service
@RequiredArgsConstructor
public class AsignacionService {

    private static final List<EstadoAsignacion> ESTADOS_ACTIVOS = List.of(
            EstadoAsignacion.ASIGNADA,
            EstadoAsignacion.EN_VINCULACION,
            EstadoAsignacion.EN_CURSO
    );

    private final AsignacionRepository asignacionRepository;
    private final CambioEstadoAsignacionRepository cambioRepository;
    private final UsuarioRepository usuarioRepository;
    private final VacanteRepository vacanteRepository;
    private final NotificacionSprint3Service notificacionService;

    @Transactional
    public Map<String, Object> crear(Long estudianteId, Long vacanteId, Long coordinadorId) {
        Usuario estudiante = usuarioRepository.findById(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado"));
        Usuario coordinador = usuarioRepository.findById(coordinadorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Coordinador no encontrado"));
        Vacante vacante = vacanteRepository.findById(vacanteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vacante no encontrada"));

        validarAsignacion(estudiante, coordinador, vacante);

        Asignacion asignacion = Asignacion.builder()
                .estudiante(estudiante)
                .vacante(vacante)
                .coordinador(coordinador)
                .estado(EstadoAsignacion.ASIGNADA)
                .build();

        vacante.setCuposOcupados(vacante.getCuposOcupados() + 1);
        if (vacante.getCuposOcupados() >= vacante.getCupos()) {
            vacante.setEstado(EstadoVacante.CERRADA);
        }

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

        return toMap(guardada);
    }

    @Transactional
    public Map<String, Object> cambiarEstado(Long asignacionId, EstadoAsignacion nuevoEstado, Long usuarioId, String motivo) {
        Asignacion asignacion = buscar(asignacionId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        EstadoAsignacion anterior = asignacion.getEstado();
        validarTransicion(anterior, nuevoEstado);

        asignacion.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoAsignacion.EN_VINCULACION) {
            asignacion.setFechaVinculacion(LocalDateTime.now());
        }
        if (nuevoEstado == EstadoAsignacion.CANCELADA) {
            asignacion.setMotivoCancelacion(motivo);
            liberarCupo(asignacion.getVacante());
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
        return toMap(asignacionRepository.save(asignacion));
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
        return page.map(this::toMap);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtener(Long id) {
        return toMap(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> historial(Long asignacionId) {
        return cambioRepository.findByAsignacion_IdOrderByFechaHoraDesc(asignacionId)
                .stream()
                .map(this::cambioToMap)
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
        cambioRepository.save(CambioEstadoAsignacion.builder()
                .asignacion(asignacion)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .motivo(motivo)
                .usuario(usuario)
                .build());
    }

    private void validarAsignacion(Usuario estudiante, Usuario coordinador, Vacante vacante) {
        if (estudiante.getRol() != Rol.ESTUDIANTE) {
            throw new OperacionNoPermitidaException("El usuario seleccionado no es estudiante");
        }
        if (estudiante.getEstadoEstudiante() != EstadoEstudiante.APTO) {
            throw new OperacionNoPermitidaException("El estudiante debe estar APTO para asignacion");
        }
        if (coordinador.getRol() != Rol.COORDINADOR_PRACTICAS) {
            throw new OperacionNoPermitidaException("Solo un Coordinador de Practicas puede asignar");
        }
        if (vacante.getEstado() != EstadoVacante.DISPONIBLE) {
            throw new OperacionNoPermitidaException("La vacante debe estar DISPONIBLE");
        }
        if (vacante.getCuposOcupados() >= vacante.getCupos()) {
            throw new OperacionNoPermitidaException("La vacante no tiene cupos disponibles");
        }
        if (asignacionRepository.existsByEstudiante_IdAndEstadoIn(estudiante.getId(), ESTADOS_ACTIVOS)) {
            throw new OperacionNoPermitidaException("El estudiante ya tiene asignacion activa");
        }
    }

    private void validarTransicion(EstadoAsignacion anterior, EstadoAsignacion nuevo) {
        boolean valida = (anterior == EstadoAsignacion.ASIGNADA && nuevo == EstadoAsignacion.EN_VINCULACION)
                || (anterior == EstadoAsignacion.ASIGNADA && nuevo == EstadoAsignacion.CANCELADA)
                || (anterior == EstadoAsignacion.EN_VINCULACION && nuevo == EstadoAsignacion.EN_CURSO);
        if (!valida) {
            throw new OperacionNoPermitidaException("Transicion de estado no permitida: " + anterior + " -> " + nuevo);
        }
    }

    private void liberarCupo(Vacante vacante) {
        if (vacante.getCuposOcupados() > 0) {
            vacante.setCuposOcupados(vacante.getCuposOcupados() - 1);
        }
        if (vacante.getEstado() == EstadoVacante.CERRADA && vacante.getCuposOcupados() < vacante.getCupos()) {
            vacante.setEstado(EstadoVacante.DISPONIBLE);
        }
    }

    public Map<String, Object> toMap(Asignacion a) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", a.getId());
        map.put("estudianteId", a.getEstudiante().getId());
        map.put("estudiante", a.getEstudiante().getNombre());
        map.put("vacanteId", a.getVacante().getId());
        map.put("vacante", a.getVacante().getTitulo());
        map.put("coordinadorId", a.getCoordinador().getId());
        map.put("estado", a.getEstado());
        map.put("motivoCancelacion", a.getMotivoCancelacion());
        map.put("fechaAsignacion", a.getFechaAsignacion());
        map.put("fechaVinculacion", a.getFechaVinculacion());
        map.put("fechaInicio", a.getFechaInicio());
        map.put("fechaFin", a.getFechaFin());
        return map;
    }

    private Map<String, Object> cambioToMap(CambioEstadoAsignacion c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("asignacionId", c.getAsignacion().getId());
        map.put("estadoAnterior", c.getEstadoAnterior());
        map.put("estadoNuevo", c.getEstadoNuevo());
        map.put("motivo", c.getMotivo());
        map.put("usuarioId", c.getUsuario().getId());
        map.put("usuario", c.getUsuario().getNombre());
        map.put("fechaHora", c.getFechaHora());
        return map;
    }
}
