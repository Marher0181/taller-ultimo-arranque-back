package gt.umg.ultimoarranque.modelo;

public enum Rol {
    ADMIN("Administrador"),
    RECEPCION("Recepción"),
    MECANICO("Mecánico");

    private static final String PREFIJO_AUTORIDAD = "ROLE_";

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String autoridad() {
        return PREFIJO_AUTORIDAD + name();
    }
}
