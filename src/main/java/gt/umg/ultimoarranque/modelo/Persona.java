package gt.umg.ultimoarranque.modelo;

public abstract class Persona extends Entidad {

    private String nombre;
    private String telefono;

    protected Persona(Long id, String nombre, String telefono) {
        super(id);
        setNombre(nombre);
        setTelefono(telefono);
    }

    public abstract String identificacion();

    public abstract String rol();

    public String presentacion() {
        return String.format("%s: %s (%s)", rol(), nombre, identificacion());
    }

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        this.nombre = Validaciones.requerido(nombre, "nombre");
    }

    public String getTelefono() {
        return telefono;
    }

    public final void setTelefono(String telefono) {
        this.telefono = Validaciones.opcional(telefono);
    }
}
