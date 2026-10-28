package gt.umg.ultimoarranque.servicio;

import gt.umg.ultimoarranque.dao.BitacoraDao;
import gt.umg.ultimoarranque.modelo.bitacora.Accion;
import gt.umg.ultimoarranque.modelo.bitacora.EntidadBitacora;
import gt.umg.ultimoarranque.modelo.bitacora.EntradaBitacora;
import gt.umg.ultimoarranque.seguridad.SesionUsuario;
import gt.umg.ultimoarranque.seguridad.UsuarioAutenticado;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class Auditoria {

    private final BitacoraDao bitacoraDao;
    private final SesionUsuario sesion;

    public Auditoria(BitacoraDao bitacoraDao, SesionUsuario sesion) {
        this.bitacoraDao = bitacoraDao;
        this.sesion = sesion;
    }

    public void registrar(Connection conexion, Accion accion, EntidadBitacora entidad, Long idEntidad, String detalle)
            throws SQLException {
        UsuarioAutenticado usuario = sesion.actual();
        registrarComo(conexion, usuario.id(), usuario.username(), accion, entidad, idEntidad, detalle);
    }

    public void registrarComo(Connection conexion, Long idUsuario, String usuario, Accion accion,
                              EntidadBitacora entidad, Long idEntidad, String detalle) throws SQLException {
        bitacoraDao.insertar(conexion, EntradaBitacora.nueva(idUsuario, usuario, accion, entidad, idEntidad, detalle,
                sesion.direccionIp()));
    }
}
