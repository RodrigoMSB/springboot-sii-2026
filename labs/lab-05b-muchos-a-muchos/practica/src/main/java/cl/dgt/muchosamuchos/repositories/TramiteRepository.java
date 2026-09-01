package cl.dgt.muchosamuchos.repositories;

import cl.dgt.muchosamuchos.entities.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TramiteRepository extends JpaRepository<Tramite, Long> {

    // Paso 5 · un método cuyo nombre navegue de trámite a documento.
    // escribe aquí
}
