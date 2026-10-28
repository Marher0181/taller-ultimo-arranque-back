package gt.umg.ultimoarranque.modelo.consulta;

import gt.umg.ultimoarranque.modelo.NivelInventario;
import gt.umg.ultimoarranque.modelo.Repuesto;

import java.math.BigDecimal;

public record RepuestoInventario(
        Long id,
        String codigo,
        String nombre,
        BigDecimal precio,
        int existencia,
        NivelInventario nivel) {

    public static RepuestoInventario de(Repuesto repuesto, int umbralBajo) {
        return new RepuestoInventario(repuesto.getId(), repuesto.getCodigo(), repuesto.getNombre(),
                repuesto.getPrecio(), repuesto.getExistencia(), repuesto.nivelInventario(umbralBajo));
    }
}
