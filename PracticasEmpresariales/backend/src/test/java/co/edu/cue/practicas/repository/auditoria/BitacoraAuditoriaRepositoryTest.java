package co.edu.cue.practicas.repository.auditoria;

import co.edu.cue.practicas.model.entity.BitacoraAuditoria;
import co.edu.cue.practicas.model.entity.Usuario;
import co.edu.cue.practicas.model.enums.Rol;
import co.edu.cue.practicas.model.enums.TipoAccion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("h2")
class BitacoraAuditoriaRepositoryTest {

    @Autowired
    private BitacoraAuditoriaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void deberiaFiltrarBitacoraPorUsuarioTipoFechasYModulo() {
        Usuario usuario = entityManager.persist(Usuario.builder()
                .nombre("Admin DTI")
                .correo("admin@cue.edu.co")
                .passwordHash("hash")
                .rol(Rol.ADMIN_DTI)
                .activo(true)
                .primerIngreso(false)
                .build());

        LocalDateTime fechaDentroDelRango = LocalDateTime.of(2026, 5, 15, 10, 0);
        LocalDateTime fechaFueraDelRango = LocalDateTime.of(2026, 4, 30, 23, 59);

        entityManager.persist(BitacoraAuditoria.builder()
                .usuario(usuario)
                .nombreUsuario("Admin DTI")
                .rolUsuario(Rol.ADMIN_DTI)
                .fechaHora(fechaDentroDelRango)
                .modulo("AUTH")
                .tipoAccion(TipoAccion.LOGIN_EXITOSO)
                .exitoso(true)
                .build());

        entityManager.persist(BitacoraAuditoria.builder()
                .usuario(usuario)
                .nombreUsuario("Admin DTI")
                .rolUsuario(Rol.ADMIN_DTI)
                .fechaHora(fechaFueraDelRango)
                .modulo("AUTH")
                .tipoAccion(TipoAccion.LOGIN_FALLIDO)
                .exitoso(false)
                .build());

        entityManager.flush();

        Page<BitacoraAuditoria> resultado = repository.filtrar(
                usuario.getId(),
                TipoAccion.LOGIN_EXITOSO,
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 31, 23, 59),
                "AUTH",
                PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        BitacoraAuditoria entrada = resultado.getContent().getFirst();
        assertThat(entrada.getNombreUsuario()).isEqualTo("Admin DTI");
        assertThat(entrada.getTipoAccion()).isEqualTo(TipoAccion.LOGIN_EXITOSO);
        assertThat(entrada.getFechaHora()).isEqualTo(fechaDentroDelRango);
    }
}
