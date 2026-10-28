package gt.umg.ultimoarranque.dto;

import gt.umg.ultimoarranque.modelo.orden.DetalleServicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DetalleServicioResponse(
        Long idServicio,
        String codigo,
        String descripcion,
        BigDecimal costoManoObra,
        LocalDateTime fechaRegistro) {

    public static DetalleServicioResponse desde(DetalleServicio detalle) {
        return new DetalleServicioResponse(
                detalle.getServicio().getId(),
                detalle.getServicio().getCodigo(),
                detalle.getServicio().getDescripcion(),
                detalle.getCostoManoObra(),
                detalle.getFechaRegistro());
    }
}
