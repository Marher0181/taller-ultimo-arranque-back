package gt.umg.ultimoarranque.vista.pdf;

import gt.umg.ultimoarranque.modelo.consulta.IngresoOrden;
import gt.umg.ultimoarranque.modelo.consulta.ReporteIngresos;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.PageSize;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPTable;

public class IngresosPdf extends DocumentoPdf {

    private final ReporteIngresos reporte;

    public IngresosPdf(ReporteIngresos reporte) {
        this.reporte = reporte;
    }

    @Override
    public String nombreArchivo() {
        return "ingresos-" + reporte.desde() + "-a-" + reporte.hasta() + ".pdf";
    }

    @Override
    protected String titulo() {
        return "Ingresos por reparaciones";
    }

    @Override
    protected String subtitulo() {
        return "Del " + fecha(reporte.desde()) + " al " + fecha(reporte.hasta())
                + " · Órdenes facturadas: " + reporte.cantidadOrdenes();
    }

    @Override
    protected Rectangle tamanioPagina() {
        return PageSize.LETTER.rotate();
    }

    @Override
    protected void agregarContenido(Document documento) {
        if (reporte.detalle().isEmpty()) {
            documento.add(sinDatos("No hay órdenes finalizadas en el rango indicado."));
            return;
        }
        PdfPTable tabla = tabla(new float[]{1.2f, 1.3f, 0.6f, 0.9f, 1.8f, 1.8f, 1.1f, 1.1f, 1f, 1.2f},
                "Comprobante", "Fecha", "Orden", "Placa", "Cliente", "Mecánico", "Mano de obra", "Repuestos", "IVA", "Total");
        for (IngresoOrden ingreso : reporte.detalle()) {
            celda(tabla, ingreso.numeroComprobante());
            celda(tabla, fechaHora(ingreso.fechaEmision()));
            celdaNumerica(tabla, String.valueOf(ingreso.idOrden()));
            celda(tabla, ingreso.placa());
            celda(tabla, ingreso.cliente());
            celda(tabla, ingreso.mecanico());
            celdaNumerica(tabla, moneda(ingreso.manoObra()));
            celdaNumerica(tabla, moneda(ingreso.repuestos()));
            celdaNumerica(tabla, moneda(ingreso.iva()));
            celdaNumerica(tabla, moneda(ingreso.total()));
        }
        celdaTotal(tabla, "Totales", 6, Element.ALIGN_RIGHT);
        celdaTotal(tabla, moneda(reporte.manoObra()), 1, Element.ALIGN_RIGHT);
        celdaTotal(tabla, moneda(reporte.repuestos()), 1, Element.ALIGN_RIGHT);
        celdaTotal(tabla, moneda(reporte.iva()), 1, Element.ALIGN_RIGHT);
        celdaTotal(tabla, moneda(reporte.total()), 1, Element.ALIGN_RIGHT);
        documento.add(tabla);
    }
}
