package gt.umg.ultimoarranque.modelo.orden;

import gt.umg.ultimoarranque.modelo.Entidad;
import gt.umg.ultimoarranque.modelo.Repuesto;
import gt.umg.ultimoarranque.modelo.Validaciones;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DetalleRepuesto extends Entidad {

    private final Repuesto repuesto;
    private int cantidad;
    private final BigDecimal precioUnitario;
    private final LocalDateTime fechaAsignacion;

    public DetalleRepuesto(Long id, Repuesto repuesto, int cantidad, BigDecimal precioUnitario, LocalDateTime fechaAsignacion) {
        super(id);
        this.repuesto = Validaciones.obligatorio(repuesto, "repuesto");
        this.cantidad = Validaciones.positivo(cantidad, "cantidad");
        this.precioUnitario = Validaciones.monto(precioUnitario, "precio unitario");
        this.fechaAsignacion = Validaciones.obligatorio(fechaAsignacion, "fecha de asignación");
    }

    boolean admiteAcumular(Repuesto otro) {
        return repuesto.equals(otro) && precioUnitario.compareTo(otro.getPrecio()) == 0;
    }

    void aumentar(int unidades) {
        cantidad = Math.addExact(cantidad, Validaciones.positivo(unidades, "cantidad"));
    }

    public BigDecimal subtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public Repuesto getRepuesto() {
        return repuesto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }
}
