package gt.umg.ultimoarranque.modelo.orden;

import gt.umg.ultimoarranque.modelo.Validaciones;

import java.time.LocalDateTime;

public class Comprobante {

    private static final String FORMATO_NUMERO = "CMP-%06d";

    private final String numero;
    private final LocalDateTime fechaEmision;
    private final ResumenCostos costos;

    public Comprobante(String numero, LocalDateTime fechaEmision, ResumenCostos costos) {
        this.numero = Validaciones.requerido(numero, "número de comprobante");
        this.fechaEmision = Validaciones.obligatorio(fechaEmision, "fecha de emisión");
        this.costos = Validaciones.obligatorio(costos, "costos");
    }

    static Comprobante emitir(OrdenTrabajo orden, ResumenCostos costos, LocalDateTime fechaEmision) {
        if (orden.esNueva()) {
            throw new IllegalStateException("La orden debe estar registrada para emitir su comprobante");
        }
        return new Comprobante(String.format(FORMATO_NUMERO, orden.getId()), fechaEmision, costos);
    }

    public String getNumero() {
        return numero;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public ResumenCostos getCostos() {
        return costos;
    }
}
