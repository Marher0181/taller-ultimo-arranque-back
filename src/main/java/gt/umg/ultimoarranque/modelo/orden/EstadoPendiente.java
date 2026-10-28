package gt.umg.ultimoarranque.modelo.orden;

import gt.umg.ultimoarranque.modelo.Responsable;

final class EstadoPendiente extends EstadoActivo {

    @Override
    public EstadoOrden codigo() {
        return EstadoOrden.PENDIENTE;
    }

    @Override
    void iniciar(OrdenTrabajo orden, Responsable responsable) {
        orden.registrarInicio(responsable);
    }
}
