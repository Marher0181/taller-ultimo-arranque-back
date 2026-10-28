package gt.umg.ultimoarranque.modelo.consulta;

import java.util.List;

public record ReporteInventarioBajo(int umbral, int agotados, List<RepuestoInventario> repuestos) {
}
