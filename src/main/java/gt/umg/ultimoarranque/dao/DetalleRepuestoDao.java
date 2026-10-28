package gt.umg.ultimoarranque.dao;

import gt.umg.ultimoarranque.dao.jdbc.JdbcDao;
import gt.umg.ultimoarranque.modelo.orden.DetalleRepuesto;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class DetalleRepuestoDao extends JdbcDao<DetalleRepuesto> {

    private static final String CONSULTA_POR_ORDEN = "SELECT dr.id_detalle_repuesto AS dr_id, dr.cantidad AS dr_cantidad, "
            + "dr.precio_unitario AS dr_precio, dr.fecha_asignacion AS dr_fecha, " + RepuestoDao.COLUMNAS
            + " FROM detalle_repuesto dr JOIN repuesto r ON r.id_repuesto = dr.id_repuesto"
            + " WHERE dr.id_orden = ? ORDER BY dr.id_detalle_repuesto";

    @Override
    protected DetalleRepuesto mapear(ResultSet fila) throws SQLException {
        return new DetalleRepuesto(
                fila.getLong("dr_id"),
                RepuestoDao.desdeFila(fila),
                fila.getInt("dr_cantidad"),
                fila.getBigDecimal("dr_precio"),
                fechaHora(fila, "dr_fecha"));
    }

    public List<DetalleRepuesto> listarPorOrden(Connection conexion, long idOrden) throws SQLException {
        return consultarLista(conexion, CONSULTA_POR_ORDEN, idOrden);
    }

    public void guardar(Connection conexion, long idOrden, DetalleRepuesto detalle) throws SQLException {
        if (detalle.esNueva()) {
            long id = insertarConClave(conexion,
                    "INSERT INTO detalle_repuesto (id_orden, id_repuesto, cantidad, precio_unitario, fecha_asignacion) "
                            + "VALUES (?, ?, ?, ?, ?)",
                    idOrden, detalle.getRepuesto().getId(), detalle.getCantidad(), detalle.getPrecioUnitario(),
                    detalle.getFechaAsignacion());
            detalle.asignarId(id);
            return;
        }
        ejecutarActualizacion(conexion, "UPDATE detalle_repuesto SET cantidad = ? WHERE id_detalle_repuesto = ?",
                detalle.getCantidad(), detalle.getId());
    }

    public void eliminar(Connection conexion, long idDetalle) throws SQLException {
        ejecutarActualizacion(conexion, "DELETE FROM detalle_repuesto WHERE id_detalle_repuesto = ?", idDetalle);
    }
}
