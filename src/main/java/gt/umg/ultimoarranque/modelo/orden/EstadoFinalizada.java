package gt.umg.ultimoarranque.modelo.orden;

final class EstadoFinalizada extends EstadoOrdenTrabajo {

    @Override
    public EstadoOrden codigo() {
        return EstadoOrden.FINALIZADA;
    }
}
