// Test de controller con MockMvc: comprueba el 404 con cuerpo, sin levantar el servidor.
// Tu equivalente: el mismo test sobre `/consolidados/{rut}`.
package cl.dgt.consolidado;

import cl.dgt.consolidado.controllers.ManejadorDeErrores;
import cl.dgt.consolidado.controllers.ResumenController;
import cl.dgt.consolidado.services.OficinaNoEncontradaException;
import cl.dgt.consolidado.services.ResumenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ResumenController.class)
@Import(ManejadorDeErrores.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(authorities = "ROLE_FISCALIZADOR")
class ResumenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResumenService servicio;

    @Test
    void unCodigoQueNoExisteDevuelve404ConCuerpo() throws Exception {
        when(servicio.delPeriodo(eq("NO-EXISTE"), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new OficinaNoEncontradaException("NO-EXISTE"));

        mockMvc.perform(get("/resumenes/NO-EXISTE")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-12-31"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la oficina NO-EXISTE"));
    }
    // ^ LAS DOS COMPROBACIONES SON EL TEST:
    //
    //     status().isNotFound()  →  que la excepción no se escapó como un 500
    //     jsonPath("$.mensaje")  →  que el error trae un cuerpo legible
    //
    //   Ninguna la escribió el controller: las produce `ManejadorDeErrores`, y por eso hay que
    //   importarlo con `@Import` — `@WebMvcTest` sólo carga el controller que se le nombra.
    //
    //   `addFilters = false` apaga la cadena de filtros de seguridad EN ESTE TEST, y hay que
    //   saber por qué: lo que se prueba aquí es el 404 y su cuerpo, no la seguridad. Montar la
    //   cadena entera obligaría a construir un `JwtDecoder` y a firmar un token de mentira para
    //   llegar a comprobar... el manejador de errores.
    //
    //   La seguridad se comprueba aparte, con los curl de la rúbrica (401 sin token, 403 con el
    //   token equivocado). Cada test al nivel más barato que responda su pregunta — Lab 08.
    //
    //   ---------------------------------------------------------------------------------------
    //   Y SI TE VAS AL OTRO EXTREMO, LEE ESTO ANTES DE PERDER LA TARDE.
    //
    //   Si en vez de este slice montas un `@SpringBootTest` + `@AutoConfigureMockMvc` con la
    //   cadena ENCENDIDA, `@WithMockUser` NO autentica la petición. Sale 401, y encima con
    //   cabecera `WWW-Authenticate: Bearer`, que se lee como «mi token está mal» cuando lo que
    //   pasa es que no hubo token ninguno.
    //
    //   El motivo: `@WithMockUser` sólo llena el `TestSecurityContextHolder`. Para que ese
    //   contexto llegue a la cadena hace falta el post-procesador que instala
    //   `SecurityMockMvcConfigurers.springSecurity()`, y el `MockMvc` que inyecta
    //   `@AutoConfigureMockMvc` en este proyecto monta el filtro de seguridad pero NO ese
    //   post-procesador. Comprobado, y comprobado también que la culpa no es de `STATELESS` ni
    //   de `oauth2ResourceServer`: quitarlos no arregla nada, sólo cambia el 401 por un 403.
    //
    //   Cualquiera de estas cuatro funciona, y las cuatro dan 200:
    //
    //       .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_FISCALIZADOR")))
    //       .with(user("ana").authorities(new SimpleGrantedAuthority("ROLE_FISCALIZADOR")))
    //       @WithMockUser  +  .with(testSecurityContext())
    //       MockMvcBuilders.webAppContextSetup(ctx).apply(springSecurity()).build()
    //
    //   Para el encargo no necesitas ninguna: el test que pide el brief es éste, el del 404.
}
