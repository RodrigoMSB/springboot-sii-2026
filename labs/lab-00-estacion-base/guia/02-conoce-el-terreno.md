# Guía 02 · Conoce el terreno

Tu estación está lista. Ahora, cinco minutos para saber dónde estás parado.

---

## El mundo

Trabajas en la **DGT**, Dirección General de Tributación. Es ficticia, y es idéntica a un
servicio de impuestos real: contribuyentes que declaran, trámites que avanzan, folios que
no pueden repetirse.

- **Mi DGT** es el portal que usan los contribuyentes. No lo construyes tú.
- **`dgt-tramites-api`** es lo que hay **detrás del botón**. Eso sí lo construyes tú.
- **Carolina** es tu jefa. Va a pedirte cosas incómodas y va a tener razón.
- **Valentina Rojas** (`11111111-1`) y **Comercial Andina SpA** (`12345678-5`) son
  contribuyentes. Los vas a ver mil veces.

---

## El repositorio, en cuatro carpetas

```
dgt-tramites-api/    ← la aplicación. Crece contigo, lab a lab.
labs/                ← aquí vives tú. Cada lab, una carpeta.
docs/                ← el temario, las decisiones, las especificaciones
tools/               ← tooling interno del repo (no lo necesitas)
```

Dentro de `dgt-tramites-api/`, cuatro paquetes y una regla:

| Paquete | Qué vive ahí |
|---|---|
| `domain/` | Las entidades y las reglas del negocio. **No sabe que existen Spring ni HTTP.** |
| `application/` | Los servicios. Aquí sí vive Spring. |
| `infrastructure/` | Los repositorios: cómo se habla con la base de datos. |
| `web/` | Los controladores y los DTO. La frontera con el mundo. |

**La regla:** un controlador jamás toca una entidad. No es un consejo — hay siete tests de
arquitectura que fallan si lo intentas. Los conocerás en el Módulo 3.

---

## Levanta la DGT y mírala a los ojos

```bash
cd labs/lab-00-estacion-base
./bin/start-lab.sh
```

Cuando termine, haz lo que te pide:

```bash
curl http://localhost:8080/api/contribuyentes/11111111-1
```

```json
{"rut":"11111111-1","razonSocial":"Valentina Rojas"}
```

Fíjate en lo que **no** está ahí. En la base de datos, Valentina tiene además un
`puntaje_riesgo_interno`. No aparece en la respuesta, y no aparecerá jamás: es un juicio
interno del servicio sobre una persona. Filtrarlo no sería un bug de formato — sería una
filtración. Dos guardianes lo impiden. En el Módulo 2 sabrás cuáles.

Cuando termines de mirar:

```bash
./bin/99-destruir.sh
```

Tu máquina queda como estaba. Los contenedores de tus otros proyectos, intactos.

---

## Cómo pedir ayuda (para que te la den rápido)

1. Mira [`docs/troubleshooting.md`](../docs/troubleshooting.md). Cada problema tiene número.
2. Si tu problema está ahí, **cita el número**: *"me pasa la T-03"*.
3. Si no está, manda tres cosas: la salida completa del script (pegada, no una captura),
   tu sistema operativo, y qué esperabas que pasara.

Nunca escribas solo *"no me funciona"*. Nadie puede ayudarte con eso — ni tú mismo, dentro
de tres días, cuando lo vuelvas a leer.

---

## Nos vemos en la sesión 1

Llegas con la estación lista y con la DGT levantada al menos una vez. Eso es todo lo que
se te pide hoy.

Carolina te espera con un problema.
