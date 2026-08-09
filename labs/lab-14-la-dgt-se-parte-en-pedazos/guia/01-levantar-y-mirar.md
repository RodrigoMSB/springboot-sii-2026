# Bloque 1 · Levantar y mirar (~20 min)

*Antes de romper nada, hay que saber qué hay.*

---

## 1 · Levanta el sistema

```bash
cd labs/lab-14-la-dgt-se-parte-en-pedazos
./bin/start-lab.sh
```

La primera vez compila cinco proyectos y construye cinco imágenes: entre cuatro y seis
minutos, según tu red. Las siguientes, **menos de un minuto**.

Mientras arranca, léete el mensaje que va imprimiendo. Dice tres cosas distintas, y la
diferencia entre ellas es la primera lección del día:

| Lo que dice | Qué significa de verdad |
|---|---|
| *«Cinco imágenes listas»* | Los procesos pueden arrancar |
| *«Las SEIS piezas están anotadas en el registro»* | Cada una existe y se presentó |
| *«El portal devuelve respuestas completas de forma sostenida»* | El sistema **funciona** |

Entre la segunda y la tercera hay varios segundos. **Con las seis piezas sanas, el sistema
todavía no sirve.** ¿Por qué? Porque el balanceador de `dgt-tramites` aún no se ha bajado
la lista de instancias del registro. Sabe que existe un servicio llamado
`dgt-contribuyentes`; todavía no sabe dónde está.

Guárdate esa idea. Vuelve en el bloque 3, y otra vez en el 4.

---

## 2 · Mira el registro

Abre **http://localhost:8761** en el navegador.

En *Instances currently registered with Eureka* hay **seis** filas. Cuéntalas:

- `DGT-REGISTRO` — sí, el registro se anota en su propia guía telefónica
- `DGT-CONFIG`
- `DGT-PORTAL`
- `DGT-CONTRIBUYENTES` — **dos** filas, dos instancias
- `DGT-TRAMITES`

Fíjate en la columna de la derecha: cada instancia tiene su dirección y su puerto. Los de
contribuyentes y trámites son **números raros** — 34521, 51044, lo que haya tocado. Nadie
los eligió; nadie los conoce; nadie los necesita.

**Recarga la página cada pocos segundos.** No cambia nada, y ese «nada» son las seis piezas
llamando por teléfono a decir que siguen vivas.

---

## 3 · Cruza el portal

```bash
curl -s http://localhost:8099/api/v1/tramites
```

```json
[
  {
    "id": 1,
    "tipo": "DECLARACION_F29",
    "estado": "EN_PROCESO",
    "rutContribuyente": "11111111-1",
    "nombreContribuyente": "Valentina Rojas",
    "atendidoPor": "dgt-contribuyentes:801639643da0"
  },
  ...
]
```

> `curl` lo devuelve **en una sola línea**; aquí está indentado para que se lea. Si quieres
> verlo así en tu máquina, pega la URL en el navegador o abre la respuesta en tu IDE — no
> hace falta instalar nada.

Ahora mira ese JSON con cuidado, porque **hay dos servicios ahí dentro**:

- `id`, `tipo`, `estado`, `rutContribuyente` → salieron de la base de `dgt-tramites`
- `nombreContribuyente`, `atendidoPor` → **vinieron por la red**, de otra pieza

Esa línea invisible que parte el JSON en dos es toda la arquitectura de hoy. Lo de arriba
no puede fallar. Lo de abajo, sí.

**Y fíjate en `atendidoPor`:** ejecuta el `curl` cuatro o cinco veces y mira cómo cambia el
identificador. Es el balanceo, ya funcionando, sin que hayas configurado nada.

---

## 4 · Comprueba que la frontera existe

`dgt-tramites` necesita el nombre del contribuyente. ¿Por qué no lo consulta y ya está?

```bash
cd sistema
docker compose exec -T postgres psql -U svc_tramites -d dgt_contribuyentes -c 'SELECT 1'
```

```
psql: error: connection to server failed: FATAL:  permission denied for database "dgt_contribuyentes"
```

**Ahí está la frontera.** No es una convención del equipo ni una nota en un README: es un
`GRANT` que no existe. Está en `sistema/db-init/01-bases-y-usuarios.sql`.

Si ese permiso existiera, alguien acabaría haciendo el JOIN «rápido» para ahorrarse la
llamada HTTP, y en ese momento dejarían de ser dos servicios para pasar a ser un **monolito
distribuido**: dos despliegues, dos procesos, una base que los ata, todos los costos de
partir el sistema y ninguna de sus ventajas.

---

## 5 · Mira de dónde sale la configuración

El puerto del portal es el 8099. ¿Dónde está escrito?

```bash
grep -rn "8099" sistema/dgt-portal/src/    # no está
cat sistema/config-repo/dgt-portal.yml     # aquí
```

Y el propio Config Server te lo sirve por HTTP, que es exactamente lo que hacen los
servicios al arrancar:

```bash
curl -s http://localhost:8888/dgt-portal/default | head -30
```

Cambiar una ruta del gateway es **editar un archivo de texto y reiniciar el portal**. No se
recompila, no se despliega un jar nuevo, no se toca código.

Ahora la contrapartida, que hay que ver también: abre
`sistema/config-repo/application.yml`. Ese archivo se lo sirve a **todos**. Un error ahí no
tumba un servicio: tumba seis. Una sola fuente de verdad es también un solo sitio donde
equivocarse para todo el mundo a la vez.

---

## 6 · ⭐ Dibújalo

**Esta es la entrega del bloque, y es a mano.** En la plantilla del reporte hay un hueco.

Dibuja las seis piezas y las flechas entre ellas, **con dos tipos de flecha distintos**:

- **Flecha sólida** — tráfico real, el de cada petición del contribuyente
- **Flecha de puntos** — «me anoto en» y «pido mi configuración a»

Cuando lo tengas, hazte estas tres preguntas y anota la respuesta:

1. Si tapas `dgt-registro`, **¿cuántas flechas sólidas desaparecen?**
2. Si tapas `dgt-config`, ¿cuántas desaparecen?
3. Si tapas `dgt-portal`, ¿cuántas?

Las respuestas no son las que parecen, y el bloque 4 va sobre la primera.

*(En `diagramas/el-mapa-del-sistema.mermaid` está la versión oficial. Mírala **después** de
haber dibujado la tuya: comparar los dos dibujos enseña más que copiar el bueno.)*

---

## Antes de seguir

Deja el sistema levantado. El bloque 2 empieza matándolo.

**Si algo no arrancó:** `docs/troubleshooting.md`, tabla numerada. Y si el arranque se pasó
de los cuatro minutos, mira T-1 antes de reintentar.
