package co.edu.cue.practicas.repository.vacante;

import co.edu.cue.practicas.model.entity.Vacante;
import co.edu.cue.practicas.model.enums.EstadoVacante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacanteRepository extends JpaRepository<Vacante, Long> {

    Page<Vacante> findByEstado(EstadoVacante estado, Pageable pageable);

    Page<Vacante> findByEmpresa_Id(Long empresaId, Pageable pageable);

    Page<Vacante> findByEmpresa_IdAndEstado(Long empresaId, EstadoVacante estado, Pageable pageable);

    Page<Vacante> findByPrograma_Id(Long programaId, Pageable pageable);

    Page<Vacante> findByPrograma_IdAndEstado(Long programaId, EstadoVacante estado, Pageable pageable);

    /** Vacantes que un tutor empresarial ve: las que están asignadas a él. */
    Page<Vacante> findByTutor_Id(Long tutorId, Pageable pageable);

    long countByEstado(EstadoVacante estado);

    long countByEmpresa_Id(Long empresaId);
}
