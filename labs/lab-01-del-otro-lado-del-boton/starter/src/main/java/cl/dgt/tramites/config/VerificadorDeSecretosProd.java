package cl.dgt.tramites.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * En {@code prod}, la aplicación debe negarse a arrancar sin sus secretos — y decirlo claro.
 *
 * <p><strong>TODO_2 — completa el chequeo (≈15 min).</strong>
 *
 * <p>El andamio ya está: la clase corre en el perfil {@code prod}, es un
 * {@link BeanFactoryPostProcessor} (se ejecuta <em>antes</em> de instanciar cualquier bean,
 * así que gana la carrera contra el {@code DataSource}), y recibe el {@link Environment}.
 * Lo único que falta es el corazón: mirar las tres variables y negarse a arrancar si alguna
 * falta.
 *
 * <p><em>Por qué existe esta clase.</em> Sin ella, con {@code url: ${DGT_DB_URL}} ausente,
 * Spring falla veinte capas más abajo con {@code 'url' must start with "jdbc"}: un mensaje
 * que no nombra la variable ni dice quién debía definirla. Fallar rápido no basta; hay que
 * fallar <em>claro</em>.
 *
 * <p><em>Qué debe hacer tu código.</em> Recorrer {@link #OBLIGATORIAS}, juntar las que no
 * estén definidas en el entorno, y —si hay alguna— lanzar un {@link IllegalStateException}
 * que las <strong>nombre a todas</strong> (no de una en una) y recuerde que los secretos van
 * en el entorno, jamás en un archivo del repositorio.
 *
 * <p>Pista 2: {@code environment.getProperty("DGT_DB_URL")} devuelve {@code null} si falta.
 * {@code org.springframework.util.StringUtils.hasText(...)} te dice si un valor es usable.
 * Cuando termines, {@code T2_PerfilProdFallaRapidoTest} se pondrá verde.
 */
@Component
@Profile("prod")
public class VerificadorDeSecretosProd implements BeanFactoryPostProcessor, EnvironmentAware {

    private static final List<String> OBLIGATORIAS =
            List.of("DGT_DB_URL", "DGT_DB_USER", "DGT_DB_PASSWORD");

    private Environment entorno;

    @Override
    public void setEnvironment(Environment entorno) {
        this.entorno = entorno;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // TODO_2 — recorre OBLIGATORIAS, junta las que falten en `entorno`, y si hay alguna
        //          lanza IllegalStateException nombrándolas todas. Mira el Javadoc de arriba.
        throw new UnsupportedOperationException("{{TODO_2}}");
    }
}
