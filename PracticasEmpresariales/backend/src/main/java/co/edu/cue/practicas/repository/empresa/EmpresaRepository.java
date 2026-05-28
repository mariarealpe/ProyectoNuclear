package co.edu.cue.practicas.repository.empresa;

import co.edu.cue.practicas.model.entity.Empresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByNit(String nit);

    boolean existsByNit(String nit);

    boolean existsByNitAndIdNot(String nit, Long id);

    Page<Empresa> findByActivoTrue(Pageable pageable);

    Page<Empresa> findByRazonSocialContainingIgnoreCaseAndActivoTrue(String razonSocial, Pageable pageable);

    long countByActivoTrue();
}
