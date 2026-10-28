package gt.umg.ultimoarranque.servicio;

import gt.umg.ultimoarranque.dao.ClienteDao;
import gt.umg.ultimoarranque.dao.VehiculoDao;
import gt.umg.ultimoarranque.dao.jdbc.GestorTransacciones;
import gt.umg.ultimoarranque.dto.ClienteRequest;
import gt.umg.ultimoarranque.excepcion.RecursoNoEncontradoException;
import gt.umg.ultimoarranque.excepcion.RegistroConDependenciasException;
import gt.umg.ultimoarranque.excepcion.RegistroDuplicadoException;
import gt.umg.ultimoarranque.modelo.Cliente;
import gt.umg.ultimoarranque.modelo.Vehiculo;
import gt.umg.ultimoarranque.modelo.bitacora.Accion;
import gt.umg.ultimoarranque.modelo.bitacora.EntidadBitacora;
import gt.umg.ultimoarranque.modelo.documento.DocumentoIdentidad;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class ClienteService {

    private static final long SIN_EXCLUSION = 0L;

    private final GestorTransacciones transacciones;
    private final ClienteDao clienteDao;
    private final VehiculoDao vehiculoDao;
    private final Auditoria auditoria;

    public ClienteService(GestorTransacciones transacciones, ClienteDao clienteDao, VehiculoDao vehiculoDao,
                          Auditoria auditoria) {
        this.transacciones = transacciones;
        this.clienteDao = clienteDao;
        this.vehiculoDao = vehiculoDao;
        this.auditoria = auditoria;
    }

    public List<Cliente> listar(String filtro) {
        return transacciones.consultar(conexion -> clienteDao.listar(conexion, filtro));
    }

    public Cliente obtener(long id) {
        return transacciones.consultar(conexion -> buscar(conexion, id));
    }

    public List<Vehiculo> listarVehiculos(long idCliente) {
        return transacciones.consultar(conexion -> {
            buscar(conexion, idCliente);
            return vehiculoDao.listarPorCliente(conexion, idCliente);
        });
    }

    public Cliente registrar(ClienteRequest solicitud) {
        DocumentoIdentidad documento = DocumentoIdentidad.crear(solicitud.tipoDocumento(), solicitud.numeroDocumento());
        Cliente cliente = Cliente.nuevo(documento, solicitud.nombre(), solicitud.telefono(),
                solicitud.correo(), solicitud.direccion());
        return transacciones.enTransaccion(conexion -> {
            validarDocumentoUnico(conexion, documento, SIN_EXCLUSION);
            cliente.asignarId(clienteDao.insertar(conexion, cliente));
            auditoria.registrar(conexion, Accion.CREAR, EntidadBitacora.CLIENTE, cliente.getId(), describir(cliente));
            return cliente;
        });
    }

    public Cliente actualizar(long id, ClienteRequest solicitud) {
        DocumentoIdentidad documento = DocumentoIdentidad.crear(solicitud.tipoDocumento(), solicitud.numeroDocumento());
        return transacciones.enTransaccion(conexion -> {
            Cliente cliente = buscar(conexion, id);
            validarDocumentoUnico(conexion, documento, id);
            cliente.actualizar(documento, solicitud.nombre(), solicitud.telefono(), solicitud.correo(), solicitud.direccion());
            clienteDao.actualizar(conexion, cliente);
            auditoria.registrar(conexion, Accion.MODIFICAR, EntidadBitacora.CLIENTE, id, describir(cliente));
            return cliente;
        });
    }

    public void eliminar(long id) {
        transacciones.ejecutarEnTransaccion(conexion -> {
            Cliente cliente = buscar(conexion, id);
            if (vehiculoDao.existePorCliente(conexion, id)) {
                throw new RegistroConDependenciasException("No se puede eliminar el cliente porque tiene vehículos asociados");
            }
            clienteDao.eliminar(conexion, id);
            auditoria.registrar(conexion, Accion.ELIMINAR, EntidadBitacora.CLIENTE, id, describir(cliente));
        });
    }

    private static String describir(Cliente cliente) {
        return cliente.identificacion() + " · " + cliente.getNombre();
    }

    private Cliente buscar(Connection conexion, long id) throws SQLException {
        return clienteDao.buscarPorId(conexion, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
    }

    private void validarDocumentoUnico(Connection conexion, DocumentoIdentidad documento, long idExcluido)
            throws SQLException {
        if (clienteDao.existeDocumento(conexion, documento, idExcluido)) {
            throw new RegistroDuplicadoException("Ya existe un cliente con " + documento);
        }
    }
}
