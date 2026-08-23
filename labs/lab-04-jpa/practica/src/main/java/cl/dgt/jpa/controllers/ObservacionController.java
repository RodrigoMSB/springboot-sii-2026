package cl.dgt.jpa.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/observaciones")
public class ObservacionController {

    @GetMapping
    public List<?> listar(@RequestParam(required = false) String autor) {
        // Devuelve todas, o las del autor si viene el parámetro.
        // escribe aquí
        return List.of();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> porId(@PathVariable Long id) {
        // Devuelve la observación, o 404 si no está.
        // escribe aquí
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> nueva) {
        // Guarda la observación que llega y responde 201 con la guardada.
        // escribe aquí
        return ResponseEntity.status(501).build();
    }
}
