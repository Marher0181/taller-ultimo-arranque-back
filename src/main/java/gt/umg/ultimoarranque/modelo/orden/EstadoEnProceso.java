package gt.umg.ultimoarranque.modelo.orden;

import gt.umg.ultimoarranque.excepcion.DatoInvalidoException;
import gt.umg.ultimoarranque.excepcion.ReglaNegocioException;
import gt.umg.ultimoarranque.modelo.Repuesto;
import gt.umg.ultimoarranque.modelo.Responsable;

import java.math.BigDecimal;

final class EstadoEnProceso extends EstadoActivo {

    @Override
    public EstadoOrden codigo() {
        return EstadoOrden.EN_PROCESO;
    }

    @Override
    DetalleRepuesto asignarRepuesto(OrdenTrabajo orden, Repuesto repuesto, int cantidad) {
        repuesto.descontar(cantidad);
        return orden.incorporarRepuesto(repuesto, cantidad);
    }

    @Override
    DetalleRepuesto retirarRepuesto(OrdenTrabajo orden, long idDetalle, Repuesto repuesto) {
        DetalleRepuesto detalle = orden.obtenerDetalleRepuesto(idDetalle);
        if (!detalle.getRepuesto().equals(repuesto)) {
            throw new DatoInvalidoException("El repuesto indicado no corresponde al detalle " + idDetalle);
        }
        orden.removerRepuesto(detalle);
        repuesto.reintegrar(detalle.getCantidad());
        return detalle;
    }

    @Override
    Comprobante finalizar(OrdenTrabajo orden, BigDecimal tasaIva, Responsable responsable) {
        if (orden.getServicios().isEmpty()) {
            throw new ReglaNegocioException("No se puede finalizar una orden sin servicios registrados");
        }
        return orden.registrarFinalizacion(tasaIva, responsable);
    }
}
