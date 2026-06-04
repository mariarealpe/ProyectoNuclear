package co.edu.cue.practicas.service.notificacion;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.ActualizarPlantillaNotificacionRequest;
import co.edu.cue.practicas.dto.response.PlantillaNotificacionResponse;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.PlantillaNotificacion;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoEventoNotificacion;
import co.edu.cue.practicas.repository.notificacion.PlantillaNotificacionRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlantillaNotificacionServiceTest {

    @Mock private PlantillaNotificacionRepository plantillaRepository;
    @Mock private AuditoriaLogger auditoriaLogger;

    private PlantillaNotificacionService service;
    private CustomUserDetails admin;

    @BeforeEach
    void configurar() {
        service = new PlantillaNotificacionService(plantillaRepository, auditoriaLogger);
        Usuario adminUsuario = DatosDePrueba.administradorDti();
        admin = DatosDePrueba.userDetails(adminUsuario);
    }

    @Test
    void deberiaCrearPlantillaCuandoNoExisteParaElEvento() {
        ActualizarPlantillaNotificacionRequest request = nuevaRequest();
        when(plantillaRepository.findByEvento(TipoEventoNotificacion.NUEVA_ASIGNACION))
                .thenReturn(Optional.empty());
        when(plantillaRepository.save(any(PlantillaNotificacion.class)))
                .thenAnswer(inv -> {
                    PlantillaNotificacion p = inv.getArgument(0);
                    p.setId(1L);
                    return p;
                });

        PlantillaNotificacionResponse response =
                service.upsert(TipoEventoNotificacion.NUEVA_ASIGNACION, request, admin);

        assertThat(response.getEvento()).isEqualTo(TipoEventoNotificacion.NUEVA_ASIGNACION);
        assertThat(response.getAsunto()).isEqualTo(request.getAsunto());
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaActualizarPlantillaExistente() {
        PlantillaNotificacion existente = PlantillaNotificacion.builder()
                .id(99L)
                .evento(TipoEventoNotificacion.NUEVA_ASIGNACION)
                .asunto("Anterior")
                .cuerpoHtml("<p>viejo</p>")
                .rolReceptor(Rol.ESTUDIANTE)
                .obligatorio(false)
                .frecuenciaRecordatorioDias(5)
                .activa(true)
                .build();
        ActualizarPlantillaNotificacionRequest request = nuevaRequest();

        when(plantillaRepository.findByEvento(TipoEventoNotificacion.NUEVA_ASIGNACION))
                .thenReturn(Optional.of(existente));
        when(plantillaRepository.save(any(PlantillaNotificacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PlantillaNotificacionResponse response =
                service.upsert(TipoEventoNotificacion.NUEVA_ASIGNACION, request, admin);

        assertThat(response.getAsunto()).isEqualTo(request.getAsunto());
        assertThat(response.getCuerpoHtml()).isEqualTo(request.getCuerpoHtml());
        assertThat(response.getId()).isEqualTo(99L);
    }

    @Test
    void deberiaFallarObtenerSiPlantillaNoExiste() {
        when(plantillaRepository.findByEvento(TipoEventoNotificacion.ALERTA_INACTIVIDAD))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.obtenerPorEvento(TipoEventoNotificacion.ALERTA_INACTIVIDAD))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void deberiaReemplazarVariablesEnPrevisualizacion() {
        String cuerpo = "Hola {{nombre_estudiante}}, ingresa a {{enlace_encuesta}}";
        Map<String, String> vars = Map.of(
                "nombre_estudiante", "Juan",
                "enlace_encuesta", "https://x.com/e/1");

        String resultado = service.previsualizar(cuerpo, vars);

        assertThat(resultado).isEqualTo("Hola Juan, ingresa a https://x.com/e/1");
    }

    @Test
    void deberiaIgnorarVariableNoProvistaYDejarVacio() {
        String cuerpo = "Hola {{nombre}}, tu correo es {{correo}}";
        Map<String, String> vars = Map.of("nombre", "Ana");

        String resultado = service.previsualizar(cuerpo, vars);

        assertThat(resultado).isEqualTo("Hola Ana, tu correo es ");
    }

    @Test
    void deberiaRenderizarPlantillaActivaConVariables() {
        PlantillaNotificacion p = PlantillaNotificacion.builder()
                .evento(TipoEventoNotificacion.NUEVA_ASIGNACION)
                .asunto("Asignación para {{nombre_estudiante}}")
                .cuerpoHtml("<p>Empresa: {{empresa}}</p>")
                .obligatorio(true)
                .frecuenciaRecordatorioDias(3)
                .activa(true)
                .build();
        when(plantillaRepository.findByEvento(TipoEventoNotificacion.NUEVA_ASIGNACION))
                .thenReturn(Optional.of(p));

        PlantillaNotificacionService.RenderResult result =
                service.renderizarParaEvento(TipoEventoNotificacion.NUEVA_ASIGNACION,
                        Map.of("nombre_estudiante", "Ana", "empresa", "ACME"));

        assertThat(result).isNotNull();
        assertThat(result.asunto()).isEqualTo("Asignación para Ana");
        assertThat(result.cuerpoHtml()).contains("ACME");
        assertThat(result.obligatorio()).isTrue();
    }

    @Test
    void deberiaRetornarNullSiPlantillaInactiva() {
        PlantillaNotificacion p = PlantillaNotificacion.builder()
                .evento(TipoEventoNotificacion.NUEVA_ASIGNACION)
                .asunto("Asunto")
                .cuerpoHtml("<p>Cuerpo</p>")
                .activa(false)
                .obligatorio(false)
                .frecuenciaRecordatorioDias(3)
                .build();
        when(plantillaRepository.findByEvento(TipoEventoNotificacion.NUEVA_ASIGNACION))
                .thenReturn(Optional.of(p));

        PlantillaNotificacionService.RenderResult result =
                service.renderizarParaEvento(TipoEventoNotificacion.NUEVA_ASIGNACION, Map.of());

        assertThat(result).isNull();
    }

    private ActualizarPlantillaNotificacionRequest nuevaRequest() {
        ActualizarPlantillaNotificacionRequest r = new ActualizarPlantillaNotificacionRequest();
        r.setAsunto("Nueva asignación — {{nombre_estudiante}}");
        r.setCuerpoHtml("<p>Hola {{nombre_estudiante}}, tienes asignación en {{empresa}}.</p>");
        r.setRolReceptor(Rol.ESTUDIANTE);
        r.setObligatorio(true);
        r.setFrecuenciaRecordatorioDias(3);
        r.setActiva(true);
        return r;
    }
}
