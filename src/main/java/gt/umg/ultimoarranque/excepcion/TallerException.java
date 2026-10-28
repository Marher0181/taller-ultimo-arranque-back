package gt.umg.ultimoarranque.excepcion;

public abstract class TallerException extends RuntimeException {

    protected TallerException(String mensaje) {
        super(mensaje);
    }

    protected TallerException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
