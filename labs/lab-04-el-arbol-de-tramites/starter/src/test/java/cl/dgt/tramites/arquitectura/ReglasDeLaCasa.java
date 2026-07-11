package cl.dgt.tramites.arquitectura;

import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/**
 * Las siete reglas de la casa, en un solo lugar.
 *
 * <p>Se definen aquí una vez para que la suite de producción ({@code ArquitecturaTest})
 * y los meta-tests ({@code MordidaDeLosGuardianesTest}) juzguen con <em>la misma</em>
 * regla. Si la regla viviera duplicada, un guardián podría certificar una mordida que la
 * producción no aplica: no hay dos verdades (P-14 del ADN).
 *
 * <p>Artefacto {@code archunit} core, sin {@code archunit-junit5} y sin
 * {@code @AnalyzeClasses}: las reglas corren como {@code @Test} Jupiter comunes con
 * {@code ClassFileImporter}, inmunes a la versión del runner (SPEC-000 §6, nota 1;
 * validado por el spike S-1).
 *
 * <p><strong>Cada {@code because(...)} nombra el crimen, no la regla.</strong>
 */
public final class ReglasDeLaCasa {

    private ReglasDeLaCasa() {
    }

    /** AU-01 · La capa web no conoce las entidades. */
    public static final ArchRule AU_01 = noClasses()
            .that().resideInAPackage("..web..")
            .should().dependOnClassesThat().resideInAPackage("..domain.entity..")
            .because("La web no toca la entidad. Ese fue el folio filtrado (Lab 02).");

    /**
     * AU-02 · Ninguna {@code @Entity} es alcanzable desde un {@code @RestController}.
     *
     * <p>Se redacta sobre DEPENDENCIAS, jamás sobre el tipo de retorno crudo.
     * {@code haveRawReturnType} es la trampa: un controlador que devuelve
     * {@code ResponseEntity<Contribuyente>} tiene tipo crudo {@code ResponseEntity} y
     * pasa en verde mientras el puntaje de riesgo viaja por el genérico. El spike S-1 lo
     * midió. {@code dependOnClassesThat()} lee el atributo {@code Signature} del bytecode
     * y sí lo caza.
     */
    public static final ArchRule AU_02 = noClasses()
            .that().areAnnotatedWith(RestController.class)
            .should().dependOnClassesThat().areAnnotatedWith(Entity.class)
            .because("Un controlador jamás toca la entidad. Ese fue el folio filtrado (Lab 02).");

    /** AU-03 · El dominio no conoce ni la web ni Spring. */
    public static final ArchRule AU_03 = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..web..", "org.springframework..")
            .because("El dominio no sabe que existen HTTP ni Spring. Si lo supiera, no se "
                     + "podría probar sin encender la aplicación entera.");

    /**
     * AU-04 · Todo {@code @ManyToOne} y {@code @OneToOne} declara {@code LAZY} explícito.
     *
     * <p>El default de JPA para estas dos anotaciones es EAGER. Ese default es el que hace
     * que el listado del Lab 05 tarde once segundos. Escribir {@code LAZY} a mano no es
     * redundancia: es una declaración de intención que se puede auditar.
     */
    /**
     * AU-04 · Toda relación {@code @ManyToOne}/{@code @OneToOne} declara {@code fetch} explícito.
     *
     * <p><strong>TODO_1 — este guardián NO existe todavía. Instálalo.</strong> Ahora es un
     * cascarón tautológico que no vigila nada. Reemplázalo entero.
     *
     * <p><em>Qué escribir:</em> una regla sobre CAMPOS ({@code fields()}) anotados con
     * {@code @ManyToOne} o {@code @OneToOne} que exija que cada uno declare {@code fetch}. La
     * solución del tronco usa una condición a medida; te basta con exigir que el atributo
     * {@code fetch} de la anotación sea {@code LAZY} (o, como mínimo, que esté declarado).
     *
     * <p>Cuando lo instales, {@code E5_AU04InstaladaTest} exige que pase sobre tu producción
     * (si dejaste una relación en EAGER, te caza) y que muerda su fixture.
     */
    public static final ArchRule AU_04 = classes()   // {{TODO_1}} — reemplaza la regla entera
            .should().haveNameMatching(".*")             // tautología: pasa siempre, no vigila nada
            .because("cascarón sin instalar — TODO_1");

    /** AU-05 · Ningún test duerme: se usa Awaitility. */
    public static final ArchRule AU_05 = noClasses()
            .should().callMethod(Thread.class, "sleep", long.class)
            .because("Un Thread.sleep es una apuesta: pasa en tu máquina y falla en el CI. "
                     + "Se espera una condición con Awaitility, no un número de milisegundos.");

    /** AU-06 · Ningún bean se inyecta por campo. */
    public static final ArchRule AU_06 = noFields()
            .should().beAnnotatedWith(Autowired.class)
            .because("Inyección por constructor, siempre: un bean con @Autowired en el campo "
                     + "no se puede construir en un test sin levantar el contexto entero.");

    /** AU-07 · La infraestructura no conoce la web. */
    public static final ArchRule AU_07 = noClasses()
            .that().resideInAPackage("..infrastructure..")
            .should().dependOnClassesThat().resideInAPackage("..web..")
            .because("La infraestructura no sabe quién la llama. Si lo supiera, cambiar un "
                     + "DTO obligaría a recompilar el repositorio.");

    // -------------------------------------------------------------------------
    //  Condición a medida de AU-04.
    // -------------------------------------------------------------------------
    private static ArchCondition<JavaField> declararFetchLazyExplicito() {
        return new ArchCondition<>("declarar fetch = FetchType.LAZY explícitamente") {
            @Override
            public void check(JavaField campo, ConditionEvents eventos) {
                FetchType fetch = fetchDeclarado(campo);
                boolean cumple = fetch == FetchType.LAZY;
                eventos.add(new SimpleConditionEvent(campo, cumple,
                        "El campo " + campo.getFullName() + " usa fetch = "
                        + (fetch == null ? "EAGER (por omisión)" : fetch.name())
                        + " — debe declarar LAZY explícitamente"));
            }
        };
    }

    private static FetchType fetchDeclarado(JavaField campo) {
        if (campo.isAnnotatedWith(ManyToOne.class)) {
            return campo.getAnnotationOfType(ManyToOne.class).fetch();
        }
        if (campo.isAnnotatedWith(OneToOne.class)) {
            return campo.getAnnotationOfType(OneToOne.class).fetch();
        }
        return null;
    }
}
