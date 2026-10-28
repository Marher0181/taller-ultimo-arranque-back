package gt.umg.ultimoarranque.dao;

import gt.umg.ultimoarranque.dao.jdbc.JdbcDao;
import gt.umg.ultimoarranque.modelo.Repuesto;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class RepuestoDao extends JdbcDao<Repuesto> {

    static final String COLUMNAS = "r.id_repuesto AS rep_id, r.codigo AS rep_codigo, r.nombre AS rep_nombre, "
            + "r.descripcion AS rep_descripcion, r.precio AS rep_precio, r.existencia AS rep_existencia";

    private static final String CONSULTA_BASE = "SELECT " + COLUMNAS + " FROM repuesto r";
    private static final String BLOQUEO = " FOR UPDATE";

    static Repuesto desdeFila(ResultSet fila) throws SQLException {
        return new Repuesto(
                fila.getLong("rep_id"),
                fila.getString("rep_codigo"),
                fila.getString("rep_nombre"),
                fila.getString("rep_descripcion"),
                fila.getBigDecimal("rep_precio"),
                fila.getInt("rep_existencia"));
    }

    @Override
    protected Repuesto mapear(ResultSet fila) throws SQLException {
        return desdeFila(fila);
    }

    public List<Repuesto> listar(Connection conexion) throws SQLException {
        return consultarLista(conexion, CONSULTA_BASE + " ORDER BY r.nombre");
    }

    public List<Repuesto> listarConExistenciaHasta(Connection conexion, int existenciaMaxima) throws SQLException {
        return consultarLista(conexion,
                CONSULTA_BASE + " WHERE r.existencia <= ? ORDER BY r.existencia, r.nombre", existenciaMaxima);
    }

    public Optional<Repuesto> buscarPorId(Connection conexion, long id) throws SQLException {
        return consultarUno(conexion, CONSULTA_BASE + " WHERE r.id_repuesto = ?", id);
    }

    public Optional<Repuesto> bloquearPorId(Connection conexion, long id) throws SQLException {
        return consultarUno(conexion, CONSULTA_BASE + " WHERE r.id_repuesto = ?" + BLOQUEO, id);
    }

    public List<Repuesto> bloquearVarios(Connection conexion, Collection<Long> ids) throws SQLException {
        if (ids.isEmpty()) {
            return List.of();
        }
        String sql = CONSULTA_BASE + " WHERE r.id_repuesto IN (" + marcadores(ids) + ") ORDER BY r.id_repuesto" + BLOQUEO;
        return consultarLista(conexion, sql, ids.toArray());
    }

    public boolean existeCodigo(Connection conexion, String codigo, long idExcluido) throws SQLException {
        return existe(conexion, "SELECT 1 FROM repuesto WHERE codigo = ? AND id_repuesto <> ?", codigo, idExcluido);
    }

    public long insertar(Connection conexion, Repuesto repuesto) throws SQLException {
        return insertarConClave(conexion,
                "INSERT INTO repuesto (codigo, nombre, descripcion, precio, existencia) VALUES (?, ?, ?, ?, ?)",
                repuesto.getCodigo(), repuesto.getNombre(), repuesto.getDescripcion(), repuesto.getPrecio(),
                repuesto.getExistencia());
    }

    public void actualizarDatos(Connection conexion, Repuesto repuesto) throws SQLException {
        ejecutarActualizacion(conexion,
                "UPDATE repuesto SET codigo = ?, nombre = ?, descripcion = ?, precio = ? WHERE id_repuesto = ?",
                repuesto.getCodigo(), repuesto.getNombre(), repuesto.getDescripcion(), repuesto.getPrecio(),
                repuesto.getId());
    }

    public void actualizarExistencia(Connection conexion, Repuesto repuesto) throws SQLException {
        ejecutarActualizacion(conexion, "UPDATE repuesto SET existencia = ? WHERE id_repuesto = ?",
                repuesto.getExistencia(), repuesto.getId());
    }
}
