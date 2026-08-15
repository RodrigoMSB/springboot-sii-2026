package cl.dgt.tramites.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadatos de la API que Swagger UI muestra en su portada. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dgtOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("DGT · API de Trámites")
                .version("v1")
                .description("El backend de la Dirección General de Tributación."));
    }
}
