# Spike S-1 · ArchUnit core sobre Spring Boot 4.1 + Java 25

> Ordenado por [SPEC-000 §10.B](../specs/SPEC-000-especificacion-maestra.md). Proyecto Maven
> desechable, ejecutado **fuera del repo** (scratchpad de sesión). Aquí queda el reporte con
> los comandos, las salidas citadas y el fixture negativo, para que la evidencia no muera
> con la sesión.

## Veredicto

## ✅ VIABLE

Las tres afirmaciones técnicas del §2 y del §6 se sostienen. **AU-02 no necesita reescritura.**

## Entorno verificado

| Componente | Resuelto | Cómo se comprobó |
|---|---|---|
| Java | **Temurin 25.0.3+9 LTS** | `java -version`; ArchUnit lo detecta: `Detected Java version 25.0.3` |
| Spring Boot | **4.1.0** (`spring-boot-starter-parent`) | resuelto desde Maven Central, `BUILD SUCCESS` |
| JUnit Jupiter | **6.0.3**, heredado del BOM | `mvn dependency:tree` → `org.junit.jupiter:junit-jupiter-api:jar:6.0.3:test` |
| ArchUnit | **`archunit` core 1.4.2**, sin `archunit-junit5` | declarado en el `pom.xml` del spike |

Confirma dos supuestos de la SPEC-000 §2: que Boot 4.1 trae **JUnit 6** (no 5), y que el
artefacto core basta.

## Pregunta 1 — ¿Corre una regla como `@Test` Jupiter común, sin `@AnalyzeClasses`?

**Sí.** Reglas construidas con `ClassFileImporter` + `ArchRuleDefinition`, ejecutadas como
`@Test` Jupiter ordinarios bajo `mvn test`:

```
[INFO] Running cl.dgt.arch.SpikeArchUnitTest
17:52:10.625 [main] INFO com.tngtech.archunit.core.PluginLoader -- Detected Java version 25.0.3
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.659 s
[INFO] BUILD SUCCESS
```

Sin dependencia del runner: la versión de JUnit puede cambiar sin tocar las reglas.

## Pregunta 2 — ¿`dependOnClassesThat()` caza una dependencia que vive **solo** en un parámetro de tipo genérico?

**Sí, y lo nombra con precisión.**

### El fixture negativo

La única mención a `Contribuyente` está en el genérico del retorno. El cuerpo devuelve
`null` para que no exista ninguna otra referencia:

```java
@RestController
public class ControladorQueFiltra {
    @GetMapping("/contribuyentes/{rut}")
    public ResponseEntity<Contribuyente> ver(@PathVariable String rut) {
        return null;   // el folio se filtra por el generico
    }
}
```

### Prueba de que el aislamiento es real (bytecode)

El descriptor del método **no** menciona `Contribuyente`; el cuerpo es `aconst_null; areturn`:

```
public org.springframework.http.ResponseEntity<cl.dgt.domain.Contribuyente> ver(java.lang.String);
  descriptor: (Ljava/lang/String;)Lorg/springframework/http/ResponseEntity;
       0: aconst_null
       1: areturn
```

Las dos únicas ocurrencias de `cl/dgt/domain/Contribuyente` en el `.class` viven en el
atributo `Signature`, no en el código ni en el descriptor:

```
#20 = Utf8   (Ljava/lang/String;)Lorg/springframework/http/ResponseEntity<Lcl/dgt/domain/Contribuyente;>;
Signature: #20
```

### La regla, y lo que dice al fallar

```java
noClasses().that().areAnnotatedWith(RestController.class)
    .should().dependOnClassesThat().resideInAPackage("..domain..")
    .because("Un controlador jamas toca la entidad. Ese fue el folio filtrado (Lab 02).");
```

Salida real de ArchUnit contra el fixture:

```
Architecture Violation [Priority: MEDIUM] - Rule 'no classes that are annotated with
@RestController should depend on classes that reside in a package '..domain..', because
Un controlador jamas toca la entidad. Ese fue el folio filtrado (Lab 02).' was violated (1 times):
Method <cl.dgt.fixtures.violaciones.ControladorQueFiltra.ver(java.lang.String)> has generic
return type <org.springframework.http.ResponseEntity<cl.dgt.domain.Contribuyente>> with type
argument depending on <cl.dgt.domain.Contribuyente> in (ControladorQueFiltra.java:0)
```

ArchUnit lee el atributo `Signature` y dice, textualmente, *"with type argument depending on"*.
La regla **falla con el fixture** y **pasa sin él** (controlador que devuelve
`ResponseEntity<ContribuyenteDto>`): las dos mitades del contrato de la meta-regla del §6.3.

## La trampa, confirmada por experimento

La redacción ingenua sobre el **tipo de retorno crudo** pasa en verde con el fixture presente:

```java
methods().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
    .should().notHaveRawReturnType(/* una clase de ..domain.. */);
// PASA. El tipo crudo es ResponseEntity; el generico se le escapa.
```

Es exactamente el guardián-adorno que la SPEC-000 §6 nota 2 prohíbe: verde mientras el folio
se filtra. **La nota 2 no es una precaución teórica: está medida.**

## Consecuencias para la construcción

1. `pom.xml` de la app canónica: `archunit` core `1.4.2`, `scope=test`. Nada de `archunit-junit5`.
2. AU-02 se escribe con `dependOnClassesThat()`, tal como está redactada en §6. Sin cambios.
3. Cada regla se entrega con su fixture negativo bajo
   `src/test/java/.../fixtures/violaciones/` (fuera del classpath de producción) y un test
   que verifica que la regla **muerde** — el patrón del punto 2 de este spike sirve de plantilla.

## Reproducción

```bash
mvn -B test                     # 4 tests, BUILD SUCCESS
javap -v target/test-classes/cl/dgt/fixtures/violaciones/ControladorQueFiltra.class
```

El proyecto era desechable y no se versiona: lo reproducible es este reporte.
