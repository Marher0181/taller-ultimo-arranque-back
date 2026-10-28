package gt.umg.ultimoarranque.modelo.documento;

import gt.umg.ultimoarranque.excepcion.DatoInvalidoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentoIdentidadTest {

    @Test
    void creaNitValidoYLoNormaliza() {
        DocumentoIdentidad nit = DocumentoIdentidad.crear(TipoDocumento.NIT, " 1234567-9 ");
        assertInstanceOf(Nit.class, nit);
        assertEquals("12345679", nit.getNumero());
        assertEquals("NIT 1234567-9", nit.toString());
    }

    @Test
    void aceptaNitConDigitoVerificadorK() {
        assertEquals("7781245K", DocumentoIdentidad.crear(TipoDocumento.NIT, "7781245-k").getNumero());
    }

    @Test
    void rechazaNitConDigitoVerificadorIncorrecto() {
        assertThrows(DatoInvalidoException.class, () -> DocumentoIdentidad.crear(TipoDocumento.NIT, "1234567-1"));
    }

    @Test
    void creaDpiValidoConFormato() {
        DocumentoIdentidad dpi = DocumentoIdentidad.crear(TipoDocumento.DPI, "2543198740101");
        assertInstanceOf(Dpi.class, dpi);
        assertEquals("2543 19874 0101", dpi.formateado());
    }

    @Test
    void rechazaDpiConDepartamentoInexistente() {
        assertThrows(DatoInvalidoException.class, () -> DocumentoIdentidad.crear(TipoDocumento.DPI, "2543198742301"));
    }

    @Test
    void rechazaDpiConLongitudIncorrecta() {
        assertThrows(DatoInvalidoException.class, () -> DocumentoIdentidad.crear(TipoDocumento.DPI, "123456"));
    }
}
