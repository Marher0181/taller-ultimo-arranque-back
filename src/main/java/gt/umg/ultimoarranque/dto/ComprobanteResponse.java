package gt.umg.ultimoarranque.dto;

import gt.umg.ultimoarranque.modelo.orden.Comprobante;
import gt.umg.ultimoarranque.modelo.orden.ResumenCostos;

import java.time.LocalDateTime;

public record ComprobanteResponse(String numero, LocalDateTime fechaEmision, ResumenCostos costos) {

    public static ComprobanteResponse desde(Comprobante comprobante) {
        return new ComprobanteResponse(comprobante.getNumero(), comprobante.getFechaEmision(), comprobante.getCostos());
    }
}
