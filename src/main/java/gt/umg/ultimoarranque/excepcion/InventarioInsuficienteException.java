package gt.umg.ultimoarranque.excepcion;

public class InventarioInsuficienteException extends ReglaNegocioException {

    private final int disponible;
    private final int solicitado;

    public InventarioInsuficienteException(String repuesto, int disponible, int solicitado) {
        super(String.format("Inventario insuficiente para '%s': disponible %d, solicitado %d", repuesto, disponible, solicitado));
        this.disponible = disponible;
        this.solicitado = solicitado;
    }

    public int getDisponible() {
        return disponible;
    }

    public int getSolicitado() {
        return solicitado;
    }
}
