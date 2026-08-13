package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.application.EmisionService;
import cl.dgt.tramites.application.ResultadoEmision;
import cl.dgt.tramites.domain.entity.Contribuyente;
import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.infrastructure.repository.ContribuyenteRepository;
import cl.dgt.tramites.infrastructure.repository.FolioRepository;
import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RN-05 · idempotencia por {@code tramiteId}. Emitir dos veces el mismo trámite NO crea dos
 * folios: la primera vez nace uno ({@code creado = true}); la segunda devuelve EL MISMO
 * ({@code creado = false}). El {@code UNIQUE (tramite_id)} de la V1 deja de ser adorno: es el
 * suelo que hace esto posible aun si dos reintentos corren a la vez.
 */
@SpringBootTest(properties = "dgt.base-embebida.enabled=false")
@Import(BaseConcurrenciaIT.class)
class E2_IdempotenciaIT {

    @Autowired EmisionService emision;
    @Autowired TramiteRepository tramites;
    @Autowired ContribuyenteRepository contribuyentes;
    @Autowired FolioRepository folios;

    @Test
    @DisplayName("emitir dos veces el mismo trámite devuelve el mismo folio, sin crear otro")
    void reintentarEsIdempotente() {
        Contribuyente c = contribuyentes.findByRut("11111111-1").orElseThrow();
        Long tramiteId = tramites.save(new Tramite(c, "DECLARACION_F29")).getId();

        ResultadoEmision primera = emision.emitir(tramiteId);
        ResultadoEmision segunda = emision.emitir(tramiteId);

        assertThat(primera.creado()).as("la primera emisión CREA (201)").isTrue();
        assertThat(segunda.creado()).as("el reintento NO crea (200)").isFalse();
        assertThat(segunda.folio().numero())
                .as("mismo trámite, mismo folio")
                .isEqualTo(primera.folio().numero());
        assertThat(folios.findByTramiteId(tramiteId))
                .as("hay UN folio para el trámite, no dos")
                .isPresent();
    }
}
