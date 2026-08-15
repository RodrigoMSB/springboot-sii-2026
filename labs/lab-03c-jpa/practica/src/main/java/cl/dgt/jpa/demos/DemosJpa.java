package cl.dgt.jpa.demos;

import org.springframework.stereotype.Component;

/**
 * Las ocho demos del laboratorio, en el orden en que se construyen.
 *
 * <p>Los métodos están declarados y vacíos. Cada paso de la sesión llena uno y descomenta su
 * llamada en {@code Lab35Application}. El comentario que hay encima de cada método dice qué
 * demuestra y qué SQL debería aparecer: es la instrucción, no un adorno.
 *
 * <p>En el paso 3 vas a necesitar el repositorio aquí dentro. Se recibe por constructor, como
 * cualquier dependencia en Spring.
 */
@Component
public class DemosJpa {

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
        // escribe aquí
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
        // escribe aquí
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
        // escribe aquí
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
        // escribe aquí
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
        // escribe aquí
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
    public void actualizar() {
        seccion(6, "ACTUALIZAR SIN save() · dirty checking");
        // escribe aquí
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
        // escribe aquí
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
        // escribe aquí
    }

    private void seccion(int numero, String titulo) {
        System.out.println();
        System.out.println("=== " + numero + " · " + titulo + " ===");
    }
}
