package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.domain.entity.Contribuyente;
import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.infrastructure.repository.ContadorFolioRepository;
import cl.dgt.tramites.infrastructure.repository.ContribuyenteRepository;
import cl.dgt.tramites.infrastructure.repository.FolioRepository;
import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * El rollback completo. Si algo falla DESPUÉS de emitir, dentro de la misma transacción, no debe
 * quedar ni folio huérfano ni número gastado: el proxy de {@code @Transactional} revierte TODO.
 *
 * <p>Este es el guardián contra el parche del acto 2b: si alguien "aísla" la toma del número con
 * {@code REQUIRES_NEW}, ese incremento commitea aparte y sobrevive al rollback — el contador
 * avanza sin folio, y el libro foliado salta. Con la propagación por defecto (REQUIRED), no.
 */
@SpringBootTest(properties = "dgt.base-embebida.enabled=false")
@Import(BaseConcurrenciaIT.class)
class E4_RollbackIT {

    @Autowired BaseConcurrenciaIT.EmisorQueFallaDespues emisor;
    @Autowired TramiteRepository tramites;
    @Autowired ContribuyenteRepository contribuyentes;
    @Autowired FolioRepository folios;
    @Autowired ContadorFolioRepository contadores;

    @Test
    @DisplayName("una falla post-emisión revierte el folio Y devuelve el número (sin saltos)")
    void elRollbackNoDejaFolioNiNumeroGastado() {
        Contribuyente c = contribuyentes.findByRut("11111111-1").orElseThrow();
        Long tramiteId = tramites.save(new Tramite(c, "DECLARACION_F29")).getId();
        long contadorAntes = contadores.findById((short) 1).orElseThrow().getUltimoNumero();

        assertThatThrownBy(() -> emisor.emitirYExplotar(tramiteId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(folios.findByTramiteId(tramiteId))
                .as("la falla revirtió: no quedó folio huérfano")
                .isEmpty();
        assertThat(contadores.findById((short) 1).orElseThrow().getUltimoNumero())
                .as("el número NO se gastó: el contador volvió a su valor")
                .isEqualTo(contadorAntes);
    }
}
