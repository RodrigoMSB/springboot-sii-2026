package cl.dgt.jpa.demos;

import cl.dgt.jpa.entities.Observacion;
import cl.dgt.jpa.repositories.ObservacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DemosJpa {

    // El repositorio llega por constructor, igual que en el Lab 02. Nadie hace
    // `new`: Spring Data creó la implementación al arrancar y Spring la entrega.
    private final ObservacionRepository repositorio;

    private Long primerId;

    public DemosJpa(ObservacionRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void guardar() {
        seccion(1, "GUARDAR · save()");

        repositorio.deleteAll();

        Observacion nueva = new Observacion(
                "Revisión anual sin hallazgos.", "Carolina", LocalDate.of(2026, 3, 10));
        System.out.println("  antes de guardar -> id = " + nueva.getId());

        Observacion guardada = repositorio.save(nueva);
        System.out.println("  después de guardar -> id = " + guardada.getId());
        this.primerId = guardada.getId();

        repositorio.save(new Observacion(
                "Solicita certificado de situación.", "Carolina", LocalDate.of(2026, 8, 1)));
        repositorio.save(new Observacion(
                "Diferencias en el F29 de julio.", "Ignacio", LocalDate.of(2026, 7, 15)));
        System.out.println("  (guardadas 2 más, para las demos siguientes)");
    }

    public void buscarPorId() {
        seccion(2, "BUSCAR POR ID · findById()");

        Optional<Observacion> encontrada = repositorio.findById(primerId);
        System.out.println("  id " + primerId + " -> " + encontrada.orElse(null));

        Optional<Observacion> inexistente = repositorio.findById(9999L);
        System.out.println("  id 9999 -> " + inexistente.map(Object::toString).orElse("no existe"));
    }

    public void listarTodas() {
        seccion(3, "LISTAR TODAS · findAll()");

        List<Observacion> todas = repositorio.findAll();
        System.out.println("  " + todas.size() + " observaciones:");
        todas.forEach(o -> System.out.println("    " + o));
    }

    public void buscarPorAutor() {
        seccion(4, "BUSCAR POR AUTOR · findByAutor()");

        List<Observacion> deCarolina = repositorio.findByAutor("Carolina");
        System.out.println("  autor = Carolina -> " + deCarolina.size());
        deCarolina.forEach(o -> System.out.println("    " + o));
    }

    public void buscarConDosCondiciones() {
        seccion(5, "DOS CONDICIONES · findByAutorAndFechaAfter()");

        LocalDate corte = LocalDate.of(2026, 6, 1);
        List<Observacion> recientes = repositorio.findByAutorAndFechaAfter("Carolina", corte);
        System.out.println("  autor = Carolina y fecha > " + corte + " -> " + recientes.size());
        recientes.forEach(o -> System.out.println("    " + o));
    }

    @Transactional
    public void actualizar() {
        seccion(6, "ACTUALIZAR SIN save() · dirty checking");

        Observacion observacion = repositorio.findById(primerId).orElseThrow();
        System.out.println("  antes:  " + observacion.getTexto());

        observacion.setTexto("Revisión anual: se detecta diferencia menor.");
        System.out.println("  después: " + observacion.getTexto());
        System.out.println("  NO llamamos a save(). El UPDATE aparece justo aquí abajo,");
        System.out.println("  cuando esta transacción se cierre:");
    }

    public void borrar() {
        seccion(7, "BORRAR · deleteById()");

        System.out.println("  filas antes:  " + repositorio.count());
        repositorio.deleteById(primerId);
        System.out.println("  filas después: " + repositorio.count());
    }

    public void contar() {
        seccion(8, "CONTAR · count() vs findAll().size()");

        System.out.println("  count()          -> " + repositorio.count());
        System.out.println("  findAll().size() -> " + repositorio.findAll().size());
        System.out.println("  countByAutor(\"Carolina\") -> " + repositorio.countByAutor("Carolina"));
    }

    private void seccion(int numero, String titulo) {
        System.out.println();
        System.out.println("=== " + numero + " · " + titulo + " ===");
    }
}
