package cl.dgt.auditoria.controllers;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

// El paso 2 mira aquí: lo que hay en MI base, y solo en la mía. La tabla del
// servicio de al lado no aparece — no está escondida, es que no existe.
@RestController
public class BaseController {

    private final JdbcTemplate jdbc;

    public BaseController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/mi-base")
    public Map<String, Object> misTablas() {
        List<String> tablas = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables "
                        + "WHERE table_schema = 'public' ORDER BY table_name", String.class);
        return Map.of("servicio", "auditoria", "tablas", tablas);
    }
}
