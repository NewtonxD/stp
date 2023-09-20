/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dom.stp.omsa.services;

import com.dom.stp.omsa.entities.Usuario;
import com.dom.stp.omsa.repositorys.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author cabreu
 */


@Transactional
@Service
class UsuarioService {
    
    @Autowired
    private UserRepository repo;
    
    public Usuario guardar(Usuario gd, Integer idUsuario,boolean existe){
        
        if(existe){ 
            gd.setActualizado_por(idUsuario);
        }else{
            gd.setHecho_por(idUsuario);
            gd.setFecha_registro(new Date());
        }
        gd.setFecha_actualizacion(new Date());
        
        return repo.save(gd);
    }
    
    public List<Usuario> consultar(){
        return repo.findAll();
    }
    
    public Optional<Usuario> obtener(String usuario){
        return repo.findByUsuario(usuario);
    }
}
