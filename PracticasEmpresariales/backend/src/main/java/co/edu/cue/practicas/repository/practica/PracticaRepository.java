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

    /**
     * Verifica si una práctica pertenece a un Docente Asesor concreto.
     * Usado en NotaDocenteService para validar propiedad antes de registrar nota.
     */
    boolean existsByIdAndDocenteAsesor_Id(Long practicaId, Long docenteId);

    /**
     * Lista las prácticas asignadas a un Docente Asesor filtradas por estado.
     * Usado en el dashboard "Mis Estudiantes" del docente.
     */
    List<Practica> findByDocenteAsesor_IdAndEstado(Long docenteId, EstadoPractica estado);

    // ====== Agregaciones para reportes RF-10-01 / RF-10-04 ======

    long countByEstado(EstadoPractica estado);

    long countByPrograma_IdAndEstado(Long programaId, EstadoPractica estado);

    long countByPrograma_Facultad_IdAndEstado(Long facultadId, EstadoPractica estado);

    long countByPrograma_IdAndNumeroPracticaAndEstado(
            Long programaId, int numeroPractica, EstadoPractica estado);

    long countByCreadoEnBetweenAndEstado(
            java.time.LocalDateTime desde,
            java.time.LocalDateTime hasta,
            EstadoPractica estado);
}
