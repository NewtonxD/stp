/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package abreusapp.core.control.transporte;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * @author cabreu
 */

@Transactional
@Service
@RequiredArgsConstructor
public class LocRutaServ {
    
    private final LocRutaRepo repo;
    
    public List<LocRuta> consultar(){
        return repo.findAll();
    }
    
    public List<LocRuta> consultarPorRuta(Ruta ruta){
        return repo.findAllByRuta(ruta);
    }
    
    public void borrarPorRuta(Ruta ruta){
        repo.deleteAllByRuta(ruta);
    }
    
    public void guardarTodos(List<LocRuta> gd){
        repo.saveAll(gd);
    }
    
    public List<LocRuta> generarLista(String lista_cadena,Ruta ruta){
        List<LocRuta> points = new ArrayList<>();
        String[] coordinatePairs = lista_cadena.split("],\\[");

        for (String pair : coordinatePairs) {
            pair = pair.replace("[", "").replace("]", "");
            String[] latLng = pair.split(", ");
            double latitude = Double.parseDouble(latLng[1]);
            double longitude = Double.parseDouble(latLng[0]);
            
            points.add(new LocRuta(null,ruta,latitude, longitude));
        }

        return points;
    }
    
}

