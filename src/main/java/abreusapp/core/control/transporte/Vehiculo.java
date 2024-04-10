/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package abreusapp.core.control.transporte;

import abreusapp.core.control.general.Dato;
import abreusapp.core.control.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  
 * Esta entidad representa al autobus y sus atributos.
 *
 * @author Carlos Abreu Pérez
 *  
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vhl",schema = "transport")
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@VehiculoId")
public class Vehiculo {

    @Id
    @Column(name = "pl")
    private String placa;

    @ManyToOne
    @PrimaryKeyJoinColumn
    private Dato marca;

    @ManyToOne
    @PrimaryKeyJoinColumn
    private Dato modelo;
    
    @Column(name = "cap_pax")
    private Integer capacidad_pasajeros;
    
    @Column(name = "mke_at")
    private short anio_fabricacion;
    
    @ManyToOne
    @PrimaryKeyJoinColumn
    private Dato tipo_vehiculo;

    @ManyToOne
    @PrimaryKeyJoinColumn
    private Dato estado;
    
    @ManyToOne
    @PrimaryKeyJoinColumn
    private Dato color;

    @ManyToOne
    @PrimaryKeyJoinColumn
    private Usuario hecho_por;
    
    @Column(name= "mde_at")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date fecha_registro;
    
    @ManyToOne
    @PrimaryKeyJoinColumn
    private Usuario actualizado_por;
    
    @Column(name= "upd_at")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date fecha_actualizacion;
}
