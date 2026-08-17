package cl.dgt.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Esta clase ES la tabla {@code observacion}.
 *
 * <p>Las anotaciones son el mapa: qué clase corresponde a qué tabla, qué propiedad a qué
 * columna, y cuál es la clave. Con eso, Hibernate escribe el SQL de todas las operaciones.
 *
 * <p>{@code @Entity} es la que cuenta: marca la clase como gestionada por JPA, y sin ella el resto
 * de anotaciones no significan nada. {@code @Table(name = "observacion")} nombra la tabla; sin él
 * Hibernate usaría el nombre de la clase, que aquí coincidiría — pero escribirlo evita que un
 * renombrado de la clase rompa el mapeo en silencio.
 *
 * <p>Fíjate en que <strong>no es un record</strong>, a diferencia de los DTO de los labs
 * anteriores. JPA necesita poder construir el objeto vacío y rellenarlo campo a campo, y un record
 * es inmutable: no se deja. Es la razón del constructor protegido de abajo.
 */
@Entity
@Table(name = "observacion")
public class Observacion {

    // =========================================================================
    //  LA CLAVE PRIMARIA
    // -------------------------------------------------------------------------
    //  @Id marca cuál es. @GeneratedValue dice quién pone el número, y con
    //  IDENTITY la respuesta es «la base»: la columna es BIGSERIAL y Postgres
    //  lleva su propio contador.
    //  Por eso el INSERT que sale en consola NO menciona la columna id, y por
    //  eso el objeto entra con id null y sale con id puesto — Hibernate lee de
    //  vuelta el que generó la base.
    //  Es `Long` y no `long`: necesita poder valer null para distinguir «aún no
    //  guardado» de «guardado con id 0».
    // =========================================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================================
    //  LAS COLUMNAS
    // -------------------------------------------------------------------------
    //  @Column no hace falta para que el campo se mapee —eso pasa solo, por
    //  nombre—, pero aquí sirve para dos cosas: dejar escrito en la clase lo que
    //  la tabla ya exige, y que `ddl-auto: validate` pueda comprobar que las dos
    //  dicen lo mismo al arrancar.
    //  `nullable = false` y `length` reflejan el NOT NULL y el VARCHAR(n) del
    //  archivo de Flyway. Si no cuadraran, la aplicación no arrancaría.
    //  Para pensar: ¿quién manda si la clase dice 500 y la tabla dice 100?
    // =========================================================================
    @Column(nullable = false, length = 500)
    private String texto;

    @Column(nullable = false, length = 100)
    private String autor;

    // LocalDate se corresponde con DATE: solo fecha, sin hora ni zona. Hibernate
    // sabe convertirlo sin que haya que decirle nada.
    @Column(nullable = false)
    private LocalDate fecha;

    // =========================================================================
    //  EL CONSTRUCTOR VACÍO QUE NADIE LLAMA
    // -------------------------------------------------------------------------
    //  JPA lo exige: para reconstruir una fila que viene de la base, crea el
    //  objeto vacío y después le mete los valores. Sin este constructor, la
    //  aplicación falla al arrancar.
    //  Es `protected` y no `public` a propósito: Hibernate llega igual, pero el
    //  código del laboratorio no puede crear por accidente una observación sin
    //  texto ni autor. El constructor de verdad es el de abajo.
    // =========================================================================
    protected Observacion() {
    }

    /** El constructor que sí se usa. Sin id: ese lo pone la base al guardar. */
    public Observacion(String texto, String autor, LocalDate fecha) {
        this.texto = texto;
        this.autor = autor;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public String getTexto() { return texto; }
    public String getAutor() { return autor; }
    public LocalDate getFecha() { return fecha; }

    // =========================================================================
    //  EL ÚNICO SETTER, Y ES DELIBERADO
    // -------------------------------------------------------------------------
    //  No hay setters de autor ni de fecha: una observación no cambia de dueño
    //  ni de día. Solo su texto se corrige, y para eso está este.
    //  Es la pieza del paso 6: dentro de una transacción basta con llamarlo para
    //  que el UPDATE salga solo, sin pasar por save(). Hibernate compara el
    //  objeto con cómo lo cargó y ve que el texto cambió — dirty checking.
    //  Para pensar: si hubiera setters de todo, ¿qué garantías perdería la clase?
    // =========================================================================
    public void setTexto(String texto) { this.texto = texto; }

    /** Para poder imprimirla en las demos. `formatted` es String.format en versión método. */
    @Override
    public String toString() {
        return "Observacion{id=%d, texto='%s', autor='%s', fecha=%s}"
                .formatted(id, texto, autor, fecha);
    }
}
