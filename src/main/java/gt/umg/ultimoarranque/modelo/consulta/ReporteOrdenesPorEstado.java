package gt.umg.ultimoarranque.modelo.consulta;

import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;

import java.util.List;
import java.util.Map;

public record ReporteOrdenesPorEstado(
        EstadoOrden estadoFiltrado,
        Map<EstadoOrden, Long> conteoPorEstado,
        List<OrdenResumen> ordenes) {
}
