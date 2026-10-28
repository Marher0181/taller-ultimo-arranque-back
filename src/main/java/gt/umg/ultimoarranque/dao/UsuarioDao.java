package gt.umg.ultimoarranque.dao;

import gt.umg.ultimoarranque.dao.jdbc.JdbcDao;
import gt.umg.ultimoarranque.modelo.DatosCuenta;
import gt.umg.ultimoarranque.modelo.Mecanico;
import gt.umg.ultimoarranque.modelo.Responsable;
import gt.umg.ultimoarranque.modelo.Rol;
import gt.umg.ultimoarranque.modelo.Usuario;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioDao extends JdbcDao<Usuario> {

    private static final String CONSULTA_BASE = "SELECT u.id_usuario, u.username, u.nombre, u.password_hash, u.rol, "
            + "u.activo, u.version_token, u.fecha_creacion, u.ultimo_acceso, " + MecanicoDao.COLUMNAS
            + " FROM usuario u LEFT JOIN mecanico m ON m.id_mecanico = u.id_mecanico";

    static String columnasResponsable(String alias, String prefijo) {
        return alias + ".id_usuario AS " + prefijo + "_id, " + alias + ".username AS " + prefijo + "_username, "
                + alias + ".nombre AS " + prefijo + "_nombre";
    }

    static Responsable responsableDesde(ResultSet fila, String prefijo) throws SQLException {
        long id = fila.getLong(prefijo + "_id");
        if (fila.wasNull()) {
            return null;
        }
        return new Responsable(id, fila.getString(prefijo + "_username"), fila.getString(prefijo + "_nombre"));
    }

    @Override
    protected Usuario mapear(ResultSet fila) throws SQLException {
        fila.getLong("mec_id");
        Mecanico mecanico = fila.wasNull() ? null : MecanicoDao.desdeFila(fila);
        DatosCuenta cuenta = new DatosCuenta(
                fila.getBoolean("activo"),
                fila.getInt("version_token"),
                fechaHora(fila, "fecha_creacion"),
                fechaHora(fila, "ultimo_acceso"));
        return new Usuario(
                fila.getLong("id_usuario"),
                fila.getString("username"),
                fila.getString("nombre"),
                fila.getString("password_hash"),
                Rol.valueOf(fila.getString("rol")),
                mecanico,
                cuenta);
    }

    public List<Usuario> listar(Connection conexion) throws SQLException {
        return consultarLista(conexion, CONSULTA_BASE + " ORDER BY u.username");
    }

    public Optional<Usuario> buscarPorId(Connection conexion, long id) throws SQLException {
        return consultarUno(conexion, CONSULTA_BASE + " WHERE u.id_usuario = ?", id);
    }

    public Optional<Usuario> bloquearPorId(Connection conexion, long id) throws SQLException {
        return consultarUno(conexion, CONSULTA_BASE + " WHERE u.id_usuario = ? FOR UPDATE OF u", id);
    }

    public Optional<Usuario> buscarPorUsername(Connection conexion, String username) throws SQLException {
        return consultarUno(conexion, CONSULTA_BASE + " WHERE u.username = ?", username);
    }

    public boolean existeUsername(Connection conexion, String username) throws SQLException {
        return existe(conexion, "SELECT 1 FROM usuario WHERE username = ?", username);
    }

    public boolean existePorMecanico(Connection conexion, long idMecanico, long idUsuarioExcluido) throws SQLException {
        return existe(conexion, "SELECT 1 FROM usuario WHERE id_mecanico = ? AND id_usuario <> ?",
                idMecanico, idUsuarioExcluido);
    }

    public long insertar(Connection conexion, Usuario usuario) throws SQLException {
        return insertarConClave(conexion,
                "INSERT INTO usuario (username, nombre, password_hash, rol, id_mecanico, activo, version_token, "
                        + "fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                usuario.getUsername(), usuario.getNombre(), usuario.getPasswordHash(), usuario.getRol(),
                usuario.getIdMecanico(), usuario.estaActivo(), usuario.getVersionToken(), usuario.getFechaCreacion());
    }

    public void actualizar(Connection conexion, Usuario usuario) throws SQLException {
        ejecutarActualizacion(conexion,
                "UPDATE usuario SET nombre = ?, password_hash = ?, rol = ?, id_mecanico = ?, activo = ?, "
                        + "version_token = ? WHERE id_usuario = ?",
                usuario.getNombre(), usuario.getPasswordHash(), usuario.getRol(), usuario.getIdMecanico(),
                usuario.estaActivo(), usuario.getVersionToken(), usuario.getId());
    }

    public void actualizarUltimoAcceso(Connection conexion, Usuario usuario) throws SQLException {
        ejecutarActualizacion(conexion, "UPDATE usuario SET ultimo_acceso = ? WHERE id_usuario = ?",
                usuario.getUltimoAcceso(), usuario.getId());
    }
}
