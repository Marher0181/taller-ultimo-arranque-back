package gt.umg.ultimoarranque.modelo;

import gt.umg.ultimoarranque.excepcion.InventarioInsuficienteException;

import java.math.BigDecimal;

public class Repuesto extends Entidad {

    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private int existencia;

    public Repuesto(Long id, String codigo, String nombre, String descripcion, BigDecimal precio, int existencia) {
        super(id);
        actualizar(codigo, nombre, descripcion, precio);
        this.existencia = Validaciones.noNegativo(existencia, "existencia");
    }

    public final void actualizar(String codigo, String nombre, String descripcion, BigDecimal precio) {
        this.codigo = Validaciones.requerido(codigo, "código").toUpperCase();
        this.nombre = Validaciones.requerido(nombre, "nombre");
        this.descripcion = Validaciones.opcional(descripcion);
        this.precio = Validaciones.monto(precio, "precio");
    }

    public void abastecer(int cantidad) {
        Validaciones.positivo(cantidad, "cantidad a abastecer");
        existencia = Math.addExact(existencia, cantidad);
    }

    public void descontar(int cantidad) {
        Validaciones.positivo(cantidad, "cantidad");
        if (cantidad > existencia) {
            throw new InventarioInsuficienteException(nombre, existencia, cantidad);
        }
        existencia -= cantidad;
    }

    public void reintegrar(int cantidad) {
        Validaciones.positivo(cantidad, "cantidad a reintegrar");
        existencia = Math.addExact(existencia, cantidad);
    }

    public NivelInventario nivelInventario(int umbralBajo) {
        return NivelInventario.evaluar(existencia, umbralBajo);
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public int getExistencia() {
        return existencia;
    }
}
