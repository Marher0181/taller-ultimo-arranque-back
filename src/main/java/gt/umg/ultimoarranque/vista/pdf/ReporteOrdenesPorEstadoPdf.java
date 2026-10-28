package gt.umg.ultimoarranque.vista.pdf;

import gt.umg.ultimoarranque.modelo.consulta.ReporteOrdenesPorEstado;
import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.Rectangle;

public class ReporteOrdenesPorEstadoPdf extends DocumentoPdf {

    private final ReporteOrdenesPorEstado reporte;

    public ReporteOrdenesPorEstadoPdf(ReporteOrdenesPorEstado reporte) {
        this.reporte = reporte;
    }

    @Override
    public String nombreArchivo() {
        String sufijo = reporte.estadoFiltrado() == null ? "todas" : reporte.estadoFiltrado().name().toLowerCase();
        return "ordenes-por-estado-" + sufijo + ".pdf";
    }

    @Override
    protected String titulo() {
        return "Reporte de órdenes por estado";
    }

    @Override
    protected String subtitulo() {
        return reporte.estadoFiltrado() == null
                ? "Todos los estados"
                : "Estado: " + reporte.estadoFiltrado().getEtiqueta();
    }

    @Override
    protected Rectangle tamanioPagina() {
        return PageSize.LETTER.rotate();
    }

    @Override
    protected void agregarContenido(Document documento) {
        documento.add(seccion("Resumen general"));
        documento.add(tablaConteo(reporte.conteoPorEstado()));
        documento.add(seccion("Detalle de órdenes (" + reporte.ordenes().size() + ")"));
        if (reporte.ordenes().isEmpty()) {
            documento.add(sinDatos("No hay órdenes para el estado seleccionado."));
            return;
        }
        documento.add(tablaOrdenes(reporte.ordenes()));
    }
}
