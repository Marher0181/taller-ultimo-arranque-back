package gt.umg.ultimoarranque.dao;

import gt.umg.ultimoarranque.dao.jdbc.JdbcDao;
import gt.umg.ultimoarranque.modelo.Mecanico;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class MecanicoDao extends JdbcDao<Mecanico> {

    static final String COLUMNAS = "m.id_mecanico AS mec_id, m.codigo AS mec_codigo, m.nombre AS mec_nombre, "
            + "m.especialidad AS mec_especialidad, m.telefono AS mec_telefono";

    private static final String CONSULTA_BASE = "SELECT " + COLUMNAS + " FROM mecanico m";

    static Mecanico desdeFila(ResultSet fila) throws SQLException {
        return new Mecanico(
                fila.getLong("mec_id"),
                fila.getString("mec_codigo"),
                fila.getString("mec_nombre"),
                fila.getString("mec_especialidad"),
                fila.getString("mec_telefono"));
    }

    @Override
    protected Mecanico mapear(ResultSet fila) throws SQLException {
        return desdeFila(fila);
    }

    public List<Mecanico> listar(Connection conexion) throws SQLException {
        return consultarLista(conexion, CONSULTA_BASE + " ORDER BY m.nombre");
    }

    public Optional<Mecanico> buscarPorId(Connection conexion, long id) throws SQLException {
        return consultarUno(conexion, CONSULTA_BASE + " WHERE m.id_mecanico = ?", id);
    }

    public boolean existeCodigo(Connection conexion, String codigo, long idExcluido) throws SQLException {
        return existe(conexion, "SELECT 1 FROM mecanico WHERE codigo = ? AND id_mecanico <> ?", codigo, idExcluido);
    }

    public long insertar(Connection conexion, Mecanico mecanico) throws SQLException {
        return insertarConClave(conexion,
                "INSERT INTO mecanico (codigo, nombre, especialidad, telefono) VALUES (?, ?, ?, ?)",
                mecanico.getCodigo(), mecanico.getNombre(), mecanico.getEspecialidad(), mecanico.getTelefono());
    }

    public void actualizar(Connection conexion, Mecanico mecanico) throws SQLException {
        ejecutarActualizacion(conexion,
                "UPDATE mecanico SET codigo = ?, nombre = ?, especialidad = ?, telefono = ? WHERE id_mecanico = ?",
                mecanico.getCodigo(), mecanico.getNombre(), mecanico.getEspecialidad(), mecanico.getTelefono(),
                mecanico.getId());
    }

    public void eliminar(Connection conexion, long id) throws SQLException {
        ejecutarActualizacion(conexion, "DELETE FROM mecanico WHERE id_mecanico = ?", id);
    }
}
