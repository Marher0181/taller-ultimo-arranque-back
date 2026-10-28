package gt.umg.ultimoarranque.dao;

import gt.umg.ultimoarranque.dao.jdbc.JdbcDao;
import gt.umg.ultimoarranque.modelo.orden.Comprobante;
import gt.umg.ultimoarranque.modelo.orden.ResumenCostos;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class ComprobanteDao extends JdbcDao<Comprobante> {

    @Override
    protected Comprobante mapear(ResultSet fila) throws SQLException {
        ResumenCostos costos = new ResumenCostos(
                fila.getBigDecimal("total_mano_obra"),
                fila.getBigDecimal("total_repuestos"),
                fila.getBigDecimal("subtotal"),
                fila.getBigDecimal("tasa_iva"),
                fila.getBigDecimal("iva"),
                fila.getBigDecimal("total"));
        return new Comprobante(fila.getString("numero"), fechaHora(fila, "fecha_emision"), costos);
    }

    public Optional<Comprobante> buscarPorOrden(Connection conexion, long idOrden) throws SQLException {
        return consultarUno(conexion,
                "SELECT numero, fecha_emision, total_mano_obra, total_repuestos, subtotal, tasa_iva, iva, total "
                        + "FROM comprobante WHERE id_orden = ?",
                idOrden);
    }

    public void insertar(Connection conexion, long idOrden, Comprobante comprobante) throws SQLException {
        ResumenCostos costos = comprobante.getCostos();
        insertarConClave(conexion,
                "INSERT INTO comprobante (id_orden, numero, fecha_emision, total_mano_obra, total_repuestos, subtotal, "
                        + "tasa_iva, iva, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                idOrden, comprobante.getNumero(), comprobante.getFechaEmision(), costos.manoObra(), costos.repuestos(),
                costos.subtotal(), costos.tasaIva(), costos.iva(), costos.total());
    }
}
