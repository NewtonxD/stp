package abreuapps.core.control;

import abreuapps.core.control.transporte.LocRuta;
import abreuapps.core.control.transporte.LocRutaServ;
import abreuapps.core.control.transporte.ParadaServ;
import abreuapps.core.control.transporte.Ruta;
import abreuapps.core.control.transporte.RutaServ;
import abreuapps.core.control.usuario.AccesoServ;
import abreuapps.core.control.usuario.Usuario;
import abreuapps.core.control.utils.DateUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
@RequestMapping("/rta")
public class RutasCntr {
    
    private final DateUtils FechaUtils;

    private final AccesoServ AccesoServicio;
        
    private final RutaServ RutaServicio;
    
    private final LocRutaServ LocRutaServicio;
    
    private final ParadaServ ParadaServicio;
    
//----------------------------------------------------------------------------//
//------------------ENDPOINTS RUTAS-------------------------------------------//
//----------------------------------------------------------------------------//   
    @PostMapping("/save")
    public String GuardarRuta(
        Model model,
        Ruta rutaCliente,
        @RequestParam(name = "fecha_actualizacionn", 
                        required = false) String fechaActualizacionCliente,
        @RequestParam("data_poly") String data
    ) {
        
        String plantillaRespuesta="fragments/trp_rutas_consulta :: content-default";
        
        Usuario u = AccesoServicio.getUsuarioLogueado();
        
        //INICIO DE VALIDACIONES
        String sinPermisoPlantilla = AccesoServicio.verificarPermisos("trp_rutas_consulta", model);
        
        //USUARIO NO TIENE PERMISOS PARA EJECUTAR ESTA ACCION
        boolean valido = sinPermisoPlantilla.equals("");
        
        
        if(valido){
            
            Optional<Ruta> rutaDB = RutaServicio.obtener(rutaCliente.getRuta());

            if (rutaDB.isPresent()) {

                if (! FechaUtils.FechaFormato2.format(
                        rutaDB.get().getFecha_actualizacion()
                        ).equals(fechaActualizacionCliente)
                ) {
                    
                    model.addAttribute(
                        "msg",
                        ! ( fechaActualizacionCliente == null || 
                             fechaActualizacionCliente.equals("") ) ? 
                        "Al parecer alguien ha realizado cambios en la información primero. Por favor, inténtalo otra vez. COD: 00656" :
                        "No podemos realizar los cambios porque ya esta Ruta se encuentra registrado."
                    );
                    valido = false;
                    
                }

            }
            
            //SI TODAS LAS ANTERIORES SON VALIDAS PROCEDEMOS
            if(valido){
                
            
                if ( ! ( fechaActualizacionCliente == null || 
                        fechaActualizacionCliente.equals("") )
                ) {
                    rutaCliente.setFecha_actualizacion(
                        FechaUtils.Formato2ToDate(fechaActualizacionCliente)
                    );
                }
                
                if (rutaDB.isPresent()) {
                    rutaCliente.setFecha_registro(rutaDB.get().getFecha_registro());
                    rutaCliente.setHecho_por(rutaDB.get().getHecho_por());
                }

                Ruta d = RutaServicio.guardar(rutaCliente, u, rutaDB.isPresent());
                model.addAttribute("msg", "Registro guardado exitosamente!");
                
                // GUARDAMOS LOC RUTA
                if(!data.equals("")){
                    String cadenaListaLocRuta=data.replace("LatLng(","[").replace(")", "]");
                    List<LocRuta> listaLocRuta = LocRutaServicio.generarLista(cadenaListaLocRuta, d);
                    LocRutaServicio.borrarPorRuta(d);
                    LocRutaServicio.guardarTodos(listaLocRuta);
                }
                AccesoServicio.cargarPagina("trp_rutas_consulta", model);
            }
            
            model.addAttribute("status", valido);
        }

        return sinPermisoPlantilla.equals("") ? plantillaRespuesta : sinPermisoPlantilla;

    }    
//----------------------------------------------------------------------------//
    @PostMapping("/update")
    public String ActualizarRuta(
        HttpServletRequest request,
        Model model,
        @RequestParam("idRuta") String idRuta
    ) {
        
        boolean valido=true;
        String plantillaRespuesta="fragments/trp_rutas_registro :: content-default";
        Optional<Ruta> ruta = RutaServicio.obtener(idRuta);

        if (!ruta.isPresent()) {

            //log.error("Error COD: 00637 al editar parada. No encontrado ({})",idRuta);
            plantillaRespuesta = "redirect:/error";
            valido=false;

        }
        
        //SI TODAS LAS ANTERIORES SON VALIDAS PROCEDEMOS
        if(valido){
            model.addAttribute("ruta", ruta.get());
            model.addAllAttributes(
                    AccesoServicio.consultarAccesosPantallaUsuario("trp_rutas_registro" )
            );
        }

        return plantillaRespuesta;
    }
//----------------------------------------------------------------------------//
    
    @PostMapping(value="/getLoc", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity ObtenerLocRuta(
        @RequestParam("idRuta") String idRuta
    ) {
        
        //VERIFICAMOS PERMISOS PARA ESTA ACCION
        String sinPermisoPlantilla = 
            AccesoServicio.verificarPermisos("trp_paradas_registro", null);

        boolean valido = sinPermisoPlantilla.equals("");
        
        Map<String, Object> respuesta= new HashMap<>();
        
        if(valido){
            Optional<Ruta> Ruta = RutaServicio.obtener(idRuta); 
            
            if(Ruta.isPresent()){
                respuesta.put("ruta",
                    LocRutaServicio.consultar(Ruta.get().getRuta(),null)) 
                ;
            } 
            
            respuesta.put("paradas",ParadaServicio.consultarTodo( 
                null , 
                true)
            );
        
        }
        
        return new ResponseEntity<>(
                respuesta.isEmpty() ? null: respuesta,
                new HttpHeaders(),
                HttpStatus.OK);  
    }
    
}
