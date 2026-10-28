package gt.umg.ultimoarranque.servicio;

import gt.umg.ultimoarranque.config.TallerProperties;
import gt.umg.ultimoarranque.dao.RepuestoDao;
import gt.umg.ultimoarranque.dao.jdbc.GestorTransacciones;
import gt.umg.ultimoarranque.dto.RepuestoActualizacionRequest;
import gt.umg.ultimoarranque.dto.RepuestoRequest;
import gt.umg.ultimoarranque.excepcion.RecursoNoEncontradoException;
import gt.umg.ultimoarranque.excepcion.RegistroDuplicadoException;
import gt.umg.ultimoarranque.modelo.NivelInventario;
import gt.umg.ultimoarranque.modelo.Repuesto;
import gt.umg.ultimoarranque.modelo.bitacora.Accion;
import gt.umg.ultimoarranque.modelo.bitacora.EntidadBitacora;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class RepuestoService {

    private static final long SIN_EXCLUSION = 0L;

    private final GestorTransacciones transacciones;
    private final RepuestoDao repuestoDao;
    private final Auditoria auditoria;
    private final int umbralBajo;

    public RepuestoService(GestorTransacciones transacciones, RepuestoDao repuestoDao, Auditoria auditoria,
                           TallerProperties propiedades) {
        this.transacciones = transacciones;
        this.repuestoDao = repuestoDao;
        this.auditoria = auditoria;
        this.umbralBajo = propiedades.inventario().umbralBajo();
    }

    public int umbralBajo() {
        return umbralBajo;
    }

    public List<Repuesto> listar(NivelInventario nivel) {
        List<Repuesto> repuestos = transacciones.consultar(repuestoDao::listar);
        if (nivel == null) {
            return repuestos;
        }
        return repuestos.stream()
                .filter(repuesto -> repuesto.nivelInventario(umbralBajo) == nivel)
                .toList();
    }

    public List<Repuesto> listarBajoInventario() {
        return transacciones.consultar(conexion -> repuestoDao.listarConExistenciaHasta(conexion, umbralBajo));
    }

    public Repuesto obtener(long id) {
        return transacciones.consultar(conexion -> repuestoDao.buscarPorId(conexion, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Repuesto", id)));
    }

    public Repuesto registrar(RepuestoRequest solicitud) {
        Repuesto repuesto = new Repuesto(null, solicitud.codigo(), solicitud.nombre(), solicitud.descripcion(),
                solicitud.precio(), solicitud.existencia());
        return transacciones.enTransaccion(conexion -> {
            validarCodigoUnico(conexion, repuesto.getCodigo(), SIN_EXCLUSION);
            repuesto.asignarId(repuestoDao.insertar(conexion, repuesto));
            auditoria.registrar(conexion, Accion.CREAR, EntidadBitacora.REPUESTO, repuesto.getId(),
                    describir(repuesto) + " · existencia inicial " + repuesto.getExistencia());
            return repuesto;
        });
    }

    public Repuesto actualizar(long id, RepuestoActualizacionRequest solicitud) {
        return transacciones.enTransaccion(conexion -> {
            Repuesto repuesto = bloquear(conexion, id);
            repuesto.actualizar(solicitud.codigo(), solicitud.nombre(), solicitud.descripcion(), solicitud.precio());
            validarCodigoUnico(conexion, repuesto.getCodigo(), id);
            repuestoDao.actualizarDatos(conexion, repuesto);
            auditoria.registrar(conexion, Accion.MODIFICAR, EntidadBitacora.REPUESTO, id,
                    describir(repuesto) + " · precio " + repuesto.getPrecio());
            return repuesto;
        });
    }

    public Repuesto abastecer(long id, int cantidad) {
        return transacciones.enTransaccion(conexion -> {
            Repuesto repuesto = bloquear(conexion, id);
            repuesto.abastecer(cantidad);
            repuestoDao.actualizarExistencia(conexion, repuesto);
            auditoria.registrar(conexion, Accion.ABASTECER, EntidadBitacora.REPUESTO, id,
                    describir(repuesto) + " · +" + cantidad + " unidades, existencia " + repuesto.getExistencia());
            return repuesto;
        });
    }

    private static String describir(Repuesto repuesto) {
        return repuesto.getCodigo() + " · " + repuesto.getNombre();
    }

    private Repuesto bloquear(Connection conexion, long id) throws SQLException {
        return repuestoDao.bloquearPorId(conexion, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Repuesto", id));
    }

    private void validarCodigoUnico(Connection conexion, String codigo, long idExcluido) throws SQLException {
        if (repuestoDao.existeCodigo(conexion, codigo, idExcluido)) {
            throw new RegistroDuplicadoException("Ya existe un repuesto con código " + codigo);
        }
    }
}
