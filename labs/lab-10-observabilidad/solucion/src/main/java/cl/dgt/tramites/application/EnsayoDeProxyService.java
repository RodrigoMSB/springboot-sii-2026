package cl.dgt.tramites.application;

import org.springframework.stereotype.Service;

/**
 * Existe para DEMOSTRAR el límite del proxy (lo prueba el enunciado). {@link #externo()} llama a
 * {@link #interno()} con {@code this.}: esa llamada NO pasa por el proxy, así que el aspecto de
 * auditoría ve {@code externo} pero NO {@code interno}. Es la misma trampa de {@code @Transactional}.
 */
@Service
public class EnsayoDeProxyService {

    public String externo() {
        return interno();          // autoinvocación: el proxy (y el aspecto) no se entera
    }

    public String interno() {
        return "trabajo interno";
    }
}
