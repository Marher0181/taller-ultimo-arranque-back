package gt.umg.ultimoarranque.servicio;

import gt.umg.ultimoarranque.dao.ClienteDao;
import gt.umg.ultimoarranque.dao.OrdenConsultaDao;
import gt.umg.ultimoarranque.dao.OrdenTrabajoDao;
import gt.umg.ultimoarranque.dao.VehiculoDao;
import gt.umg.ultimoarranque.dao.jdbc.GestorTransacciones;
import gt.umg.ultimoarranque.dto.VehiculoRequest;
import gt.umg.ultimoarranque.excepcion.RecursoNoEncontradoException;
import gt.umg.ultimoarranque.excepcion.RegistroConDependenciasException;
import gt.umg.ultimoarranque.excepcion.RegistroDuplicadoException;
import gt.umg.ultimoarranque.modelo.Cliente;
import gt.umg.ultimoarranque.modelo.Vehiculo;
import gt.umg.ultimoarranque.modelo.bitacora.Accion;
import gt.umg.ultimoarranque.modelo.bitacora.EntidadBitacora;
import gt.umg.ultimoarranque.modelo.consulta.FiltroOrdenes;
import gt.umg.ultimoarranque.modelo.consulta.OrdenResumen;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class VehiculoService {

    private static final long SIN_EXCLUSION = 0L;

    private final GestorTransacciones transacciones;
    private final VehiculoDao vehiculoDao;
    private final ClienteDao clienteDao;
    private final OrdenTrabajoDao ordenDao;
    private final OrdenConsultaDao ordenConsultaDao;
    private final Auditoria auditoria;

    public VehiculoService(GestorTransacciones transacciones, VehiculoDao vehiculoDao, ClienteDao clienteDao,
                           OrdenTrabajoDao ordenDao, OrdenConsultaDao ordenConsultaDao, Auditoria auditoria) {
        this.transacciones = transacciones;
        this.vehiculoDao = vehiculoDao;
        this.clienteDao = clienteDao;
        this.ordenDao = ordenDao;
        this.ordenConsultaDao = ordenConsultaDao;
        this.auditoria = auditoria;
    }

    public List<Vehiculo> listar(String placa) {
        return transacciones.consultar(conexion -> vehiculoDao.listar(conexion, placa));
    }

    public Vehiculo obtener(long id) {
        return transacciones.consultar(conexion -> buscar(conexion, id));
    }

    public Vehiculo obtenerPorPlaca(String placa) {
        return transacciones.consultar(conexion -> vehiculoDao.buscarPorPlaca(conexion, placa)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo", Vehiculo.normalizarPlaca(placa))));
    }

    public List<OrdenResumen> historial(long id) {
        return transacciones.consultar(conexion -> {
            buscar(conexion, id);
            return ordenConsultaDao.listar(conexion, FiltroOrdenes.porVehiculo(id));
        });
    }

    public Vehiculo registrar(VehiculoRequest solicitud) {
        return transacciones.enTransaccion(conexion -> {
            Cliente propietario = buscarCliente(conexion, solicitud.idCliente());
            Vehiculo vehiculo = new Vehiculo(null, solicitud.placa(), solicitud.marca(), solicitud.modelo(),
                    solicitud.anio(), solicitud.color(), propietario);
            validarPlacaUnica(conexion, vehiculo.getPlaca(), SIN_EXCLUSION);
            vehiculo.asignarId(vehiculoDao.insertar(conexion, vehiculo));
            auditoria.registrar(conexion, Accion.CREAR, EntidadBitacora.VEHICULO, vehiculo.getId(), describir(vehiculo));
            return vehiculo;
        });
    }

    public Vehiculo actualizar(long id, VehiculoRequest solicitud) {
        return transacciones.enTransaccion(conexion -> {
            Vehiculo vehiculo = buscar(conexion, id);
            Cliente propietario = buscarCliente(conexion, solicitud.idCliente());
            vehiculo.actualizar(solicitud.placa(), solicitud.marca(), solicitud.modelo(), solicitud.anio(),
                    solicitud.color(), propietario);
            validarPlacaUnica(conexion, vehiculo.getPlaca(), id);
            vehiculoDao.actualizar(conexion, vehiculo);
            auditoria.registrar(conexion, Accion.MODIFICAR, EntidadBitacora.VEHICULO, id, describir(vehiculo));
            return vehiculo;
        });
    }

    public void eliminar(long id) {
        transacciones.ejecutarEnTransaccion(conexion -> {
            Vehiculo vehiculo = buscar(conexion, id);
            if (ordenDao.existePorVehiculo(conexion, id)) {
                throw new RegistroConDependenciasException("No se puede eliminar el vehículo porque tiene órdenes registradas");
            }
            vehiculoDao.eliminar(conexion, id);
            auditoria.registrar(conexion, Accion.ELIMINAR, EntidadBitacora.VEHICULO, id, describir(vehiculo));
        });
    }

    private static String describir(Vehiculo vehiculo) {
        return vehiculo.getPlaca() + " · " + vehiculo.descripcion() + " · " + vehiculo.getPropietario().getNombre();
    }

    private Vehiculo buscar(Connection conexion, long id) throws SQLException {
        return vehiculoDao.buscarPorId(conexion, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo", id));
    }

    private Cliente buscarCliente(Connection conexion, long idCliente) throws SQLException {
        return clienteDao.buscarPorId(conexion, idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", idCliente));
    }

    private void validarPlacaUnica(Connection conexion, String placa, long idExcluido) throws SQLException {
        if (vehiculoDao.existePlaca(conexion, placa, idExcluido)) {
            throw new RegistroDuplicadoException("Ya existe un vehículo con placa " + placa);
        }
    }
}
