package gt.umg.ultimoarranque.modelo;

public record Responsable(Long idUsuario, String username, String nombre) {

    public String descripcion() {
        return nombre + " (" + username + ")";
    }
}
