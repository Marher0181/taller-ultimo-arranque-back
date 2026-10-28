package gt.umg.ultimoarranque.dao;

import gt.umg.ultimoarranque.dao.jdbc.JdbcDao;
import gt.umg.ultimoarranque.modelo.orden.DetalleServicio;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class DetalleServicioDao extends JdbcDao<DetalleServicio> {

    private static final String CONSULTA_POR_ORDEN = "SELECT ds.costo_mano_obra AS ds_costo, ds.fecha_registro AS ds_fecha, "
            + ServicioTallerDao.COLUMNAS
            + " FROM detalle_servicio ds JOIN servicio_taller s ON s.id_servicio = ds.id_servicio"
            + " WHERE ds.id_orden = ? ORDER BY ds.fecha_registro, s.descripcion";

    @Override
    protected DetalleServicio mapear(ResultSet fila) throws SQLException {
        return new DetalleServicio(
                ServicioTallerDao.desdeFila(fila),
                fila.getBigDecimal("ds_costo"),
                fechaHora(fila, "ds_fecha"));
    }

    public List<DetalleServicio> listarPorOrden(Connection conexion, long idOrden) throws SQLException {
        return consultarLista(conexion, CONSULTA_POR_ORDEN, idOrden);
    }

    public boolean existePorServicio(Connection conexion, long idServicio) throws SQLException {
        return existe(conexion, "SELECT 1 FROM detalle_servicio WHERE id_servicio = ? LIMIT 1", idServicio);
    }

    public void insertar(Connection conexion, long idOrden, DetalleServicio detalle) throws SQLException {
        ejecutarActualizacion(conexion,
                "INSERT INTO detalle_servicio (id_orden, id_servicio, costo_mano_obra, fecha_registro) VALUES (?, ?, ?, ?)",
                idOrden, detalle.getServicio().getId(), detalle.getCostoManoObra(), detalle.getFechaRegistro());
    }

    public void eliminar(Connection conexion, long idOrden, long idServicio) throws SQLException {
        ejecutarActualizacion(conexion, "DELETE FROM detalle_servicio WHERE id_orden = ? AND id_servicio = ?",
                idOrden, idServicio);
    }
}
