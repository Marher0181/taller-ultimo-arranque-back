package gt.umg.ultimoarranque.modelo.orden;

import gt.umg.ultimoarranque.excepcion.RecursoNoEncontradoException;
import gt.umg.ultimoarranque.excepcion.RegistroDuplicadoException;
import gt.umg.ultimoarranque.modelo.Entidad;
import gt.umg.ultimoarranque.modelo.FechaHora;
import gt.umg.ultimoarranque.modelo.Mecanico;
import gt.umg.ultimoarranque.modelo.Repuesto;
import gt.umg.ultimoarranque.modelo.Responsable;
import gt.umg.ultimoarranque.modelo.ServicioTaller;
import gt.umg.ultimoarranque.modelo.Validaciones;
import gt.umg.ultimoarranque.modelo.Vehiculo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrdenTrabajo extends Entidad {

    private final Vehiculo vehiculo;
    private Mecanico mecanico;
    private String descripcionProblema;
    private EstadoOrdenTrabajo estado;
    private final LocalDateTime fechaCreacion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFinalizacion;
    private LocalDateTime fechaCancelacion;
    private String motivoCancelacion;
    private final List<DetalleServicio> servicios;
    private final List<DetalleRepuesto> repuestos;
    private Comprobante comprobante;
    private final Responsable registradaPor;
    private Responsable iniciadaPor;
    private Responsable cerradaPor;

    private OrdenTrabajo(DatosOrden datos, Vehiculo vehiculo, Mecanico mecanico,
                         List<DetalleServicio> servicios, List<DetalleRepuesto> repuestos, Comprobante comprobante) {
        super(datos.id());
        this.vehiculo = Validaciones.obligatorio(vehiculo, "vehículo");
        establecerDatos(mecanico, datos.descripcionProblema());
        this.estado = EstadoOrdenTrabajo.desde(Validaciones.obligatorio(datos.estado(), "estado"));
        this.fechaCreacion = Validaciones.obligatorio(datos.fechaCreacion(), "fecha de creación");
        this.fechaInicio = datos.fechaInicio();
        this.fechaFinalizacion = datos.fechaFinalizacion();
        this.fechaCancelacion = datos.fechaCancelacion();
        this.motivoCancelacion = datos.motivoCancelacion();
        this.servicios = new ArrayList<>(servicios);
        this.repuestos = new ArrayList<>(repuestos);
        this.comprobante = comprobante;
        this.registradaPor = Validaciones.obligatorio(datos.registradaPor(), "usuario que registra la orden");
        this.iniciadaPor = datos.iniciadaPor();
        this.cerradaPor = datos.cerradaPor();
    }

    public static OrdenTrabajo nueva(Vehiculo vehiculo, Mecanico mecanico, String descripcionProblema,
                                     Responsable registradaPor) {
        DatosOrden datos = new DatosOrden(null, descripcionProblema, EstadoOrden.PENDIENTE,
                FechaHora.ahora(), null, null, null, null, registradaPor, null, null);
        return new OrdenTrabajo(datos, vehiculo, mecanico, List.of(), List.of(), null);
    }

    public static OrdenTrabajo reconstruir(DatosOrden datos, Vehiculo vehiculo, Mecanico mecanico,
                                           List<DetalleServicio> servicios, List<DetalleRepuesto> repuestos,
                                           Comprobante comprobante) {
        return new OrdenTrabajo(datos, vehiculo, mecanico, servicios, repuestos, comprobante);
    }

    public void actualizarDatos(Mecanico mecanico, String descripcionProblema) {
        estado.actualizarDatos(this, mecanico, descripcionProblema);
    }

    public DetalleServicio agregarServicio(ServicioTaller servicio) {
        return estado.agregarServicio(this, servicio);
    }

    public DetalleServicio quitarServicio(long idServicio) {
        return estado.quitarServicio(this, idServicio);
    }

    public void iniciar(Responsable responsable) {
        estado.iniciar(this, Validaciones.obligatorio(responsable, "responsable"));
    }

    public DetalleRepuesto asignarRepuesto(Repuesto repuesto, int cantidad) {
        return estado.asignarRepuesto(this, repuesto, cantidad);
    }

    public DetalleRepuesto retirarRepuesto(long idDetalle, Repuesto repuesto) {
        return estado.retirarRepuesto(this, idDetalle, repuesto);
    }

    public Comprobante finalizar(BigDecimal tasaIva, Responsable responsable) {
        return estado.finalizar(this, Validaciones.obligatorio(tasaIva, "tasa de IVA"),
                Validaciones.obligatorio(responsable, "responsable"));
    }

    public void cancelar(String motivo, Map<Long, Repuesto> inventario, Responsable responsable) {
        estado.cancelar(this, motivo, inventario, Validaciones.obligatorio(responsable, "responsable"));
    }

    public boolean estaAsignadaA(Long idMecanico) {
        return idMecanico != null && idMecanico.equals(mecanico.getId());
    }

    public ResumenCostos calcularCostos(BigDecimal tasaIva) {
        return ResumenCostos.calcular(servicios, repuestos, tasaIva);
    }

    public DetalleRepuesto obtenerDetalleRepuesto(long idDetalle) {
        return repuestos.stream()
                .filter(detalle -> !detalle.esNueva() && detalle.getId() == idDetalle)
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Repuesto asignado a la orden", idDetalle));
    }

    void modificarDatos(Mecanico mecanico, String descripcionProblema) {
        establecerDatos(mecanico, descripcionProblema);
    }

    private void establecerDatos(Mecanico mecanico, String descripcionProblema) {
        this.mecanico = Validaciones.obligatorio(mecanico, "mecánico");
        this.descripcionProblema = Validaciones.requerido(descripcionProblema, "descripción del problema");
    }

    DetalleServicio incorporarServicio(ServicioTaller servicio) {
        Validaciones.obligatorio(servicio, "servicio");
        boolean registrado = servicios.stream().anyMatch(detalle -> detalle.corresponde(servicio.getId()));
        if (registrado) {
            throw new RegistroDuplicadoException("El servicio '" + servicio.getDescripcion() + "' ya está registrado en la orden");
        }
        DetalleServicio detalle = new DetalleServicio(servicio, servicio.getCostoManoObra(), FechaHora.ahora());
        servicios.add(detalle);
        return detalle;
    }

    DetalleServicio removerServicio(long idServicio) {
        DetalleServicio detalle = servicios.stream()
                .filter(registrado -> registrado.corresponde(idServicio))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio registrado en la orden", idServicio));
        servicios.remove(detalle);
        return detalle;
    }

    DetalleRepuesto incorporarRepuesto(Repuesto repuesto, int cantidad) {
        Optional<DetalleRepuesto> existente = repuestos.stream()
                .filter(detalle -> detalle.admiteAcumular(repuesto))
                .findFirst();
        if (existente.isPresent()) {
            existente.get().aumentar(cantidad);
            return existente.get();
        }
        DetalleRepuesto nuevo = new DetalleRepuesto(null, repuesto, cantidad, repuesto.getPrecio(), FechaHora.ahora());
        repuestos.add(nuevo);
        return nuevo;
    }

    void removerRepuesto(DetalleRepuesto detalle) {
        repuestos.remove(detalle);
    }

    void registrarInicio(Responsable responsable) {
        fechaInicio = FechaHora.ahora();
        iniciadaPor = responsable;
        estado = new EstadoEnProceso();
    }

    Comprobante registrarFinalizacion(BigDecimal tasaIva, Responsable responsable) {
        LocalDateTime ahora = FechaHora.ahora();
        cerradaPor = responsable;
        comprobante = Comprobante.emitir(this, calcularCostos(tasaIva), ahora);
        fechaFinalizacion = ahora;
        estado = new EstadoFinalizada();
        return comprobante;
    }

    void registrarCancelacion(String motivo, Responsable responsable) {
        motivoCancelacion = Validaciones.opcional(motivo);
        cerradaPor = responsable;
        fechaCancelacion = FechaHora.ahora();
        estado = new EstadoCancelada();
    }

    public EstadoOrden getEstado() {
        return estado.codigo();
    }

    public boolean admiteModificaciones() {
        return estado.admiteModificaciones();
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public Mecanico getMecanico() {
        return mecanico;
    }

    public String getDescripcionProblema() {
        return descripcionProblema;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public List<DetalleServicio> getServicios() {
        return Collections.unmodifiableList(servicios);
    }

    public List<DetalleRepuesto> getRepuestos() {
        return Collections.unmodifiableList(repuestos);
    }

    public Responsable getRegistradaPor() {
        return registradaPor;
    }

    public Responsable getIniciadaPor() {
        return iniciadaPor;
    }

    public Responsable getCerradaPor() {
        return cerradaPor;
    }

    public Optional<Comprobante> getComprobante() {
        return Optional.ofNullable(comprobante);
    }
}
