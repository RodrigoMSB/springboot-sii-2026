package cl.dgt.jpa.demos;

import cl.dgt.jpa.entities.Observacion;
import cl.dgt.jpa.repositories.ObservacionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Las ocho demos del laboratorio, en el orden en que se construyen.
 *
 * <p>Cada método imprime lo que hace. El SQL que aparece entre medio no lo escribió nadie: lo
 * genera Hibernate a partir de la entidad y del nombre de cada método del repositorio.
 */
@Component
public class DemosJpa {

    private final ObservacionRepository repositorio;

    /** El id de la primera observación guardada. Lo usan las demos 2 y 6. */
    private Long primerId;

    public DemosJpa(ObservacionRepository repositorio) {
        this.repositorio = repositorio;
    }

    // =========================================================================
    //  1 · GUARDAR
    // -------------------------------------------------------------------------
    //  Un objeto Java entra sin id y sale con id. Eso es todo lo que hace save():
    //  toma el objeto, genera el INSERT con las columnas del mapeo, y lee de
    //  vuelta el id que generó la base para escribirlo en el objeto.
    //  El SQL: insert into observacion (autor, fecha, texto) values (?, ?, ?)
    //  Para pensar: ¿por qué el INSERT no menciona la columna id?
    // =========================================================================
    public void guardar() {
        seccion(1, "GUARDAR · save()");

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

    // =========================================================================
    //  2 · BUSCAR POR ID
    // -------------------------------------------------------------------------
    //  findById devuelve Optional, y no es capricho: preguntar por un id que no
    //  existe es normal, no es un error. El Optional obliga a decidir qué hacer
    //  en ese caso, en vez de recibir un null que revienta más adelante.
    //  El SQL: select ... from observacion where id = ?
    //  Para pensar: ¿qué habría pasado si esto devolviera Observacion a secas?
    // =========================================================================
    public void buscarPorId() {
        seccion(2, "BUSCAR POR ID · findById()");

        Optional<Observacion> encontrada = repositorio.findById(primerId);
        System.out.println("  id " + primerId + " -> " + encontrada.orElse(null));

        Optional<Observacion> inexistente = repositorio.findById(9999L);
        System.out.println("  id 9999 -> " + inexistente.map(Object::toString).orElse("no existe"));
    }

    // =========================================================================
    //  3 · LISTAR TODAS
    // -------------------------------------------------------------------------
    //  findAll trae la tabla entera. Es cómodo y es peligroso: con tres filas se
    //  ve igual que con tres millones, y la diferencia solo aparece en producción.
    //  El SQL: select ... from observacion (sin where)
    //  Para pensar: ¿qué pasaría con esta línea si la tabla tuviera 500.000 filas?
    // =========================================================================
    public void listarTodas() {
        seccion(3, "LISTAR TODAS · findAll()");

        List<Observacion> todas = repositorio.findAll();
        System.out.println("  " + todas.size() + " observaciones:");
        todas.forEach(o -> System.out.println("    " + o));
    }

    // =========================================================================
    //  4 · BUSCAR POR AUTOR
    // -------------------------------------------------------------------------
    //  La primera consulta derivada. No se escribe SQL: se escribe el NOMBRE del
    //  método —findByAutor— y Spring Data lo lee, comprueba que la entidad tiene
    //  una propiedad `autor`, y genera la consulta con su parámetro.
    //  El SQL: select ... from observacion where autor = ?
    //  Para pensar: si te equivocas y escribes findByAutorr, ¿cuándo te enteras?
    // =========================================================================
    public void buscarPorAutor() {
        seccion(4, "BUSCAR POR AUTOR · findByAutor()");

        List<Observacion> deCarolina = repositorio.findByAutor("Carolina");
        System.out.println("  autor = Carolina -> " + deCarolina.size());
        deCarolina.forEach(o -> System.out.println("    " + o));
    }

    // =========================================================================
    //  5 · DOS CONDICIONES
    // -------------------------------------------------------------------------
    //  El vocabulario de los nombres da para más: And, Or, After, Before,
    //  Between, LessThan, OrderBy. Aquí se combinan dos condiciones y el método
    //  sigue sin tener cuerpo.
    //  El SQL: select ... where autor = ? and fecha > ?
    //  Para pensar: ¿hasta qué largo de nombre sigue siendo esto legible?
    // =========================================================================
    public void buscarConDosCondiciones() {
        seccion(5, "DOS CONDICIONES · findByAutorAndFechaAfter()");

        LocalDate corte = LocalDate.of(2026, 6, 1);
        List<Observacion> recientes = repositorio.findByAutorAndFechaAfter("Carolina", corte);
        System.out.println("  autor = Carolina y fecha > " + corte + " -> " + recientes.size());
        recientes.forEach(o -> System.out.println("    " + o));
    }

    // =========================================================================
    //  6 · ACTUALIZAR SIN save()
    // -------------------------------------------------------------------------
    //  El momento raro del laboratorio. Dentro de una transacción, el objeto que
    //  cargaste está VIGILADO: Hibernate recuerda cómo venía. Al cerrar compara,
    //  ve que el texto cambió, y lanza el UPDATE solo. Se llama dirty checking.
    //  El SQL: update observacion set autor=?, fecha=?, texto=? where id=?
    //  Para pensar: si esto es así, ¿para qué sirve save() entonces?
    // =========================================================================
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

    // =========================================================================
    //  7 · BORRAR
    // -------------------------------------------------------------------------
    //  deleteById hace lo que dice. Se cuenta antes y después para no creerle al
    //  método: un borrado que no borra y un borrado que borra se ven igual desde
    //  fuera si nadie mira la tabla.
    //  El SQL: select (para cargarla) y después delete from observacion where id=?
    //  Para pensar: ¿por qué Hibernate hace un SELECT antes del DELETE?
    // =========================================================================
    public void borrar() {
        seccion(7, "BORRAR · deleteById()");

        System.out.println("  filas antes:  " + repositorio.count());
        repositorio.deleteById(primerId);
        System.out.println("  filas después: " + repositorio.count());
    }

    // =========================================================================
    //  8 · CONTAR
    // -------------------------------------------------------------------------
    //  Dos formas de saber cuántas hay, y una es mucho peor. count() le pregunta
    //  a la base y recibe un número. findAll().size() se trae todas las filas a
    //  memoria, las convierte en objetos, y después las cuenta.
    //  El SQL: select count(*) from observacion  ·  contra  select ... from observacion
    //  Para pensar: ¿cuál de las dos se nota con 500.000 filas?
    // =========================================================================
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
