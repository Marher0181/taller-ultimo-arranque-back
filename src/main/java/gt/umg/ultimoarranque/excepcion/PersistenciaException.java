package gt.umg.ultimoarranque.excepcion;

public class PersistenciaException extends TallerException {

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
