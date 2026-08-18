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

@RestController
public class HolaController {

    private static final List<String> CONOCIDOS = List.of("mundo", "Carolina", "Ignacio");

    @GetMapping("/hola")
    public String hola() {
        return "Hola, mundo.";
    }

    @GetMapping("/hola/{nombre}")
    public String holaANombre(@PathVariable String nombre) {
        return "Hola, " + nombre + ".";
    }

    @GetMapping("/saludo")
    public String saludo(@RequestParam String nombre,
                         @RequestParam(defaultValue = "false") boolean formal) {
        return formal ? "Buenos días, " + nombre + "." : "Hola, " + nombre + ".";
    }

    @GetMapping("/saludos/{nombre}")
    public ResponseEntity<SaludoDto> saludoDe(@PathVariable String nombre) {
        if (!CONOCIDOS.contains(nombre)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new SaludoDto("Hola, " + nombre + ".", nombre, false));
    }

    // El 201 y la cabecera Location no salen solos: se declaran.
    @PostMapping("/saludos")
    public ResponseEntity<SaludoDto> crearSaludo(@RequestBody SolicitudSaludoDto solicitud) {
        String texto = solicitud.formal()
                ? "Buenos días, " + solicitud.nombre() + "."
                : "Hola, " + solicitud.nombre() + ".";
        SaludoDto saludo = new SaludoDto(texto, solicitud.nombre(), solicitud.formal());
        return ResponseEntity.status(HttpStatus.CREATED).body(saludo);
    }

}
