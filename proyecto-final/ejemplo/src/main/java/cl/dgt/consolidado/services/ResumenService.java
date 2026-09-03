// Arma el DTO y suma el total. Es donde vive la regla de negocio.
// Tu equivalente: `services/ConsolidadoService`, con la misma forma exacta.
package cl.dgt.consolidado.services;

import cl.dgt.consolidado.dto.ResumenOficina;
import cl.dgt.consolidado.dto.TramiteDelResumen;
import cl.dgt.consolidado.entities.Oficina;
import cl.dgt.consolidado.entities.Tramite;
import cl.dgt.consolidado.repositories.OficinaRepository;
import cl.dgt.consolidado.repositories.TramiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ResumenService {

    private final OficinaRepository oficinas;
    private final TramiteRepository tramites;
    private final ContadorDeConsolidados contador;

    public ResumenService(OficinaRepository oficinas,
                          TramiteRepository tramites,
                          ContadorDeConsolidados contador) {
        this.oficinas = oficinas;
        this.tramites = tramites;
        this.contador = contador;
    }
    // ^ Las tres dependencias entran por el CONSTRUCTOR. Es lo que permite construir esta clase
    //   con `new` en un test y pasarle dobles, sin levantar Spring. Con `@Autowired` sobre
    //   campos, el test del servicio necesitaría el contexto entero.

    @Transactional(readOnly = true)
    public ResumenOficina delPeriodo(String codigo, LocalDate desde, LocalDate hasta) {
        Oficina oficina = oficinas.findByCodigo(codigo)
                .orElseThrow(() -> new OficinaNoEncontradaException(codigo));
        // ^ Primero se comprueba que EXISTE. Si no, 404 — y eso es distinto de «existe y no tiene
        //   trámites», que es un 200 con lista vacía. Confundir los dos casos es el error clásico
        //   de este encargo.

        List<TramiteDelResumen> delPeriodo = tramites.delPeriodo(codigo, desde, hasta).stream()
                .map(ResumenService::aDto)
                .toList();

        BigDecimal total = delPeriodo.stream()
                .map(TramiteDelResumen::montoDeclarado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // ^ `BigDecimal` y no `double`, y esto no es cosmético: `double` no representa exactamente
        //   los decimales y en dinero eso se acumula. `reduce` desde `BigDecimal.ZERO` devuelve 0
        //   con la lista vacía, que es justo el borde del contribuyente sin trámites.
        //
        //   Y suma TODOS los trámites del período, sin filtrar por estado. Es una decisión del
        //   requerimiento, no un descuido: quien pide el resumen quiere saber cuánto se declaró,
        //   no cuánto se pagó.

        contador.emitidos().increment();
        // ^ La métrica de negocio, que YA venía declarada en `base/`. Se incrementa DESPUÉS de
        //   armar el resultado: si algo falla arriba, no se cuenta un resumen que no se emitió.

        return new ResumenOficina(oficina.getCodigo(), oficina.getNombre(),
                desde, hasta, delPeriodo, total);
    }

    private static TramiteDelResumen aDto(Tramite t) {
        return new TramiteDelResumen(t.getId(), t.getTipo(), t.getEstado(), t.getFecha(), t.getMonto());
    }
    // ^ La traducción de entidad a DTO, en un solo sitio. Nótese que `getMonto()` se convierte en
    //   `montoDeclarado`: el nombre de dentro y el de fuera son distintos a propósito.
}
