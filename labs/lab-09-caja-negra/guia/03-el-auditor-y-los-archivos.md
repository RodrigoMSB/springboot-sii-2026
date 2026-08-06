# Guía 03 · El auditor y los archivos (TODO_3 y TODO_4)

## TODO_3 — El auditor invisible (AOP)

Un aspecto que audita cada invocación al dominio, sin tocar el negocio:

```java
@Aspect @Component
public class AspectoDeAuditoria {
    @Around("execution(* cl.dgt.tramites.application..*Service.*(..))")
    public Object auditar(ProceedingJoinPoint punto) throws Throwable {
        long inicio = System.nanoTime();
        try {
            Object r = punto.proceed();
            log.info("invocacion metodo={} args={} ms={} resultado=ok", metodo, enmascarar(args), ms);
            return r;
        } catch (Throwable e) {
            log.warn("invocacion metodo={} ms={} resultado=error tipo={}", metodo, ms, ...);
            throw e;   // NO se traga: registra CON CONTEXTO y re-lanza
        }
    }
}
```

Dos cuidados:
- **Enmascarar:** los argumentos de texto van parciales (`123***`). La clave jamás llega —el
  emisor de tokens no es un `*Service`, el pointcut no lo toca—.
- **El límite del proxy:** una autoinvocación (`this.otro()`) NO pasa por el proxy, así que el
  aspecto no la ve. Es la misma trampa de `@Transactional` (Lab 06). El test `E3` lo prueba.

El código de negocio queda **idéntico** al Lab 08 (byte a byte): el auditor es invisible.

## TODO_4 — Los adjuntos con desconfianza (M11)

Un archivo que sube un usuario es entrada hostil:

```java
byte[] cabecera = archivo.getInputStream().readNBytes(16);
String mimeReal = DetectorDeMime.detectar(cabecera);      // magic bytes, NO el Content-Type
String nombre  = sanearNombre(archivo.getOriginalFilename()); // ../../etc/passwd -> passwd
```

- **Tipo REAL:** los magic bytes (`%PDF`, `MZ`), no la extensión ni el header que el cliente
  declara. Un `.exe` disfrazado de `.pdf` se caza. El test `E4` lo prueba.
- **Path traversal:** quedarse con el último segmento del nombre. Sin barras, no hay escape.
- **Tamaño:** limitado (5 MB); y la **descarga en streaming**, sin cargar el archivo en memoria.

## Cierra

```bash
./bin/90-validar.sh --dir starter
```

Los cuatro tests en verde, y tu sistema sabe contar lo que hizo. Completa
`plantillas/reporte-entregable.md` y entrégalo.
