package cl.dgt.tramites.enunciado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_3 · <strong>Exposición con criterio.</strong>
 *
 * <p>Corre con la configuración REAL de la aplicación —sin propiedades inyectadas por el test—.
 * Es deliberado: un test que se fabrica la configuración que va a auditar se aprueba a sí mismo y
 * no mira nunca la del alumno.
 *
 * <p><strong>La distinción que este test existe para grabar:</strong>
 *
 * <table border="1">
 *   <caption>404 no es 401</caption>
 *   <tr><th>Respuesta</th><th>Significa</th><th>Quién lo decide</th></tr>
 *   <tr><td>404</td><td>El endpoint NO EXISTE. No hay nada que forzar, adivinar ni robar.</td>
 *       <td>La lista blanca de Actuator (M12)</td></tr>
 *   <tr><td>401 / 403</td><td>EXISTE, y con la credencial correcta se abre.</td>
 *       <td>La cadena de filtros de Spring Security (M9, Lab 07)</td></tr>
 * </table>
 *
 * <p>Se usan las dos, y en ese orden: lo que no necesita estar publicado, no se publica; lo que sí,
 * se cierra con llave. Defensa en profundidad — no una en lugar de la otra. Confundirlas es lo que
 * lleva a dejar {@code /actuator/env} «protegido por seguridad» y descubrir el día del incidente
 * que un rol de más lo abría.
 */
@DisplayName("TODO_3 · los endpoints sensibles no existen; los útiles están cerrados con llave")
class E3_ExposicionConCriterioIT extends BaseTableroIT {

    @Test
    @DisplayName("/actuator/env NO responde ni con un token de FUNCIONARIO: no está expuesto (404)")
    void elEndpointQueVuelcaLaConfiguracionNoExiste() {
        // /env vuelca TODA la configuración resuelta: incluidas las variables de entorno, es decir
        // el secreto de firma del JWT y la contraseña de la base. Es el crimen del Lab 01 otra vez,
        // por otra puerta. Y ojo con el detalle que hace fuerte a este assert: va CON credencial
        // válida de funcionario. Si respondiera 401 no probaríamos nada — solo que hay un portero.
        // 404 prueba que no hay puerta.
        cliente().get().uri("/actuator/env")
                .header("Authorization", bearer(CAROLINA))
                .exchange().expectStatus().isNotFound();
    }

    @Test
    @DisplayName("/actuator/beans y /actuator/heapdump tampoco existen")
    void losOtrosDosSospechososHabitualesTampoco() {
        String carolina = bearer(CAROLINA);

        // /beans dibuja el mapa interno de la aplicación: qué hay y cómo se conecta. Es
        // reconocimiento gratis para quien busca por dónde entrar.
        cliente().get().uri("/actuator/beans")
                .header("Authorization", carolina)
                .exchange().expectStatus().isNotFound();

        // /heapdump descarga la MEMORIA del proceso: dentro va todo lo demás —tokens en vuelo,
        // datos de contribuyentes, el secreto de firma—. Es la fuga total en un solo GET.
        cliente().get().uri("/actuator/heapdump")
                .header("Authorization", carolina)
                .exchange().expectStatus().isNotFound();
    }

    @Test
    @DisplayName("/actuator/health sí responde, y sin credencial: lo consulta el orquestador, no una persona")
    void lasSondasQuedanAbiertas() {
        // Si las sondas exigieran token, el kubelet leería 401, lo interpretaría como «no responde»
        // y reiniciaría una aplicación perfectamente sana. Van abiertas a propósito; su DETALLE es
        // lo que se restringe en producción (show-details: when-authorized).
        cliente().get().uri("/actuator/health").exchange().expectStatus().isOk();
    }

    @Test
    @DisplayName("/actuator/prometheus está EXPUESTO pero CERRADO: 401 sin token, 200 con token")
    void elPulsoDelNegocioEstaPublicadoPeroNoEsPublico() {
        // Aquí se ve la diferencia en una sola prueba: el endpoint existe (por eso no da 404) y
        // está protegido (por eso da 401). Las métricas de negocio cuentan cuántos folios emite la
        // DGT y a qué ritmo: eso no se le regala al primero que pregunte.
        cliente().get().uri("/actuator/prometheus")
                .exchange().expectStatus().isUnauthorized();

        cliente().get().uri("/actuator/prometheus")
                .header("Authorization", bearer(CAROLINA))
                .exchange().expectStatus().isOk();
    }

    /**
     * Y lo que de verdad audita el oficial de seguridad: el archivo de PRODUCCIÓN.
     *
     * <p>Los cuatro tests de arriba corren sobre el perfil de laboratorio. Este mira el YAML de
     * producción tal cual se va a desplegar. No se levanta el perfil {@code prod} entero —exige
     * credenciales reales por variable de entorno, y con razón—: se carga el archivo y se
     * comprueban sus claves, que es exactamente lo que hay que poder defender en una auditoría.
     */
    @Test
    @DisplayName("application-prod.yml declara lista blanca nominal: sin comodín y con los sensibles excluidos")
    void produccionNoSeDespliegaConComodin() throws IOException {
        String include = String.valueOf(propiedadDeProd("management.endpoints.web.exposure.include"));
        String exclude = String.valueOf(propiedadDeProd("management.endpoints.web.exposure.exclude"));

        assertThat(include)
                .as("producción NO se despliega con `*`: eso publica /env, /beans y /heapdump en "
                    + "el servidor de la DGT, en internet")
                .isNotBlank()
                .doesNotContain("*");

        assertThat(include)
                .as("las sondas y las métricas sí se publican: son las que mantienen el servicio en pie")
                .contains("health");

        assertThat(exclude)
                .as("los tres sospechosos habituales, excluidos explícitamente: el día que alguien "
                    + "amplíe el include «solo para depurar un rato», esto sigue en pie")
                .contains("env")
                .contains("beans")
                .contains("heapdump");
    }

    /** Lee una clave del YAML de producción tal como Boot lo interpretaría. */
    private static Object propiedadDeProd(String clave) throws IOException {
        List<PropertySource<?>> fuentes = new YamlPropertySourceLoader()
                .load("application-prod", new ClassPathResource("application-prod.yml"));
        for (PropertySource<?> fuente : fuentes) {
            if (fuente.containsProperty(clave)) {
                return fuente.getProperty(clave);
            }
        }
        return null;
    }
}
