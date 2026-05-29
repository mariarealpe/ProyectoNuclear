package co.edu.cue.practicas.controller.vacante;

import co.edu.cue.practicas.dto.request.CrearVacanteRequest;
import co.edu.cue.practicas.dto.request.DecisionVacanteRequest;
import co.edu.cue.practicas.dto.request.EditarVacanteRequest;
import co.edu.cue.practicas.dto.response.BitacoraVacanteResponse;
import co.edu.cue.practicas.dto.response.VacanteResponse;
import co.edu.cue.practicas.exception.AccesoNoAutorizadoException;
import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.enums.EstadoVacante;
import co.edu.cue.practicas.model.enums.JornadaVacante;
import co.edu.cue.practicas.model.enums.ModalidadVacante;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.vacante.VacanteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VacanteControllerTest {

    @Mock
    private VacanteService vacanteService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new VacanteController(vacanteService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    void deberiaCrearVacanteYResponderCreated() throws Exception {
        when(vacanteService.crearVacante(any(CrearVacanteRequest.class), nullable(CustomUserDetails.class)))
                .thenReturn(vacanteResponse(EstadoVacante.PENDIENTE));

        mockMvc.perform(post("/vacantes")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Practicante Backend Java",
                                  "descripcion": "Apoyo en desarrollo de servicios",
                                  "empresaId": 8,
                                  "tutorId": 5,
                                  "programaId": 2,
                                  "modalidad": "HIBRIDO",
                                  "jornada": "MEDIO_TIEMPO",
                                  "ciudad": "Armenia",
                                  "cupos": 2,
                                  "duracionMeses": 6,
                                  "remunerada": true,
                                  "valorRemuneracion": 1300000,
                                  "fechaInicio": "2026-06-15",
                                  "fechaFin": "2026-12-15",
                                  "requisitos": "Java basico",
                                  "responsabilidades": "Construir endpoints"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value(containsString("Vacante registrada")))
                .andExpect(jsonPath("$.datos.id").value(20))
                .andExpect(jsonPath("$.datos.estado").value("PENDIENTE"));
    }

    @Test
    void deberiaResponderBadRequestCuandoTituloEsVacio() throws Exception {
        mockMvc.perform(post("/vacantes")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "",
                                  "descripcion": "Apoyo en desarrollo de servicios",
                                  "empresaId": 8,
                                  "programaId": 2,
                                  "modalidad": "HIBRIDO",
                                  "jornada": "MEDIO_TIEMPO",
                                  "cupos": 2,
                                  "duracionMeses": 6
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.datos.titulo").exists());

        verify(vacanteService, never()).crearVacante(any(), any());
    }

    @Test
    void deberiaListarVacantesFiltradasPorEstado() throws Exception {
        when(vacanteService.listar(eq(EstadoVacante.DISPONIBLE), any(Pageable.class), nullable(CustomUserDetails.class)))
                .thenReturn(new PageImpl<>(List.of(vacanteResponse(EstadoVacante.DISPONIBLE)),
                        PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/vacantes")
                        .param("estado", "DISPONIBLE")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos.content[0].estado").value("DISPONIBLE"));
    }

    @Test
    void deberiaResponderNotFoundCuandoVacanteNoExiste() throws Exception {
        when(vacanteService.obtenerPorId(eq(99L), nullable(CustomUserDetails.class)))
                .thenThrow(new RecursoNoEncontradoException("Vacante no encontrada: 99"));

        mockMvc.perform(get("/vacantes/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Vacante no encontrada: 99"));
    }

    @Test
    void deberiaResponderForbiddenCuandoUsuarioNoPuedeVerVacante() throws Exception {
        when(vacanteService.obtenerPorId(eq(20L), nullable(CustomUserDetails.class)))
                .thenThrow(new AccesoNoAutorizadoException("Esta vacante no esta disponible para usted."));

        mockMvc.perform(get("/vacantes/{id}", 20L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Esta vacante no esta disponible para usted."));
    }

    @Test
    void deberiaAprobarVacanteYResponderOk() throws Exception {
        when(vacanteService.aprobarVacante(eq(20L), any(DecisionVacanteRequest.class), nullable(CustomUserDetails.class)))
                .thenReturn(vacanteResponse(EstadoVacante.DISPONIBLE));

        mockMvc.perform(patch("/vacantes/{id}/aprobar", 20L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "motivo": "Cumple requisitos"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Vacante aprobada"))
                .andExpect(jsonPath("$.datos.estado").value("DISPONIBLE"));
    }

    @Test
    void deberiaResponderConflictCuandoRechazoNoTieneMotivo() throws Exception {
        when(vacanteService.rechazarVacante(eq(20L), any(DecisionVacanteRequest.class), nullable(CustomUserDetails.class)))
                .thenThrow(new OperacionNoPermitidaException("El motivo del rechazo es obligatorio."));

        mockMvc.perform(patch("/vacantes/{id}/rechazar", 20L)
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("El motivo del rechazo es obligatorio."));
    }

    @Test
    void deberiaEditarVacanteYResponderOk() throws Exception {
        when(vacanteService.editarVacante(eq(20L), any(EditarVacanteRequest.class), nullable(CustomUserDetails.class)))
                .thenReturn(vacanteResponse(EstadoVacante.PENDIENTE));

        mockMvc.perform(put("/vacantes/{id}", 20L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Practicante Backend Java",
                                  "descripcion": "Apoyo en desarrollo de APIs",
                                  "tutorId": 5,
                                  "modalidad": "REMOTO",
                                  "jornada": "MEDIO_TIEMPO",
                                  "ciudad": "Armenia",
                                  "cupos": 2,
                                  "duracionMeses": 6,
                                  "remunerada": false,
                                  "fechaInicio": "2026-06-15",
                                  "fechaFin": "2026-12-15",
                                  "requisitos": "Java basico",
                                  "responsabilidades": "Construir endpoints"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Vacante actualizada"))
                .andExpect(jsonPath("$.datos.titulo").value("Practicante Backend Java"));
    }

    @Test
    void deberiaConsultarHistorialDeVacante() throws Exception {
        when(vacanteService.historial(eq(20L), nullable(CustomUserDetails.class)))
                .thenReturn(List.of(BitacoraVacanteResponse.builder()
                        .id(1L)
                        .vacanteId(20L)
                        .estadoAnterior(EstadoVacante.PENDIENTE)
                        .estadoNuevo(EstadoVacante.DISPONIBLE)
                        .motivo("Cumple requisitos")
                        .build()));

        mockMvc.perform(get("/vacantes/{id}/historial", 20L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos[0].estadoNuevo").value("DISPONIBLE"));
    }

    private VacanteResponse vacanteResponse(EstadoVacante estado) {
        return VacanteResponse.builder()
                .id(20L)
                .titulo("Practicante Backend Java")
                .descripcion("Apoyo en desarrollo de servicios")
                .empresaId(8L)
                .empresaRazonSocial("Soft CUE SAS")
                .empresaNit("900123456-7")
                .tutorId(5L)
                .tutorNombre("Laura Tutor")
                .programaId(2L)
                .programaNombre("Ingenieria de Sistemas")
                .modalidad(ModalidadVacante.HIBRIDO)
                .jornada(JornadaVacante.MEDIO_TIEMPO)
                .ciudad("Armenia")
                .cupos(2)
                .cuposOcupados(0)
                .duracionMeses(6)
                .remunerada(true)
                .valorRemuneracion(1300000.0)
                .requisitos("Java basico")
                .responsabilidades("Construir endpoints")
                .estado(estado)
                .build();
    }
}
