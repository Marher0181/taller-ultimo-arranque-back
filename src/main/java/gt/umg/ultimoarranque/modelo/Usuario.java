package gt.umg.ultimoarranque.modelo;

import gt.umg.ultimoarranque.excepcion.DatoInvalidoException;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class Usuario extends Persona {

    private static final Pattern FORMATO_USERNAME = Pattern.compile("[a-z0-9._-]{3,50}");

    private final String username;
    private String passwordHash;
    private Rol rol;
    private Mecanico mecanico;
    private boolean activo;
    private int versionToken;
    private final LocalDateTime fechaCreacion;
    private LocalDateTime ultimoAcceso;

    public Usuario(Long id, String username, String nombre, String passwordHash, Rol rol, Mecanico mecanico,
                   DatosCuenta cuenta) {
        super(id, nombre, null);
        this.username = validarUsername(username);
        this.passwordHash = Validaciones.requerido(passwordHash, "contraseña");
        establecerRol(rol, mecanico);
        DatosCuenta datos = Validaciones.obligatorio(cuenta, "datos de la cuenta");
        this.activo = datos.activo();
        this.versionToken = datos.versionToken();
        this.fechaCreacion = Validaciones.obligatorio(datos.fechaCreacion(), "fecha de creación");
        this.ultimoAcceso = datos.ultimoAcceso();
    }

    public static Usuario nuevo(String username, String nombre, String passwordHash, Rol rol, Mecanico mecanico) {
        return new Usuario(null, username, nombre, passwordHash, rol, mecanico, DatosCuenta.nueva());
    }

    public static String normalizarUsername(String username) {
        return username == null ? "" : username.strip().toLowerCase(Locale.ROOT);
    }

    public void asignarRol(Rol nuevoRol, Mecanico nuevoMecanico) {
        boolean cambia = rol != nuevoRol || !Objects.equals(mecanico, nuevoMecanico);
        establecerRol(nuevoRol, nuevoMecanico);
        if (cambia) {
            invalidarSesiones();
        }
    }

    public void cambiarPassword(String nuevoHash) {
        passwordHash = Validaciones.requerido(nuevoHash, "contraseña");
        invalidarSesiones();
    }

    public void activar() {
        activo = true;
    }

    public void desactivar() {
        activo = false;
        invalidarSesiones();
    }

    public void registrarAcceso() {
        ultimoAcceso = FechaHora.ahora();
    }

    public boolean sesionVigente(int version) {
        return activo && versionToken == version;
    }

    public Responsable comoResponsable() {
        return new Responsable(getId(), username, getNombre());
    }

    @Override
    public String identificacion() {
        return "usuario " + username;
    }

    @Override
    public String rol() {
        return rol.getEtiqueta();
    }

    private void invalidarSesiones() {
        versionToken++;
    }

    private void establecerRol(Rol nuevoRol, Mecanico nuevoMecanico) {
        Validaciones.obligatorio(nuevoRol, "rol");
        if (nuevoRol == Rol.MECANICO && nuevoMecanico == null) {
            throw new DatoInvalidoException("Un usuario con rol MECANICO debe estar vinculado a un mecánico");
        }
        if (nuevoRol != Rol.MECANICO && nuevoMecanico != null) {
            throw new DatoInvalidoException("Solo los usuarios con rol MECANICO se vinculan a un mecánico");
        }
        this.rol = nuevoRol;
        this.mecanico = nuevoMecanico;
    }

    private static String validarUsername(String username) {
        String normalizado = normalizarUsername(username);
        if (!FORMATO_USERNAME.matcher(normalizado).matches()) {
            throw new DatoInvalidoException(
                    "El usuario debe tener de 3 a 50 caracteres: letras, números, punto, guion o guion bajo");
        }
        return normalizado;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public Mecanico getMecanico() {
        return mecanico;
    }

    public Long getIdMecanico() {
        return mecanico == null ? null : mecanico.getId();
    }

    public boolean estaActivo() {
        return activo;
    }

    public int getVersionToken() {
        return versionToken;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }
}
