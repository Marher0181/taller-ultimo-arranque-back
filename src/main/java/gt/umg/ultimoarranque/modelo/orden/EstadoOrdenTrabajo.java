package gt.umg.ultimoarranque.modelo.orden;

import gt.umg.ultimoarranque.excepcion.OperacionNoPermitidaException;
import gt.umg.ultimoarranque.modelo.Mecanico;
import gt.umg.ultimoarranque.modelo.Repuesto;
import gt.umg.ultimoarranque.modelo.Responsable;
import gt.umg.ultimoarranque.modelo.ServicioTaller;

import java.math.BigDecimal;
import java.util.Map;

public abstract class EstadoOrdenTrabajo {

    static EstadoOrdenTrabajo desde(EstadoOrden codigo) {
        return switch (codigo) {
            case PENDIENTE -> new EstadoPendiente();
            case EN_PROCESO -> new EstadoEnProceso();
            case FINALIZADA -> new EstadoFinalizada();
            case CANCELADA -> new EstadoCancelada();
        };
    }

    public abstract EstadoOrden codigo();

    public boolean admiteModificaciones() {
        return false;
    }

    void actualizarDatos(OrdenTrabajo orden, Mecanico mecanico, String descripcionProblema) {
        throw rechazar("modificar");
    }

    DetalleServicio agregarServicio(OrdenTrabajo orden, ServicioTaller servicio) {
        throw rechazar("agregar servicios a");
    }

    DetalleServicio quitarServicio(OrdenTrabajo orden, long idServicio) {
        throw rechazar("quitar servicios de");
    }

    void iniciar(OrdenTrabajo orden, Responsable responsable) {
        throw rechazar("iniciar");
    }

    DetalleRepuesto asignarRepuesto(OrdenTrabajo orden, Repuesto repuesto, int cantidad) {
        throw rechazar("asignar repuestos a");
    }

    DetalleRepuesto retirarRepuesto(OrdenTrabajo orden, long idDetalle, Repuesto repuesto) {
        throw rechazar("retirar repuestos de");
    }

    Comprobante finalizar(OrdenTrabajo orden, BigDecimal tasaIva, Responsable responsable) {
        throw rechazar("finalizar");
    }

    void cancelar(OrdenTrabajo orden, String motivo, Map<Long, Repuesto> inventario, Responsable responsable) {
        throw rechazar("cancelar");
    }

    protected OperacionNoPermitidaException rechazar(String accion) {
        return new OperacionNoPermitidaException(
                "No se puede " + accion + " una orden en estado " + codigo().getEtiqueta().toUpperCase());
    }
}
