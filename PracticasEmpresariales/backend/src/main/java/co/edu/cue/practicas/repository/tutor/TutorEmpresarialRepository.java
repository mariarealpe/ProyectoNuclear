package co.edu.cue.practicas.repository.tutor;

import co.edu.cue.practicas.model.entity.TutorEmpresarial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutorEmpresarialRepository extends JpaRepository<TutorEmpresarial, Long> {

    Optional<TutorEmpresarial> findByUsuario_Id(Long usuarioId);

    boolean existsByUsuario_Id(Long usuarioId);

    Page<TutorEmpresarial> findByActivoTrue(Pageable pageable);

    Page<TutorEmpresarial> findByEmpresa_Id(Long empresaId, Pageable pageable);

    List<TutorEmpresarial> findByEmpresa_IdAndActivoTrue(Long empresaId);

    long countByEmpresa_IdAndActivoTrue(Long empresaId);
}
