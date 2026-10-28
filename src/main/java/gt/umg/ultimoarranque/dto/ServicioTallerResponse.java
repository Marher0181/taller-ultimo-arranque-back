package gt.umg.ultimoarranque.dto;

import gt.umg.ultimoarranque.modelo.ServicioTaller;

import java.math.BigDecimal;

public record ServicioTallerResponse(Long id, String codigo, String descripcion, BigDecimal costoManoObra) {

    public static ServicioTallerResponse desde(ServicioTaller servicio) {
        return new ServicioTallerResponse(
                servicio.getId(),
                servicio.getCodigo(),
                servicio.getDescripcion(),
                servicio.getCostoManoObra());
    }
}
