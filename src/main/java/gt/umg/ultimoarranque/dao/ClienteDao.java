package gt.umg.ultimoarranque.dao;

import gt.umg.ultimoarranque.dao.jdbc.JdbcDao;
import gt.umg.ultimoarranque.modelo.Cliente;
import gt.umg.ultimoarranque.modelo.documento.DocumentoIdentidad;
import gt.umg.ultimoarranque.modelo.documento.TipoDocumento;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ClienteDao extends JdbcDao<Cliente> {

    static final String COLUMNAS = "c.id_cliente AS cli_id, c.tipo_documento AS cli_tipo_documento, "
            + "c.numero_documento AS cli_numero_documento, c.nombre AS cli_nombre, c.telefono AS cli_telefono, "
            + "c.correo AS cli_correo, c.direccion AS cli_direccion, c.fecha_registro AS cli_fecha_registro";

    private static final String CONSULTA_BASE = "SELECT " + COLUMNAS + " FROM cliente c";

    static Cliente desdeFila(ResultSet fila) throws SQLException {
        DocumentoIdentidad documento = DocumentoIdentidad.crear(
                TipoDocumento.valueOf(fila.getString("cli_tipo_documento")),
                fila.getString("cli_numero_documento"));
        return new Cliente(
                fila.getLong("cli_id"),
                documento,
                fila.getString("cli_nombre"),
                fila.getString("cli_telefono"),
                fila.getString("cli_correo"),
                fila.getString("cli_direccion"),
                fechaHora(fila, "cli_fecha_registro"));
    }

    @Override
    protected Cliente mapear(ResultSet fila) throws SQLException {
        return desdeFila(fila);
    }

    public List<Cliente> listar(Connection conexion, String filtro) throws SQLException {
        if (filtro == null || filtro.isBlank()) {
            return consultarLista(conexion, CONSULTA_BASE + " ORDER BY c.nombre");
        }
        String patron = "%" + filtro.strip() + "%";
        return consultarLista(conexion,
                CONSULTA_BASE + " WHERE c.nombre LIKE ? OR c.numero_documento LIKE ? ORDER BY c.nombre",
                patron, patron);
    }

    public Optional<Cliente> buscarPorId(Connection conexion, long id) throws SQLException {
        return consultarUno(conexion, CONSULTA_BASE + " WHERE c.id_cliente = ?", id);
    }

    public boolean existeDocumento(Connection conexion, DocumentoIdentidad documento, long idExcluido) throws SQLException {
        return existe(conexion,
                "SELECT 1 FROM cliente WHERE tipo_documento = ? AND numero_documento = ? AND id_cliente <> ?",
                documento.getTipo(), documento.getNumero(), idExcluido);
    }

    public long insertar(Connection conexion, Cliente cliente) throws SQLException {
        return insertarConClave(conexion,
                "INSERT INTO cliente (tipo_documento, numero_documento, nombre, telefono, correo, direccion, fecha_registro) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                cliente.getDocumento().getTipo(), cliente.getDocumento().getNumero(), cliente.getNombre(),
                cliente.getTelefono(), cliente.getCorreo(), cliente.getDireccion(), cliente.getFechaRegistro());
    }

    public void actualizar(Connection conexion, Cliente cliente) throws SQLException {
        ejecutarActualizacion(conexion,
                "UPDATE cliente SET tipo_documento = ?, numero_documento = ?, nombre = ?, telefono = ?, correo = ?, "
                        + "direccion = ? WHERE id_cliente = ?",
                cliente.getDocumento().getTipo(), cliente.getDocumento().getNumero(), cliente.getNombre(),
                cliente.getTelefono(), cliente.getCorreo(), cliente.getDireccion(), cliente.getId());
    }

    public void eliminar(Connection conexion, long id) throws SQLException {
        ejecutarActualizacion(conexion, "DELETE FROM cliente WHERE id_cliente = ?", id);
    }
}
