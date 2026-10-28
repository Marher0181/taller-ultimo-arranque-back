package gt.umg.ultimoarranque.modelo;

import gt.umg.ultimoarranque.excepcion.DatoInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepuestoTest {

    private static final int UMBRAL = 5;

    private Repuesto repuesto(int existencia) {
        return new Repuesto(1L, "rep-001", "Filtro", null, new BigDecimal("65"), existencia);
    }

    @Test
    void normalizaCodigoYPrecio() {
        Repuesto filtro = repuesto(1);
        assertEquals("REP-001", filtro.getCodigo());
        assertEquals(new BigDecimal("65.00"), filtro.getPrecio());
    }

    @Test
    void clasificaNivelDeInventario() {
        assertEquals(NivelInventario.AGOTADO, repuesto(0).nivelInventario(UMBRAL));
        assertEquals(NivelInventario.BAJO, repuesto(5).nivelInventario(UMBRAL));
        assertEquals(NivelInventario.DISPONIBLE, repuesto(6).nivelInventario(UMBRAL));
    }

    @Test
    void abastecerSumaALaExistencia() {
        Repuesto filtro = repuesto(2);
        filtro.abastecer(10);
        assertEquals(12, filtro.getExistencia());
    }

    @Test
    void rechazaValoresNegativos() {
        assertThrows(DatoInvalidoException.class, () -> repuesto(-1));
        assertThrows(DatoInvalidoException.class, () -> repuesto(1).abastecer(-3));
        assertThrows(DatoInvalidoException.class,
                () -> new Repuesto(1L, "R", "Filtro", null, new BigDecimal("-1"), 0));
    }
}
