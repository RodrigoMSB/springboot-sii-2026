package cl.dgt.web.controllers;

import cl.dgt.web.dto.SaludoDto;
import cl.dgt.web.dto.SolicitudSaludoDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Los seis endpoints del laboratorio, en el orden en que se escriben.
 *
 * <p>{@code @RestController} es lo que convierte esta clase en algo que atiende peticiones:
 * Spring la encuentra al arrancar, lee sus anotaciones, y arma la tabla de rutas. Nadie registra
 * nada a mano.
 */
@RestController
public class HolaController {

    /** La agenda de gente conocida. Sirve para que exista un caso de "no está" en el paso 6. */
    private static final List<String> CONOCIDOS = List.of("mundo", "Carolina", "Ignacio");

    // =========================================================================
    //  1 · EL PRIMER ENDPOINT
    // -------------------------------------------------------------------------
    //  Un método anotado con @GetMapping y poco más. Lo que devuelve el método
    //  es, literalmente, lo que llega al navegador.
    //  Qué se espera ver: `curl localhost:8082/hola` responde una frase en texto
    //  plano, sin comillas ni llaves.
    //  Para pensar: ¿quién decidió que este método atendiera /hola y no otra ruta?
    // =========================================================================
    @GetMapping("/hola")
    public String hola() {
        return "Hola, mundo.";
    }

    // =========================================================================
    //  2 · EL DATO VIENE EN LA URL
    // -------------------------------------------------------------------------
    //  Las llaves de {nombre} marcan un hueco en la ruta, y @PathVariable dice
    //  "mete ahí lo que venga". La ruta identifica UNA cosa concreta.
    //  Qué se espera ver: /hola/Carolina y /hola/Ignacio responden distinto con
    //  el mismo método escrito una sola vez.
    //  Para pensar: ¿qué pasa si se pide /hola/ sin nada detrás?
    // =========================================================================
    @GetMapping("/hola/{nombre}")
    public String holaANombre(@PathVariable String nombre) {
        return "Hola, " + nombre + ".";
    }

    // =========================================================================
    //  3 · EL DATO VIENE DESPUÉS DEL ?
    // -------------------------------------------------------------------------
    //  @RequestParam lee lo que va tras el `?`. La diferencia con el anterior es
    //  de intención: la ruta dice QUÉ cosa se pide; los parámetros, CÓMO se pide.
    //  `required = false` con `defaultValue` es lo que permite omitirlo.
    //  Qué se espera ver: /saludo?nombre=Carolina&formal=true cambia el tratamiento.
    //  Para pensar: ¿por qué el nombre aquí no va en la ruta, como en el paso 2?
    // =========================================================================
    @GetMapping("/saludo")
    public String saludo(@RequestParam String nombre,
                         @RequestParam(defaultValue = "false") boolean formal) {
        return formal ? "Buenos días, " + nombre + "." : "Hola, " + nombre + ".";
    }

    // =========================================================================
    //  4 · DEVOLVER UN OBJETO, NO UN TEXTO
    // -------------------------------------------------------------------------
    //  El método devuelve un record Java. Nadie escribe llaves ni comillas: el
    //  starter-web trae Jackson, y Spring lo usa para convertir el objeto en JSON
    //  antes de mandarlo.
    //  Qué se espera ver: la respuesta ahora tiene llaves y los nombres de los
    //  campos del record salen tal cual se llaman en Java.
    //  Para pensar: si se le cambia el nombre a un campo del record, ¿qué se rompe?
    // =========================================================================
    @GetMapping("/saludos/{nombre}")
    public ResponseEntity<SaludoDto> saludoDe(@PathVariable String nombre) {
        if (!CONOCIDOS.contains(nombre)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new SaludoDto("Hola, " + nombre + ".", nombre, false));
    }

    // =========================================================================
    //  5 · EL DATO VIENE EN EL CUERPO
    // -------------------------------------------------------------------------
    //  Tercera y última forma de recibir datos. @RequestBody es el camino inverso
    //  del paso 4: el JSON que manda el cliente se convierte en un objeto Java
    //  antes de que este método empiece.
    //  Qué se espera ver: un POST con {"nombre":"Carolina","formal":true} responde
    //  201 y el saludo formal.
    //  Para pensar: ¿por qué esto no se puede probar desde la barra del navegador?
    // =========================================================================
    @PostMapping("/saludos")
    public ResponseEntity<SaludoDto> crearSaludo(@RequestBody SolicitudSaludoDto solicitud) {
        String texto = solicitud.formal()
                ? "Buenos días, " + solicitud.nombre() + "."
                : "Hola, " + solicitud.nombre() + ".";
        SaludoDto saludo = new SaludoDto(texto, solicitud.nombre(), solicitud.formal());
        return ResponseEntity.status(HttpStatus.CREATED).body(saludo);
    }

    // =========================================================================
    //  6 · EL CÓDIGO DE ESTADO — no hay método nuevo, está en los dos de arriba
    // -------------------------------------------------------------------------
    //  ResponseEntity es el sobre que envuelve al cuerpo y lleva escrito el
    //  código: 200 cuando está, 404 cuando no está, 201 cuando se creó algo.
    //  En el paso 6 los dos métodos anteriores se envuelven así; aquí ya llegan
    //  envueltos porque este proyecto está terminado.
    //  Qué se espera ver: /saludos/Carolina da 200 y /saludos/Pedro da 404 vacío.
    //  Para pensar: ese 404 no explica nada. ¿Le sirve así a quien lo recibe?
    // =========================================================================
}
