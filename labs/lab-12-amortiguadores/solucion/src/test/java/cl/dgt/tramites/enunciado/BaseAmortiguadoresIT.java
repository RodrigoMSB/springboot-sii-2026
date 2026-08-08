package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.config.AmqpConfig;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.util.Map;

/**
 * Base del enunciado del Lab 12: PostgreSQL y <strong>RabbitMQ reales</strong>, los dos en
 * contenedores singleton.
 *
 * <p><strong>Por qué un broker de verdad y no un doble.</strong> Lo que este laboratorio afirma no
 * es «mi código llama a un método»: es que el <em>broker</em> se comporta de cierta manera — que
 * guarda el mensaje mientras nadie lo consume, que reintenta un número acotado de veces, que enruta
 * el rechazado a la DLQ, que puede entregar dos veces. Nada de eso lo puede demostrar un mock: un
 * mock demuestra lo que yo creo que hace RabbitMQ, que es justamente lo que hay que comprobar.
 *
 * <p>El tag de la imagen va fijado (mismo criterio que postgres y wiremock): la reproducibilidad es
 * parte del contenido.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.docker.compose.enabled=false",
                "dgt.instancia=test-instancia",
                "dgt.cierre.intervalo-ms=86400000",
                "dgt.cierre.retardo-inicial-ms=86400000"
        })
abstract class BaseAmortiguadoresIT {

    static final String CLAVE = "dgt-2026";
    static final String CAROLINA = "9876543-2";

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine3.24");
    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:4.2.4-management");

    static {
        POSTGRES.start();
        RABBIT.start();
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registro.add("spring.datasource.username", POSTGRES::getUsername);
        registro.add("spring.datasource.password", POSTGRES::getPassword);

        registro.add("spring.rabbitmq.host", RABBIT::getHost);
        registro.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registro.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registro.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
    }

    @LocalServerPort
    int puerto;

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitListenerEndpointRegistry registroDeListeners;

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

    @SuppressWarnings("unchecked")
    Long crearTramite(String bearer) {
        Map<String, Object> creado = cliente().post().uri("/api/v1/tramites")
                .header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("rutContribuyente", "11111111-1", "tipo", "DECLARACION_F29"))
                .exchange().expectStatus().isCreated()
                .expectBody(Map.class).returnResult().getResponseBody();
        return ((Number) creado.get("id")).longValue();
    }

    /** Cuántos mensajes esperan en una cola. Es la evidencia de que NO se perdieron. */
    int mensajesEn(String cola) {
        Object cantidad = amqpAdmin.getQueueProperties(cola).get("QUEUE_MESSAGE_COUNT");
        return cantidad == null ? 0 : ((Number) cantidad).intValue();
    }

    /**
     * Apaga el consumidor sin apagar el broker: así se simula «el servicio de avisos está caído»
     * mientras la cola sigue aceptando mensajes. Es exactamente el escenario del crimen.
     */
    void apagarConsumidor() {
        for (MessageListenerContainer c : registroDeListeners.getListenerContainers()) {
            c.stop();
        }
    }

    void encenderConsumidor() {
        for (MessageListenerContainer c : registroDeListeners.getListenerContainers()) {
            c.start();
        }
    }

    /** Vacía las dos colas. Cada prueba parte de un buzón limpio. */
    void vaciarColas() {
        amqpAdmin.purgeQueue(AmqpConfig.COLA, false);
        amqpAdmin.purgeQueue(AmqpConfig.COLA_DLQ, false);
    }
}
