package gt.umg.ultimoarranque.modelo.consulta;

import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;

import java.util.List;
import java.util.Map;

public record OrdenesPorMecanico(
        Long idMecanico,
        String codigo,
        String nombre,
        String especialidad,
        Map<EstadoOrden, Long> conteoPorEstado,
        List<OrdenResumen> ordenes) {
}
