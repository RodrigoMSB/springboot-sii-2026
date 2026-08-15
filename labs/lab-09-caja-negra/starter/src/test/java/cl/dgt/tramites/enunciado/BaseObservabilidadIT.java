package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.PostgresEmbebido;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Base de los tests de observabilidad: PostgreSQL real (embebido, ver Lab 08) + login, y un
 * helper para CAPTURAR los logs con un appender en memoria —no se lee un archivo—. Así el test
 * inspecciona el evento de log (su MDC, su mensaje) directamente.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "dgt.base-embebida.enabled=false")
abstract class BaseObservabilidadIT {

    static final String CLAVE = "dgt-2026";
    static final String CAROLINA = "9876543-2";

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        // Una base recién creada para ESTE contexto: se pide una sola vez y se guarda.
        String url = PostgresEmbebido.nuevaBase();
        registro.add("spring.datasource.url", () -> url);
        registro.add("spring.datasource.username", PostgresEmbebido::usuario);
        registro.add("spring.datasource.password", PostgresEmbebido::clave);
    }

    @LocalServerPort
    int puerto;

    RestTestClient cliente() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + puerto).build();
    }

    @SuppressWarnings("unchecked")
    String bearer(String rut) {
        Map<String, Object> cuerpo = cliente().post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("rut", rut, "clave", CLAVE))
                .exchange().expectStatus().isOk()
                .expectBody(Map.class).returnResult().getResponseBody();
        return "Bearer " + cuerpo.get("token");
    }

    /** Adjunta un appender en memoria a un logger, corre el bloque, y devuelve los eventos capturados. */
    static List<ILoggingEvent> capturarLogs(String nombreLogger, Runnable bloque) {
        Logger logger = (Logger) LoggerFactory.getLogger(nombreLogger);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            bloque.run();
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
        return List.copyOf(appender.list);
    }

    /**
     * Renderiza un evento con el MISMO encoder que usa la consola. Si el logging estructurado está
     * activo (TODO_2), ese encoder produce JSON; si no (el starter, texto plano), produce texto — y
     * el test que espera JSON se pone rojo, como debe.
     */
    static String renderizarConEncoderDeConsola(ILoggingEvent evento) {
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = ctx.getLogger(Logger.ROOT_LOGGER_NAME);
        Iterator<Appender<ILoggingEvent>> it = root.iteratorForAppenders();
        while (it.hasNext()) {
            Appender<ILoggingEvent> a = it.next();
            if (a instanceof OutputStreamAppender<ILoggingEvent> osa && osa.getEncoder() != null) {
                return new String(osa.getEncoder().encode(evento), StandardCharsets.UTF_8);
            }
        }
        throw new IllegalStateException("no hay appender de consola con encoder");
    }

    @SuppressWarnings("unchecked")
    Long crearTramite(String bearer) {
        Map<String, Object> creado = cliente().post().uri("/api/v1/tramites")
                .header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("rutContribuyente", "11111111-1", "tipo", "DECLARACION_F29"))
                .exchange().expectStatus().isCreated()
                .expectBody(Map.class).returnResult().getResponseBody();
        return ((Number) creado.get("id")).longValue();
    }
}
