/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dom.stp.omsa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * Esta entidad representa los accesos de los usuarios.
 *
 * @author Carlos Abreu Pérez
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "acc")
public class Accesos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "dsc", nullable = true)
    private String descripción;
    
    @Column(name = "dat_tpe", columnDefinition = "varchar(50)",nullable=false)
    private String tipo_dato;
    
    @Column(name = "acc_tpe", columnDefinition = "varchar(50)",nullable=false)
    private String tipo_acceso;
    
    @Column(name = "scr", columnDefinition = "varchar(50)",nullable=false)
    private String pantalla;
    
    @Column(name = "act",nullable=false)
    private boolean activo;
}
