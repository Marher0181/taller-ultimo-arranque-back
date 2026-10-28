package gt.umg.ultimoarranque.controlador.web;

import gt.umg.ultimoarranque.dto.MecanicoResponse;
import gt.umg.ultimoarranque.dto.ServicioTallerResponse;
import gt.umg.ultimoarranque.dto.VehiculoResponse;
import gt.umg.ultimoarranque.servicio.MecanicoService;
import gt.umg.ultimoarranque.servicio.ServicioTallerService;
import gt.umg.ultimoarranque.servicio.VehiculoService;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

/** Listas que necesita el diálogo "Nueva orden de trabajo" (presente en Taller hoy y en Órdenes). */
@Component
public class NuevaOrdenCatalogos {

    private final VehiculoService vehiculoService;
    private final MecanicoService mecanicoService;
    private final ServicioTallerService servicioTallerService;

    public NuevaOrdenCatalogos(VehiculoService vehiculoService, MecanicoService mecanicoService,
                               ServicioTallerService servicioTallerService) {
        this.vehiculoService = vehiculoService;
        this.mecanicoService = mecanicoService;
        this.servicioTallerService = servicioTallerService;
    }

    public void agregar(Model model) {
        model.addAttribute("noVehiculos", vehiculoService.listar(null).stream().map(VehiculoResponse::desde).toList());
        model.addAttribute("noMecanicos", mecanicoService.listar().stream().map(MecanicoResponse::desde).toList());
        model.addAttribute("noServicios", servicioTallerService.listar().stream().map(ServicioTallerResponse::desde).toList());
    }
}
