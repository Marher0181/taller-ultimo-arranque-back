package gt.umg.ultimoarranque.modelo.consulta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public record ReporteIngresos(
        LocalDate desde,
        LocalDate hasta,
        int cantidadOrdenes,
        BigDecimal manoObra,
        BigDecimal repuestos,
        BigDecimal iva,
        BigDecimal total,
        List<IngresoOrden> detalle) {

    public static ReporteIngresos de(LocalDate desde, LocalDate hasta, List<IngresoOrden> detalle) {
        return new ReporteIngresos(desde, hasta, detalle.size(),
                sumar(detalle, IngresoOrden::manoObra),
                sumar(detalle, IngresoOrden::repuestos),
                sumar(detalle, IngresoOrden::iva),
                sumar(detalle, IngresoOrden::total),
                detalle);
    }

    private static BigDecimal sumar(List<IngresoOrden> detalle, Function<IngresoOrden, BigDecimal> campo) {
        return detalle.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }
}
