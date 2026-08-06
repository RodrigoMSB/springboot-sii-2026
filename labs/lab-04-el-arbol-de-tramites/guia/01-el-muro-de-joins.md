# Guía 01 · El muro de JOINs

**Acto 1 · El choque** 💥

Levanta el starter y pide un trámite:

```bash
./bin/start-lab.sh --dir starter
curl http://localhost:8099/api/v1/tramites/1
```

Ahora mira lo que Hibernate hizo por debajo:

```bash
grep -A25 '    select' ../lab-04-el-arbol-de-tramites/.estado/dgt.log | head -40
```

Verás una consulta con `join contribuyente`, `left join formulario29`, `left join folio`… y
luego **más consultas**. Para responder UNA ficha viajó medio árbol de la base.

| Pregunta | Tu respuesta |
|---|---|
| ¿Cuántas tablas aparecen en el primer `from`/`join`? Cuéntalas. | |
| ¿Cuántas sentencias `select` se dispararon en total? | |
| Abre `domain/entity/Tramite.java`. ¿Qué dice el comentario del practicante? | |
| Funciona, ¿no? ¿Por qué habría de importar? | |

> Guárdate esa última respuesta. La vamos a necesitar la próxima semana.

➡️ Siguiente: [`02-el-parche-del-default.md`](02-el-parche-del-default.md)
