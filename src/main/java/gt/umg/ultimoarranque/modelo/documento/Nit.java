package gt.umg.ultimoarranque.modelo.documento;

import gt.umg.ultimoarranque.excepcion.DatoInvalidoException;

import java.util.regex.Pattern;

public final class Nit extends DocumentoIdentidad {

    private static final Pattern FORMATO = Pattern.compile("\\d{1,12}[0-9K]");
    private static final int MODULO = 11;

    public Nit(String numero) {
        super(validar(normalizar(numero)));
    }

    private static String validar(String nit) {
        if (!FORMATO.matcher(nit).matches() || !digitoVerificadorValido(nit)) {
            throw new DatoInvalidoException("El NIT '" + nit + "' no es válido");
        }
        return nit;
    }

    private static boolean digitoVerificadorValido(String nit) {
        int longitudCuerpo = nit.length() - 1;
        int suma = 0;
        for (int i = 0; i < longitudCuerpo; i++) {
            suma += digito(nit, i) * (longitudCuerpo + 1 - i);
        }
        int residuo = (MODULO - suma % MODULO) % MODULO;
        char esperado = residuo == 10 ? 'K' : Character.forDigit(residuo, 10);
        return nit.charAt(longitudCuerpo) == esperado;
    }

    @Override
    public TipoDocumento getTipo() {
        return TipoDocumento.NIT;
    }

    @Override
    public String formateado() {
        String numero = getNumero();
        int ultimo = numero.length() - 1;
        return numero.substring(0, ultimo) + "-" + numero.charAt(ultimo);
    }
}
