package gt.umg.ultimoarranque.dao;

import gt.umg.ultimoarranque.dao.jdbc.JdbcDao;
import gt.umg.ultimoarranque.modelo.consulta.FiltroOrdenes;
import gt.umg.ultimoarranque.modelo.consulta.IngresoOrden;
import gt.umg.ultimoarranque.modelo.consulta.OrdenResumen;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrdenConsultaDao extends JdbcDao<OrdenResumen> {

    private static final String UNIONES = " FROM orden_trabajo o"
            + " JOIN vehiculo v ON v.id_vehiculo = o.id_vehiculo"
            + " JOIN cliente c ON c.id_cliente = v.id_cliente"
            + " JOIN mecanico m ON m.id_mecanico = o.id_mecanico";

    private static final String CONSULTA_ORDENES = "SELECT o.id_orden, o.estado, o.descripcion_problema, o.fecha_creacion, "
            + "o.fecha_finalizacion, v.id_vehiculo, v.placa, CONCAT(v.marca, ' ', v.modelo, ' ', v.anio) AS vehiculo, "
            + "c.id_cliente, c.nombre AS cliente, m.id_mecanico, m.nombre AS mecanico, cp.total"
            + UNIONES
            + " LEFT JOIN comprobante cp ON cp.id_orden = o.id_orden";

    private static final String CONSULTA_INGRESOS = "SELECT cp.numero, cp.fecha_emision, o.id_orden, v.placa, "
            + "c.nombre AS cliente, m.nombre AS mecanico, cp.total_mano_obra, cp.total_repuestos, cp.iva, cp.total"
            + UNIONES
            + " JOIN comprobante cp ON cp.id_orden = o.id_orden"
            + " WHERE cp.fecha_emision >= ? AND cp.fecha_emision < ?"
            + " ORDER BY cp.fecha_emision, cp.numero";

    @Override
    protected OrdenResumen mapear(ResultSet fila) throws SQLException {
        return new OrdenResumen(
                fila.getLong("id_orden"),
                EstadoOrden.valueOf(fila.getString("estado")),
                fila.getString("descripcion_problema"),
                fila.getLong("id_vehiculo"),
                fila.getString("placa"),
                fila.getString("vehiculo"),
                fila.getLong("id_cliente"),
                fila.getString("cliente"),
                fila.getLong("id_mecanico"),
                fila.getString("mecanico"),
                fechaHora(fila, "fecha_creacion"),
                fechaHora(fila, "fecha_finalizacion"),
                fila.getBigDecimal("total"));
    }

    public List<OrdenResumen> listar(Connection conexion, FiltroOrdenes filtro) throws SQLException {
        List<String> condiciones = new ArrayList<>();
        List<Object> parametros = new ArrayList<>();
        agregarCondicion(condiciones, parametros, "o.estado = ?", filtro.estado());
        agregarCondicion(condiciones, parametros, "o.id_vehiculo = ?", filtro.idVehiculo());
        agregarCondicion(condiciones, parametros, "o.id_mecanico = ?", filtro.idMecanico());
        String donde = condiciones.isEmpty() ? "" : " WHERE " + String.join(" AND ", condiciones);
        return consultarLista(conexion, CONSULTA_ORDENES + donde + " ORDER BY o.fecha_creacion DESC, o.id_orden DESC",
                parametros.toArray());
    }

    public List<IngresoOrden> listarIngresos(Connection conexion, LocalDateTime desde, LocalDateTime hastaExclusivo)
            throws SQLException {
        return consultar(conexion, CONSULTA_INGRESOS, this::mapearIngreso, desde, hastaExclusivo);
    }

    private IngresoOrden mapearIngreso(ResultSet fila) throws SQLException {
        return new IngresoOrden(
                fila.getString("numero"),
                fechaHora(fila, "fecha_emision"),
                fila.getLong("id_orden"),
                fila.getString("placa"),
                fila.getString("cliente"),
                fila.getString("mecanico"),
                fila.getBigDecimal("total_mano_obra"),
                fila.getBigDecimal("total_repuestos"),
                fila.getBigDecimal("iva"),
                fila.getBigDecimal("total"));
    }

    private static void agregarCondicion(List<String> condiciones, List<Object> parametros, String condicion, Object valor) {
        if (valor != null) {
            condiciones.add(condicion);
            parametros.add(valor);
        }
    }
}
