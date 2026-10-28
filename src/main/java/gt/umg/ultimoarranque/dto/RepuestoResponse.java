package gt.umg.ultimoarranque.dto;

import gt.umg.ultimoarranque.modelo.NivelInventario;
import gt.umg.ultimoarranque.modelo.Repuesto;

import java.math.BigDecimal;

public record RepuestoResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        BigDecimal precio,
        int existencia,
        NivelInventario nivelInventario) {

    public static RepuestoResponse desde(Repuesto repuesto, int umbralBajo) {
        return new RepuestoResponse(
                repuesto.getId(),
                repuesto.getCodigo(),
                repuesto.getNombre(),
                repuesto.getDescripcion(),
                repuesto.getPrecio(),
                repuesto.getExistencia(),
                repuesto.nivelInventario(umbralBajo));
    }
}
