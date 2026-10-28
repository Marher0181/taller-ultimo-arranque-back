package gt.umg.ultimoarranque.modelo.consulta;

import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;

public record FiltroOrdenes(EstadoOrden estado, Long idVehiculo, Long idMecanico) {

    public static FiltroOrdenes porEstado(EstadoOrden estado) {
        return new FiltroOrdenes(estado, null, null);
    }

    public static FiltroOrdenes porVehiculo(long idVehiculo) {
        return new FiltroOrdenes(null, idVehiculo, null);
    }

    public static FiltroOrdenes porMecanico(Long idMecanico, EstadoOrden estado) {
        return new FiltroOrdenes(estado, null, idMecanico);
    }
}
