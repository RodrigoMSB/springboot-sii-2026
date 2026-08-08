package cl.dgt.contribuyentes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * El servicio de contribuyentes: dueño de una sola cosa, y dueño de verdad.
 *
 * <p>Sabe quién es el titular de un RUT. Nadie más lo sabe. No porque esté
 * prohibido por convención, sino porque <strong>ningún otro servicio tiene
 * credenciales para su base de datos</strong> — mira
 * {@code sistema/db-init/01-bases-y-usuarios.sql}: el usuario {@code dgt_tramites}
 * no tiene ni un permiso sobre {@code dgt_contribuyentes}.
 *
 * <p>Esa es la línea que separa un microservicio de un módulo. Si el vecino
 * puede hacer un JOIN contra tu tabla, no tienes dos servicios: tienes un
 * monolito repartido en dos procesos, con toda la latencia de la red y ninguna
 * de sus ventajas. Es el peor de los dos mundos y tiene nombre propio —
 * <em>monolito distribuido</em>— y sale en la teoría.
 *
 * <p>Esta pieza es la que se va a <strong>matar</strong> en el bloque 2 y a
 * <strong>duplicar</strong> en el bloque 3. Arranca en puerto efímero
 * ({@code server.port: 0}) precisamente para poder duplicarla: con un puerto
 * fijo, la segunda instancia moriría al arrancar y media lección se perdería.
 */
@SpringBootApplication
public class DgtContribuyentesApplication {

    public static void main(String[] args) {
        SpringApplication.run(DgtContribuyentesApplication.class, args);
    }
}
