package co.edu.cue.practicas.service.programa;

import co.edu.cue.practicas.DatosDePrueba;
import co.edu.cue.practicas.model.entity.Facultad;
import co.edu.cue.practicas.model.entity.Programa;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProgramaBuilderTest {

    @Test
    void deberiaConstruirProgramaConValoresYRequisitosAsociados() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");

        Programa programa = ProgramaBuilder.nuevo()
                .conNombre("Ingenieria de Sistemas")
                .conDescripcion("Programa academico")
                .enFacultad(facultad)
                .conNumeroDePracticas(2)
                .conPromedioMinimoGeneral(3.4)
                .agregarRequisitoPractica(1, 80, 3.2, false, "Hoja de vida")
                .construir();

        assertThat(programa.getNombre()).isEqualTo("Ingenieria de Sistemas");
        assertThat(programa.getFacultad()).isSameAs(facultad);
        assertThat(programa.getNumeroTotalPracticas()).isEqualTo(2);
        assertThat(programa.getPromedioMinimoGeneral()).isEqualTo(3.4);
        assertThat(programa.isActivo()).isTrue();
        assertThat(programa.getRequisitos()).hasSize(1);
        assertThat(programa.getRequisitos().getFirst().getPrograma()).isSameAs(programa);
    }

    @Test
    void deberiaLanzarExcepcionCuandoFaltaNombre() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");

        assertThatThrownBy(() -> ProgramaBuilder.nuevo()
                .enFacultad(facultad)
                .construir())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    void deberiaLanzarExcepcionCuandoFaltaFacultad() {
        assertThatThrownBy(() -> ProgramaBuilder.nuevo()
                .conNombre("Ingenieria de Sistemas")
                .construir())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("facultad");
    }

    @Test
    void deberiaLanzarExcepcionCuandoNumeroDePracticasEsMenorAUno() {
        Facultad facultad = DatosDePrueba.facultad(1L, "Ingenieria");

        assertThatThrownBy(() -> ProgramaBuilder.nuevo()
                .conNombre("Ingenieria de Sistemas")
                .enFacultad(facultad)
                .conNumeroDePracticas(0)
                .construir())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("al menos 1");
    }
}
