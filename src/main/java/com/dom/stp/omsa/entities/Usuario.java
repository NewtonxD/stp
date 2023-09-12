package com.dom.stp.omsa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 
 * Esta entidad representa los usuarios que manejan el sistema.
 *
 * @author Carlos Abreu Pérez
 * 
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usr")
public class Usuario implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="id")
  private Integer id;
  
  @Column(name="nom",nullable = false)
  private String nombre;
  
  @Column(name="apl",nullable = false)
  private String apellido;
  
  @Column(name="usr",nullable = false,unique = true)
  private String usuario;
  
  @Column(name="mail",nullable = false,unique = true)
  private String correo;
  
  @Column(name="pwd",nullable = false)
  private String contraseña;
  
  @Column(name="act",nullable=false)
  private boolean activo;
  

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }  

  @Override
  public String getPassword() {
    return contraseña;
  }

  @Override
  public String getUsername() {
    return usuario;
  }

  @Override
  public boolean isAccountNonExpired() {
    return activo;
  }

  @Override
  public boolean isAccountNonLocked() {
    return activo;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return activo;
  }

  @Override
  public boolean isEnabled() {
    return activo;
  }
}
