package com.dom.stp.omsa.control.domain.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaRepo extends JpaRepository<Persona, Integer> {

  Optional<Persona> findByCedula(String cedula);

}
