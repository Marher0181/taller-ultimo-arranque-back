package gt.umg.ultimoarranque.modelo.orden;

final class EstadoCancelada extends EstadoOrdenTrabajo {

    @Override
    public EstadoOrden codigo() {
        return EstadoOrden.CANCELADA;
    }
}
