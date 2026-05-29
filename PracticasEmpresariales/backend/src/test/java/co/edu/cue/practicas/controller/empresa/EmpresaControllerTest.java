package co.edu.cue.practicas.controller.empresa;

import co.edu.cue.practicas.dto.request.CrearEmpresaRequest;
import co.edu.cue.practicas.dto.request.EditarEmpresaRequest;
import co.edu.cue.practicas.dto.response.EmpresaResponse;
import co.edu.cue.practicas.exception.GlobalExceptionHandler;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.security.CustomUserDetails;
import co.edu.cue.practicas.service.empresa.EmpresaService;
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
class EmpresaControllerTest {

    @Mock
    private EmpresaService empresaService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new EmpresaController(empresaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    void deberiaCrearEmpresaYResponderCreated() throws Exception {
        when(empresaService.crearEmpresa(any(CrearEmpresaRequest.class), nullable(CustomUserDetails.class)))
                .thenReturn(empresaResponse());

        mockMvc.perform(post("/empresas")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nit": "900123456-7",
                                  "razonSocial": "Soft CUE SAS",
                                  "nombreComercial": "Soft CUE",
                                  "sector": "Tecnologia",
                                  "direccion": "Calle 10 # 20-30",
                                  "ciudad": "Armenia",
                                  "correoContacto": "contacto@softcue.com",
                                  "telefono": "6061234567",
                                  "sitioWeb": "https://softcue.com",
                                  "representanteLegal": "Ana Gomez",
                                  "descripcion": "Empresa aliada"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Empresa registrada exitosamente"))
                .andExpect(jsonPath("$.datos.id").value(8))
                .andExpect(jsonPath("$.datos.nit").value("900123456-7"));
    }

    @Test
    void deberiaResponderBadRequestCuandoNitEsVacio() throws Exception {
        mockMvc.perform(post("/empresas")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nit": "",
                                  "razonSocial": "Soft CUE SAS",
                                  "correoContacto": "contacto@softcue.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.datos.nit").exists());

        verify(empresaService, never()).crearEmpresa(any(), any());
    }

    @Test
    void deberiaListarEmpresasConFiltro() throws Exception {
        when(empresaService.listar(any(Pageable.class), eq("soft")))
                .thenReturn(new PageImpl<>(List.of(empresaResponse()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/empresas")
                        .param("page", "0")
                        .param("size", "20")
                        .param("filtro", "soft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.datos.content[0].razonSocial").value("Soft CUE SAS"));
    }

    @Test
    void deberiaResponderNotFoundCuandoEmpresaNoExiste() throws Exception {
        when(empresaService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Empresa no encontrada: 99"));

        mockMvc.perform(get("/empresas/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Empresa no encontrada: 99"));
    }

    @Test
    void deberiaEditarEmpresaYResponderOk() throws Exception {
        when(empresaService.editarEmpresa(eq(8L), any(EditarEmpresaRequest.class), nullable(CustomUserDetails.class)))
                .thenReturn(empresaResponseEditada());

        mockMvc.perform(put("/empresas/{id}", 8L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nit": "900123456-7",
                                  "razonSocial": "Soft CUE Actualizada SAS",
                                  "nombreComercial": "Soft CUE",
                                  "sector": "Tecnologia",
                                  "direccion": "Calle 10 # 20-30",
                                  "ciudad": "Armenia",
                                  "correoContacto": "contacto@softcue.com",
                                  "telefono": "6061234567",
                                  "sitioWeb": "https://softcue.com",
                                  "representanteLegal": "Ana Gomez",
                                  "descripcion": "Empresa aliada actualizada"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Empresa actualizada"))
                .andExpect(jsonPath("$.datos.razonSocial").value("Soft CUE Actualizada SAS"));
    }

    @Test
    void deberiaResponderConflictCuandoNitYaEstaEnUsoAlEditar() throws Exception {
        when(empresaService.editarEmpresa(eq(8L), any(EditarEmpresaRequest.class), nullable(CustomUserDetails.class)))
                .thenThrow(new OperacionNoPermitidaException("El NIT 900999999-1 ya esta en uso por otra empresa."));

        mockMvc.perform(put("/empresas/{id}", 8L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nit": "900999999-1",
                                  "razonSocial": "Soft CUE SAS",
                                  "correoContacto": "contacto@softcue.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("El NIT 900999999-1 ya esta en uso por otra empresa."));
    }

    @Test
    void deberiaActivarEmpresaYResponderOk() throws Exception {
        mockMvc.perform(patch("/empresas/{id}/activar", 8L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.mensaje").value("Empresa activada"));

        verify(empresaService).activarEmpresa(eq(8L), nullable(CustomUserDetails.class));
    }

    private EmpresaResponse empresaResponse() {
        return EmpresaResponse.builder()
                .id(8L)
                .nit("900123456-7")
                .razonSocial("Soft CUE SAS")
                .nombreComercial("Soft CUE")
                .sector("Tecnologia")
                .direccion("Calle 10 # 20-30")
                .ciudad("Armenia")
                .correoContacto("contacto@softcue.com")
                .telefono("6061234567")
                .sitioWeb("https://softcue.com")
                .representanteLegal("Ana Gomez")
                .descripcion("Empresa aliada")
                .activo(true)
                .build();
    }

    private EmpresaResponse empresaResponseEditada() {
        EmpresaResponse response = empresaResponse();
        response.setRazonSocial("Soft CUE Actualizada SAS");
        response.setDescripcion("Empresa aliada actualizada");
        return response;
    }
}
