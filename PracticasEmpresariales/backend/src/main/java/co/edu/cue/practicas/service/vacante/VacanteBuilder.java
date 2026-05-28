package co.edu.cue.practicas.service.vacante;

import co.edu.cue.practicas.model.entity.Empresa;
import co.edu.cue.practicas.model.entity.Programa;
import co.edu.cue.practicas.model.entity.TutorEmpresarial;
import co.edu.cue.practicas.model.entity.Vacante;
import co.edu.cue.practicas.model.enums.EstadoVacante;
import co.edu.cue.practicas.model.enums.JornadaVacante;
import co.edu.cue.practicas.model.enums.ModalidadVacante;

import java.time.LocalDate;

/**
 * PATRON BUILDER — GPE-152
 *
 * Construye paso a paso la configuración de una vacante de práctica empresarial:
 * empresa → programa → tutor → modalidad → jornada → cupos → fechas → remuneración.
 *
 * Evita un constructor con 15+ parámetros y permite armar la vacante de forma
 * legible y fluida. Cada método retorna el propio Builder para encadenamiento.
 *
 * Toda vacante recién construida queda con estado PENDIENTE — solo el flujo de
 * aprobación (Coordinador de Prácticas) puede cambiarlo a DISPONIBLE o RECHAZADA.
 *
 * Ejemplo de uso desde VacanteService:
 *
 *   VacanteBuilder.nuevo()
 *       .conTitulo("Practicante Backend Java")
 *       .conDescripcion("Apoyo en desarrollo de microservicios")
 *       .paraEmpresa(empresa)
 *       .paraPrograma(programa)
 *       .conTutor(tutor)
 *       .conModalidad(ModalidadVacante.HIBRIDO)
 *       .conJornada(JornadaVacante.MEDIO_TIEMPO)
 *       .conCupos(2)
 *       .conDuracion(6)
 *       .conRemuneracion(true, 1300000.0)
 *       .construir();
 */
public class VacanteBuilder {

    private String titulo;
    private String descripcion;
    private Empresa empresa;
    private Programa programa;
    private TutorEmpresarial tutor;
    private ModalidadVacante modalidad = ModalidadVacante.PRESENCIAL;
    private JornadaVacante jornada = JornadaVacante.TIEMPO_COMPLETO;
    private String ciudad;
    private int cupos = 1;
    private int duracionMeses = 6;
    private boolean remunerada = false;
    private Double valorRemuneracion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String requisitos;
    private String responsabilidades;

    /** Punto de entrada estático — evita escribir new VacanteBuilder() en el llamador. */
    public static VacanteBuilder nuevo() {
        return new VacanteBuilder();
    }

    public VacanteBuilder conTitulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

    public VacanteBuilder conDescripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }

    public VacanteBuilder paraEmpresa(Empresa empresa) {
        this.empresa = empresa;
        return this;
    }

    public VacanteBuilder paraPrograma(Programa programa) {
        this.programa = programa;
        return this;
    }

    public VacanteBuilder conTutor(TutorEmpresarial tutor) {
        this.tutor = tutor;
        return this;
    }

    public VacanteBuilder conModalidad(ModalidadVacante modalidad) {
        if (modalidad != null) this.modalidad = modalidad;
        return this;
    }

    public VacanteBuilder conJornada(JornadaVacante jornada) {
        if (jornada != null) this.jornada = jornada;
        return this;
    }

    public VacanteBuilder enCiudad(String ciudad) {
        this.ciudad = ciudad;
        return this;
    }

    public VacanteBuilder conCupos(int cupos) {
        this.cupos = cupos;
        return this;
    }

    public VacanteBuilder conDuracion(int meses) {
        this.duracionMeses = meses;
        return this;
    }

    /** Configura la remuneración. Si remunerada=true y valor=null, se considera "a convenir". */
    public VacanteBuilder conRemuneracion(boolean remunerada, Double valor) {
        this.remunerada = remunerada;
        this.valorRemuneracion = remunerada ? valor : null;
        return this;
    }

    public VacanteBuilder entreFechas(LocalDate inicio, LocalDate fin) {
        this.fechaInicio = inicio;
        this.fechaFin = fin;
        return this;
    }

    public VacanteBuilder conRequisitos(String requisitos) {
        this.requisitos = requisitos;
        return this;
    }

    public VacanteBuilder conResponsabilidades(String responsabilidades) {
        this.responsabilidades = responsabilidades;
        return this;
    }

    /**
     * Valida los datos mínimos y construye la vacante en estado PENDIENTE.
     * Lanza IllegalStateException si falta algún campo obligatorio o si los datos
     * son inconsistentes (cupos < 1, fechas invertidas, etc.).
     */
    public Vacante construir() {

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalStateException("El título de la vacante es obligatorio.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalStateException("La descripción de la vacante es obligatoria.");
        }
        if (empresa == null) {
            throw new IllegalStateException("La empresa de la vacante es obligatoria.");
        }
        if (programa == null) {
            throw new IllegalStateException("El programa académico de la vacante es obligatorio.");
        }
        if (cupos < 1) {
            throw new IllegalStateException("La vacante debe ofrecer al menos 1 cupo.");
        }
        if (duracionMeses < 1) {
            throw new IllegalStateException("La duración debe ser al menos 1 mes.");
        }
        // Si la empresa proporcionó fechas, validamos coherencia: la fin no puede ser anterior a la inicio.
        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new IllegalStateException("La fecha de fin no puede ser anterior a la de inicio.");
        }

        return Vacante.builder()
                .titulo(titulo)
                .descripcion(descripcion)
                .empresa(empresa)
                .programa(programa)
                .tutor(tutor)
                .modalidad(modalidad)
                .jornada(jornada)
                .ciudad(ciudad)
                .cupos(cupos)
                .cuposOcupados(0)
                .duracionMeses(duracionMeses)
                .remunerada(remunerada)
                .valorRemuneracion(valorRemuneracion)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .requisitos(requisitos)
                .responsabilidades(responsabilidades)
                .estado(EstadoVacante.PENDIENTE)
                .build();
    }
}
