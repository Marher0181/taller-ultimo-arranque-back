package gt.umg.ultimoarranque.modelo;

import java.math.BigDecimal;

public class ServicioTaller extends Entidad {

    private String codigo;
    private String descripcion;
    private BigDecimal costoManoObra;

    public ServicioTaller(Long id, String codigo, String descripcion, BigDecimal costoManoObra) {
        super(id);
        actualizar(codigo, descripcion, costoManoObra);
    }

    public final void actualizar(String codigo, String descripcion, BigDecimal costoManoObra) {
        this.codigo = Validaciones.requerido(codigo, "código").toUpperCase();
        this.descripcion = Validaciones.requerido(descripcion, "descripción");
        this.costoManoObra = Validaciones.monto(costoManoObra, "costo de mano de obra");
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getCostoManoObra() {
        return costoManoObra;
    }
}
