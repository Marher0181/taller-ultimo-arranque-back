package gt.umg.ultimoarranque.modelo;

import gt.umg.ultimoarranque.modelo.documento.DocumentoIdentidad;

import java.time.LocalDateTime;

public class Cliente extends Persona {

    private DocumentoIdentidad documento;
    private String correo;
    private String direccion;
    private final LocalDateTime fechaRegistro;

    public Cliente(Long id, DocumentoIdentidad documento, String nombre, String telefono,
                   String correo, String direccion, LocalDateTime fechaRegistro) {
        super(id, nombre, telefono);
        setDocumento(documento);
        setCorreo(correo);
        setDireccion(direccion);
        this.fechaRegistro = Validaciones.obligatorio(fechaRegistro, "fecha de registro");
    }

    public static Cliente nuevo(DocumentoIdentidad documento, String nombre, String telefono,
                                String correo, String direccion) {
        return new Cliente(null, documento, nombre, telefono, correo, direccion, FechaHora.ahora());
    }

    public void actualizar(DocumentoIdentidad documento, String nombre, String telefono,
                           String correo, String direccion) {
        setDocumento(documento);
        setNombre(nombre);
        setTelefono(telefono);
        setCorreo(correo);
        setDireccion(direccion);
    }

    @Override
    public String identificacion() {
        return documento.toString();
    }

    @Override
    public String rol() {
        return "Cliente";
    }

    public DocumentoIdentidad getDocumento() {
        return documento;
    }

    public final void setDocumento(DocumentoIdentidad documento) {
        this.documento = Validaciones.obligatorio(documento, "documento de identificación");
    }

    public String getCorreo() {
        return correo;
    }

    public final void setCorreo(String correo) {
        this.correo = Validaciones.opcional(correo);
    }

    public String getDireccion() {
        return direccion;
    }

    public final void setDireccion(String direccion) {
        this.direccion = Validaciones.opcional(direccion);
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
