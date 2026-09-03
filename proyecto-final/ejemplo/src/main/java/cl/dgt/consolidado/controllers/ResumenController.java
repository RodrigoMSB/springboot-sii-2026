// El endpoint. Recibe, delega y devuelve: no calcula nada.
// Tu equivalente: `controllers/ConsolidadoController`, con `/consolidados/{rut}`.
package cl.dgt.consolidado.controllers;

import cl.dgt.consolidado.dto.ResumenOficina;
import cl.dgt.consolidado.services.ResumenService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class ResumenController {

    private final ResumenService servicio;

    public ResumenController(ResumenService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/resumenes/{codigo}")
    public ResumenOficina resumen(
            @PathVariable String codigo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        return servicio.delPeriodo(codigo, desde, hasta);
    }
}
// ^ TRES COSAS QUE MIRAR, porque las tres son del encargo:
//
//   1. `@RequestParam` SIN `required = false` y sin valor por defecto: los dos parámetros son
//      OBLIGATORIOS. Si falta uno, Spring lanza antes de entrar aquí y el manejador lo convierte
//      en 400 con cuerpo. No hay que escribir ningún `if`.
//
//   2. `LocalDate` y no `String`: Spring parsea la fecha, y una fecha mal escrita también acaba
//      en 400. Traer un `String` y parsearlo a mano sería escribir código para hacer peor lo que
//      el framework ya hace.
//
//   3. NO HAY LÓGICA. Ni suma, ni `if` de negocio, ni acceso a repositorios. El controller recibe,
//      delega y devuelve — y por eso su test puede ser un `@WebMvcTest` con el servicio doblado.
//
//   Y no lleva ninguna anotación de OpenAPI: `springdoc` lo descubre solo y el endpoint aparece
//   en `/swagger-ui.html` sin que haya que decir nada.
