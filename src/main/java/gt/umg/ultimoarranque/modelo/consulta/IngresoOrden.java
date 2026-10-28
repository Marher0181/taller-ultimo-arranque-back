package gt.umg.ultimoarranque.modelo.consulta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IngresoOrden(
        String numeroComprobante,
        LocalDateTime fechaEmision,
        Long idOrden,
        String placa,
        String cliente,
        String mecanico,
        BigDecimal manoObra,
        BigDecimal repuestos,
        BigDecimal iva,
        BigDecimal total) {
}
