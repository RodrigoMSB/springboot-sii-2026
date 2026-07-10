# Guía 01 · El curl que duele

**Acto 1 · El choque** 💥

Levanta el starter y pídele la ficha de Comercial Andina:

```bash
cd starter
./mvnw spring-boot:run
# en otra terminal:
curl http://localhost:8099/api/v1/contribuyentes/12345678-5/ficha
```

```json
{"rut":"12345678-5","razonSocial":"Comercial Andina SpA","puntajeRiesgoInterno":67,"id":2}
```

Cuenta los campos. Cuatro. Uno de ellos, `puntajeRiesgoInterno`, es el número con que la
DGT decide a quién fiscaliza. Acaba de salir por una API pública, en texto plano, a quien
sepa el RUT.

Ahora mira el código que lo produjo: `web/controller/FichaController.java`. Devuelve la
entidad `Contribuyente` directamente. Nadie lo hizo con malicia. Estaba "para ayer".

> *«No te llamé porque hay un bug. Te llamé porque nada lo impidió.»*

| Pregunta | Tu respuesta |
|---|---|
| ¿Qué campos devuelve la ficha? | |
| ¿Cuáles de ellos debería ver el público? | |
| ¿Qué clase se está serializando, exactamente? | |
| ¿Qué habría pasado si la entidad tuviera diez campos sensibles en vez de uno? | |

➡️ Siguiente: [`02-el-parche-que-engana-al-ojo.md`](02-el-parche-que-engana-al-ojo.md)
