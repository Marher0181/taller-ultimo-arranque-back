package gt.umg.ultimoarranque.dto;

import gt.umg.ultimoarranque.modelo.Rol;
import gt.umg.ultimoarranque.modelo.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String username,
        String nombre,
        Rol rol,
        Long idMecanico,
        String mecanico,
        boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime ultimoAcceso) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getRol(),
                usuario.getIdMecanico(),
                usuario.getMecanico() == null ? null : usuario.getMecanico().presentacion(),
                usuario.estaActivo(),
                usuario.getFechaCreacion(),
                usuario.getUltimoAcceso());
    }
}
