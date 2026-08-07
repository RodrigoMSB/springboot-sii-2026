package cl.dgt.tramites.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * La topología de mensajería de la DGT: dos exchanges, dos colas y sus bindings.
 *
 * <p><strong>El vocabulario, en una frase:</strong> el productor NUNCA le habla a una cola. Le habla
 * a un <em>exchange</em>, que es un clasificador de correo: recibe el mensaje con una
 * <em>routing key</em> y, según los <em>bindings</em> que le hayan declarado, lo deja en cero, una o
 * varias colas. Esa indirección es lo que permite agregar un segundo consumidor —un tablero, un
 * archivo para el fiscalizador— sin tocar una línea del productor. Es el mismo desacople que el
 * evento de aplicación del Lab 11, ahora cruzando el límite del proceso.
 *
 * <p><strong>Y la parte que importa hoy: la DLQ.</strong> La cola principal se declara con
 * {@code x-dead-letter-exchange}. Eso significa: «el mensaje que yo rechace definitivamente, no lo
 * tires — mándalo a este otro exchange». De ahí cae a {@code dgt.avisos.dlq}, donde un humano lo
 * puede mirar.
 *
 * <p>Sin DLQ, un mensaje que siempre falla tiene dos destinos posibles y los dos son malos: se
 * descarta en silencio (perdiste el aviso y no lo sabes) o se re-encola para siempre (la cola se
 * atasca detrás de él y los mensajes BUENOS dejan de fluir). Esa segunda es la peor: un solo
 * mensaje envenenado tumba el proceso entero de avisos.
 */
@Configuration
public class AmqpConfig {

    /** El exchange al que publica el productor. */
    public static final String EXCHANGE = "dgt.avisos";
    /** La cola donde esperan los avisos hasta que el consumidor pueda con ellos. */
    public static final String COLA = "dgt.avisos.q";
    /** La routing key de los avisos de folio. */
    public static final String ROUTING_KEY = "folio.emitido";

    /** El exchange de los muertos. */
    public static final String EXCHANGE_DLQ = "dgt.avisos.dlx";
    /** Donde termina lo que nadie pudo procesar. */
    public static final String COLA_DLQ = "dgt.avisos.dlq";

    @Bean
    DirectExchange avisosExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    /**
     * La cola principal. <strong>Durable</strong>: sobrevive a un reinicio del broker.
     *
     * <p>Ese `durable` es la diferencia entre esta sesión y la anterior. En el Lab 11 los avisos
     * vivían en una cola <em>en memoria</em> del propio proceso, y un despliegue se los llevaba
     * enteros. Aquí viven fuera de la aplicación, en disco, y siguen ahí aunque la DGT se caiga,
     * se actualice o se reinicie tres veces.
     */
    @Bean
    Queue avisosCola() {
        return QueueBuilder.durable(COLA)
                .deadLetterExchange(EXCHANGE_DLQ)
                .deadLetterRoutingKey(ROUTING_KEY)
                .build();
    }

    @Bean
    Binding avisosBinding(Queue avisosCola, DirectExchange avisosExchange) {
        return BindingBuilder.bind(avisosCola).to(avisosExchange).with(ROUTING_KEY);
    }

    @Bean
    DirectExchange avisosDlx() {
        return new DirectExchange(EXCHANGE_DLQ, true, false);
    }

    /**
     * La cola de los muertos.
     *
     * <p>No lleva {@code deadLetterExchange} a propósito: una DLQ que reenvía a otra DLQ es un
     * bucle con más pasos. Aquí el mensaje se queda, y se queda <strong>con su causa</strong> — el
     * broker adjunta cabeceras {@code x-death} contando cuántas veces falló y por qué. Eso es lo
     * que convierte la DLQ en una bandeja de trabajo y no en un basurero.
     */
    @Bean
    Queue avisosDlq() {
        return QueueBuilder.durable(COLA_DLQ).build();
    }

    @Bean
    Binding dlqBinding(Queue avisosDlq, DirectExchange avisosDlx) {
        return BindingBuilder.bind(avisosDlq).to(avisosDlx).with(ROUTING_KEY);
    }

    /**
     * JSON en el cable, no serialización de Java.
     *
     * <p>Ojo con el nombre: es {@code JacksonJsonMessageConverter}, sin el «2». Boot 4 migró a
     * Jackson 3 (paquete {@code tools.jackson}), y spring-amqp mantiene el
     * {@code Jackson2JsonMessageConverter} solo por herencia — usarlo aquí pediría una Jackson 2 que
     * este proyecto ya no trae. Es el mismo tipo de renombre silencioso que el de
     * {@code spring-boot-starter-aop}, {@code webmvc-test} y {@code testcontainers-postgresql}:
     * verificado contra el jar, no de memoria.
     *
     * <p>La serialización nativa ataría el mensaje a la CLASE: el consumidor tendría que tener el
     * mismo {@code record}, del mismo paquete, de la misma versión. Con JSON, el contrato es la
     * FORMA del dato, y el otro lado puede estar escrito en otro lenguaje o ir una versión por
     * detrás. Lo que cruza un límite de proceso se negocia por datos, no por tipos.
     */
    @Bean
    MessageConverter conversorJson() {
        return new JacksonJsonMessageConverter();
    }
}
