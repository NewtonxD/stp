package abreuapps.core.control.general;

import abreuapps.core.control.usuario.Usuario;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 *
 * @author cabreu
 */
@Service
@RequiredArgsConstructor
public class ConfServ {
    
    private final ConfRepo repo;
    
    public final String DEFAULT_NONE_PERMISSION = "n/a" ;
 
    public Map<String,String> consultarConfMap(){
        List<ConfDTO> results=repo.customFindAll();
        Map<String, String> convert=new HashMap<>();
        
        for (ConfDTO result : results) convert.put(result.cod(), result.val());
        
        return convert;
    }
    
    @Cacheable("Conf")
    public String consultar(String codigo){
        return repo.customFind(codigo).orElse(DEFAULT_NONE_PERMISSION);
    }
    
    public List<ConfDTO> consultar(){
        return repo.customFindAll();
    }
    
    @Transactional
    @CacheEvict(allEntries = true,value = {"Conf"})
    public void GuardarTodosMap(Map<String,String> configuracion,Usuario usuario){
        List<Conf> listaConf = new ArrayList<>();
        for (Map.Entry<String,String> val : configuracion.entrySet()) {
            Optional<Conf> conf=repo.findById(val.getKey());
            if(conf.isPresent()){
                if(! conf.get().getValor().equals(val.getValue())){
                    listaConf.add(
                        new Conf(
                            val.getKey(), 
                            conf.get().getDescripcion(), 
                            val.getValue(), 
                            usuario, 
                            new Date())
                    );
                }
            }
        }
        
        repo.saveAllAndFlush(listaConf);
    }
    
}
