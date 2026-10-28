package gt.umg.ultimoarranque.controlador.web;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Geometría del gráfico de barras SVG (misma escala y medidas que {@code grafico-barras.component.ts}). */
public record GraficoBarras(String viewBox, List<Marca> marcas, List<Barra> barras) {

    private static final double ALTO = 180;
    private static final double MARGEN_IZQ = 64;
    private static final double MARGEN_INF = 24;
    private static final double MARGEN_SUP = 8;
    private static final double ANCHO = 640;

    public record Entrada(String etiqueta, BigDecimal valor, String detalle) {
    }

    public record Marca(double y, String texto, boolean base) {
    }

    public record Barra(double x, double y, double ancho, double alto, double xTexto, double yTexto, String etiqueta,
                        String detalle) {
    }

    public static GraficoBarras de(List<Entrada> entradas) {
        double maximo = entradas.stream().mapToDouble(e -> e.valor().doubleValue()).max().orElse(0);
        List<Double> escala = escala(Math.max(0, maximo));
        double tope = escala.getLast();
        List<Marca> marcas = escala.stream().map(v -> new Marca(y(v, tope), abreviar(v), v == 0)).toList();
        double espacio = (ANCHO - MARGEN_IZQ) / Math.max(1, entradas.size());
        double anchoBarra = Math.min(56, espacio * 0.6);
        List<Barra> barras = new ArrayList<>();
        for (int i = 0; i < entradas.size(); i++) {
            Entrada entrada = entradas.get(i);
            double x = MARGEN_IZQ + i * espacio + (espacio - anchoBarra) / 2;
            double y = y(entrada.valor().doubleValue(), tope);
            barras.add(new Barra(x, y, anchoBarra, ALTO + MARGEN_SUP - y, x + anchoBarra / 2, ALTO + MARGEN_SUP + 18,
                    entrada.etiqueta(), entrada.detalle()));
        }
        return new GraficoBarras("0 0 " + (int) ANCHO + " " + (int) (ALTO + MARGEN_INF + MARGEN_SUP), marcas, barras);
    }

    public double margenIzq() {
        return MARGEN_IZQ;
    }

    public double ancho() {
        return ANCHO;
    }

    private static double y(double valor, double tope) {
        return MARGEN_SUP + ALTO - (valor / tope) * ALTO;
    }

    private static List<Double> escala(double maximo) {
        double tope = maximo <= 0 ? 1 : maximo;
        double crudo = tope / 4;
        double magnitud = Math.pow(10, Math.floor(Math.log10(crudo)));
        double paso = 10 * magnitud;
        for (double m : new double[]{1, 2, 2.5, 5, 10}) {
            if (m * magnitud >= crudo) {
                paso = m * magnitud;
                break;
            }
        }
        double alto = Math.ceil(tope / paso) * paso;
        if (alto <= tope) {
            alto += paso;
        }
        List<Double> resultado = new ArrayList<>();
        for (double v = 0; v <= alto + paso / 2; v += paso) {
            resultado.add(v);
        }
        return resultado;
    }

    private static String abreviar(double valor) {
        DecimalFormatSymbols simbolos = DecimalFormatSymbols.getInstance(Locale.US);
        return valor >= 1000
                ? "Q" + new DecimalFormat("#,##0.#", simbolos).format(valor / 1000) + " mil"
                : "Q" + new DecimalFormat("#,##0", simbolos).format(valor);
    }
}
