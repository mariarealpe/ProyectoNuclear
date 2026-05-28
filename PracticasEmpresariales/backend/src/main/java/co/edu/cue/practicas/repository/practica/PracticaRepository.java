package co.edu.cue.practicas.repository.practica;

import co.edu.cue.practicas.model.entity.Practica;
import co.edu.cue.practicas.model.enums.EstadoPractica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PracticaRepository extends JpaRepository<Practica, Long> {

    long countByEstudiante_IdAndEstado( Long estudianteId, EstadoPractica estado);

    boolean existsByEstudiante_IdAndEstado( Long estudianteId, EstadoPractica estado);

    @Query("SELECT DISTINCT p.estudiante.id FROM Practica p " +
            "WHERE p.estudiante.id IN :estudianteIds AND p.estado = :estado")
    List<Long> findEstudiantesConEstado(
            @Param("estudianteIds") List<Long> estudianteIds,
            @Param("estado") EstadoPractica estado);

    List<Practica> findByEstudiante_IdOrderByCreadoEnDesc(Long estudianteId);
}
