package gt.umg.ultimoarranque.seguridad;

public interface SesionUsuario {

    UsuarioAutenticado actual();

    String direccionIp();
}
