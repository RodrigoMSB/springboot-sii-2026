package cl.dgt.tramites.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * En {@code prod}, la aplicación se niega a arrancar sin sus secretos — y lo dice claro.
 *
 * <p><strong>¿Por qué existe esta clase?</strong> Porque Spring, por sí solo, no falla
 * bien. Con {@code url: ${DGT_DB_URL}} y la variable ausente, el arranque muere veinte
 * capas más abajo con este mensaje real:
 *
 * <pre>
 *   Failed to instantiate [com.zaxxer.hikari.HikariDataSource]:
 *   Factory method 'dataSource' threw exception with message: 'url' must start with "jdbc"
 * </pre>
 *
 * <p>Ese mensaje no le sirve a nadie a las tres de la mañana: no nombra la variable, no
 * dice quién debía definirla, y manda a quien lo lee a leer el código de Hikari.
 *
 * <p>Es un {@link BeanFactoryPostProcessor} a propósito: corre <em>antes</em> de que se
 * instancie ningún bean, así que gana la carrera contra el {@code DataSource}. Fallar
 * rápido no es solo que falle: es que falle <em>antes</em>, y con un mensaje accionable.
 *
 * <p>Compáralo con {@code ${DGT_DB_PASSWORD:cambiame}}: eso arranca en silencio, se
 * conecta a donde no debe, y el error aparece un martes de madrugada, lejos de quien lo
 * escribió.
 */
@Component
@Profile("prod")
public class VerificadorDeSecretosProd implements BeanFactoryPostProcessor, EnvironmentAware {

    private static final List<String> OBLIGATORIAS =
            List.of("DGT_DB_URL", "DGT_DB_USER", "DGT_DB_PASSWORD",
                    // El secreto de firma del JWT (Lab 07). Sin él, el portero no puede firmar ni
                    // validar tokens: en prod es tan obligatorio como la clave de la base.
                    "DGT_JWT_SECRET");

    private Environment entorno;

    @Override
    public void setEnvironment(Environment entorno) {
        this.entorno = entorno;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        List<String> faltantes = new ArrayList<>();
        for (String variable : OBLIGATORIAS) {
            if (!StringUtils.hasText(entorno.getProperty(variable))) {
                faltantes.add(variable);
            }
        }
        if (!faltantes.isEmpty()) {
            throw new IllegalStateException(
                    "El perfil 'prod' no arranca sin sus secretos. Faltan estas variables de "
                    + "entorno: " + faltantes + ". Defínelas en el entorno del servidor, "
                    + "jamás en un archivo del repositorio. Ejemplo: export DGT_DB_PASSWORD='...'");
        }
    }
}
