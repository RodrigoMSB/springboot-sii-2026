package cl.dgt.tramites.config;

import cl.dgt.tramites.application.ReporteService;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * El caché de la DGT. Tres decisiones, y las tres se pueden defender ante Carolina.
 *
 * <p><strong>1 · Caffeine, no el mapa por omisión.</strong> {@code @EnableCaching} sin proveedor
 * deja a Boot con un {@code ConcurrentMapCacheManager}: un {@code HashMap} que nunca expira, nunca
 * se acota y nunca cuenta nada. Sirve para una demo y para nada más. Un caché que no expira es una
 * fuga de memoria con buena prensa, y uno que no mide es un acto de fe: no puedes decir si te está
 * ayudando o solo sirviendo datos viejos.
 *
 * <p><strong>2 · TTL de 5 minutos ({@code expireAfterWrite}).</strong> El TTL no es la
 * invalidación: es la RED por si la invalidación falla. La invalidación correcta es explícita
 * ({@code @CacheEvict} en {@link cl.dgt.tramites.application.DeclaracionService}); el TTL acota
 * cuánto puede durar una mentira si alguien olvida evictar, si otra instancia escribió, o si un
 * proceso batch tocó la base por fuera de la aplicación. Cinco minutos es el compromiso declarado:
 * un reporte tributario tolera cinco minutos de retraso; una hora, no.
 *
 * <p><strong>3 · {@code recordStats()}.</strong> Sin estadísticas no hay hit-rate, y sin hit-rate
 * el caché es una opinión. Con ellas, {@code /actuator/metrics/cache.gets} responde la única
 * pregunta que importa: ¿está sirviendo, o solo ocupando memoria? Un caché con 2 % de aciertos no
 * es un caché: es una tabla de despiste que además puede servir datos rancios. Medirlo es lo que
 * te da derecho a mantenerlo — o el argumento para borrarlo.
 *
 * <p><strong>Lo que NO se hace aquí:</strong> Redis. La caché distribuida es la respuesta cuando
 * hay varias instancias y cada una con su copia local empieza a contradecir a las otras. Se nombra
 * en {@code TEORIA.md} y se conecta con el Lab 11 —donde dos instancias descubren, dolorosamente,
 * que cada una se creía la única—. Instalarla aquí sería cambiar el problema por otro más grande
 * antes de haber medido el primero.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** Cuánto puede durar una mentira si la invalidación explícita falla. */
    public static final Duration TTL = Duration.ofMinutes(5);

    /** Tope de entradas: un caché sin cota es una fuga de memoria con otro nombre. */
    public static final long MAXIMO_ENTRADAS = 500;

    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager gestor = new CaffeineCacheManager(ReporteService.CACHE_TOTALES);
        gestor.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(TTL)
                .maximumSize(MAXIMO_ENTRADAS)
                .recordStats());          // sin esto, stats() devuelve ceros y el TODO_4 no se puede medir
        return gestor;
    }
}
