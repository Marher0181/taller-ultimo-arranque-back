package gt.umg.ultimoarranque.modelo.orden;

import gt.umg.ultimoarranque.excepcion.InventarioInsuficienteException;
import gt.umg.ultimoarranque.excepcion.OperacionNoPermitidaException;
import gt.umg.ultimoarranque.excepcion.RegistroDuplicadoException;
import gt.umg.ultimoarranque.excepcion.ReglaNegocioException;
import gt.umg.ultimoarranque.modelo.Cliente;
import gt.umg.ultimoarranque.modelo.Mecanico;
import gt.umg.ultimoarranque.modelo.Repuesto;
import gt.umg.ultimoarranque.modelo.Responsable;
import gt.umg.ultimoarranque.modelo.ServicioTaller;
import gt.umg.ultimoarranque.modelo.Vehiculo;
import gt.umg.ultimoarranque.modelo.documento.DocumentoIdentidad;
import gt.umg.ultimoarranque.modelo.documento.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdenTrabajoTest {

    private static final BigDecimal IVA = new BigDecimal("0.12");
    private static final Responsable RECEPCION = new Responsable(2L, "recepcion", "Recepción");
    private static final Responsable MECANICO = new Responsable(3L, "lramirez", "Luis Ramírez");

    private OrdenTrabajo orden;
    private ServicioTaller revisionFrenos;
    private Repuesto pastillas;

    @BeforeEach
    void preparar() {
        Cliente cliente = Cliente.nuevo(DocumentoIdentidad.crear(TipoDocumento.NIT, "1234567-9"),
                "Cliente Prueba", null, null, null);
        cliente.asignarId(1);
        Vehiculo vehiculo = new Vehiculo(1L, "P123ABC", "Toyota", "Corolla", 2018, "Gris", cliente);
        Mecanico mecanico = new Mecanico(1L, "MEC-001", "Mecánico Prueba", "Frenos", null);
        revisionFrenos = new ServicioTaller(1L, "SRV-001", "Revisión de frenos", new BigDecimal("200.00"));
        pastillas = new Repuesto(1L, "REP-001", "Pastillas", null, new BigDecimal("85.50"), 5);
        orden = OrdenTrabajo.nueva(vehiculo, mecanico, "Ruido al frenar", RECEPCION);
        orden.asignarId(10);
    }

    @Test
    void nuevaOrdenIniciaPendienteYAdmiteModificaciones() {
        assertEquals(EstadoOrden.PENDIENTE, orden.getEstado());
        assertTrue(orden.admiteModificaciones());
    }

    @Test
    void noPermiteAsignarRepuestosAntesDeIniciar() {
        assertThrows(OperacionNoPermitidaException.class, () -> orden.asignarRepuesto(pastillas, 1));
        assertEquals(5, pastillas.getExistencia());
    }

    @Test
    void asignarRepuestoDescuentaInventario() {
        orden.iniciar(MECANICO);
        orden.asignarRepuesto(pastillas, 2);
        assertEquals(3, pastillas.getExistencia());
        assertEquals(1, orden.getRepuestos().size());
    }

    @Test
    void asignarMasDeLoDisponibleNoDescuenta() {
        orden.iniciar(MECANICO);
        assertThrows(InventarioInsuficienteException.class, () -> orden.asignarRepuesto(pastillas, 6));
        assertEquals(5, pastillas.getExistencia());
        assertTrue(orden.getRepuestos().isEmpty());
    }

    @Test
    void retirarRepuestoReintegraUnidades() {
        orden.iniciar(MECANICO);
        DetalleRepuesto detalle = orden.asignarRepuesto(pastillas, 2);
        detalle.asignarId(99);
        orden.retirarRepuesto(99, pastillas);
        assertEquals(5, pastillas.getExistencia());
        assertTrue(orden.getRepuestos().isEmpty());
    }

    @Test
    void identificaMecanicoAsignado() {
        assertTrue(orden.estaAsignadaA(1L));
        assertFalse(orden.estaAsignadaA(2L));
        assertFalse(orden.estaAsignadaA(null));
    }

    @Test
    void servicioDuplicadoSeRechaza() {
        orden.agregarServicio(revisionFrenos);
        assertThrows(RegistroDuplicadoException.class, () -> orden.agregarServicio(revisionFrenos));
    }

    @Test
    void noPermiteFinalizarSinServicios() {
        orden.iniciar(MECANICO);
        assertThrows(ReglaNegocioException.class, () -> orden.finalizar(IVA, MECANICO));
        assertEquals(EstadoOrden.EN_PROCESO, orden.getEstado());
    }

    @Test
    void finalizarCalculaTotalConIva() {
        orden.agregarServicio(revisionFrenos);
        orden.iniciar(MECANICO);
        orden.asignarRepuesto(pastillas, 2);
        Comprobante comprobante = orden.finalizar(IVA, MECANICO);
        ResumenCostos costos = comprobante.getCostos();
        assertEquals(new BigDecimal("200.00"), costos.manoObra());
        assertEquals(new BigDecimal("171.00"), costos.repuestos());
        assertEquals(new BigDecimal("44.52"), costos.iva());
        assertEquals(new BigDecimal("415.52"), costos.total());
        assertEquals("CMP-000010", comprobante.getNumero());
        assertEquals(EstadoOrden.FINALIZADA, orden.getEstado());
        assertEquals(RECEPCION, orden.getRegistradaPor());
        assertEquals(MECANICO, orden.getIniciadaPor());
        assertEquals(MECANICO, orden.getCerradaPor());
    }

    @Test
    void ordenFinalizadaNoAdmiteModificaciones() {
        orden.agregarServicio(revisionFrenos);
        orden.iniciar(MECANICO);
        orden.finalizar(IVA, MECANICO);
        assertFalse(orden.admiteModificaciones());
        assertThrows(OperacionNoPermitidaException.class, () -> orden.asignarRepuesto(pastillas, 1));
        assertThrows(OperacionNoPermitidaException.class, () -> orden.quitarServicio(1));
        assertThrows(OperacionNoPermitidaException.class, () -> orden.cancelar(null, Map.of(), RECEPCION));
    }

    @Test
    void cancelarReintegraRepuestosAsignados() {
        orden.iniciar(MECANICO);
        orden.asignarRepuesto(pastillas, 4);
        orden.cancelar("Cliente desistió", Map.of(pastillas.getId(), pastillas), RECEPCION);
        assertEquals(5, pastillas.getExistencia());
        assertEquals(EstadoOrden.CANCELADA, orden.getEstado());
        assertEquals(RECEPCION, orden.getCerradaPor());
        assertThrows(OperacionNoPermitidaException.class, () -> orden.iniciar(MECANICO));
    }
}
