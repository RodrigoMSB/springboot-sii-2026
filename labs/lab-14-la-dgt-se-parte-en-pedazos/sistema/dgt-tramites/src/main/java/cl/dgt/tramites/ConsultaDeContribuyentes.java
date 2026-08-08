package cl.dgt.tramites;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * La llamada al otro servicio, con todo lo que hay que ponerle alrededor.
 *
 * <p>En el monolito esto era una línea:
 * {@code contribuyenteRepository.findByRut(rut)}. Aquí es una llamada de red, y
 * una llamada de red puede tardar, fallar, contestar a medias o no contestar
 * nunca. Las anotaciones de abajo son el precio.
 *
 * <h2>Por qué el fallback está en {@code @Retry} y no en {@code @CircuitBreaker}</h2>
 *
 * <p>Esto no es un detalle de estilo: puesto al revés, <strong>el retry no
 * reintenta nunca</strong>, y es un error tan silencioso que puede vivir años en
 * producción sin que nadie lo note.
 *
 * <p>Resilience4j compone los aspectos en un orden fijo, con {@code Retry} por
 * fuera:
 *
 * <pre>{@code
 * Retry ( CircuitBreaker ( tu método ) )
 * }</pre>
 *
 * <p>Si el {@code fallbackMethod} estuviera en {@code @CircuitBreaker}, la
 * excepción se convertiría en una respuesta válida <em>dentro</em> del anillo
 * interior. El {@code Retry} de fuera vería un método que devolvió sin lanzar
 * nada, concluiría que todo salió bien a la primera, y no reintentaría jamás.
 *
 * <p>Con el fallback en el anillo exterior, el orden real es el que se quiere:
 * intento → falla → reintento → falla → se agotan los intentos → fallback. Y
 * cada intento fallido queda contado por el circuit breaker.
 *
 * <h2>Consecuencia aritmética, que hay que tener presente</h2>
 *
 * <p>Con {@code max-attempts: 2}, <strong>una</strong> petición del usuario
 * produce <strong>dos</strong> llamadas registradas en el circuit breaker. Si el
 * umbral pide 6 llamadas mínimas, se abre con 3 peticiones, no con 6. Cuando
 * ajustes los números del TODO, cuenta llamadas al circuito, no clics.
 */
@Service
public class ConsultaDeContribuyentes {

    private static final Logger log = LoggerFactory.getLogger(ConsultaDeContribuyentes.class);

    /**
     * El nombre de la instancia de Resilience4j. Tiene que coincidir, letra por
     * letra, con la clave de {@code resilience4j.circuitbreaker.instances.*} en
     * {@code config-repo/dgt-tramites.yml}.
     *
     * <p>Si no coincide, no falla nada: Resilience4j crea una instancia nueva con
     * los valores por defecto y sigue. Tu configuración simplemente no se aplica,
     * en silencio. Es la forma número uno de perder una tarde.
     */
    public static final String CIRCUITO = "contribuyentes";

    private final ContribuyenteCliente cliente;

    public ConsultaDeContribuyentes(ContribuyenteCliente cliente) {
        this.cliente = cliente;
    }

    /**
     * Pide la ficha del contribuyente al servicio que la tiene.
     *
     * @param rut el RUT a consultar
     * @return la ficha real, o la degradada si el otro servicio no está
     *         disponible — nunca lanza
     */
    @Retry(name = CIRCUITO, fallbackMethod = "fichaDegradada")
    @CircuitBreaker(name = CIRCUITO)
    public FichaContribuyente buscar(String rut) {
        return cliente.buscarPorRut(rut);
    }

    /**
     * El fallback: qué devolver cuando el otro servicio no está.
     *
     * <p>La firma es obligatoria y Resilience4j la busca por reflexión: mismos
     * parámetros que el método original, <strong>más</strong> un
     * {@code Throwable} al final. Si no coincide, el arranque no falla — falla la
     * primera vez que el fallback tenga que dispararse, en producción, a las
     * tres de la mañana. Está en {@code docs/troubleshooting.md}, fila T-4.
     *
     * <p><strong>Y aquí está el crimen de este laboratorio.</strong> Mira lo que
     * devuelve: una ficha con {@code razonSocial} en {@code null}. La respuesta
     * al usuario será un HTTP 200, con su JSON bien formado, sin una sola línea
     * roja en ninguna pantalla. El sistema «funciona». Solo que el nombre del
     * contribuyente no está, y nadie avisó.
     *
     * <p>Un fallback es una decisión de negocio disfrazada de detalle técnico.
     * Devolver datos incompletos en silencio es una opción; devolver un 503
     * honesto es otra; devolver el último valor conocido desde una caché es una
     * tercera. Las tres son defendibles y las tres tienen consecuencias
     * distintas para quien firma la declaración. Elegir sin darse cuenta de que
     * se está eligiendo es lo único que no es defendible — y el desafío
     * {@code 99-} de este laboratorio consiste exactamente en cambiar esta
     * elección.
     */
    @SuppressWarnings("unused") // la invoca Resilience4j por reflexión, no el compilador
    private FichaContribuyente fichaDegradada(String rut, Throwable causa) {
        log.warn("[FALLBACK] No pude consultar al servicio de contribuyentes para {} — {}: {}",
                rut, causa.getClass().getSimpleName(), causa.getMessage());
        return new FichaContribuyente(rut, null, null);
    }
}
