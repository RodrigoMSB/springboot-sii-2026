package cl.dgt.examen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Un test por hueco. Doce en total.
 *
 * <p>Todos entran por HTTP, y eso no es un capricho: si un test llamara directamente al método que
 * el alumno tiene que escribir, la suite **no compilaría** mientras ese método no exista, y el
 * alumno no podría correr ninguno de los otros once. Entrando por la URL, los doce tests compilan
 * desde el primer minuto y cada uno se pone verde por su cuenta.
 *
 * <p>La clase es `@Transactional`: lo que escriba el test del POST se deshace al terminar, así que
 * la suite se puede correr las veces que haga falta sin que los conteos se muevan.
 */
@SpringBootTest
@Transactional
@ExtendWith(RegistroDeHuecos.class)
class HuecosTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;

    private MockMvc mvc() {
        if (mvc == null) {
            mvc = MockMvcBuilders.webAppContextSetup(contexto)
                    .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
                            .springSecurity())
                    .build();
        }
        return mvc;
    }

    // =========================================================================
    //  H-01 · La relación: el lado inverso en `Oficina`
    // =========================================================================
    @Test
    @DisplayName("H-01 · la relacion: SCL-01 tiene 5 solicitudes")
    @WithMockUser(username = "ana", roles = "FISCALIZADOR")
    void h01_relacion() throws Exception {
        mvc().perform(get("/oficinas/SCL-01/conteo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("SCL-01"))
                .andExpect(jsonPath("$.solicitudes").value(5));
    }

    // =========================================================================
    //  H-02 · Consulta derivada por un campo
    // =========================================================================
    @Test
    @DisplayName("H-02 · derivada por comuna: Santiago tiene 2 oficinas")
    @WithMockUser(username = "ana", roles = "FISCALIZADOR")
    void h02_derivadaPorComuna() throws Exception {
        mvc().perform(get("/oficinas/comuna/Santiago"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].comuna").value("Santiago"));
    }

    // =========================================================================
    //  H-03 · Consulta derivada de conteo
    // =========================================================================
    @Test
    @DisplayName("H-03 · derivada de conteo: hay 5 solicitudes PAGADO")
    @WithMockUser(username = "ana", roles = "FISCALIZADOR")
    void h03_conteoPorEstado() throws Exception {
        mvc().perform(get("/solicitudes/conteo").param("estado", "PAGADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solicitudes").value(5));
    }

    // =========================================================================
    //  H-04 · Consulta derivada con orden
    // =========================================================================
    @Test
    @DisplayName("H-04 · derivada con orden: la PAGADO mas reciente es del 2026-04-15")
    @WithMockUser(username = "ana", roles = "FISCALIZADOR")
    void h04_recientesPorEstado() throws Exception {
        mvc().perform(get("/solicitudes/recientes").param("estado", "PAGADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].fecha").value("2026-04-15"))
                .andExpect(jsonPath("$[4].fecha").value("2025-06-30"));
    }

    // =========================================================================
    //  H-05 · El DTO del resumen
    // =========================================================================
    @Test
    @DisplayName("H-05 · el DTO del resumen trae codigo, nombre, comuna y conteo")
    @WithMockUser(username = "ana", roles = "FISCALIZADOR")
    void h05_dtoDelResumen() throws Exception {
        mvc().perform(get("/oficinas/SCL-01/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("SCL-01"))
                .andExpect(jsonPath("$.nombre").value("Oficina Santiago Centro"))
                .andExpect(jsonPath("$.comuna").value("Santiago"))
                .andExpect(jsonPath("$.solicitudes").value(5));
    }

    // =========================================================================
    //  H-06 · La lista completa, mapeada a DTO
    // =========================================================================
    @Test
    @DisplayName("H-06 · la lista de oficinas: 3, con codigo y comuna y sin nada mas")
    @WithMockUser(username = "ana", roles = "FISCALIZADOR")
    void h06_listaDeOficinas() throws Exception {
        mvc().perform(get("/oficinas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].codigo").value("SCL-01"))
                .andExpect(jsonPath("$[0].comuna").value("Santiago"))
                .andExpect(jsonPath("$[0].nombre").doesNotExist());
    }

    // =========================================================================
    //  H-07 · La regla de negocio en el servicio
    // =========================================================================
    @Test
    @DisplayName("H-07 · el servicio suma: lo PAGADO son 5.670.000")
    @WithMockUser(username = "ana", roles = "FISCALIZADOR")
    void h07_totalPorEstado() throws Exception {
        mvc().perform(get("/solicitudes/total").param("estado", "PAGADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5670000.00));
    }

    // =========================================================================
    //  H-08 · La configuración externa
    // =========================================================================
    @Test
    @DisplayName("H-08 · la configuracion: el listado se corta en el tope, y el tope es 3")
    @WithMockUser(username = "ana", roles = "FISCALIZADOR")
    void h08_topeConfigurado() throws Exception {
        mvc().perform(get("/solicitudes/ultimas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].fecha").value("2026-05-05"));
    }

    // =========================================================================
    //  H-09 · El POST que crea
    // =========================================================================
    @Test
    @DisplayName("H-09 · el POST responde 201 y dice donde quedo")
    @WithMockUser(username = "ana", roles = "FISCALIZADOR")
    void h09_postCrea() throws Exception {
        String cuerpo = """
                {"tipo":"F29","estado":"PENDIENTE","fecha":"2026-07-01",
                 "monto":123456.00,"oficinaCodigo":"VAL-01"}
                """;

        mvc().perform(post("/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.tipo").value("F29"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    // =========================================================================
    //  H-10 · El 404 con cuerpo
    // =========================================================================
    @Test
    @DisplayName("H-10 · el 404 trae cuerpo y nombra el codigo que no existe")
    @WithMockUser(username = "ana", roles = "FISCALIZADOR")
    void h10_404ConCuerpo() throws Exception {
        mvc().perform(get("/oficinas/ZZZ-99/ficha"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.codigo").value("ZZZ-99"));
    }

    // =========================================================================
    //  H-11 · El 400 que nombra el campo
    // =========================================================================
    @Test
    @DisplayName("H-11 · el 400 nombra el campo que viene mal")
    @WithMockUser(username = "ana", roles = "FISCALIZADOR")
    void h11_400NombraElCampo() throws Exception {
        mvc().perform(put("/solicitudes/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"INVENTADO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.estado").exists());
    }

    // =========================================================================
    //  H-12 · La seguridad por rol
    // =========================================================================
    @Test
    @DisplayName("H-12 · seguridad: 401 sin token, 403 con el rol equivocado, 200 con el bueno")
    void h12_seguridadPorRol() throws Exception {
        mvc().perform(get("/oficinas/SCL-01/ficha"))
                .andExpect(status().isUnauthorized());

        mvc().perform(get("/oficinas/SCL-01/ficha")
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.user("luis").roles("CONTRIBUYENTE")))
                .andExpect(status().isForbidden());

        mvc().perform(get("/oficinas/SCL-01/ficha")
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.user("ana").roles("FISCALIZADOR")))
                .andExpect(status().isOk());
    }
}
