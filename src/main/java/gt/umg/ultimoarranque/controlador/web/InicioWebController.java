package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.dto.MecanicoResponse;
import gt.umg.ultimoarranque.dto.RepuestoResponse;
import gt.umg.ultimoarranque.modelo.bitacora.FiltroBitacora;
import gt.umg.ultimoarranque.modelo.consulta.OrdenResumen;
import gt.umg.ultimoarranque.modelo.orden.EstadoOrden;
import gt.umg.ultimoarranque.seguridad.SesionUsuario;
import gt.umg.ultimoarranque.seguridad.UsuarioAutenticado;
import gt.umg.ultimoarranque.servicio.BitacoraService;
import gt.umg.ultimoarranque.servicio.MecanicoService;
import gt.umg.ultimoarranque.servicio.OrdenTrabajoService;
import gt.umg.ultimoarranque.servicio.RepuestoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Login, buscador global y la pantalla "Taller hoy". */
@Controller
public class InicioWebController {

    private static final Pattern NUMERO_ORDEN = Pattern.compile("^(?:OT-?)?0*(\\d+)$", Pattern.CASE_INSENSITIVE);
    private static final int ACTIVIDAD_RECIENTE = 5;
    private static final int RECIENTES = 5;

    private final OrdenTrabajoService ordenService;
    private final RepuestoService repuestoService;
    private final MecanicoService mecanicoService;
    private final BitacoraService bitacoraService;
    private final NuevaOrdenCatalogos nuevaOrden;
    private final FormatoVista vista;
    private final SesionUsuario sesion;

    public InicioWebController(OrdenTrabajoService ordenService, RepuestoService repuestoService,
                               MecanicoService mecanicoService, BitacoraService bitacoraService,
                               NuevaOrdenCatalogos nuevaOrden, FormatoVista vista, SesionUsuario sesion) {
        this.ordenService = ordenService;
        this.repuestoService = repuestoService;
        this.mecanicoService = mecanicoService;
        this.bitacoraService = bitacoraService;
        this.nuevaOrden = nuevaOrden;
        this.vista = vista;
        this.sesion = sesion;
    }

    public record Carril(Long id, String nombre, String codigo, List<OrdenResumen> ordenes) {
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam(defaultValue = "") String busqueda, RedirectAttributes atributos) {
        String texto = busqueda.trim();
        if (texto.isEmpty()) {
            return "redirect:/ordenes";
        }
        Matcher numero = NUMERO_ORDEN.matcher(texto);
        if (numero.matches()) {
            return "redirect:/ordenes/" + Long.parseLong(numero.group(1));
        }
        atributos.addAttribute("buscar", texto);
        return "redirect:/ordenes";
    }

    @GetMapping("/")
    public String inicio(Model model) {
        UsuarioAutenticado usuario = sesion.actual();
        List<OrdenResumen> ordenes = ordenService.listar(null);
        int umbral = repuestoService.umbralBajo();
        List<RepuestoResponse> bajos = repuestoService.listarBajoInventario().stream()
                .map(repuesto -> RepuestoResponse.desde(repuesto, umbral)).toList();

        List<OrdenResumen> pendientes = filtrar(ordenes, EstadoOrden.PENDIENTE);
        List<OrdenResumen> enProceso = filtrar(ordenes, EstadoOrden.EN_PROCESO);
        YearMonth mes = YearMonth.now();
        List<OrdenResumen> finalizadasMes = ordenes.stream()
                .filter(o -> o.estado() == EstadoOrden.FINALIZADA && o.fechaFinalizacion() != null
                        && YearMonth.from(o.fechaFinalizacion()).equals(mes))
                .toList();
        List<RepuestoResponse> agotados = bajos.stream().filter(r -> r.existencia() == 0).toList();

        model.addAttribute("esMecanico", usuario.esMecanico());
        model.addAttribute("mes", vista.mes(LocalDate.now()));
        model.addAttribute("pendientes", pendientes.size());
        model.addAttribute("enProceso", enProceso.size());
        model.addAttribute("finalizadasMes", finalizadasMes.size());
        model.addAttribute("facturadoMes", finalizadasMes.stream().map(OrdenResumen::total).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("repuestos", bajos);
        model.addAttribute("agotados", agotados.size());
        model.addAttribute("recientes", ordenes.stream().limit(RECIENTES).toList());
        model.addAttribute("carriles", carriles(usuario, Stream.concat(enProceso.stream(), pendientes.stream()).toList()));
        model.addAttribute("hallazgo", hallazgo(pendientes, agotados));
        if (vista.puede(usuario, "bitacora")) {
            model.addAttribute("actividad", bitacoraService.consultar(
                    new FiltroBitacora(null, null, null, null, null, null, ACTIVIDAD_RECIENTE)));
        }
        if (vista.puede(usuario, "ordenes.crear")) {
            nuevaOrden.agregar(model);
        }
        return "inicio";
    }

    /** Un carril por mecánico con sus vehículos pendientes y en reparación; el más cargado primero. */
    private List<Carril> carriles(UsuarioAutenticado usuario, List<OrdenResumen> activas) {
        Map<Long, Carril> base = new LinkedHashMap<>();
        if (vista.puede(usuario, "mecanicos")) {
            for (MecanicoResponse m : mecanicoService.listar().stream().map(MecanicoResponse::desde).toList()) {
                base.put(m.id(), new Carril(m.id(), m.nombre(), m.codigo(), new ArrayList<>()));
            }
        } else {
            activas.forEach(o -> base.putIfAbsent(o.idMecanico(), new Carril(o.idMecanico(), o.mecanico(), "", new ArrayList<>())));
        }
        activas.forEach(o -> {
            Carril carril = base.get(o.idMecanico());
            if (carril != null) {
                carril.ordenes().add(o);
            }
        });
        return base.values().stream()
                .sorted(Comparator.comparingInt((Carril c) -> -c.ordenes().size()).thenComparing(Carril::nombre))
                .toList();
    }

    private String hallazgo(List<OrdenResumen> pendientes, List<RepuestoResponse> agotados) {
        List<String> partes = new ArrayList<>();
        if (pendientes.isEmpty()) {
            partes.add("Ningún vehículo espera inicio");
        } else {
            OrdenResumen masAntigua = pendientes.stream().min(Comparator.comparing(OrdenResumen::fechaCreacion)).orElseThrow();
            long dias = Duration.between(masAntigua.fechaCreacion(), LocalDateTime.now()).toDays();
            String espera = dias == 0 ? "hoy" : dias == 1 ? "ayer" : "hace " + dias + " días";
            partes.add(pendientes.size() + (pendientes.size() == 1 ? " vehículo espera" : " vehículos esperan")
                    + " que inicie la reparación; el " + masAntigua.placa() + " (" + vista.orden(masAntigua.id())
                    + ") fue recibido " + espera);
        }
        if (!agotados.isEmpty()) {
            partes.add(agotados.stream().map(RepuestoResponse::nombre).collect(Collectors.joining(", "))
                    + (agotados.size() == 1 ? " está agotado" : " están agotados"));
        }
        return String.join(". ", partes) + ".";
    }

    private static List<OrdenResumen> filtrar(List<OrdenResumen> ordenes, EstadoOrden estado) {
        return ordenes.stream().filter(o -> o.estado() == estado).toList();
    }
}
