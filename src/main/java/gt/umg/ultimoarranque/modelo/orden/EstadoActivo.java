package gt.umg.ultimoarranque.modelo.orden;

import gt.umg.ultimoarranque.modelo.Mecanico;
import gt.umg.ultimoarranque.modelo.Repuesto;
import gt.umg.ultimoarranque.modelo.Responsable;
import gt.umg.ultimoarranque.modelo.ServicioTaller;

import java.util.Map;

abstract class EstadoActivo extends EstadoOrdenTrabajo {

    @Override
    public boolean admiteModificaciones() {
        return true;
    }

    @Override
    void actualizarDatos(OrdenTrabajo orden, Mecanico mecanico, String descripcionProblema) {
        orden.modificarDatos(mecanico, descripcionProblema);
    }

    @Override
    DetalleServicio agregarServicio(OrdenTrabajo orden, ServicioTaller servicio) {
        return orden.incorporarServicio(servicio);
    }

    @Override
    DetalleServicio quitarServicio(OrdenTrabajo orden, long idServicio) {
        return orden.removerServicio(idServicio);
    }

    @Override
    void cancelar(OrdenTrabajo orden, String motivo, Map<Long, Repuesto> inventario, Responsable responsable) {
        for (DetalleRepuesto detalle : orden.getRepuestos()) {
            Repuesto repuesto = inventario.get(detalle.getRepuesto().getId());
            if (repuesto == null) {
                throw new IllegalStateException("No se cargó el repuesto " + detalle.getRepuesto().getId() + " para reintegrarlo");
            }
            repuesto.reintegrar(detalle.getCantidad());
        }
        orden.registrarCancelacion(motivo, responsable);
    }
}
