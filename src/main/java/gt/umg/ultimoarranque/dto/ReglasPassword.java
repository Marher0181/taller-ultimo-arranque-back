package gt.umg.ultimoarranque.dto;

public final class ReglasPassword {

    public static final String PATRON = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$";
    public static final String MENSAJE = "debe tener entre 8 y 72 caracteres e incluir letras y números";

    private ReglasPassword() {
    }
}
