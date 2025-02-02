package abreuapps.core.control.utils;

import abreuapps.core.control.general.PublicidadServ;
import abreuapps.core.control.transporte.LocVehiculoServ;
import abreuapps.core.control.transporte.ParadaServ;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.CloseNowException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 *
 * Servicio para manejar los Server Site Event que refrescan los datos en pantalla.
 * 
 * @author Carlos Abreu Pérez
 */
@Service
@RequiredArgsConstructor
public class SSEServ {
    
    private final Map<String,SseEmitter> dtGnrEmitters = new ConcurrentHashMap<>();
    
    private final Map<String,SseEmitter> usrMgrEmitters = new ConcurrentHashMap<>();
    
    private final Map<String,SseEmitter> vhlEmitters = new ConcurrentHashMap<>();
        
    private final Map<String,SseEmitter> pdaEmitters = new ConcurrentHashMap<>();
    
    private final Map<String,SseEmitter> rtaEmitters = new ConcurrentHashMap<>();
    
    //--------------------------------------------------------------------------
    private final Map<String,SseEmitter> trpInfoEmitters = new ConcurrentHashMap<>();
    
    private final Map<String,SseEmitter> pdaInfoEmitters = new ConcurrentHashMap<>();
    
    private final Map<String,SseEmitter> vhlLogEmitters = new ConcurrentHashMap<>();
    
    private final Map<String,SseEmitter> pubEmitters = new ConcurrentHashMap<>();
    
    private final Map<String,SseEmitter> prdEmitters = new ConcurrentHashMap<>();
    
    private final ParadaServ ParadaServicio;
    
    private final LocVehiculoServ LocServicio;
    
    private final PublicidadServ PublicidadServicio;
    //--------------------------------------------------------------------------
    
    private Map<String,SseEmitter> obtenerEmitter(String nombre){
        return switch (nombre) {
            case "dtgnr" -> dtGnrEmitters;
            case "usrmgr" -> usrMgrEmitters;
            case "vhl" -> vhlEmitters;
            case "pda" -> pdaEmitters;
            case "rta" -> rtaEmitters;
            case "vhl_log","vis_log" -> vhlLogEmitters;
            case "pub" -> pubEmitters;
            case "prd" -> prdEmitters;    
            case "trpInfo" -> trpInfoEmitters;
            case "pdaInfo" -> pdaInfoEmitters;
            case "" -> null;
            default -> null;
        };
    }
    //--------------------------------------------------------------------------
    
    @Async
    public void publicar(String nombre,HashMap<String, Object> Datos){
        
        Map<String,SseEmitter> emitters=obtenerEmitter(nombre);
        
        if(nombre.equals("vis_log")){
            Datos.put("total_views_today", PublicidadServicio.getTotalViewsHoy());
            Datos.put("total_active_views", trpInfoEmitters.size());
        }
        
        if(emitters!=null){
            for (Map.Entry<String,SseEmitter> val : emitters.entrySet()) {
                try {
                    val.getValue().send(Datos);
                } catch (CloseNowException | AsyncRequestNotUsableException ex) {
                    val.getValue().complete();
                    emitters.remove(val.getKey());
                } catch (IOException ex) {
                    val.getValue().complete();
                    emitters.remove(val.getKey());
                    Logger.getLogger(SSEServ.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    @Async
    public void publicarParadaInfo(){        
        if(pdaInfoEmitters!=null){
            for (Map.Entry<String,SseEmitter> val : pdaInfoEmitters.entrySet()) {
                Integer idParada=Integer.valueOf(val.getKey().split("-")[2]);
                try {
                    val.getValue().send(ParadaServicio.getParadaInfo(idParada));
                } catch (CloseNowException | AsyncRequestNotUsableException ex) {
                    val.getValue().complete();
                    pdaInfoEmitters.remove(val.getKey());
                } catch (IOException ex) {
                    val.getValue().complete();
                    pdaInfoEmitters.remove(val.getKey());
                    Logger.getLogger(SSEServ.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    @Async
    public void publicarTransporteInfo(){
        if(trpInfoEmitters!=null){
            for (Map.Entry<String,SseEmitter> val : trpInfoEmitters.entrySet()) {
                try {
                    val.getValue().send(LocServicio.consultarDatosTransporteEnCamino());
                } catch (CloseNowException | AsyncRequestNotUsableException ex) {
                    val.getValue().complete();
                    trpInfoEmitters.remove(val.getKey());
                } catch (IOException ex) {
                    val.getValue().complete();
                    trpInfoEmitters.remove(val.getKey());
                    Logger.getLogger(SSEServ.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    @Async
    public void cerrar(String id, String nombre){
        Map<String,SseEmitter> emitter=obtenerEmitter(nombre);
        if(emitter!=null){
            if (emitter.get(id)!=null){
                emitter.get(id).complete();
                emitter.remove(id);
            }
        }
    }
    
    public SseEmitter agregar(String id,String nombre){
        Map<String,SseEmitter> emitters=obtenerEmitter(nombre);
        
        if(nombre.equals("trpInfo")){
            PublicidadServicio.aumentarVisitas();
        }
        
        if(emitters!=null){
            long timeout = 7200000;
            SseEmitter emitter = new SseEmitter(timeout);
            emitter.onCompletion(() -> emitters.remove(id));
            emitter.onTimeout(() -> emitter.complete());
            emitters.put(id,emitter);
            return emitter ;
        }else return null;
    }
    
    public int obtenerTotalClientesActivos(){
        return trpInfoEmitters.size();
    }
}
