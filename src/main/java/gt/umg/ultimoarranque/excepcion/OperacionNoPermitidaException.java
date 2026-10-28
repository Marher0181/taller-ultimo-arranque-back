package gt.umg.ultimoarranque.excepcion;

public class OperacionNoPermitidaException extends ReglaNegocioException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
