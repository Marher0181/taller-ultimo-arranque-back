package gt.umg.ultimoarranque.seguridad;

import gt.umg.ultimoarranque.modelo.Responsable;
import gt.umg.ultimoarranque.modelo.Rol;
import gt.umg.ultimoarranque.modelo.Usuario;

import java.security.Principal;

public record UsuarioAutenticado(long id, String username, String nombre, Rol rol, Long idMecanico) implements Principal {

    public static UsuarioAutenticado de(Usuario usuario) {
        return new UsuarioAutenticado(usuario.getId(), usuario.getUsername(), usuario.getNombre(), usuario.getRol(),
                usuario.getIdMecanico());
    }

    public boolean esMecanico() {
        return rol == Rol.MECANICO;
    }

    public Responsable comoResponsable() {
        return new Responsable(id, username, nombre);
    }

    @Override
    public String getName() {
        return username;
    }
}
