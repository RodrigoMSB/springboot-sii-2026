package cl.dgt.seguridad.seguridad;

import cl.dgt.seguridad.repositories.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// El puente entre la tabla `usuario` y lo que Spring Security entiende.
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repositorio;

    public UsuarioDetailsService(UsuarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String nombre) {
        return repositorio.findByNombre(nombre)
                .map(u -> User.withUsername(u.getNombre())
                        .password(u.getClaveHash())
                        .roles(u.getRol())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("No existe el usuario " + nombre));
    }
}
