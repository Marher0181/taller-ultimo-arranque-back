package gt.umg.ultimoarranque.servicio;

import gt.umg.ultimoarranque.dao.MecanicoDao;
import gt.umg.ultimoarranque.dao.UsuarioDao;
import gt.umg.ultimoarranque.dao.jdbc.GestorTransacciones;
import gt.umg.ultimoarranque.dto.UsuarioActualizacionRequest;
import gt.umg.ultimoarranque.dto.UsuarioRequest;
import gt.umg.ultimoarranque.excepcion.OperacionNoPermitidaException;
import gt.umg.ultimoarranque.excepcion.RecursoNoEncontradoException;
import gt.umg.ultimoarranque.excepcion.RegistroDuplicadoException;
import gt.umg.ultimoarranque.modelo.Mecanico;
import gt.umg.ultimoarranque.modelo.Usuario;
import gt.umg.ultimoarranque.modelo.bitacora.Accion;
import gt.umg.ultimoarranque.modelo.bitacora.EntidadBitacora;
import gt.umg.ultimoarranque.seguridad.SesionUsuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class UsuarioService {

    private static final long SIN_EXCLUSION = 0L;

    private final GestorTransacciones transacciones;
    private final UsuarioDao usuarioDao;
    private final MecanicoDao mecanicoDao;
    private final PasswordEncoder codificador;
    private final Auditoria auditoria;
    private final SesionUsuario sesion;

    public UsuarioService(GestorTransacciones transacciones, UsuarioDao usuarioDao, MecanicoDao mecanicoDao,
                          PasswordEncoder codificador, Auditoria auditoria, SesionUsuario sesion) {
        this.transacciones = transacciones;
        this.usuarioDao = usuarioDao;
        this.mecanicoDao = mecanicoDao;
        this.codificador = codificador;
        this.auditoria = auditoria;
        this.sesion = sesion;
    }

    public List<Usuario> listar() {
        return transacciones.consultar(usuarioDao::listar);
    }

    public Usuario obtener(long id) {
        return transacciones.consultar(conexion -> usuarioDao.buscarPorId(conexion, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id)));
    }

    public Usuario registrar(UsuarioRequest solicitud) {
        String hash = codificador.encode(solicitud.password());
        return transacciones.enTransaccion(conexion -> {
            Mecanico mecanico = resolverMecanico(conexion, solicitud.idMecanico(), SIN_EXCLUSION);
            Usuario usuario = Usuario.nuevo(solicitud.username(), solicitud.nombre(), hash, solicitud.rol(), mecanico);
            if (usuarioDao.existeUsername(conexion, usuario.getUsername())) {
                throw new RegistroDuplicadoException("Ya existe el usuario " + usuario.getUsername());
            }
            usuario.asignarId(usuarioDao.insertar(conexion, usuario));
            auditoria.registrar(conexion, Accion.CREAR, EntidadBitacora.USUARIO, usuario.getId(), describir(usuario));
            return usuario;
        });
    }

    public Usuario actualizar(long id, UsuarioActualizacionRequest solicitud) {
        return transacciones.enTransaccion(conexion -> {
            Usuario usuario = bloquear(conexion, id);
            if (esUsuarioActual(id) && solicitud.rol() != usuario.getRol()) {
                throw new OperacionNoPermitidaException("No puede cambiar su propio rol");
            }
            Mecanico mecanico = resolverMecanico(conexion, solicitud.idMecanico(), id);
            usuario.setNombre(solicitud.nombre());
            usuario.asignarRol(solicitud.rol(), mecanico);
            usuarioDao.actualizar(conexion, usuario);
            auditoria.registrar(conexion, Accion.MODIFICAR, EntidadBitacora.USUARIO, id, describir(usuario));
            return usuario;
        });
    }

    public Usuario activar(long id) {
        return transacciones.enTransaccion(conexion -> {
            Usuario usuario = bloquear(conexion, id);
            usuario.activar();
            usuarioDao.actualizar(conexion, usuario);
            auditoria.registrar(conexion, Accion.ACTIVAR, EntidadBitacora.USUARIO, id, usuario.getUsername());
            return usuario;
        });
    }

    public Usuario desactivar(long id) {
        if (esUsuarioActual(id)) {
            throw new OperacionNoPermitidaException("No puede desactivar su propio usuario");
        }
        return transacciones.enTransaccion(conexion -> {
            Usuario usuario = bloquear(conexion, id);
            usuario.desactivar();
            usuarioDao.actualizar(conexion, usuario);
            auditoria.registrar(conexion, Accion.DESACTIVAR, EntidadBitacora.USUARIO, id, usuario.getUsername());
            return usuario;
        });
    }

    public Usuario restablecerPassword(long id, String passwordNueva) {
        String hash = codificador.encode(passwordNueva);
        return transacciones.enTransaccion(conexion -> {
            Usuario usuario = bloquear(conexion, id);
            usuario.cambiarPassword(hash);
            usuarioDao.actualizar(conexion, usuario);
            auditoria.registrar(conexion, Accion.RESTABLECIMIENTO_PASSWORD, EntidadBitacora.USUARIO, id,
                    usuario.getUsername());
            return usuario;
        });
    }

    private Mecanico resolverMecanico(Connection conexion, Long idMecanico, long idUsuarioExcluido) throws SQLException {
        if (idMecanico == null) {
            return null;
        }
        Mecanico mecanico = mecanicoDao.buscarPorId(conexion, idMecanico)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mecánico", idMecanico));
        if (usuarioDao.existePorMecanico(conexion, idMecanico, idUsuarioExcluido)) {
            throw new RegistroDuplicadoException("El mecánico " + mecanico.getCodigo() + " ya tiene un usuario asociado");
        }
        return mecanico;
    }

    private Usuario bloquear(Connection conexion, long id) throws SQLException {
        return usuarioDao.bloquearPorId(conexion, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    private boolean esUsuarioActual(long id) {
        return sesion.actual().id() == id;
    }

    private static String describir(Usuario usuario) {
        String vinculo = usuario.getMecanico() == null ? "" : " · " + usuario.getMecanico().getCodigo();
        return usuario.getUsername() + " · " + usuario.getRol() + vinculo;
    }
}
