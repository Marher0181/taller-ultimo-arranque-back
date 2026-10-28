package gt.umg.ultimoarranque.controlador.web;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Aviso flotante (toast) que se muestra en la siguiente página tras un redirect. */
public record Aviso(String tipo, String titulo, String detalle) {

    public static final String ATRIBUTO = "aviso";

    public static void exito(RedirectAttributes atributos, String titulo, String detalle) {
        atributos.addFlashAttribute(ATRIBUTO, new Aviso("exito", titulo, detalle));
    }

    public static void exito(RedirectAttributes atributos, String titulo) {
        exito(atributos, titulo, null);
    }
}
