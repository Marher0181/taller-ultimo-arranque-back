package gt.umg.ultimoarranque.servicio;

import gt.umg.ultimoarranque.config.TallerProperties;
import gt.umg.ultimoarranque.dao.ComprobanteDao;
import gt.umg.ultimoarranque.dao.DetalleRepuestoDao;
import gt.umg.ultimoarranque.dao.DetalleServicioDao;
import gt.umg.ultimoarranque.dao.MecanicoDao;
import gt.umg.ultimoarranque.dao.OrdenConsultaDao;
import gt.umg.ultimoarranque.dao.OrdenTrabajoDao;
import gt.umg.ultimoarranque.dao.RepuestoDao;
import gt.umg.ultimoarranque.dao.ServicioTallerDao;
import gt.umg.ultimoarranque.dao.VehiculoDao;
import gt.umg.ultimoarranque.dao.jdbc.GestorTransacciones;
import gt.umg.ultimoarranque.dto.OrdenActualizacionRequest;
import gt.umg.ultimoarranque.dto.OrdenCreacionRequest;
import gt.umg.ultimoarranque.excepcion.AccesoDenegadoException;
import gt.umg.ultimoarranque.excepcion.OperacionNoPermitidaException;
import gt.umg.ultimoarranque.excepcion.RecursoNoEncontradoException;
import gt.umg.ultimoarranque.modelo.Mecanico;
import gt.umg.ultimoarranque.modelo.Repuesto;
import gt.umg.ultimoarranque.modelo.ServicioTaller;
import gt.umg.ultimoarranque.modelo.Vehiculo;
import gt.umg.ultimoarranque.modelo.bitacora.Accion;
import gt.umg.ultimoarranque.modelo.bitacora.EntidadBitacora;
import gt.umg.ultimoarranque.modelo.consulta.FiltroOrdenes;
import gt.umg.ultimoarranque.modelo.consulta.OrdenResumen;
import gt.umg.ultimoarranque.modelo.orden.Comprobante;
import gt.umg.ultimoarranque.modelo.orden.DetalleRepuesto;
import gt.umg.ultimoarranque.modelo.orden.DetalleServicio;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import gt.umg.ultimoarranque.modelo.orden.OrdenTrabajo;
import gt.umg.ultimoarranque.modelo.orden.ResumenCostos;
import gt.umg.ultimoarranque.seguridad.SesionUsuario;
import gt.umg.ultimoarranque.seguridad.UsuarioAutenticado;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrdenTrabajoService {

    private final GestorTransacciones transacciones;
    private final OrdenTrabajoDao ordenDao;
    private final OrdenConsultaDao ordenConsultaDao;
    private final DetalleServicioDao detalleServicioDao;
    private final DetalleRepuestoDao detalleRepuestoDao;
    private final ComprobanteDao comprobanteDao;
    private final VehiculoDao vehiculoDao;
    private final MecanicoDao mecanicoDao;
    private final ServicioTallerDao servicioDao;
    private final RepuestoDao repuestoDao;
    private final Auditoria auditoria;
    private final SesionUsuario sesion;
    private final BigDecimal tasaIva;

    public OrdenTrabajoService(GestorTransacciones transacciones, OrdenTrabajoDao ordenDao,
                               OrdenConsultaDao ordenConsultaDao, DetalleServicioDao detalleServicioDao,
                               DetalleRepuestoDao detalleRepuestoDao, ComprobanteDao comprobanteDao,
                               VehiculoDao vehiculoDao, MecanicoDao mecanicoDao, ServicioTallerDao servicioDao,
                               RepuestoDao repuestoDao, Auditoria auditoria, SesionUsuario sesion,
                               TallerProperties propiedades) {
        this.transacciones = transacciones;
        this.ordenDao = ordenDao;
        this.ordenConsultaDao = ordenConsultaDao;
        this.detalleServicioDao = detalleServicioDao;
        this.detalleRepuestoDao = detalleRepuestoDao;
        this.comprobanteDao = comprobanteDao;
        this.vehiculoDao = vehiculoDao;
        this.mecanicoDao = mecanicoDao;
        this.servicioDao = servicioDao;
        this.repuestoDao = repuestoDao;
        this.auditoria = auditoria;
        this.sesion = sesion;
        this.tasaIva = propiedades.facturacion().tasaIva();
    }

    public List<OrdenResumen> listar(EstadoOrden estado) {
        UsuarioAutenticado usuario = sesion.actual();
        FiltroOrdenes filtro = usuario.esMecanico()
                ? FiltroOrdenes.porMecanico(usuario.idMecanico(), estado)
                : FiltroOrdenes.porEstado(estado);
        return transacciones.consultar(conexion -> ordenConsultaDao.listar(conexion, filtro));
    }

    public OrdenTrabajo obtener(long id) {
        OrdenTrabajo orden = transacciones.consultar(conexion -> ordenDao.buscarPorId(conexion, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden de trabajo", id)));
        verificarAcceso(orden);
        return orden;
    }

    public ResumenCostos costosDe(OrdenTrabajo orden) {
        return orden.getComprobante()
                .map(Comprobante::getCostos)
                .orElseGet(() -> orden.calcularCostos(tasaIva));
    }

    public Comprobante obtenerComprobante(long id) {
        return comprobanteDe(obtener(id));
    }

    public Comprobante comprobanteDe(OrdenTrabajo orden) {
        return orden.getComprobante()
                .orElseThrow(() -> new OperacionNoPermitidaException(
                        "La orden " + orden.getId() + " aún no ha sido finalizada; no tiene comprobante"));
    }

    public OrdenTrabajo crear(OrdenCreacionRequest solicitud) {
        return transacciones.enTransaccion(conexion -> {
            Vehiculo vehiculo = vehiculoDao.buscarPorId(conexion, solicitud.idVehiculo())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo", solicitud.idVehiculo()));
            Mecanico mecanico = buscarMecanico(conexion, solicitud.idMecanico());
            OrdenTrabajo orden = OrdenTrabajo.nueva(vehiculo, mecanico, solicitud.descripcionProblema(),
                    sesion.actual().comoResponsable());
            long idOrden = ordenDao.insertar(conexion, orden);
            orden.asignarId(idOrden);
            for (Long idServicio : solicitud.serviciosSolicitados()) {
                DetalleServicio detalle = orden.agregarServicio(buscarServicio(conexion, idServicio));
                detalleServicioDao.insertar(conexion, idOrden, detalle);
            }
            registrar(conexion, Accion.CREAR, orden, vehiculo.getPlaca() + " · mecánico " + mecanico.getCodigo()
                    + " · servicios " + orden.getServicios().size());
            return orden;
        });
    }

    public OrdenTrabajo actualizar(long id, OrdenActualizacionRequest solicitud) {
        return transacciones.enTransaccion(conexion -> {
            OrdenTrabajo orden = bloquearOrden(conexion, id);
            orden.actualizarDatos(buscarMecanico(conexion, solicitud.idMecanico()), solicitud.descripcionProblema());
            ordenDao.actualizar(conexion, orden);
            registrar(conexion, Accion.MODIFICAR, orden, "Mecánico " + orden.getMecanico().getCodigo());
            return orden;
        });
    }

    public OrdenTrabajo agregarServicio(long id, long idServicio) {
        return transacciones.enTransaccion(conexion -> {
            OrdenTrabajo orden = bloquearOrden(conexion, id);
            DetalleServicio detalle = orden.agregarServicio(buscarServicio(conexion, idServicio));
            detalleServicioDao.insertar(conexion, id, detalle);
            registrar(conexion, Accion.AGREGAR_SERVICIO, orden, describir(detalle));
            return orden;
        });
    }

    public OrdenTrabajo quitarServicio(long id, long idServicio) {
        return transacciones.enTransaccion(conexion -> {
            OrdenTrabajo orden = bloquearOrden(conexion, id);
            DetalleServicio detalle = orden.quitarServicio(idServicio);
            detalleServicioDao.eliminar(conexion, id, idServicio);
            registrar(conexion, Accion.QUITAR_SERVICIO, orden, describir(detalle));
            return orden;
        });
    }

    public OrdenTrabajo iniciar(long id) {
        return transacciones.enTransaccion(conexion -> {
            OrdenTrabajo orden = bloquearOrden(conexion, id);
            orden.iniciar(sesion.actual().comoResponsable());
            ordenDao.actualizar(conexion, orden);
            registrar(conexion, Accion.INICIAR_ORDEN, orden, "Reparación iniciada");
            return orden;
        });
    }

    public OrdenTrabajo asignarRepuesto(long id, long idRepuesto, int cantidad) {
        return transacciones.enTransaccion(conexion -> {
            OrdenTrabajo orden = bloquearOrden(conexion, id);
            Repuesto repuesto = bloquearRepuesto(conexion, idRepuesto);
            DetalleRepuesto detalle = orden.asignarRepuesto(repuesto, cantidad);
            repuestoDao.actualizarExistencia(conexion, repuesto);
            detalleRepuestoDao.guardar(conexion, id, detalle);
            registrar(conexion, Accion.ASIGNAR_REPUESTO, orden, describir(repuesto, cantidad));
            return orden;
        });
    }

    public OrdenTrabajo retirarRepuesto(long id, long idDetalle) {
        return transacciones.enTransaccion(conexion -> {
            OrdenTrabajo orden = bloquearOrden(conexion, id);
            long idRepuesto = orden.obtenerDetalleRepuesto(idDetalle).getRepuesto().getId();
            Repuesto repuesto = bloquearRepuesto(conexion, idRepuesto);
            DetalleRepuesto detalle = orden.retirarRepuesto(idDetalle, repuesto);
            repuestoDao.actualizarExistencia(conexion, repuesto);
            detalleRepuestoDao.eliminar(conexion, idDetalle);
            registrar(conexion, Accion.RETIRAR_REPUESTO, orden, describir(repuesto, detalle.getCantidad()));
            return orden;
        });
    }

    public OrdenTrabajo finalizar(long id) {
        return transacciones.enTransaccion(conexion -> {
            OrdenTrabajo orden = bloquearOrden(conexion, id);
            Comprobante comprobante = orden.finalizar(tasaIva, sesion.actual().comoResponsable());
            ordenDao.actualizar(conexion, orden);
            comprobanteDao.insertar(conexion, id, comprobante);
            registrar(conexion, Accion.FINALIZAR_ORDEN, orden,
                    "Comprobante " + comprobante.getNumero() + " · total Q " + comprobante.getCostos().total());
            return orden;
        });
    }

    public OrdenTrabajo cancelar(long id, String motivo) {
        return transacciones.enTransaccion(conexion -> {
            OrdenTrabajo orden = bloquearOrden(conexion, id);
            Set<Long> idsRepuestos = orden.getRepuestos().stream()
                    .map(detalle -> detalle.getRepuesto().getId())
                    .collect(Collectors.toCollection(TreeSet::new));
            Map<Long, Repuesto> inventario = repuestoDao.bloquearVarios(conexion, idsRepuestos).stream()
                    .collect(Collectors.toMap(Repuesto::getId, Function.identity()));
            orden.cancelar(motivo, inventario, sesion.actual().comoResponsable());
            for (Repuesto repuesto : inventario.values()) {
                repuestoDao.actualizarExistencia(conexion, repuesto);
            }
            ordenDao.actualizar(conexion, orden);
            registrar(conexion, Accion.CANCELAR_ORDEN, orden, "Repuestos reintegrados: " + orden.getRepuestos().size()
                    + (orden.getMotivoCancelacion() == null ? "" : " · " + orden.getMotivoCancelacion()));
            return orden;
        });
    }

    private OrdenTrabajo bloquearOrden(Connection conexion, long id) throws SQLException {
        OrdenTrabajo orden = ordenDao.bloquearPorId(conexion, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden de trabajo", id));
        verificarAcceso(orden);
        return orden;
    }

    private void verificarAcceso(OrdenTrabajo orden) {
        UsuarioAutenticado usuario = sesion.actual();
        if (usuario.esMecanico() && !orden.estaAsignadaA(usuario.idMecanico())) {
            throw new AccesoDenegadoException("La orden " + orden.getId() + " no está asignada a su usuario");
        }
    }

    private void registrar(Connection conexion, Accion accion, OrdenTrabajo orden, String detalle) throws SQLException {
        auditoria.registrar(conexion, accion, EntidadBitacora.ORDEN, orden.getId(), detalle);
    }

    private static String describir(DetalleServicio detalle) {
        return detalle.getServicio().getCodigo() + " · " + detalle.getServicio().getDescripcion();
    }

    private static String describir(Repuesto repuesto, int cantidad) {
        return repuesto.getCodigo() + " · " + repuesto.getNombre() + " · " + cantidad
                + " unidades · existencia " + repuesto.getExistencia();
    }

    private Repuesto bloquearRepuesto(Connection conexion, long idRepuesto) throws SQLException {
        return repuestoDao.bloquearPorId(conexion, idRepuesto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Repuesto", idRepuesto));
    }

    private Mecanico buscarMecanico(Connection conexion, long idMecanico) throws SQLException {
        return mecanicoDao.buscarPorId(conexion, idMecanico)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mecánico", idMecanico));
    }

    private ServicioTaller buscarServicio(Connection conexion, long idServicio) throws SQLException {
        return servicioDao.buscarPorId(conexion, idServicio)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", idServicio));
    }
}
