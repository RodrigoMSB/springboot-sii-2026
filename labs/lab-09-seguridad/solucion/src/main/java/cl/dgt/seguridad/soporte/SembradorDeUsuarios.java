package cl.dgt.seguridad.soporte;

import cl.dgt.seguridad.entities.Usuario;
import cl.dgt.seguridad.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SembradorDeUsuarios implements CommandLineRunner {

    private final UsuarioRepository repositorio;
    private final PasswordEncoder codificador;

    public SembradorDeUsuarios(UsuarioRepository repositorio, PasswordEncoder codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }

    @Override
    public void run(String... args) {
        if (repositorio.count() > 0) {
            return;
        }
        // ana y luis tienen LA MISMA clave a propósito: el paso 3 muestra que sus hashes difieren.
        repositorio.save(new Usuario("ana", codificador.encode("secreta"), "ADMIN"));
        repositorio.save(new Usuario("luis", codificador.encode("secreta"), "USUARIO"));

        System.out.println("[semilla] usuarios ana/secreta (ADMIN) y luis/secreta (USUARIO)");
        repositorio.findAll().forEach(u ->
                System.out.printf("[semilla] %-5s %-8s %s%n", u.getNombre(), u.getRol(), u.getClaveHash()));
    }
}
