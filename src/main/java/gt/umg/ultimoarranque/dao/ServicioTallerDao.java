package gt.umg.ultimoarranque.dao;

import gt.umg.ultimoarranque.dao.jdbc.JdbcDao;
import gt.umg.ultimoarranque.modelo.ServicioTaller;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ServicioTallerDao extends JdbcDao<ServicioTaller> {

    static final String COLUMNAS = "s.id_servicio AS ser_id, s.codigo AS ser_codigo, s.descripcion AS ser_descripcion, "
            + "s.costo_mano_obra AS ser_costo_mano_obra";

    private static final String CONSULTA_BASE = "SELECT " + COLUMNAS + " FROM servicio_taller s";

    static ServicioTaller desdeFila(ResultSet fila) throws SQLException {
        return new ServicioTaller(
                fila.getLong("ser_id"),
                fila.getString("ser_codigo"),
                fila.getString("ser_descripcion"),
                fila.getBigDecimal("ser_costo_mano_obra"));
    }

    @Override
    protected ServicioTaller mapear(ResultSet fila) throws SQLException {
        return desdeFila(fila);
    }

    public List<ServicioTaller> listar(Connection conexion) throws SQLException {
        return consultarLista(conexion, CONSULTA_BASE + " ORDER BY s.descripcion");
    }

    public Optional<ServicioTaller> buscarPorId(Connection conexion, long id) throws SQLException {
        return consultarUno(conexion, CONSULTA_BASE + " WHERE s.id_servicio = ?", id);
    }

    public boolean existeCodigo(Connection conexion, String codigo, long idExcluido) throws SQLException {
        return existe(conexion, "SELECT 1 FROM servicio_taller WHERE codigo = ? AND id_servicio <> ?", codigo, idExcluido);
    }

    public long insertar(Connection conexion, ServicioTaller servicio) throws SQLException {
        return insertarConClave(conexion,
                "INSERT INTO servicio_taller (codigo, descripcion, costo_mano_obra) VALUES (?, ?, ?)",
                servicio.getCodigo(), servicio.getDescripcion(), servicio.getCostoManoObra());
    }

    public void actualizar(Connection conexion, ServicioTaller servicio) throws SQLException {
        ejecutarActualizacion(conexion,
                "UPDATE servicio_taller SET codigo = ?, descripcion = ?, costo_mano_obra = ? WHERE id_servicio = ?",
                servicio.getCodigo(), servicio.getDescripcion(), servicio.getCostoManoObra(), servicio.getId());
    }
}
