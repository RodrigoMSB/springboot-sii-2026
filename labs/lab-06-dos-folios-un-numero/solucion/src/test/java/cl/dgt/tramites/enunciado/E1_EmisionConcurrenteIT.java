package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.application.EmisionService;
import cl.dgt.tramites.application.ResultadoEmision;
import cl.dgt.tramites.domain.entity.Contribuyente;
import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.infrastructure.repository.ContadorFolioRepository;
import cl.dgt.tramites.infrastructure.repository.ContribuyenteRepository;
import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RN-01 (irrepetible) + RN-02 (secuencial sin saltos), bajo CONCURRENCIA REAL.
 *
 * <p>{@value #HILOS} hilos emiten a la vez, cada uno sobre SU trámite. El contador es el único
 * recurso compartido: si la emisión no lo bloquea, dos hilos leen el mismo número y la carrera
 * deja duplicados o huecos. La aserción es DOBLE —únicos Y contiguos— porque las dos reglas se
 * violan por el mismo pecado y se prueban en el mismo acto.
 *
 * <p>Determinista en veredicto: con el contador bloqueado, los emisores se serializan y el
 * resultado es siempre el mismo. Si este test parpadea, el candado está en el lugar equivocado.
 */
@SpringBootTest(properties = "dgt.base-embebida.enabled=false")
@Import(BaseConcurrenciaIT.class)
class E1_EmisionConcurrenteIT {

    static final int HILOS = 12;

    @Autowired EmisionService emision;
    @Autowired TramiteRepository tramites;
    @Autowired ContribuyenteRepository contribuyentes;
    @Autowired ContadorFolioRepository contadores;

    @Test
    @DisplayName("12 hilos emiten a la vez: folios únicos y secuencia sin saltos")
    void emisionConcurrenteRespetaRn01YRn02() throws InterruptedException {
        Contribuyente c = contribuyentes.findByRut("11111111-1").orElseThrow();
        List<Long> tramiteIds = new ArrayList<>();
        for (int i = 0; i < HILOS; i++) {
            tramiteIds.add(tramites.save(new Tramite(c, "DECLARACION_F29")).getId());
        }

        Queue<Long> numeros = new ConcurrentLinkedQueue<>();
        AtomicInteger errores = new AtomicInteger();

        BaseConcurrenciaIT.enParalelo(HILOS, idx -> {
            try {
                ResultadoEmision r = emision.emitir(tramiteIds.get(idx));
                numeros.add(r.folio().numero());
            } catch (RuntimeException e) {
                errores.incrementAndGet();  // el naïve muere aquí: violación de PK bajo carrera
            }
        });

        assertThat(errores.get())
                .as("ningún emisor válido debió fallar; si falló, la carrera lo reventó")
                .isZero();
        assertThat(numeros)
                .as("RN-01: todos los folios son distintos")
                .doesNotHaveDuplicates()
                .hasSize(HILOS);

        long min = Collections.min(numeros);
        long max = Collections.max(numeros);
        assertThat(max - min + 1)
                .as("RN-02: la secuencia es contigua — sin huecos en el libro foliado")
                .isEqualTo((long) HILOS);

        // La invariante de la semilla (contador ⇄ MAX(folio)) ahora la mantiene el CÓDIGO de
        // producción, no solo el INSERT de la V2: tras N emisiones concurrentes, siguen a la par.
        long contadorFinal = contadores.findById((short) 1).orElseThrow().getUltimoNumero();
        assertThat(contadorFinal)
                .as("el contador quedó exactamente en el último folio emitido")
                .isEqualTo(max);
    }
}
