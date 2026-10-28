package gt.umg.ultimoarranque.dao;

import gt.umg.ultimoarranque.dao.jdbc.JdbcDao;
import gt.umg.ultimoarranque.modelo.bitacora.Accion;
import gt.umg.ultimoarranque.modelo.bitacora.EntidadBitacora;
import gt.umg.ultimoarranque.modelo.bitacora.EntradaBitacora;
import gt.umg.ultimoarranque.modelo.bitacora.FiltroBitacora;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BitacoraDao extends JdbcDao<EntradaBitacora> {

    private static final String CONSULTA_BASE = "SELECT b.id_bitacora, b.fecha, b.id_usuario, b.usuario, b.accion, "
            + "b.entidad, b.id_entidad, b.detalle, b.direccion_ip FROM bitacora b";

    @Override
    protected EntradaBitacora mapear(ResultSet fila) throws SQLException {
        long idUsuario = fila.getLong("id_usuario");
        Long usuario = fila.wasNull() ? null : idUsuario;
        long idEntidad = fila.getLong("id_entidad");
        Long entidad = fila.wasNull() ? null : idEntidad;
        return new EntradaBitacora(
                fila.getLong("id_bitacora"),
                fechaHora(fila, "fecha"),
                usuario,
                fila.getString("usuario"),
                Accion.valueOf(fila.getString("accion")),
                EntidadBitacora.valueOf(fila.getString("entidad")),
                entidad,
                fila.getString("detalle"),
                fila.getString("direccion_ip"));
    }

    public void insertar(Connection conexion, EntradaBitacora entrada) throws SQLException {
        insertarConClave(conexion,
                "INSERT INTO bitacora (fecha, id_usuario, usuario, accion, entidad, id_entidad, detalle, direccion_ip) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                entrada.fecha(), entrada.idUsuario(), entrada.usuario(), entrada.accion(), entrada.entidad(),
                entrada.idEntidad(), entrada.detalle(), entrada.direccionIp());
    }

    public List<EntradaBitacora> listar(Connection conexion, FiltroBitacora filtro) throws SQLException {
        List<String> condiciones = new ArrayList<>();
        List<Object> parametros = new ArrayList<>();
        agregar(condiciones, parametros, "b.id_usuario = ?", filtro.idUsuario());
        agregar(condiciones, parametros, "b.accion = ?", filtro.accion());
        agregar(condiciones, parametros, "b.entidad = ?", filtro.entidad());
        agregar(condiciones, parametros, "b.id_entidad = ?", filtro.idEntidad());
        agregar(condiciones, parametros, "b.fecha >= ?", filtro.desde() == null ? null : filtro.desde().atStartOfDay());
        agregar(condiciones, parametros, "b.fecha < ?",
                filtro.hasta() == null ? null : filtro.hasta().plusDays(1).atStartOfDay());
        String donde = condiciones.isEmpty() ? "" : " WHERE " + String.join(" AND ", condiciones);
        parametros.add(filtro.limite());
        return consultarLista(conexion, CONSULTA_BASE + donde + " ORDER BY b.fecha DESC, b.id_bitacora DESC LIMIT ?",
                parametros.toArray());
    }

    private static void agregar(List<String> condiciones, List<Object> parametros, String condicion, Object valor) {
        if (valor != null) {
            condiciones.add(condicion);
            parametros.add(valor);
        }
    }
}
