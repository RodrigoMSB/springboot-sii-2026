# Guía 03 · La proyección, y el número

**Acto 3 · La forma correcta** ✅

## TODO_1 · Hacer volar el listado sin cambiar lo que devuelve

El test `E1_ContadorDeConsultasIT` exige: una página cuesta **≤ 3 consultas**, sin importar
cuántos trámites haya. Iterar entidades no lo logra (es el N+1). Una **proyección**, sí.

En `TramiteRepository`, escribe una consulta paginada que traiga SOLO lo que la tabla pinta:

```java
@Query("""
    SELECT new cl.dgt.tramites.application.TramiteResumenDto(
               t.id, t.tipo, cast(t.estado as string), c.rut)
    FROM Tramite t JOIN t.contribuyente c
    """)
Page<TramiteResumenDto> resumenPaginado(Pageable pagina);
```

Y en `ListadoService`, usa `resumenPaginado(pagina)` en vez de `findAll(pagina)`.

Comprueba las dos cosas:

```bash
cd .. && ./bin/90-validar.sh --dir starter    # E1 en verde: ≤ 3 consultas
```

Y que **E2 no cambió**: el listado devuelve lo mismo que antes. Eso es refactorizar — mismo
comportamiento, distinto costo. La pista del Lab 04 (`JOIN FETCH`) se cobra aquí: esta era la
pregunta que no te habías hecho.

## TODO_2 · Tu prueba de integración

Abre `src/test/.../integracion/ListadoIntegracionTest.java`. Bórralo y escríbelo: una IT
completa con `@SpringBootTest(RANDOM_PORT)` + Testcontainers (`@ServiceConnection`) +
`RestTestClient`. Prueba que el listado pagina de verdad (dos páginas, contenido distinto).
Mira `E2_ListadoFuncionalIT` para el patrón.

## Compara las dos soluciones

```bash
./bin/90-validar.sh --dir solucion-con-n1    # pasa lo funcional, FALLA el contador
./bin/90-validar.sh --dir solucion            # pasa TODO
```

Dos códigos que hacen lo mismo. Uno cuesta 13 consultas, el otro 3. Eso es optimizar.

## Cierre

```bash
./bin/90-validar.sh --dir starter
```

`🏆 LAB 05 APROBADO`, y el reporte.

➡️ ¿Sobró tiempo? [`99-desafio-el-detalle-pesado.md`](99-desafio-el-detalle-pesado.md)
