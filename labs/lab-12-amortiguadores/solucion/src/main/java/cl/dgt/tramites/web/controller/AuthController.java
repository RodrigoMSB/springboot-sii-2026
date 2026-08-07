package cl.dgt.tramites.web.controller;

import cl.dgt.tramites.application.EmisorDeTokens;
import cl.dgt.tramites.web.dto.CredencialesRequest;
import cl.dgt.tramites.web.dto.TokenDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * La puerta. Único endpoint público que emite credenciales: recibe rut+clave, devuelve un JWT.
 * Todo lo demás exige ya traer el token.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final EmisorDeTokens emisor;

    public AuthController(EmisorDeTokens emisor) {
        this.emisor = emisor;
    }

    @PostMapping("/login")
    public TokenDto login(@Valid @RequestBody CredencialesRequest credenciales) {
        return emisor.emitir(credenciales.rut(), credenciales.clave());
    }
}
