package gt.umg.ultimoarranque.modelo;

import gt.umg.ultimoarranque.excepcion.DatoInvalidoException;

import java.util.regex.Pattern;

public class Vehiculo extends Entidad {

    private static final Pattern FORMATO_PLACA = Pattern.compile("[A-Z]{1,3}\\d{3}[A-Z]{3}");
    private static final int ANIO_MINIMO = 1900;
    private static final int ANIO_MAXIMO = 2100;

    private String placa;
    private String marca;
    private String modelo;
    private int anio;
    private String color;
    private Cliente propietario;

    public Vehiculo(Long id, String placa, String marca, String modelo, int anio, String color, Cliente propietario) {
        super(id);
        actualizar(placa, marca, modelo, anio, color, propietario);
    }

    public static String normalizarPlaca(String placa) {
        return Validaciones.requerido(placa, "placa").replaceAll("[\\s-]", "").toUpperCase();
    }

    public final void actualizar(String placa, String marca, String modelo, int anio, String color, Cliente propietario) {
        setPlaca(placa);
        this.marca = Validaciones.requerido(marca, "marca");
        this.modelo = Validaciones.requerido(modelo, "modelo");
        setAnio(anio);
        this.color = Validaciones.opcional(color);
        this.propietario = Validaciones.obligatorio(propietario, "cliente propietario");
    }

    public String descripcion() {
        return String.format("%s %s %d", marca, modelo, anio);
    }

    private void setPlaca(String placa) {
        String normalizada = normalizarPlaca(placa);
        if (!FORMATO_PLACA.matcher(normalizada).matches()) {
            throw new DatoInvalidoException("La placa '" + placa + "' no tiene un formato válido (ejemplo: P123ABC)");
        }
        this.placa = normalizada;
    }

    private void setAnio(int anio) {
        if (anio < ANIO_MINIMO || anio > ANIO_MAXIMO) {
            throw new DatoInvalidoException("El año del vehículo debe estar entre " + ANIO_MINIMO + " y " + ANIO_MAXIMO);
        }
        this.anio = anio;
    }

    public String getPlaca() {
        return placa;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public int getAnio() {
        return anio;
    }

    public String getColor() {
        return color;
    }

    public Cliente getPropietario() {
        return propietario;
    }
}
