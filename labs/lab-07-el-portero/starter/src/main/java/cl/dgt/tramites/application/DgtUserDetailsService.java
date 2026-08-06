package cl.dgt.tramites.application;

import cl.dgt.tramites.infrastructure.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * El login real: valida contra la tabla {@code usuario} de la semilla (BCrypt, cost 10).
 *
 * <p>Cobra la siembra del primer día: los hashes ya viven en la V2, versionados a propósito
 * (un hash no es un secreto). Aquí, por fin, sirven para algo.
 *
 * <p>Devuelve el {@code claveHash} como "password" del {@link UserDetails}: quien COMPARA es el
 * {@code PasswordEncoder} (BCrypt), no este servicio. El rol se convierte en autoridad
 * {@code ROLE_<rol>} — la convención que {@code hasRole(...)} espera.
 */
@Service
public class DgtUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public DgtUserDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String rut) throws UsernameNotFoundException {
        return usuarios.findByRut(rut)
                .map(u -> User.withUsername(u.getRut())
                        .password(u.getClaveHash())
                        .roles(u.getRol().name())   // ROLE_FUNCIONARIO, ROLE_CONTRIBUYENTE, ...
                        .build())
                // El mensaje NO distingue "no existe" de "clave mala": el provider lo convierte en
                // BadCredentials genérico. Distinguir es regalarle a un atacante qué RUT existen.
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
    }
}
