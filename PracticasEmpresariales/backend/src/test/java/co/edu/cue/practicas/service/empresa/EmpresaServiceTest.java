package co.edu.cue.practicas.service.empresa;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.audit.singleton.AuditoriaLogger;
import co.edu.cue.practicas.dto.request.CrearEmpresaRequest;
import co.edu.cue.practicas.dto.request.EditarEmpresaRequest;
import co.edu.cue.practicas.dto.response.EmpresaResponse;
import co.edu.cue.practicas.exception.OperacionNoPermitidaException;
import co.edu.cue.practicas.exception.RecursoNoEncontradoException;
import co.edu.cue.practicas.model.entity.Empresa;
import co.edu.cue.practicas.repository.empresa.EmpresaRepository;
import co.edu.cue.practicas.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private AuditoriaLogger auditoriaLogger;

    private EmpresaService empresaService;
    private CustomUserDetails admin;

    @BeforeEach
    void configurar() {
        empresaService = new EmpresaService(empresaRepository, auditoriaLogger, new ObjectMapper());
        admin = DatosDePrueba.userDetails(DatosDePrueba.administradorDti());
    }

    @Test
    void deberiaCrearEmpresaCuandoNitNoExiste() {
        CrearEmpresaRequest request = crearEmpresaRequest();
        when(empresaRepository.existsByNit("900123456-7")).thenReturn(false);
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocation -> {
            Empresa empresa = invocation.getArgument(0);
            empresa.setId(8L);
            return empresa;
        });

        EmpresaResponse response = empresaService.crearEmpresa(request, admin);

        assertThat(response.getId()).isEqualTo(8L);
        assertThat(response.getNit()).isEqualTo("900123456-7");
        assertThat(response.getRazonSocial()).isEqualTo("Soft CUE SAS");
        assertThat(response.isActivo()).isTrue();

        ArgumentCaptor<Empresa> empresaCaptor = ArgumentCaptor.forClass(Empresa.class);
        verify(empresaRepository).save(empresaCaptor.capture());
        assertThat(empresaCaptor.getValue().getCorreoContacto()).isEqualTo("contacto@softcue.com");
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaRechazarCreacionConNitDuplicado() {
        CrearEmpresaRequest request = crearEmpresaRequest();
        when(empresaRepository.existsByNit("900123456-7")).thenReturn(true);

        assertThatThrownBy(() -> empresaService.crearEmpresa(request, admin))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("NIT 900123456-7");

        verify(empresaRepository, never()).save(any());
        verify(auditoriaLogger, never()).registrar(any());
    }

    @Test
    void deberiaEditarEmpresaExistenteYRegistrarAuditoria() {
        Empresa empresa = empresa();
        EditarEmpresaRequest request = editarEmpresaRequest("900123456-7", "Soft CUE Actualizada SAS");
        when(empresaRepository.findById(8L)).thenReturn(Optional.of(empresa));

        EmpresaResponse response = empresaService.editarEmpresa(8L, request, admin);

        assertThat(response.getRazonSocial()).isEqualTo("Soft CUE Actualizada SAS");
        assertThat(empresa.getCiudad()).isEqualTo("Pereira");
        assertThat(empresa.getDescripcion()).isEqualTo("Datos actualizados");
        verify(empresaRepository).save(empresa);
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaRechazarEdicionCuandoNuevoNitPerteneceAOtraEmpresa() {
        Empresa empresa = empresa();
        EditarEmpresaRequest request = editarEmpresaRequest("900999999-1", "Soft CUE SAS");
        when(empresaRepository.findById(8L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.existsByNitAndIdNot("900999999-1", 8L)).thenReturn(true);

        assertThatThrownBy(() -> empresaService.editarEmpresa(8L, request, admin))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("900999999-1");

        verify(empresaRepository, never()).save(any());
    }

    @Test
    void deberiaDesactivarEmpresaExistente() {
        Empresa empresa = empresa();
        when(empresaRepository.findById(8L)).thenReturn(Optional.of(empresa));

        empresaService.desactivarEmpresa(8L, admin);

        assertThat(empresa.isActivo()).isFalse();
        verify(empresaRepository).save(empresa);
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaActivarEmpresaExistente() {
        Empresa empresa = empresa();
        empresa.setActivo(false);
        when(empresaRepository.findById(8L)).thenReturn(Optional.of(empresa));

        empresaService.activarEmpresa(8L, admin);

        assertThat(empresa.isActivo()).isTrue();
        verify(empresaRepository).save(empresa);
        verify(auditoriaLogger).registrar(any());
    }

    @Test
    void deberiaLanzarNoEncontradoCuandoEmpresaNoExiste() {
        when(empresaRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> empresaService.obtenerPorId(404L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Empresa no encontrada: 404");
    }

    @Test
    void deberiaListarEmpresasActivasFiltradas() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(empresaRepository.findByRazonSocialContainingIgnoreCaseAndActivoTrue("soft", pageable))
                .thenReturn(new PageImpl<>(List.of(empresa()), pageable, 1));

        Page<EmpresaResponse> response = empresaService.listar(pageable, "soft");

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().getNit()).isEqualTo("900123456-7");
    }

    private CrearEmpresaRequest crearEmpresaRequest() {
        CrearEmpresaRequest request = new CrearEmpresaRequest();
        request.setNit("900123456-7");
        request.setRazonSocial("Soft CUE SAS");
        request.setNombreComercial("Soft CUE");
        request.setSector("Tecnologia");
        request.setDireccion("Calle 10 # 20-30");
        request.setCiudad("Armenia");
        request.setCorreoContacto("contacto@softcue.com");
        request.setTelefono("6061234567");
        request.setSitioWeb("https://softcue.com");
        request.setRepresentanteLegal("Ana Gomez");
        request.setDescripcion("Empresa aliada");
        return request;
    }

    private EditarEmpresaRequest editarEmpresaRequest(String nit, String razonSocial) {
        EditarEmpresaRequest request = new EditarEmpresaRequest();
        request.setNit(nit);
        request.setRazonSocial(razonSocial);
        request.setNombreComercial("Soft CUE");
        request.setSector("Tecnologia");
        request.setDireccion("Carrera 15 # 10-20");
        request.setCiudad("Pereira");
        request.setCorreoContacto("actualizado@softcue.com");
        request.setTelefono("6067654321");
        request.setSitioWeb("https://softcue.com");
        request.setRepresentanteLegal("Ana Gomez");
        request.setDescripcion("Datos actualizados");
        return request;
    }

    private Empresa empresa() {
        return Empresa.builder()
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
}
