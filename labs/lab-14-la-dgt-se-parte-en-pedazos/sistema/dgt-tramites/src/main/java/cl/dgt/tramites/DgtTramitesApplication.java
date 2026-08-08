package cl.dgt.tramites;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * El servicio de trámites: el que sufre.
 *
 * <p>{@code @EnableFeignClients} enciende el escaneo de las interfaces anotadas
 * con {@code @FeignClient}. Sin esta anotación, {@link ContribuyenteCliente} es
 * una interfaz que no implementa nadie y la aplicación arranca perfectamente…
 * hasta que algo intenta inyectarla. Es el primer error clásico de Feign.
 */
@SpringBootApplication
@EnableFeignClients
public class DgtTramitesApplication {

    public static void main(String[] args) {
        SpringApplication.run(DgtTramitesApplication.class, args);
    }
}
