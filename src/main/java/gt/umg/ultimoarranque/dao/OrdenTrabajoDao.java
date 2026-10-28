package gt.umg.ultimoarranque.dao;

import gt.umg.ultimoarranque.dao.jdbc.JdbcDao;
import gt.umg.ultimoarranque.modelo.Mecanico;
import gt.umg.ultimoarranque.modelo.Responsable;
import gt.umg.ultimoarranque.modelo.Vehiculo;
import gt.umg.ultimoarranque.modelo.orden.DatosOrden;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import gt.umg.ultimoarranque.modelo.orden.OrdenTrabajo;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class OrdenTrabajoDao extends JdbcDao<OrdenTrabajoDao.FilaOrden> {

    record FilaOrden(DatosOrden datos, Vehiculo vehiculo, Mecanico mecanico) {
    }

    private static final String REGISTRO = "ur";
    private static final String INICIO = "ui";
    private static final String CIERRE = "uc";

    private static final String CONSULTA_POR_ID = "SELECT o.id_orden, o.descripcion_problema, o.estado, o.fecha_creacion, "
            + "o.fecha_inicio, o.fecha_finalizacion, o.fecha_cancelacion, o.motivo_cancelacion, "
            + VehiculoDao.COLUMNAS + ", " + MecanicoDao.COLUMNAS + ", "
            + UsuarioDao.columnasResponsable("ur", REGISTRO) + ", "
            + UsuarioDao.columnasResponsable("ui", INICIO) + ", "
            + UsuarioDao.columnasResponsable("uc", CIERRE)
            + " FROM orden_trabajo o"
            + " JOIN vehiculo v ON v.id_vehiculo = o.id_vehiculo" + VehiculoDao.UNION_CLIENTE
            + " JOIN mecanico m ON m.id_mecanico = o.id_mecanico"
            + " JOIN usuario ur ON ur.id_usuario = o.id_usuario_registro"
            + " LEFT JOIN usuario ui ON ui.id_usuario = o.id_usuario_inicio"
            + " LEFT JOIN usuario uc ON uc.id_usuario = o.id_usuario_cierre"
            + " WHERE o.id_orden = ?";

    private static final String BLOQUEO = " FOR UPDATE OF o";

    private final DetalleServicioDao detalleServicioDao;
    private final DetalleRepuestoDao detalleRepuestoDao;
    private final ComprobanteDao comprobanteDao;

    public OrdenTrabajoDao(DetalleServicioDao detalleServicioDao, DetalleRepuestoDao detalleRepuestoDao,
                           ComprobanteDao comprobanteDao) {
        this.detalleServicioDao = detalleServicioDao;
        this.detalleRepuestoDao = detalleRepuestoDao;
        this.comprobanteDao = comprobanteDao;
    }

    @Override
    protected FilaOrden mapear(ResultSet fila) throws SQLException {
        DatosOrden datos = new DatosOrden(
                fila.getLong("id_orden"),
                fila.getString("descripcion_problema"),
                EstadoOrden.valueOf(fila.getString("estado")),
                fechaHora(fila, "fecha_creacion"),
                fechaHora(fila, "fecha_inicio"),
                fechaHora(fila, "fecha_finalizacion"),
                fechaHora(fila, "fecha_cancelacion"),
                fila.getString("motivo_cancelacion"),
                UsuarioDao.responsableDesde(fila, REGISTRO),
                UsuarioDao.responsableDesde(fila, INICIO),
                UsuarioDao.responsableDesde(fila, CIERRE));
        return new FilaOrden(datos, VehiculoDao.desdeFila(fila), MecanicoDao.desdeFila(fila));
    }

    public Optional<OrdenTrabajo> buscarPorId(Connection conexion, long id) throws SQLException {
        return cargar(conexion, CONSULTA_POR_ID, id);
    }

    public Optional<OrdenTrabajo> bloquearPorId(Connection conexion, long id) throws SQLException {
        return cargar(conexion, CONSULTA_POR_ID + BLOQUEO, id);
    }

    public boolean existePorVehiculo(Connection conexion, long idVehiculo) throws SQLException {
        return existe(conexion, "SELECT 1 FROM orden_trabajo WHERE id_vehiculo = ? LIMIT 1", idVehiculo);
    }

    public boolean existePorMecanico(Connection conexion, long idMecanico) throws SQLException {
        return existe(conexion, "SELECT 1 FROM orden_trabajo WHERE id_mecanico = ? LIMIT 1", idMecanico);
    }

    public long insertar(Connection conexion, OrdenTrabajo orden) throws SQLException {
        return insertarConClave(conexion,
                "INSERT INTO orden_trabajo (id_vehiculo, id_mecanico, descripcion_problema, estado, fecha_creacion, "
                        + "id_usuario_registro) VALUES (?, ?, ?, ?, ?, ?)",
                orden.getVehiculo().getId(), orden.getMecanico().getId(), orden.getDescripcionProblema(),
                orden.getEstado(), orden.getFechaCreacion(), orden.getRegistradaPor().idUsuario());
    }

    public void actualizar(Connection conexion, OrdenTrabajo orden) throws SQLException {
        ejecutarActualizacion(conexion,
                "UPDATE orden_trabajo SET id_mecanico = ?, descripcion_problema = ?, estado = ?, fecha_inicio = ?, "
                        + "fecha_finalizacion = ?, fecha_cancelacion = ?, motivo_cancelacion = ?, id_usuario_inicio = ?, "
                        + "id_usuario_cierre = ? WHERE id_orden = ?",
                orden.getMecanico().getId(), orden.getDescripcionProblema(), orden.getEstado(), orden.getFechaInicio(),
                orden.getFechaFinalizacion(), orden.getFechaCancelacion(), orden.getMotivoCancelacion(),
                idUsuario(orden.getIniciadaPor()), idUsuario(orden.getCerradaPor()), orden.getId());
    }

    private static Long idUsuario(Responsable responsable) {
        return responsable == null ? null : responsable.idUsuario();
    }

    private Optional<OrdenTrabajo> cargar(Connection conexion, String sql, long id) throws SQLException {
        Optional<FilaOrden> encontrada = consultarUno(conexion, sql, id);
        if (encontrada.isEmpty()) {
            return Optional.empty();
        }
        FilaOrden fila = encontrada.get();
        return Optional.of(OrdenTrabajo.reconstruir(
                fila.datos(),
                fila.vehiculo(),
                fila.mecanico(),
                detalleServicioDao.listarPorOrden(conexion, id),
                detalleRepuestoDao.listarPorOrden(conexion, id),
                comprobanteDao.buscarPorOrden(conexion, id).orElse(null)));
    }
}
