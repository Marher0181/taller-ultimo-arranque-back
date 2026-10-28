package gt.umg.ultimoarranque.modelo;

public class Mecanico extends Persona {

    private String codigo;
    private String especialidad;

    public Mecanico(Long id, String codigo, String nombre, String especialidad, String telefono) {
        super(id, nombre, telefono);
        setCodigo(codigo);
        setEspecialidad(especialidad);
    }

    public void actualizar(String codigo, String nombre, String especialidad, String telefono) {
        setCodigo(codigo);
        setNombre(nombre);
        setEspecialidad(especialidad);
        setTelefono(telefono);
    }

    @Override
    public String identificacion() {
        return "Código " + codigo;
    }

    @Override
    public String rol() {
        return "Mecánico";
    }

    public String getCodigo() {
        return codigo;
    }

    public final void setCodigo(String codigo) {
        this.codigo = Validaciones.requerido(codigo, "código").toUpperCase();
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public final void setEspecialidad(String especialidad) {
        this.especialidad = Validaciones.opcional(especialidad);
    }
}
