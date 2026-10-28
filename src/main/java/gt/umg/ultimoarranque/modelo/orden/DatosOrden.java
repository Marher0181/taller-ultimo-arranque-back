package gt.umg.ultimoarranque.modelo.orden;

import gt.umg.ultimoarranque.modelo.Responsable;

import java.time.LocalDateTime;

public record DatosOrden(
        Long id,
        String descripcionProblema,
        EstadoOrden estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFinalizacion,
        LocalDateTime fechaCancelacion,
        String motivoCancelacion,
        Responsable registradaPor,
        Responsable iniciadaPor,
        Responsable cerradaPor) {
}
