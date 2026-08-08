package cl.dgt.contribuyentes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * La API del proveedor. Un solo endpoint, y a propósito.
 *
 * <p>{@code GET /api/v1/contribuyentes/{rut}} devuelve la ficha, o 404 si el RUT
 * no existe. Todo lo demás del dominio se quedó en el monolito.
 */
@RestController
@RequestMapping("/api/v1/contribuyentes")
public class ContribuyenteController {

    private static final Logger log = LoggerFactory.getLogger(ContribuyenteController.class);

    private final ContribuyenteRepository repositorio;

    /**
     * Cómo se llama esta instancia. Se inyecta desde
     * {@code eureka.instance.instance-id}, que en el compose lleva el nombre del
     * contenedor: así, cuando haya dos instancias, cada respuesta dirá cuál
     * contestó y el balanceo se ve a ojo desnudo.
     */
    private final String instancia;

    /**
     * El sabotaje del bloque 2, por bandera (P-04 del ADN).
     *
     * <p>Milisegundos de retardo artificial antes de responder. Cero en
     * condiciones normales; {@code --contribuyentes-lento} lo sube por variable
     * de entorno y reinicia el contenedor.
     *
     * <p>Un servicio LENTO es peor que uno caído, y esa es la lección entera del
     * bloque 2: uno caído devuelve «conexión rechazada» en un milisegundo y el
     * que llama se entera enseguida. Uno lento se queda con el hilo del que
     * llama, y con los del que llama al que llama, hasta que se acaban los hilos
     * de todo el sistema. Se llama <em>fallo en cascada</em> y es la razón de
     * que exista el circuit breaker.
     */
    private final long retardoMs;

    public ContribuyenteController(ContribuyenteRepository repositorio,
                                   @Value("${eureka.instance.instance-id:desconocida}") String instancia,
                                   @Value("${dgt.contribuyentes.retardo-ms:0}") long retardoMs) {
        this.repositorio = repositorio;
        this.instancia = instancia;
        this.retardoMs = retardoMs;
        if (retardoMs > 0) {
            log.warn("[SABOTAJE] Esta instancia responderá con {} ms de retardo artificial.", retardoMs);
        }
    }

    @GetMapping("/{rut}")
    public ResponseEntity<FichaContribuyente> buscarPorRut(@PathVariable String rut) {
        retardar();
        return repositorio.findByRut(rut)
                .map(c -> new FichaContribuyente(c.getRut(), c.getRazonSocial(), instancia))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * El retardo artificial. Sí, es un {@code Thread.sleep} en código de
     * producción, y sí, normalmente sería imperdonable: aquí es el inyector de
     * fallos del laboratorio, vive detrás de una bandera que por defecto está en
     * cero, y grita en el log cuando está activo.
     */
    private void retardar() {
        if (retardoMs <= 0) {
            return;
        }
        try {
            Thread.sleep(retardoMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
