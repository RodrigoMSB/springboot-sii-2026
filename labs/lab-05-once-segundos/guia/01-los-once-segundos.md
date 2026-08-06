# Guía 01 · Los once segundos

**Acto 1 · El choque** 💥

Siembra el escenario y cronometra:

```bash
./bin/start-lab.sh --dir starter --lotes 5000
{ time curl -s -o /dev/null "http://localhost:8099/api/v1/tramites?page=0&size=5000"; }
```

Mira el `real`. Y ahora el número de consultas:

```bash
grep -c '    select' .estado/dgt.log
```

Miles de consultas. Segundos de reloj. Para responder UNA petición.

| Pregunta | Tu respuesta |
|---|---|
| ¿Cuántos segundos tardó? (transcribe el `real`) | |
| ¿Cuántas consultas se dispararon? | |
| No agregaste código desde ayer. ¿Qué cambió? | |
| Carolina no quiere oír "optimizar" sin un número. ¿Cuál es tu número? | |

➡️ Siguiente: [`02-medir-antes-de-tocar.md`](02-medir-antes-de-tocar.md)
