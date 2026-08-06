# Guía 03 · LAZY, y el guardián

**Acto 3 · La forma correcta** ✅

## TODO_1 · LAZY explícito + instalar AU-04

Corrige **cada** relación de las entidades a `fetch = FetchType.LAZY`, escrito a mano, aunque
el default ya sea LAZY.

Luego instala el guardián. Abre `arquitectura/ReglasDeLaCasa.java`: `AU_04` es un cascarón.
Reemplázalo por una regla sobre campos (`fields()`) anotados con `@ManyToOne` o `@OneToOne`,
que exija que declaren `fetch`. `E5_AU04InstaladaTest` exige que pase sobre tu producción y
muerda su fixture.

> ⚠️ **Aviso que ahorra una tarde:** si instalas AU-04 pero olvidaste corregir UNA relación a
> LAZY, AU-04 te caza a ti (`E5` falla en "pasa sobre producción", nombrando el campo).
> Corrige primero las entidades.

Comprueba que ya no cargas de más:

```bash
cd .. && ./bin/start-lab.sh --dir starter
curl http://localhost:8099/api/v1/tramites/1
grep -A10 '    select' .estado/dgt.log | head   # el primer select, sin muro
```

## TODO_2 · Consultas derivadas

Abre `TramiteRepository`. Cada método tiene un cuerpo `default` que lanza. **Borra el cuerpo y
deja la firma:** Spring Data lee el nombre y escribe el JPQL. `E2` exige cuatro consultas.

## TODO_3 · JPQL multi-entidad

Reemplaza `presentadosDelPeriodo` por una declaración con `@Query`. Trámites de un período
cuyo F29 no está en BORRADOR, parámetro nombrado `:periodo`.

> **NO uses `JOIN FETCH`.** Existe, y es la respuesta a una pregunta que aún no te has hecho —
> la próxima semana te la vas a hacer. Hay un test (`E3`) que verifica que no lo usaste.

## TODO_4 · El reporte con JdbcClient

Implementa `ReporteService.totalDeclaradoPorPeriodo` con `JdbcClient`: un SQL que sume
`linea_f29.monto` agrupado por `formulario29.periodo`, mapeado a `TotalPorPeriodo`. Sin tocar
una entidad. `E4` exige el total de 2026-04 = 910.000.

## Cierre

```bash
./bin/90-validar.sh --dir starter
```

`🏆 LAB 04 APROBADO`, y completa el reporte.

➡️ ¿Sobró tiempo? [`99-desafio-el-arbol-a-pedido.md`](99-desafio-el-arbol-a-pedido.md)
