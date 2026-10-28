package gt.umg.ultimoarranque.vista.pdf;

import gt.umg.ultimoarranque.modelo.NivelInventario;
import gt.umg.ultimoarranque.modelo.consulta.ReporteInventarioBajo;
import gt.umg.ultimoarranque.modelo.consulta.RepuestoInventario;
import org.openpdf.text.Document;
import org.openpdf.text.pdf.PdfPTable;

public class RepuestosBajoInventarioPdf extends DocumentoPdf {

    private final ReporteInventarioBajo reporte;

    public RepuestosBajoInventarioPdf(ReporteInventarioBajo reporte) {
        this.reporte = reporte;
    }

    @Override
    public String nombreArchivo() {
        return "repuestos-bajo-inventario.pdf";
    }

    @Override
    protected String titulo() {
        return "Repuestos con existencia menor o igual a " + reporte.umbral() + " unidades";
    }

    @Override
    protected String subtitulo() {
        return String.format("Repuestos en alerta: %d (agotados: %d)", reporte.repuestos().size(), reporte.agotados());
    }

    @Override
    protected void agregarContenido(Document documento) {
        if (reporte.repuestos().isEmpty()) {
            documento.add(sinDatos("No hay repuestos con bajo inventario."));
            return;
        }
        PdfPTable tabla = tabla(new float[]{1.2f, 3f, 1.2f, 1f, 1.2f},
                "Código", "Repuesto", "Precio", "Existencia", "Nivel");
        for (RepuestoInventario repuesto : reporte.repuestos()) {
            celda(tabla, repuesto.codigo());
            celda(tabla, repuesto.nombre());
            celdaNumerica(tabla, moneda(repuesto.precio()));
            celdaNumerica(tabla, String.valueOf(repuesto.existencia()));
            celda(tabla, repuesto.nivel() == NivelInventario.AGOTADO ? "AGOTADO" : "BAJO");
        }
        documento.add(tabla);
    }
}
