package gt.umg.ultimoarranque.modelo;

import gt.umg.ultimoarranque.excepcion.DatoInvalidoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    private static final String HASH = "$2a$10$hash";

    private final Mecanico mecanico = new Mecanico(1L, "MEC-001", "Luis Ramírez", "Motor", null);

    @Test
    void normalizaUsernameYPresentaRol() {
        Usuario usuario = Usuario.nuevo("  LRamirez ", "Luis Ramírez", HASH, Rol.MECANICO, mecanico);
        assertEquals("lramirez", usuario.getUsername());
        assertEquals("Mecánico: Luis Ramírez (usuario lramirez)", usuario.presentacion());
        assertEquals(1L, usuario.getIdMecanico());
    }

    @Test
    void rolMecanicoRequiereMecanicoVinculado() {
        assertThrows(DatoInvalidoException.class, () -> Usuario.nuevo("mec", "Mecánico", HASH, Rol.MECANICO, null));
        assertThrows(DatoInvalidoException.class, () -> Usuario.nuevo("rec", "Recepción", HASH, Rol.RECEPCION, mecanico));
    }

    @Test
    void rechazaUsernameInvalido() {
        assertThrows(DatoInvalidoException.class, () -> Usuario.nuevo("a b", "X", HASH, Rol.ADMIN, null));
    }

    @Test
    void cambiarPasswordInvalidaSesionesAnteriores() {
        Usuario usuario = Usuario.nuevo("admin", "Admin", HASH, Rol.ADMIN, null);
        assertTrue(usuario.sesionVigente(0));
        usuario.cambiarPassword("$2a$10$otro");
        assertFalse(usuario.sesionVigente(0));
        assertTrue(usuario.sesionVigente(1));
    }

    @Test
    void desactivarInvalidaSesionYCambioDeRolTambien() {
        Usuario usuario = Usuario.nuevo("recepcion", "Recepción", HASH, Rol.RECEPCION, null);
        usuario.asignarRol(Rol.RECEPCION, null);
        assertTrue(usuario.sesionVigente(0));
        usuario.asignarRol(Rol.ADMIN, null);
        assertFalse(usuario.sesionVigente(0));
        usuario.desactivar();
        assertFalse(usuario.sesionVigente(usuario.getVersionToken()));
        usuario.activar();
        assertTrue(usuario.sesionVigente(usuario.getVersionToken()));
    }
}
