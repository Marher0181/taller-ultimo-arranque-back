package gt.umg.ultimoarranque.vista.pdf;

import gt.umg.ultimoarranque.modelo.orden.Comprobante;
import gt.umg.ultimoarranque.modelo.orden.DetalleRepuesto;
import gt.umg.ultimoarranque.modelo.orden.DetalleServicio;
import gt.umg.ultimoarranque.modelo.orden.OrdenTrabajo;
import gt.umg.ultimoarranque.modelo.orden.ResumenCostos;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.pdf.PdfPTable;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class ComprobantePdf extends DocumentoPdf {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final OrdenTrabajo orden;
    private final Comprobante comprobante;

    public ComprobantePdf(OrdenTrabajo orden, Comprobante comprobante) {
        this.orden = orden;
        this.comprobante = comprobante;
    }

    @Override
    public String nombreArchivo() {
        return "comprobante-" + comprobante.getNumero() + ".pdf";
    }

    @Override
    protected String titulo() {
        return "Comprobante de servicio No. " + comprobante.getNumero();
    }

    @Override
    protected String subtitulo() {
        return "Fecha de emisión: " + fechaHora(comprobante.getFechaEmision());
    }

    @Override
    protected void agregarContenido(Document documento) {
        documento.add(seccion("Datos de la orden"));
        documento.add(tablaDatos(datosOrden()));
        documento.add(seccion("Servicios realizados"));
        documento.add(tablaServicios());
        documento.add(seccion("Repuestos utilizados"));
        if (orden.getRepuestos().isEmpty()) {
            documento.add(sinDatos("No se utilizaron repuestos."));
        } else {
            documento.add(tablaRepuestos());
        }
        documento.add(seccion("Resumen de costos"));
        documento.add(tablaTotales(comprobante.getCostos()));
    }

    private Map<String, String> datosOrden() {
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("Orden No.", String.valueOf(orden.getId()));
        datos.put("Cliente", orden.getVehiculo().getPropietario().presentacion());
        datos.put("Vehículo", orden.getVehiculo().getPlaca() + " · " + orden.getVehiculo().descripcion());
        datos.put("Atendido por", orden.getMecanico().presentacion());
        datos.put("Registrada por", orden.getRegistradaPor().descripcion());
        datos.put("Finalizada por", orden.getCerradaPor() == null ? texto(null) : orden.getCerradaPor().descripcion());
        datos.put("Problema reportado", orden.getDescripcionProblema());
        datos.put("Recepción", fechaHora(orden.getFechaCreacion()));
        datos.put("Inicio de reparación", fechaHora(orden.getFechaInicio()));
        datos.put("Finalización", fechaHora(orden.getFechaFinalizacion()));
        return datos;
    }

    private PdfPTable tablaServicios() {
        PdfPTable tabla = tabla(new float[]{1.2f, 4f, 1.5f}, "Código", "Servicio", "Mano de obra");
        for (DetalleServicio detalle : orden.getServicios()) {
            celda(tabla, detalle.getServicio().getCodigo());
            celda(tabla, detalle.getServicio().getDescripcion());
            celdaNumerica(tabla, moneda(detalle.getCostoManoObra()));
        }
        return tabla;
    }

    private PdfPTable tablaRepuestos() {
        PdfPTable tabla = tabla(new float[]{1.2f, 3f, 0.9f, 1.3f, 1.3f},
                "Código", "Repuesto", "Cantidad", "Precio unitario", "Subtotal");
        for (DetalleRepuesto detalle : orden.getRepuestos()) {
            celda(tabla, detalle.getRepuesto().getCodigo());
            celda(tabla, detalle.getRepuesto().getNombre());
            celdaNumerica(tabla, String.valueOf(detalle.getCantidad()));
            celdaNumerica(tabla, moneda(detalle.getPrecioUnitario()));
            celdaNumerica(tabla, moneda(detalle.subtotal()));
        }
        return tabla;
    }

    private PdfPTable tablaTotales(ResumenCostos costos) {
        PdfPTable tabla = new PdfPTable(new float[]{3f, 1.5f});
        tabla.setWidthPercentage(50f);
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        filaTotal(tabla, "Mano de obra", costos.manoObra());
        filaTotal(tabla, "Repuestos", costos.repuestos());
        filaTotal(tabla, "Subtotal", costos.subtotal());
        filaTotal(tabla, "IVA (" + porcentaje(costos.tasaIva()) + ")", costos.iva());
        celdaTotal(tabla, "TOTAL", 1, Element.ALIGN_LEFT);
        celdaTotal(tabla, moneda(costos.total()), 1, Element.ALIGN_RIGHT);
        return tabla;
    }

    private void filaTotal(PdfPTable tabla, String concepto, BigDecimal monto) {
        celda(tabla, concepto);
        celdaNumerica(tabla, moneda(monto));
    }

    private static String porcentaje(BigDecimal tasa) {
        return tasa.multiply(CIEN).stripTrailingZeros().toPlainString() + "%";
    }
}
