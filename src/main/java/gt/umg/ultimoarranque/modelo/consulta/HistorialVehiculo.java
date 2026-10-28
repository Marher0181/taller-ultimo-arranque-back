package gt.umg.ultimoarranque.modelo.consulta;

import java.math.BigDecimal;
import java.util.List;

public record HistorialVehiculo(
        String placa,
        String vehiculo,
        String color,
        String cliente,
        String documentoCliente,
        int totalOrdenes,
        BigDecimal totalFacturado,
        List<OrdenResumen> ordenes) {
}
