package cl.dgt.tramites.arquitectura;

import com.tngtech.archunit.lang.ArchRule;

import java.sql.DriverManager;
import java.sql.Statement;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * El guardián que nace en el Lab 3.5.
 *
 * <p>Vive en su propio archivo y no en {@link ReglasDeLaCasa} a propósito: las siete reglas de
 * la casa viajan en la cadena de derivación desde el tronco, y meter una octava ahí obligaría a
 * tocar todos los laboratorios posteriores de una vez. Esta nace aquí, viaja hacia adelante como
 * archivo nuevo, y se integrará con las demás cuando el reempaquetado pase por esos labs.
 *
 * <p>La regla la instala el alumno (TODO_4). Mientras no lo haga, abajo hay un cascarón.
 */
public final class ReglasDelApostrofe {

    private ReglasDelApostrofe() {
    }

    /**
     * AU-03b · Ninguna clase de producción habla JDBC crudo.
     *
     * <p>Es la lápida del {@code ReporteInternoLegacyDao}. La regla no dice «no concatenes SQL»
     * —eso no se puede vigilar leyendo bytecode— sino algo más fuerte y comprobable: <em>no
     * uses las herramientas con las que se concatena</em>. Sin {@code Statement} no hay dónde
     * pegar un apóstrofe, y sin {@code DriverManager} no hay conexión abierta a mano que
     * olvidar cerrar.
     *
     * <p><strong>Qué NO prohíbe:</strong> {@code JdbcClient} de Spring, que el Lab 04 usa para
     * un reporte sin entidades. Escribir SQL a propósito, con parámetros y recursos
     * gestionados, es una decisión legítima; lo que se prohíbe es el JDBC a pelo de 1998.
     *
     * <p>No hace falta declarar ningún paquete permitido: el único JDBC que queda vivo en el
     * repositorio es el que arranca el PostgreSQL embebido, y ese vive en el classpath de
     * <em>test</em> — que este guardián no mira ({@code DO_NOT_INCLUDE_TESTS}).
     */
    public static final ArchRule AU_03B = noClasses()
            .should().dependOnClassesThat().belongToAnyOf(Statement.class, DriverManager.class)
            .because("El JDBC crudo trajo el apóstrofe, el mapeo a mano, la fuga de recursos "
                     + "y el catch vacío. Se usa el repositorio (Lab 3.5).");
}
