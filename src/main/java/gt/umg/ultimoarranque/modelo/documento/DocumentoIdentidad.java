package gt.umg.ultimoarranque.modelo.documento;

import gt.umg.ultimoarranque.excepcion.DatoInvalidoException;

import java.util.Objects;

public abstract sealed class DocumentoIdentidad permits Nit, Dpi {

    private final String numero;

    protected DocumentoIdentidad(String numero) {
        this.numero = numero;
    }

    public static DocumentoIdentidad crear(TipoDocumento tipo, String numero) {
        if (tipo == null) {
            throw new DatoInvalidoException("El tipo de documento es obligatorio");
        }
        return switch (tipo) {
            case NIT -> new Nit(numero);
            case DPI -> new Dpi(numero);
        };
    }

    protected static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new DatoInvalidoException("El número de documento es obligatorio");
        }
        return valor.replaceAll("[\\s-]", "").toUpperCase();
    }

    protected static int digito(String texto, int posicion) {
        return Character.getNumericValue(texto.charAt(posicion));
    }

    public abstract TipoDocumento getTipo();

    public abstract String formateado();

    public String getNumero() {
        return numero;
    }

    @Override
    public String toString() {
        return getTipo() + " " + formateado();
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof DocumentoIdentidad documento)) {
            return false;
        }
        return getTipo() == documento.getTipo() && numero.equals(documento.numero);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTipo(), numero);
    }
}
