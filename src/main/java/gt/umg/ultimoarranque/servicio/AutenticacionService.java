package gt.umg.ultimoarranque.servicio;

import gt.umg.ultimoarranque.dao.UsuarioDao;
import gt.umg.ultimoarranque.dao.jdbc.GestorTransacciones;
import gt.umg.ultimoarranque.excepcion.CredencialesInvalidasException;
import gt.umg.ultimoarranque.excepcion.DatoInvalidoException;
import gt.umg.ultimoarranque.excepcion.RecursoNoEncontradoException;
import gt.umg.ultimoarranque.modelo.Usuario;
import gt.umg.ultimoarranque.modelo.bitacora.Accion;
import gt.umg.ultimoarranque.modelo.bitacora.EntidadBitacora;
import gt.umg.ultimoarranque.seguridad.EmisorToken;
import gt.umg.ultimoarranque.seguridad.SesionIniciada;
import gt.umg.ultimoarranque.seguridad.SesionUsuario;
import gt.umg.ultimoarranque.seguridad.UsuarioAutenticado;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@Service
public class AutenticacionService {

    private static final String MENSAJE_CREDENCIALES = "Usuario o contraseña incorrectos";
    private static final String USUARIO_VACIO = "(sin usuario)";
    private static final int LONGITUD_MAXIMA_USUARIO = 50;

    private final GestorTransacciones transacciones;
    private final UsuarioDao usuarioDao;
    private final PasswordEncoder codificador;
    private final EmisorToken emisorToken;
    private final Auditoria auditoria;
    private final SesionUsuario sesion;
    private final String hashReferencia;

    public AutenticacionService(GestorTransacciones transacciones, UsuarioDao usuarioDao, PasswordEncoder codificador,
                                EmisorToken emisorToken, Auditoria auditoria, SesionUsuario sesion) {
        this.transacciones = transacciones;
        this.usuarioDao = usuarioDao;
        this.codificador = codificador;
        this.emisorToken = emisorToken;
        this.auditoria = auditoria;
        this.sesion = sesion;
        this.hashReferencia = codificador.encode(getClass().getName());
    }

    public SesionIniciada iniciarSesion(String username, String password) {
        String nombreUsuario = Usuario.normalizarUsername(username);
        Optional<SesionIniciada> sesionIniciada = transacciones.enTransaccion(conexion ->
                autenticar(conexion, nombreUsuario, password));
        if (sesionIniciada.isPresent()) {
            return sesionIniciada.get();
        }
        registrarIntentoFallido(nombreUsuario);
        throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES);
    }

    public Optional<UsuarioAutenticado> validarSesion(long idUsuario, int versionToken) {
        return transacciones.consultar(conexion -> usuarioDao.buscarPorId(conexion, idUsuario))
                .filter(usuario -> usuario.sesionVigente(versionToken))
                .map(UsuarioAutenticado::de);
    }

    public Usuario perfil() {
        long id = sesion.actual().id();
        return transacciones.consultar(conexion -> usuarioDao.buscarPorId(conexion, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id)));
    }

    public SesionIniciada cambiarPassword(String passwordActual, String passwordNueva) {
        long id = sesion.actual().id();
        String hashNuevo = codificador.encode(passwordNueva);
        return transacciones.enTransaccion(conexion -> {
            Usuario usuario = usuarioDao.bloquearPorId(conexion, id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
            if (!codificador.matches(passwordActual, usuario.getPasswordHash())) {
                throw new DatoInvalidoException("La contraseña actual no es correcta");
            }
            if (codificador.matches(passwordNueva, usuario.getPasswordHash())) {
                throw new DatoInvalidoException("La nueva contraseña debe ser distinta de la actual");
            }
            usuario.cambiarPassword(hashNuevo);
            usuarioDao.actualizar(conexion, usuario);
            auditoria.registrar(conexion, Accion.CAMBIO_PASSWORD, EntidadBitacora.USUARIO, id,
                    "Cambio de contraseña propia");
            return new SesionIniciada(emisorToken.emitir(usuario), usuario);
        });
    }

    private Optional<SesionIniciada> autenticar(Connection conexion, String username, String password)
            throws SQLException {
        Optional<Usuario> candidato = usuarioDao.buscarPorUsername(conexion, username);
        if (candidato.isEmpty()) {
            codificador.matches(password, hashReferencia);
            return Optional.empty();
        }
        Usuario usuario = candidato.get();
        if (!codificador.matches(password, usuario.getPasswordHash()) || !usuario.estaActivo()) {
            return Optional.empty();
        }
        usuario.registrarAcceso();
        usuarioDao.actualizarUltimoAcceso(conexion, usuario);
        auditoria.registrarComo(conexion, usuario.getId(), usuario.getUsername(), Accion.INICIO_SESION,
                EntidadBitacora.SESION, usuario.getId(), "Inicio de sesión como " + usuario.getRol());
        return Optional.of(new SesionIniciada(emisorToken.emitir(usuario), usuario));
    }

    private void registrarIntentoFallido(String username) {
        String usuario = username.isBlank() ? USUARIO_VACIO : recortar(username);
        transacciones.ejecutarEnTransaccion(conexion -> auditoria.registrarComo(conexion, null, usuario,
                Accion.INICIO_SESION_FALLIDO, EntidadBitacora.SESION, null, "Credenciales inválidas o usuario inactivo"));
    }

    private static String recortar(String texto) {
        return texto.length() <= LONGITUD_MAXIMA_USUARIO ? texto : texto.substring(0, LONGITUD_MAXIMA_USUARIO);
    }
}
