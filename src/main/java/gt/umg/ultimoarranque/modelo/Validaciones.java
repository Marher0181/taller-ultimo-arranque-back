package gt.umg.ultimoarranque.modelo;

import gt.umg.ultimoarranque.excepcion.DatoInvalidoException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Validaciones {

    private Validaciones() {
    }

    public static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DatoInvalidoException("El campo '" + campo + "' es obligatorio");
        }
        return valor.strip();
    }

    public static String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }

    public static <T> T obligatorio(T valor, String campo) {
        if (valor == null) {
            throw new DatoInvalidoException("El campo '" + campo + "' es obligatorio");
        }
        return valor;
    }

    public static BigDecimal monto(BigDecimal valor, String campo) {
        obligatorio(valor, campo);
        if (valor.signum() < 0) {
            throw new DatoInvalidoException("El campo '" + campo + "' no puede ser negativo");
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static int positivo(int valor, String campo) {
        if (valor <= 0) {
            throw new DatoInvalidoException("El campo '" + campo + "' debe ser mayor que cero");
        }
        return valor;
    }

    public static int noNegativo(int valor, String campo) {
        if (valor < 0) {
            throw new DatoInvalidoException("El campo '" + campo + "' no puede ser negativo");
        }
        return valor;
    }
}
