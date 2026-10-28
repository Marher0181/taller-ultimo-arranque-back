package gt.umg.ultimoarranque.vista.pdf;

import gt.umg.ultimoarranque.modelo.consulta.HistorialVehiculo;
import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.Rectangle;

import java.util.LinkedHashMap;
import java.util.Map;

public class HistorialVehiculoPdf extends DocumentoPdf {

    private final HistorialVehiculo historial;

    public HistorialVehiculoPdf(HistorialVehiculo historial) {
        this.historial = historial;
    }

    @Override
    public String nombreArchivo() {
        return "historial-" + historial.placa() + ".pdf";
    }

    @Override
    protected String titulo() {
        return "Historial de reparaciones del vehículo " + historial.placa();
    }

    @Override
    protected Rectangle tamanioPagina() {
        return PageSize.LETTER.rotate();
    }

    @Override
    protected void agregarContenido(Document documento) {
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("Placa", historial.placa());
        datos.put("Vehículo", historial.vehiculo());
        datos.put("Color", texto(historial.color()));
        datos.put("Propietario", historial.cliente() + " (" + historial.documentoCliente() + ")");
        datos.put("Órdenes registradas", String.valueOf(historial.totalOrdenes()));
        datos.put("Total facturado", moneda(historial.totalFacturado()));
        documento.add(seccion("Datos del vehículo"));
        documento.add(tablaDatos(datos));
        documento.add(seccion("Órdenes de trabajo"));
        if (historial.ordenes().isEmpty()) {
            documento.add(sinDatos("El vehículo no tiene órdenes registradas."));
            return;
        }
        documento.add(tablaOrdenes(historial.ordenes()));
    }
}
