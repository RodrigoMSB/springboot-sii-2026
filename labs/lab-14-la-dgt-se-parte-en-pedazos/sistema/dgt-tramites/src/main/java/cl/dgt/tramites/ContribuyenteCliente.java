package cl.dgt.tramites;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * El cliente HTTP hacia {@code dgt-contribuyentes}, sin escribir un cliente HTTP.
 *
 * <p>Se declara la interfaz; Feign genera la implementación. Lo que en el Lab 08
 * se hacía a mano con {@code RestClient} —construir la URI, serializar, mapear
 * el error— aquí lo pone el framework.
 *
 * <p><strong>Mira el {@code name}:</strong> {@code "dgt-contribuyentes"}. No es
 * una URL. No hay ni un {@code http://}, ni una IP, ni un puerto. Es el
 * <em>nombre lógico</em> con el que el servicio se anotó en el registro, y en
 * tiempo de ejecución pasa esto:
 *
 * <ol>
 *   <li>Feign le pregunta al LoadBalancer por {@code dgt-contribuyentes}.</li>
 *   <li>El LoadBalancer le pregunta al registro qué instancias hay vivas.</li>
 *   <li>El registro devuelve la lista (una, dos, siete).</li>
 *   <li>El LoadBalancer elige una — por turnos, {@code round-robin}.</li>
 *   <li>Feign hace la petición contra la IP y puerto de ESA instancia.</li>
 * </ol>
 *
 * <p>Por eso los servicios pueden arrancar en puerto efímero y multiplicarse sin
 * que nadie toque una línea de configuración. Y por eso, cuando el registro se
 * cae (bloque 4), esto sigue funcionando un rato: el LoadBalancer se quedó con
 * la última lista que le dieron, en caché.
 *
 * <p>La ruta del {@code @GetMapping} tiene que coincidir con la del otro
 * servicio. Nada la verifica en tiempo de compilación: si allá cambian la URL,
 * esto se entera en producción. Es una de las cuentas que hay que pagar por
 * partir el sistema, y en la teoría se llama <em>acoplamiento por contrato</em>.
 */
@FeignClient(name = "dgt-contribuyentes")
public interface ContribuyenteCliente {

    @GetMapping("/api/v1/contribuyentes/{rut}")
    FichaContribuyente buscarPorRut(@PathVariable("rut") String rut);
}
