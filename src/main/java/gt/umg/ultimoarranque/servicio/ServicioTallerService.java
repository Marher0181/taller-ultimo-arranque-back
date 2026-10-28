package gt.umg.ultimoarranque.servicio;

import gt.umg.ultimoarranque.dao.ServicioTallerDao;
import gt.umg.ultimoarranque.dao.jdbc.GestorTransacciones;
import gt.umg.ultimoarranque.dto.ServicioTallerRequest;
import gt.umg.ultimoarranque.excepcion.RecursoNoEncontradoException;
import gt.umg.ultimoarranque.excepcion.RegistroDuplicadoException;
import gt.umg.ultimoarranque.modelo.ServicioTaller;
import gt.umg.ultimoarranque.modelo.bitacora.Accion;
import gt.umg.ultimoarranque.modelo.bitacora.EntidadBitacora;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class ServicioTallerService {

    private static final long SIN_EXCLUSION = 0L;

    private final GestorTransacciones transacciones;
    private final ServicioTallerDao servicioDao;
    private final Auditoria auditoria;

    public ServicioTallerService(GestorTransacciones transacciones, ServicioTallerDao servicioDao, Auditoria auditoria) {
        this.transacciones = transacciones;
        this.servicioDao = servicioDao;
        this.auditoria = auditoria;
    }

    public List<ServicioTaller> listar() {
        return transacciones.consultar(servicioDao::listar);
    }

    public ServicioTaller obtener(long id) {
        return transacciones.consultar(conexion -> buscar(conexion, id));
    }

    public ServicioTaller registrar(ServicioTallerRequest solicitud) {
        ServicioTaller servicio = new ServicioTaller(null, solicitud.codigo(), solicitud.descripcion(),
                solicitud.costoManoObra());
        return transacciones.enTransaccion(conexion -> {
            validarCodigoUnico(conexion, servicio.getCodigo(), SIN_EXCLUSION);
            servicio.asignarId(servicioDao.insertar(conexion, servicio));
            auditoria.registrar(conexion, Accion.CREAR, EntidadBitacora.SERVICIO, servicio.getId(), describir(servicio));
            return servicio;
        });
    }

    public ServicioTaller actualizar(long id, ServicioTallerRequest solicitud) {
        return transacciones.enTransaccion(conexion -> {
            ServicioTaller servicio = buscar(conexion, id);
            servicio.actualizar(solicitud.codigo(), solicitud.descripcion(), solicitud.costoManoObra());
            validarCodigoUnico(conexion, servicio.getCodigo(), id);
            servicioDao.actualizar(conexion, servicio);
            auditoria.registrar(conexion, Accion.MODIFICAR, EntidadBitacora.SERVICIO, id, describir(servicio));
            return servicio;
        });
    }

    private static String describir(ServicioTaller servicio) {
        return servicio.getCodigo() + " · " + servicio.getDescripcion() + " · Q " + servicio.getCostoManoObra();
    }

    private ServicioTaller buscar(Connection conexion, long id) throws SQLException {
        return servicioDao.buscarPorId(conexion, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", id));
    }

    private void validarCodigoUnico(Connection conexion, String codigo, long idExcluido) throws SQLException {
        if (servicioDao.existeCodigo(conexion, codigo, idExcluido)) {
            throw new RegistroDuplicadoException("Ya existe un servicio con código " + codigo);
        }
    }
}
