package gt.umg.ultimoarranque.modelo.orden;

import gt.umg.ultimoarranque.modelo.ServicioTaller;
import gt.umg.ultimoarranque.modelo.Validaciones;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DetalleServicio {

    private final ServicioTaller servicio;
    private final BigDecimal costoManoObra;
    private final LocalDateTime fechaRegistro;

    public DetalleServicio(ServicioTaller servicio, BigDecimal costoManoObra, LocalDateTime fechaRegistro) {
        this.servicio = Validaciones.obligatorio(servicio, "servicio");
        this.costoManoObra = Validaciones.monto(costoManoObra, "costo de mano de obra");
        this.fechaRegistro = Validaciones.obligatorio(fechaRegistro, "fecha de registro");
    }

    boolean corresponde(long idServicio) {
        return servicio.getId() != null && servicio.getId() == idServicio;
    }

    public ServicioTaller getServicio() {
        return servicio;
    }

    public BigDecimal getCostoManoObra() {
        return costoManoObra;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
