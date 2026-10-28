package gt.umg.ultimoarranque.controlador.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gt.umg.ultimoarranque.modelo.NivelInventario;
import gt.umg.ultimoarranque.modelo.Rol;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import gt.umg.ultimoarranque.seguridad.UsuarioAutenticado;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Funciones de presentación usadas por las plantillas como {@code ${vista...}}: los mismos formatos (es-GT), etiquetas,
 * íconos y permisos que tenía el front Angular, para que la interfaz se vea igual.
 */
@Component
public class FormatoVista {

    private static final String SIN_VALOR = "—";
    private static final String ESPACIO = " ";
    private static final String[] MESES_CORTOS = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sept", "oct", "nov", "dic"};
    private static final String[] MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto",
            "septiembre", "octubre", "noviembre", "diciembre"};

    private static final Map<String, String> ACCIONES = Map.ofEntries(
            Map.entry("INICIO_SESION", "Inició sesión"),
            Map.entry("INICIO_SESION_FALLIDO", "Intento de sesión fallido"),
            Map.entry("CAMBIO_PASSWORD", "Cambió su contraseña"),
            Map.entry("RESTABLECIMIENTO_PASSWORD", "Restableció una contraseña"),
            Map.entry("CREAR", "Creó"),
            Map.entry("MODIFICAR", "Modificó"),
            Map.entry("ELIMINAR", "Eliminó"),
            Map.entry("ACTIVAR", "Activó"),
            Map.entry("DESACTIVAR", "Desactivó"),
            Map.entry("ABASTECER", "Abasteció"),
            Map.entry("AGREGAR_SERVICIO", "Agregó un servicio"),
            Map.entry("QUITAR_SERVICIO", "Quitó un servicio"),
            Map.entry("INICIAR_ORDEN", "Inició la reparación"),
            Map.entry("ASIGNAR_REPUESTO", "Asignó un repuesto"),
            Map.entry("RETIRAR_REPUESTO", "Retiró un repuesto"),
            Map.entry("FINALIZAR_ORDEN", "Finalizó la orden"),
            Map.entry("CANCELAR_ORDEN", "Canceló la orden"));

    private static final Map<String, String> ENTIDADES = Map.of(
            "SESION", "Sesión", "USUARIO", "Usuario", "CLIENTE", "Cliente", "VEHICULO", "Vehículo",
            "MECANICO", "Mecánico", "REPUESTO", "Repuesto", "SERVICIO", "Servicio", "ORDEN", "Orden");

    private static final Map<String, String> CLASE_PILL = Map.of(
            "PENDIENTE", "warn", "EN_PROCESO", "accent", "FINALIZADA", "pos", "CANCELADA", "neutral",
            "AGOTADO", "neg", "BAJO", "warn", "DISPONIBLE", "neutral");

    private static final Map<NivelInventario, String> NIVELES = Map.of(
            NivelInventario.AGOTADO, "Agotado", NivelInventario.BAJO, "Bajo", NivelInventario.DISPONIBLE, "Disponible");

    private static final Map<String, String> ICONOS = Map.ofEntries(
            Map.entry("inicio", "M3 10.5 12 3l9 7.5M5.5 9v11h13V9"),
            Map.entry("ordenes", "M9 3.5h6v3H9zM7 5H5.5v15.5h13V5H17M8.5 11.5h7M8.5 15.5h5"),
            Map.entry("clientes", "M15.5 20v-1.5a4 4 0 0 0-4-4h-5a4 4 0 0 0-4 4V20M9 11a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7zM21.5 20v-1.5a4 4 0 0 0-3-3.9M15.5 4.1a3.5 3.5 0 0 1 0 6.8"),
            Map.entry("vehiculos", "M3.5 13 5.5 7h13l2 6v5h-17zM3.5 13h17M7 18v2M17 18v2M7.5 15.5h.01M16.5 15.5h.01"),
            Map.entry("mecanicos", "M14.7 6.3a4 4 0 0 0-5.4 5.4L3.5 17.5l3 3 5.8-5.8a4 4 0 0 0 5.4-5.4l-2.6 2.6-2.4-.6-.6-2.4z"),
            Map.entry("repuestos", "M20.5 7.5 12 3 3.5 7.5 12 12zM3.5 7.5v9L12 21l8.5-4.5v-9M12 12v9"),
            Map.entry("servicios", "M9 6h11M9 12h11M9 18h11M4.5 6h.01M4.5 12h.01M4.5 18h.01"),
            Map.entry("reportes", "M4 20V11M10 20V4M16 20v-6M21 20H3"),
            Map.entry("usuarios", "M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6zM9.5 12l2 2 3.5-4"),
            Map.entry("bitacora", "M12 7v5l3 2M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z"),
            Map.entry("perfil", "M19.5 20.5a7.5 7.5 0 0 0-15 0M12 12.5a4.5 4.5 0 1 0 0-9 4.5 4.5 0 0 0 0 9z"),
            Map.entry("menu", "M4 7h16M4 12h16M4 17h16"),
            Map.entry("cerrar", "M6 6l12 12M18 6 6 18"),
            Map.entry("buscar", "M11 18a7 7 0 1 0 0-14 7 7 0 0 0 0 14zM20 20l-4-4"),
            Map.entry("descargar", "M12 4v11M7.5 10.5 12 15l4.5-4.5M5 19.5h14"),
            Map.entry("abajo", "M6 9l6 6 6-6"),
            Map.entry("salir", "M15 4h3.5A1.5 1.5 0 0 1 20 5.5v13a1.5 1.5 0 0 1-1.5 1.5H15M10 16l-4-4 4-4M6 12h10"),
            Map.entry("atras", "M15 18l-6-6 6-6"),
            Map.entry("hecho", "M5.5 12.5l4 4 9-9.5"));

    /** Misma matriz que {@code modelo/permisos.ts} del front; la seguridad real la aplica el backend. */
    private static final Map<Rol, Set<String>> PERMISOS = Map.of(
            Rol.ADMIN, Set.of("clientes", "vehiculos", "mecanicos", "repuestos.ver", "repuestos.editar", "servicios.ver",
                    "servicios.editar", "ordenes.ver", "ordenes.crear", "ordenes.editar", "ordenes.cancelar",
                    "ordenes.servicios", "ordenes.trabajar", "reportes", "usuarios", "bitacora"),
            Rol.RECEPCION, Set.of("clientes", "vehiculos", "mecanicos", "repuestos.ver", "repuestos.editar",
                    "servicios.ver", "servicios.editar", "ordenes.ver", "ordenes.crear", "ordenes.editar",
                    "ordenes.cancelar", "ordenes.servicios", "reportes"),
            Rol.MECANICO, Set.of("repuestos.ver", "servicios.ver", "ordenes.ver", "ordenes.servicios", "ordenes.trabajar"));

    private final ObjectMapper json;

    public FormatoVista(ObjectMapper json) {
        this.json = json;
    }

    public String moneda(BigDecimal valor) {
        if (valor == null) {
            return SIN_VALOR;
        }
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return (valor.signum() < 0 ? "-" : "") + "Q" + ESPACIO + formato.format(valor.abs());
    }

    public String fecha(Temporal valor) {
        LocalDate dia = aFecha(valor);
        return dia == null ? SIN_VALOR : "%02d %s %d".formatted(dia.getDayOfMonth(), MESES_CORTOS[dia.getMonthValue() - 1], dia.getYear());
    }

    public String fechaHora(LocalDateTime valor) {
        return valor == null ? SIN_VALOR : fecha(valor) + ", " + hora(valor);
    }

    public String hora(Temporal valor) {
        LocalTime tiempo = valor instanceof LocalDateTime fechaHora ? fechaHora.toLocalTime() : (LocalTime) valor;
        if (tiempo == null) {
            return SIN_VALOR;
        }
        int hora12 = tiempo.getHour() % 12 == 0 ? 12 : tiempo.getHour() % 12;
        String sufijo = tiempo.getHour() < 12 ? "a." + ESPACIO + "m." : "p." + ESPACIO + "m.";
        return "%02d:%02d%s%s".formatted(hora12, tiempo.getMinute(), ESPACIO, sufijo);
    }

    public String diaCorto(LocalDate dia) {
        return dia.getDayOfMonth() + " " + MESES_CORTOS[dia.getMonthValue() - 1];
    }

    public String fechaLarga(LocalDate dia) {
        return dia.getDayOfMonth() + " de " + MESES[dia.getMonthValue() - 1] + " de " + dia.getYear();
    }

    public String mes(LocalDate dia) {
        return MESES[dia.getMonthValue() - 1];
    }

    /** Tasa como porcentaje sin ceros sobrantes: 0.12 → "12". */
    public String porcentaje(BigDecimal tasa) {
        return tasa.movePointRight(2).stripTrailingZeros().toPlainString();
    }

    public String orden(Number id) {
        return "OT-%04d".formatted(id.longValue());
    }

    public String etiqueta(Enum<?> valor) {
        return switch (valor) {
            case EstadoOrden estado -> estado.getEtiqueta();
            case Rol rol -> rol.getEtiqueta();
            case NivelInventario nivel -> NIVELES.get(nivel);
            default -> valor.name();
        };
    }

    /** Clases de {@code app-estado}: pill de color según estado de la orden o nivel de inventario. */
    public String pill(Enum<?> valor) {
        return "pill " + CLASE_PILL.getOrDefault(valor.name(), "neutral");
    }

    public String accion(Enum<?> accion) {
        return ACCIONES.getOrDefault(accion.name(), accion.name());
    }

    public String entidad(Enum<?> entidad) {
        return ENTIDADES.getOrDefault(entidad.name(), entidad.name());
    }

    public String icono(String nombre) {
        return ICONOS.getOrDefault(nombre, "");
    }

    public boolean puede(UsuarioAutenticado usuario, String permiso) {
        return usuario != null && PERMISOS.get(usuario.rol()).contains(permiso);
    }

    /** Valores de una fila para llenar el formulario del diálogo de edición ({@code data-valores}). */
    public String json(Object valor) {
        try {
            return json.writeValueAsString(valor);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo convertir a JSON", ex);
        }
    }

    /** Texto en minúsculas sobre el que filtra la búsqueda de cada tabla ({@code data-buscar}). */
    public String buscable(Object... partes) {
        StringBuilder texto = new StringBuilder();
        for (Object parte : partes) {
            if (parte != null) {
                String valor = parte.toString().toLowerCase(Locale.ROOT);
                texto.append(valor).append(' ').append(valor.replaceAll("[\\s-]", "")).append(' ');
            }
        }
        return texto.toString();
    }

    private static LocalDate aFecha(Temporal valor) {
        return switch (valor) {
            case null -> null;
            case LocalDateTime fechaHora -> fechaHora.toLocalDate();
            case LocalDate dia -> dia;
            default -> throw new IllegalArgumentException("Fecha no soportada: " + valor);
        };
    }
}
