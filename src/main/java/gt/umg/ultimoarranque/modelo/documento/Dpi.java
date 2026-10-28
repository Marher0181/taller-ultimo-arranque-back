package gt.umg.ultimoarranque.modelo.documento;

import gt.umg.ultimoarranque.excepcion.DatoInvalidoException;

import java.util.regex.Pattern;

public final class Dpi extends DocumentoIdentidad {

    private static final Pattern FORMATO = Pattern.compile("\\d{13}");
    private static final int DEPARTAMENTOS = 22;
    private static final int MODULO = 11;

    public Dpi(String numero) {
        super(validar(normalizar(numero)));
    }

    private static String validar(String cui) {
        if (!FORMATO.matcher(cui).matches() || !ubicacionValida(cui) || !digitoVerificadorValido(cui)) {
            throw new DatoInvalidoException("El DPI '" + cui + "' no es válido");
        }
        return cui;
    }

    private static boolean ubicacionValida(String cui) {
        int departamento = Integer.parseInt(cui.substring(9, 11));
        int municipio = Integer.parseInt(cui.substring(11, 13));
        return departamento >= 1 && departamento <= DEPARTAMENTOS && municipio >= 1;
    }

    private static boolean digitoVerificadorValido(String cui) {
        int suma = 0;
        for (int i = 0; i < 8; i++) {
            suma += digito(cui, i) * (i + 2);
        }
        return suma % MODULO == digito(cui, 8);
    }

    @Override
    public TipoDocumento getTipo() {
        return TipoDocumento.DPI;
    }

    @Override
    public String formateado() {
        String cui = getNumero();
        return cui.substring(0, 4) + " " + cui.substring(4, 9) + " " + cui.substring(9);
    }
}
