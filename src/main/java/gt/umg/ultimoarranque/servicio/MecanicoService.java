package gt.umg.ultimoarranque.servicio;

import gt.umg.ultimoarranque.dao.MecanicoDao;
import gt.umg.ultimoarranque.dao.OrdenConsultaDao;
import gt.umg.ultimoarranque.dao.OrdenTrabajoDao;
import gt.umg.ultimoarranque.dao.UsuarioDao;
import gt.umg.ultimoarranque.dao.jdbc.GestorTransacciones;
import gt.umg.ultimoarranque.dto.MecanicoRequest;
import gt.umg.ultimoarranque.excepcion.RecursoNoEncontradoException;
import gt.umg.ultimoarranque.excepcion.RegistroConDependenciasException;
import gt.umg.ultimoarranque.excepcion.RegistroDuplicadoException;
import gt.umg.ultimoarranque.modelo.Mecanico;
import gt.umg.ultimoarranque.modelo.bitacora.Accion;
import gt.umg.ultimoarranque.modelo.bitacora.EntidadBitacora;
import gt.umg.ultimoarranque.modelo.consulta.FiltroOrdenes;
import gt.umg.ultimoarranque.modelo.consulta.OrdenResumen;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class MecanicoService {

    private static final long SIN_EXCLUSION = 0L;

    private final GestorTransacciones transacciones;
    private final MecanicoDao mecanicoDao;
    private final OrdenTrabajoDao ordenDao;
    private final OrdenConsultaDao ordenConsultaDao;
    private final UsuarioDao usuarioDao;
    private final Auditoria auditoria;

    public MecanicoService(GestorTransacciones transacciones, MecanicoDao mecanicoDao, OrdenTrabajoDao ordenDao,
                           OrdenConsultaDao ordenConsultaDao, UsuarioDao usuarioDao, Auditoria auditoria) {
        this.transacciones = transacciones;
        this.mecanicoDao = mecanicoDao;
        this.ordenDao = ordenDao;
        this.ordenConsultaDao = ordenConsultaDao;
        this.usuarioDao = usuarioDao;
        this.auditoria = auditoria;
    }

    public List<Mecanico> listar() {
        return transacciones.consultar(mecanicoDao::listar);
    }

    public Mecanico obtener(long id) {
        return transacciones.consultar(conexion -> buscar(conexion, id));
    }

    public List<OrdenResumen> listarOrdenes(long id, EstadoOrden estado) {
        return transacciones.consultar(conexion -> {
            buscar(conexion, id);
            return ordenConsultaDao.listar(conexion, FiltroOrdenes.porMecanico(id, estado));
        });
    }

    public Mecanico registrar(MecanicoRequest solicitud) {
        Mecanico mecanico = new Mecanico(null, solicitud.codigo(), solicitud.nombre(), solicitud.especialidad(),
                solicitud.telefono());
        return transacciones.enTransaccion(conexion -> {
            validarCodigoUnico(conexion, mecanico.getCodigo(), SIN_EXCLUSION);
            mecanico.asignarId(mecanicoDao.insertar(conexion, mecanico));
            auditoria.registrar(conexion, Accion.CREAR, EntidadBitacora.MECANICO, mecanico.getId(), describir(mecanico));
            return mecanico;
        });
    }

    public Mecanico actualizar(long id, MecanicoRequest solicitud) {
        return transacciones.enTransaccion(conexion -> {
            Mecanico mecanico = buscar(conexion, id);
            mecanico.actualizar(solicitud.codigo(), solicitud.nombre(), solicitud.especialidad(), solicitud.telefono());
            validarCodigoUnico(conexion, mecanico.getCodigo(), id);
            mecanicoDao.actualizar(conexion, mecanico);
            auditoria.registrar(conexion, Accion.MODIFICAR, EntidadBitacora.MECANICO, id, describir(mecanico));
            return mecanico;
        });
    }

    public void eliminar(long id) {
        transacciones.ejecutarEnTransaccion(conexion -> {
            Mecanico mecanico = buscar(conexion, id);
            if (ordenDao.existePorMecanico(conexion, id)) {
                throw new RegistroConDependenciasException("No se puede eliminar el mecánico porque tiene órdenes asociadas");
            }
            if (usuarioDao.existePorMecanico(conexion, id, SIN_EXCLUSION)) {
                throw new RegistroConDependenciasException("No se puede eliminar el mecánico porque tiene un usuario asociado");
            }
            mecanicoDao.eliminar(conexion, id);
            auditoria.registrar(conexion, Accion.ELIMINAR, EntidadBitacora.MECANICO, id, describir(mecanico));
        });
    }

    private static String describir(Mecanico mecanico) {
        return mecanico.getCodigo() + " · " + mecanico.getNombre();
    }

    private Mecanico buscar(Connection conexion, long id) throws SQLException {
        return mecanicoDao.buscarPorId(conexion, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mecánico", id));
    }

    private void validarCodigoUnico(Connection conexion, String codigo, long idExcluido) throws SQLException {
        if (mecanicoDao.existeCodigo(conexion, codigo, idExcluido)) {
            throw new RegistroDuplicadoException("Ya existe un mecánico con código " + codigo);
        }
    }
}
