package gt.umg.ultimoarranque.dao;

import gt.umg.ultimoarranque.dao.jdbc.JdbcDao;
import gt.umg.ultimoarranque.modelo.Vehiculo;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class VehiculoDao extends JdbcDao<Vehiculo> {

    static final String COLUMNAS = "v.id_vehiculo AS veh_id, v.placa AS veh_placa, v.marca AS veh_marca, "
            + "v.modelo AS veh_modelo, v.anio AS veh_anio, v.color AS veh_color, " + ClienteDao.COLUMNAS;

    static final String UNION_CLIENTE = " JOIN cliente c ON c.id_cliente = v.id_cliente";

    private static final String CONSULTA_BASE = "SELECT " + COLUMNAS + " FROM vehiculo v" + UNION_CLIENTE;

    static Vehiculo desdeFila(ResultSet fila) throws SQLException {
        return new Vehiculo(
                fila.getLong("veh_id"),
                fila.getString("veh_placa"),
                fila.getString("veh_marca"),
                fila.getString("veh_modelo"),
                fila.getInt("veh_anio"),
                fila.getString("veh_color"),
                ClienteDao.desdeFila(fila));
    }

    @Override
    protected Vehiculo mapear(ResultSet fila) throws SQLException {
        return desdeFila(fila);
    }

    public List<Vehiculo> listar(Connection conexion, String placa) throws SQLException {
        if (placa == null || placa.isBlank()) {
            return consultarLista(conexion, CONSULTA_BASE + " ORDER BY v.placa");
        }
        return consultarLista(conexion, CONSULTA_BASE + " WHERE v.placa LIKE ? ORDER BY v.placa",
                "%" + Vehiculo.normalizarPlaca(placa) + "%");
    }

    public List<Vehiculo> listarPorCliente(Connection conexion, long idCliente) throws SQLException {
        return consultarLista(conexion, CONSULTA_BASE + " WHERE v.id_cliente = ? ORDER BY v.placa", idCliente);
    }

    public Optional<Vehiculo> buscarPorId(Connection conexion, long id) throws SQLException {
        return consultarUno(conexion, CONSULTA_BASE + " WHERE v.id_vehiculo = ?", id);
    }

    public Optional<Vehiculo> buscarPorPlaca(Connection conexion, String placa) throws SQLException {
        return consultarUno(conexion, CONSULTA_BASE + " WHERE v.placa = ?", Vehiculo.normalizarPlaca(placa));
    }

    public boolean existePlaca(Connection conexion, String placa, long idExcluido) throws SQLException {
        return existe(conexion, "SELECT 1 FROM vehiculo WHERE placa = ? AND id_vehiculo <> ?", placa, idExcluido);
    }

    public boolean existePorCliente(Connection conexion, long idCliente) throws SQLException {
        return existe(conexion, "SELECT 1 FROM vehiculo WHERE id_cliente = ? LIMIT 1", idCliente);
    }

    public long insertar(Connection conexion, Vehiculo vehiculo) throws SQLException {
        return insertarConClave(conexion,
                "INSERT INTO vehiculo (placa, marca, modelo, anio, color, id_cliente) VALUES (?, ?, ?, ?, ?, ?)",
                vehiculo.getPlaca(), vehiculo.getMarca(), vehiculo.getModelo(), vehiculo.getAnio(),
                vehiculo.getColor(), vehiculo.getPropietario().getId());
    }

    public void actualizar(Connection conexion, Vehiculo vehiculo) throws SQLException {
        ejecutarActualizacion(conexion,
                "UPDATE vehiculo SET placa = ?, marca = ?, modelo = ?, anio = ?, color = ?, id_cliente = ? "
                        + "WHERE id_vehiculo = ?",
                vehiculo.getPlaca(), vehiculo.getMarca(), vehiculo.getModelo(), vehiculo.getAnio(),
                vehiculo.getColor(), vehiculo.getPropietario().getId(), vehiculo.getId());
    }

    public void eliminar(Connection conexion, long id) throws SQLException {
        ejecutarActualizacion(conexion, "DELETE FROM vehiculo WHERE id_vehiculo = ?", id);
    }
}
