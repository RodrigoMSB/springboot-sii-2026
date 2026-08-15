package cl.dgt.tramites.arquitectura;

import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * El guardián que nace en el Lab 3.5, y que instalas tú.
 *
 * <p>Vive en su propio archivo y no en {@link ReglasDeLaCasa} a propósito: las siete reglas de
 * la casa viajan desde el tronco por toda la cadena de laboratorios, y meter una octava ahí
 * obligaría a tocarlos todos de una vez.
 */
public final class ReglasDelApostrofe {

    private ReglasDelApostrofe() {
    }

    /**
     * AU-03b · Ninguna clase de producción habla JDBC crudo.
     *
     * <p><strong>{{TODO_4}} · ~15 min · Este guardián NO existe todavía.</strong> Ahora mismo es
     * un cascarón: una regla tautológica ({@code haveNameMatching(".*")}) que pasa siempre y no
     * vigila nada. Reemplázala entera.
     *
     * <p><em>Antes de instalarlo, entierra al muerto:</em> borra
     * {@code infrastructure/legacy/ReporteInternoLegacyDao.java}. Si instalas la regla con el
     * DAO todavía ahí, la regla lo caza — que es exactamente lo que debe hacer, pero el
     * laboratorio no termina hasta que el muerto está enterrado.
     *
     * <p><em>Qué escribir:</em> ninguna clase debe depender de {@code java.sql.Statement} ni de
     * {@code java.sql.DriverManager}. Métodos que necesitas:
     * {@code noClasses().should().dependOnClassesThat().belongToAnyOf(Statement.class,
     * DriverManager.class)}, y un {@code .because(...)} que nombre el crimen, no la regla.
     *
     * <p><em>Por qué esas dos clases y no «no concatenes SQL»:</em> lo segundo no se puede
     * vigilar leyendo bytecode. Lo primero sí, y es más fuerte — sin {@code Statement} no hay
     * dónde pegar un apóstrofe, y sin {@code DriverManager} no hay conexión abierta a mano que
     * olvidar cerrar.
     *
     * <p><em>Lo que NO debe prohibir:</em> el {@code JdbcClient} de Spring. Escribir SQL a
     * propósito, con parámetros y recursos gestionados, es una decisión legítima — y el Lab 04
     * la usa. Lo que se prohíbe es el JDBC a pelo.
     *
     * <p><em>Lo verifica:</em> {@code E4_GuardianJdbcTest}, que exige las dos mitades: que la
     * regla pase sobre tu código y que MUERDA a su fixture.
     */
    public static final ArchRule AU_03B = classes()   // {{TODO_4}} — reemplaza la regla entera
            .should().haveNameMatching(".*")              // tautología: pasa siempre, no vigila nada
            .because("cascarón sin instalar — TODO_4");
}
