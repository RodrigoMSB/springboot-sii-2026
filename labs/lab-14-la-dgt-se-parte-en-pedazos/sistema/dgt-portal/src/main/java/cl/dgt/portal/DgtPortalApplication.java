package cl.dgt.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * El portal: la única puerta al exterior.
 *
 * <p>Sin gateway, el navegador del contribuyente tendría que saber que los
 * trámites están en un puerto y los contribuyentes en otro, y que ambos cambian
 * cada vez que el sistema se reinicia (arrancan en puerto efímero). Imposible.
 *
 * <p>Con gateway hay <strong>una sola URL</strong> — {@code localhost:8099} — y
 * detrás, el portal decide a quién le toca según la ruta. Y no enruta a una IP:
 * enruta a un <em>nombre lógico</em>, {@code lb://dgt-contribuyentes}, que el
 * LoadBalancer traduce preguntándole al registro qué instancias hay vivas.
 *
 * <p>Las rutas NO están aquí, en código: viven en
 * {@code config-repo/dgt-portal.yml}, y las sirve el Config Server. Cambiar una
 * ruta es editar un archivo de texto, no recompilar el portal. Eso es el punto
 * de tener las dos piezas.
 *
 * <p>El gateway es además donde en un sistema real vive lo que no quieres
 * repetir cinco veces: autenticación de borde, límite de peticiones, CORS,
 * cabeceras de traza. Esa parte se nombra en la teoría y no se construye aquí —
 * el laboratorio dura tres horas.
 *
 * <p>Puerto <strong>8099</strong>, el del curso. El material original de esta
 * práctica lo ponía en el 443, que en Linux y macOS necesita root: cada alumno
 * habría gastado sus primeros veinte minutos peleando con permisos en vez de
 * mirando cómo se cae un sistema distribuido.
 */
@SpringBootApplication
public class DgtPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(DgtPortalApplication.class, args);
    }
}
