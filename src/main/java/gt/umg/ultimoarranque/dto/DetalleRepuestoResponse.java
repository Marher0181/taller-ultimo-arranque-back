package gt.umg.ultimoarranque.dto;

import gt.umg.ultimoarranque.modelo.orden.DetalleRepuesto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DetalleRepuestoResponse(
        Long idDetalle,
        Long idRepuesto,
        String codigo,
        String nombre,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        LocalDateTime fechaAsignacion) {

    public static DetalleRepuestoResponse desde(DetalleRepuesto detalle) {
        return new DetalleRepuestoResponse(
                detalle.getId(),
                detalle.getRepuesto().getId(),
                detalle.getRepuesto().getCodigo(),
                detalle.getRepuesto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.subtotal(),
                detalle.getFechaAsignacion());
    }
}
