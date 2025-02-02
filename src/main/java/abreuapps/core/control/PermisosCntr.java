package abreuapps.core.control;

import abreuapps.core.control.usuario.AccesoServ;
import abreuapps.core.control.usuario.Usuario;
import abreuapps.core.control.usuario.UsuarioServ;
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
@RequestMapping("/acc")
public class PermisosCntr {

    private final AccesoServ AccesoServicio;
    
    private final UsuarioServ UsuarioServicio;

//----------------------------------------------------------------------------//
    @PostMapping("/access")
    public String PermisosUsuario(
        Model model, 
        String idUsuario
    ) {
        String plantillaRespuesta="fragments/usr_mgr_permisos :: content-default";
        
        //VERIFICAMOS PERMISOS PARA ESTA ACCION
        String sinPermisoPlantilla= AccesoServicio.verificarPermisos("usr_mgr_registro", model);

        boolean valido = sinPermisoPlantilla.equals("");
        
        if(valido){
            
            Optional<Usuario> usuarioBD=UsuarioServicio.obtener(idUsuario); 

            if( ! usuarioBD.isPresent() ){
                //log.error("Error COD: 00537 al editar Usuario. Usuario no encontrado ({})",idUsuario);
                plantillaRespuesta = "redirect:/error";
                valido=false;
            }
            
            
            //PROCEDEMOS SI TODOS LOS DATOS SON VALIDOS
            if(valido) model.addAttribute("usuario",usuarioBD.get());
            
        }
        
        model.addAllAttributes(
                AccesoServicio.consultarAccesosPantallaUsuario("usr_mgr_registro")
        );
        
        return valido ? plantillaRespuesta : sinPermisoPlantilla;
    }
//----------------------------------------------------------------------------//
    
    @PostMapping(value="/get-access", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity ObtenerListadoPermisosUsuario(
        @RequestParam("idUsuario") String nombreUsuario
    ) {  
        boolean valido = AccesoServicio.verificarPermisos("usr_mgr_registro", null).equals("");
        
        List<Object[]> permisosUsuario = null;
        
        if(valido){
            Optional<Usuario> usuarioBD=UsuarioServicio.obtener(nombreUsuario); 

            if(!usuarioBD.isPresent()){
                //log.error("Error COD: 00539 al editar Usuario. Usuario no encontrado ({})",nombreUsuario);
                valido=false;
            }
        
            
            //PROCEDEMOS SI TODOS LOS DATOS SON VALIDOS
            if(valido) permisosUsuario = AccesoServicio.ListadoAccesosUsuarioEditar(
                    usuarioBD.get().getId() );
            
        }
        
        return new ResponseEntity<>(
            permisosUsuario,
            new HttpHeaders(),
            valido ? HttpStatus.OK: HttpStatus.NOT_FOUND
        );  
    }
//----------------------------------------------------------------------------//
    
    @PostMapping(value="/save-acc")
    public String GuardarPermisosUsuario(
        Model model,
        @RequestParam Map<String,String> data
    ) {
        String plantillaRespuesta="fragments/usr_mgr_principal :: content-default";
        
        //VERIFICAMOS PERMISOS PARA ESTA ACCION
        String sinPermisoPlantilla= AccesoServicio.verificarPermisos(
                "usr_mgr_registro", model );

        boolean valido = sinPermisoPlantilla.equals("");
        
        if(valido){
            Optional<Usuario> usuarioBD=UsuarioServicio.obtener(data.getOrDefault("idUsuario",""));

            if(!usuarioBD.isPresent()){
                valido=false;
                model.addAttribute("msg", 
                        "No pudimos encontrar al usuario. Por favor, inténtalo otra vez. COD: 00545"
                );
            }
            
            //PROCEDEMOS SI TODOS LOS DATOS SON VALIDOS
            if(valido){
                UsuarioServicio.cerrarSesion(usuarioBD.get().getUsername());
                AccesoServicio.GuardarTodosMap(data, usuarioBD.get());
                model.addAttribute("msg", "Permisos guardados exitosamente!");

                AccesoServicio.cargarPagina("usr_mgr_principal", model);

            }
            
            model.addAttribute("status", valido);
        }
        
        return valido ? plantillaRespuesta : sinPermisoPlantilla;
    }
//----------------------------------------------------------------------------//
}
