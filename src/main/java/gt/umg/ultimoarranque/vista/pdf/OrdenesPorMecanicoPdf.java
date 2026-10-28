package gt.umg.ultimoarranque.vista.pdf;

import gt.umg.ultimoarranque.modelo.consulta.OrdenesPorMecanico;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.Rectangle;

import java.util.List;

public class OrdenesPorMecanicoPdf extends DocumentoPdf {

    private final List<OrdenesPorMecanico> mecanicos;
    private final EstadoOrden estado;

    public OrdenesPorMecanicoPdf(List<OrdenesPorMecanico> mecanicos, EstadoOrden estado) {
        this.mecanicos = mecanicos;
        this.estado = estado;
    }

    @Override
    public String nombreArchivo() {
        return "ordenes-por-mecanico.pdf";
    }

    @Override
    protected String titulo() {
        return "Órdenes por mecánico y estado";
    }

    @Override
    protected String subtitulo() {
        return estado == null ? "Todos los estados" : "Estado: " + estado.getEtiqueta();
    }

    @Override
    protected Rectangle tamanioPagina() {
        return PageSize.LETTER.rotate();
    }

    @Override
    protected void agregarContenido(Document documento) {
        if (mecanicos.isEmpty()) {
            documento.add(sinDatos("No hay mecánicos registrados."));
            return;
        }
        for (OrdenesPorMecanico mecanico : mecanicos) {
            String especialidad = mecanico.especialidad() == null ? "" : " — " + mecanico.especialidad();
            documento.add(seccion(mecanico.codigo() + " · " + mecanico.nombre() + especialidad));
            documento.add(tablaConteo(mecanico.conteoPorEstado()));
            if (mecanico.ordenes().isEmpty()) {
                documento.add(sinDatos("Sin órdenes asignadas."));
            } else {
                documento.add(tablaOrdenes(mecanico.ordenes()));
            }
        }
    }
}
