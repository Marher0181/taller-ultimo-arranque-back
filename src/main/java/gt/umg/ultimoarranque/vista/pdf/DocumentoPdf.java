package gt.umg.ultimoarranque.vista.pdf;

import gt.umg.ultimoarranque.excepcion.GeneracionPdfException;
import gt.umg.ultimoarranque.modelo.FechaHora;
import gt.umg.ultimoarranque.modelo.consulta.OrdenResumen;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.HeaderFooter;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public abstract class DocumentoPdf {

    protected static final Color COLOR_PRIMARIO = new Color(0x1F, 0x3A, 0x5F);
    protected static final Color COLOR_SECUNDARIO = new Color(0xE8, 0xED, 0xF3);
    protected static final Font FUENTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, Font.NORMAL, COLOR_PRIMARIO);
    protected static final Font FUENTE_SECCION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Font.NORMAL, COLOR_PRIMARIO);
    protected static final Font FUENTE_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    protected static final Font FUENTE_NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, Color.BLACK);
    private static final Font FUENTE_ENCABEZADO_TABLA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, Color.WHITE);
    private static final Font FUENTE_TALLER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL, Color.DARK_GRAY);
    private static final Font FUENTE_DETALLE = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, Color.GRAY);

    private static final String NOMBRE_TALLER = "Taller Mecánico \"El Último Arranque\"";
    private static final String SIN_VALOR = "—";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final float MARGEN = 36f;

    public final byte[] generar() {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(tamanioPagina(), MARGEN, MARGEN, MARGEN, MARGEN);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.setFooter(piePagina());
            documento.open();
            agregarEncabezado(documento);
            agregarContenido(documento);
            documento.close();
            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new GeneracionPdfException("No fue posible generar el documento '" + titulo() + "'", e);
        }
    }

    public abstract String nombreArchivo();

    protected abstract String titulo();

    protected abstract void agregarContenido(Document documento);

    protected String subtitulo() {
        return null;
    }

    protected Rectangle tamanioPagina() {
        return PageSize.LETTER;
    }

    private void agregarEncabezado(Document documento) {
        documento.add(new Paragraph(NOMBRE_TALLER, FUENTE_TALLER));
        Paragraph titulo = new Paragraph(titulo(), FUENTE_TITULO);
        titulo.setSpacingBefore(4f);
        documento.add(titulo);
        if (subtitulo() != null) {
            documento.add(new Paragraph(subtitulo(), FUENTE_NORMAL));
        }
        Paragraph generado = new Paragraph("Generado: " + fechaHora(FechaHora.ahora()), FUENTE_DETALLE);
        generado.setSpacingAfter(10f);
        documento.add(generado);
    }

    private HeaderFooter piePagina() {
        HeaderFooter pie = new HeaderFooter(new Phrase("Página ", FUENTE_DETALLE), true);
        pie.setAlignment(Element.ALIGN_CENTER);
        pie.setBorder(Rectangle.TOP);
        pie.setBorderColor(Color.LIGHT_GRAY);
        return pie;
    }

    protected Paragraph seccion(String texto) {
        Paragraph seccion = new Paragraph(texto, FUENTE_SECCION);
        seccion.setSpacingBefore(8f);
        seccion.setSpacingAfter(4f);
        return seccion;
    }

    protected Paragraph sinDatos(String mensaje) {
        Paragraph parrafo = new Paragraph(mensaje, FUENTE_DETALLE);
        parrafo.setSpacingAfter(8f);
        return parrafo;
    }

    protected PdfPTable tabla(float[] anchos, String... encabezados) {
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100f);
        tabla.setSpacingAfter(8f);
        tabla.setHeaderRows(1);
        for (String encabezado : encabezados) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado, FUENTE_ENCABEZADO_TABLA));
            celda.setBackgroundColor(COLOR_PRIMARIO);
            celda.setPadding(5f);
            celda.setBorderColor(Color.WHITE);
            tabla.addCell(celda);
        }
        return tabla;
    }

    protected void celda(PdfPTable tabla, String texto) {
        tabla.addCell(crearCelda(texto, FUENTE_NORMAL, Element.ALIGN_LEFT));
    }

    protected void celdaNumerica(PdfPTable tabla, String texto) {
        tabla.addCell(crearCelda(texto, FUENTE_NORMAL, Element.ALIGN_RIGHT));
    }

    protected void celdaTotal(PdfPTable tabla, String texto, int columnas, int alineacion) {
        PdfPCell celda = crearCelda(texto, FUENTE_NEGRITA, alineacion);
        celda.setColspan(columnas);
        celda.setBackgroundColor(COLOR_SECUNDARIO);
        tabla.addCell(celda);
    }

    protected PdfPTable tablaDatos(Map<String, String> datos) {
        PdfPTable tabla = new PdfPTable(new float[]{1.2f, 3.8f});
        tabla.setWidthPercentage(100f);
        tabla.setSpacingAfter(8f);
        datos.forEach((etiqueta, valor) -> {
            PdfPCell celdaEtiqueta = crearCelda(etiqueta, FUENTE_NEGRITA, Element.ALIGN_LEFT);
            celdaEtiqueta.setBackgroundColor(COLOR_SECUNDARIO);
            tabla.addCell(celdaEtiqueta);
            tabla.addCell(crearCelda(valor, FUENTE_NORMAL, Element.ALIGN_LEFT));
        });
        return tabla;
    }

    protected PdfPTable tablaConteo(Map<EstadoOrden, Long> conteo) {
        EstadoOrden[] estados = EstadoOrden.values();
        float[] anchos = new float[estados.length + 1];
        Arrays.fill(anchos, 1f);
        String[] encabezados = Stream.concat(Arrays.stream(estados).map(EstadoOrden::getEtiqueta), Stream.of("Total"))
                .toArray(String[]::new);
        PdfPTable tabla = tabla(anchos, encabezados);
        long total = 0;
        for (EstadoOrden estado : estados) {
            long cantidad = conteo.getOrDefault(estado, 0L);
            total += cantidad;
            celdaNumerica(tabla, String.valueOf(cantidad));
        }
        celdaNumerica(tabla, String.valueOf(total));
        return tabla;
    }

    protected PdfPTable tablaOrdenes(List<OrdenResumen> ordenes) {
        PdfPTable tabla = tabla(new float[]{0.6f, 1.1f, 1f, 2f, 1.8f, 1.8f, 1.1f, 1.1f},
                "No.", "Estado", "Placa", "Vehículo", "Cliente", "Mecánico", "Creación", "Total");
        for (OrdenResumen orden : ordenes) {
            celdaNumerica(tabla, String.valueOf(orden.id()));
            celda(tabla, orden.estado().getEtiqueta());
            celda(tabla, orden.placa());
            celda(tabla, orden.vehiculo());
            celda(tabla, orden.cliente());
            celda(tabla, orden.mecanico());
            celda(tabla, fecha(orden.fechaCreacion().toLocalDate()));
            celdaNumerica(tabla, moneda(orden.total()));
        }
        return tabla;
    }

    protected static String moneda(BigDecimal monto) {
        return monto == null ? SIN_VALOR : String.format(Locale.US, "Q %,.2f", monto);
    }

    protected static String fechaHora(LocalDateTime valor) {
        return valor == null ? SIN_VALOR : valor.format(FORMATO_FECHA_HORA);
    }

    protected static String fecha(LocalDate valor) {
        return valor == null ? SIN_VALOR : valor.format(FORMATO_FECHA);
    }

    protected static String texto(String valor) {
        return valor == null || valor.isBlank() ? SIN_VALOR : valor;
    }

    private PdfPCell crearCelda(String texto, Font fuente, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto(texto), fuente));
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(4f);
        celda.setBorderColor(Color.LIGHT_GRAY);
        return celda;
    }
}
