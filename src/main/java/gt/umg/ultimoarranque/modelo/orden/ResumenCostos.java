package gt.umg.ultimoarranque.modelo.orden;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.stream.Stream;

public record ResumenCostos(
        BigDecimal manoObra,
        BigDecimal repuestos,
        BigDecimal subtotal,
        BigDecimal tasaIva,
        BigDecimal iva,
        BigDecimal total) {

    private static final int DECIMALES = 2;

    public static ResumenCostos calcular(Collection<DetalleServicio> servicios,
                                         Collection<DetalleRepuesto> repuestos,
                                         BigDecimal tasaIva) {
        BigDecimal manoObra = sumar(servicios.stream().map(DetalleServicio::getCostoManoObra));
        BigDecimal totalRepuestos = sumar(repuestos.stream().map(DetalleRepuesto::subtotal));
        BigDecimal subtotal = manoObra.add(totalRepuestos);
        BigDecimal iva = subtotal.multiply(tasaIva).setScale(DECIMALES, RoundingMode.HALF_UP);
        return new ResumenCostos(manoObra, totalRepuestos, subtotal, tasaIva, iva, subtotal.add(iva));
    }

    private static BigDecimal sumar(Stream<BigDecimal> montos) {
        return montos.reduce(BigDecimal.ZERO, BigDecimal::add).setScale(DECIMALES, RoundingMode.HALF_UP);
    }
}
