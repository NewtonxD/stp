package abreuapps.core.control;

import abreuapps.core.control.general.Dato;
import abreuapps.core.control.general.DatoDTO;
import abreuapps.core.control.general.DatoServ;
import abreuapps.core.control.transporte.LocVehiculo;
import abreuapps.core.control.transporte.LocVehiculoServ;
import abreuapps.core.control.transporte.Vehiculo;
import abreuapps.core.control.transporte.VehiculoServ;
import abreuapps.core.control.usuario.AccesoServ;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import abreuapps.core.control.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author cabreu
 */

@Controller
@RequiredArgsConstructor
@RequestMapping("/vhl")
public class VehiculosCntr {

    private final AccesoServ AccesoServicio;
    
    private final VehiculoServ VehiculoServicio;
    
    private final DatoServ DatosServicio;
    
    private final LocVehiculoServ LocVehiculoServicio;

    private final DateUtils FechaUtils;

//----------------------------------------------------------------------------//
//------------------ENDPOINTS VEHICULOS---------------------------------------//
//----------------------------------------------------------------------------//
    
    @PostMapping("/save")
    public String GuardarVehiculo(
        Model model,
        Vehiculo vehiculo,
        @RequestParam(name = "fecha_actualizacionn", required = false) String fechaActualizacion
    )  {
        
        String template="fragments/trp_vehiculo_consulta :: content-default";
        boolean isOK = AccesoServicio.verificarPermisos("trp_vehiculo_registro", model );

        if(isOK){
            isOK = VehiculoServicio.guardar( vehiculo, fechaActualizacion, model);
        }

        AccesoServicio.cargarPagina("trp_vehiculo_consulta", model);
        model.addAttribute("status", isOK);

        return template;

    }
//----------------------------------------------------------------------------//
    
    @PostMapping("/update")
    public String ActualizarVehiculo(
        Model model,
        @RequestParam("placa") String placa
    ) {
        Optional<Vehiculo> vehiculo = VehiculoServicio.obtener(placa);

        if (!vehiculo.isPresent()) {
            //log.error("Error COD: 00637 al editar vehículo. No encontrado ({})",placa);
            return "redirect:/error";
        }

        model.addAttribute("vehiculo", vehiculo.get());
        model.addAttribute("marca",DatosServicio.consultarPorGrupo("Marca") );
        model.addAttribute("last_loc", LocVehiculoServicio.tieneUltimaLoc(placa));
        model.addAttribute("tipo_vehiculo", DatosServicio.consultarPorGrupo("Tipo Vehiculo") );
        model.addAttribute("estado", DatosServicio.consultarPorGrupo("Estados Vehiculo") );
        model.addAttribute("color", DatosServicio.consultarPorGrupo("Colores") );
        model.addAttribute("modelo", DatosServicio.consultarPorGrupo(vehiculo.get().getMarca().getDato() ) );
        model.addAllAttributes(AccesoServicio.consultarAccesosPantallaUsuario("trp_vehiculo_registro" ));

        return "fragments/trp_vehiculo_registro :: content-default";
    }
    
//----------------------------------------------------------------------------//
    
    @PostMapping(value="/get-modelos", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity ObtenerModelosVehiculosPorMarca(
        @RequestParam("Marca") String marca
    ) {
        List<DatoDTO> modelos = null;
        Optional<Dato> Marca = DatosServicio.obtener(marca);
        if(
            AccesoServicio.verificarPermisos("trp_vehiculo_registro", null) &&
            Marca.isPresent()
        ) modelos = DatosServicio.consultarPorGrupo(Marca.get().getDato());

        return new ResponseEntity<>(modelos, HttpStatus.OK);
    }
    
//----------------------------------------------------------------------------//
    
    @PostMapping(value="/getLastLoc", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity ObtenerUltimaLocTransporte(
        @RequestParam("placa") String placa
    ) {
        
        Map<String, Object> respuesta = new HashMap<>();

        if( AccesoServicio.verificarPermisos("trp_vehiculo_registro", null) ){

            Optional<LocVehiculo> lastLoc = LocVehiculoServicio.consultarUltimaLocVehiculo(placa);
        
            //SI TODAS LAS ANTERIORES SON VALIDAS PROCEDEMOS
            if(lastLoc.isPresent()){
                respuesta.put("placa", placa);
                respuesta.put("lon",lastLoc.get().getLongitud());
                respuesta.put("lat", lastLoc.get().getLatitud());
                respuesta.put("fecha",FechaUtils.DateToFormato1(lastLoc.get().getFecha_registro() ) );
            }

        }

        return new ResponseEntity<>( respuesta, HttpStatus.OK);
    }
    
}
