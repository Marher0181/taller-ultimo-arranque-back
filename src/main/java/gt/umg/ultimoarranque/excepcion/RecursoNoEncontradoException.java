package gt.umg.ultimoarranque.excepcion;

public class RecursoNoEncontradoException extends TallerException {

    public RecursoNoEncontradoException(String recurso, Object identificador) {
        super(recurso + " con identificador '" + identificador + "' no existe");
    }
}
