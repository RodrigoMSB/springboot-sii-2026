package cl.dgt.tramites.application;

import cl.dgt.tramites.domain.entity.Formulario29;
import cl.dgt.tramites.domain.entity.LineaF29;
import cl.dgt.tramites.domain.exception.TramiteNoEncontradoException;
import cl.dgt.tramites.infrastructure.repository.Formulario29Repository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Declarar una línea del F29: el ÚNICO camino por el que cambian los totales que
 * {@link ReporteService#totalDeclaradoPorPeriodo()} sirve cacheados.
 *
 * <p><strong>Por eso el {@code @CacheEvict} vive aquí y no allá.</strong> Un caché sin
 * invalidación es un mentiroso con buena memoria: responde rápido, con seguridad, y con el dato de
 * antes. La regla que hay que llevarse: <em>quien ESCRIBE el dato es el responsable de invalidar
 * su copia</em>. Si esa responsabilidad se deja en el lector, no hay forma de saber cuándo
 * invalidar — y el bug no se ve, porque el sistema no falla: solo miente.
 *
 * <p><strong>{@code allEntries = true}, y no una clave.</strong> El caché guarda la lista completa
 * de períodos bajo una sola entrada; una línea nueva de cualquier período cambia esa lista. Evictar
 * «la clave de ese período» sería falso: la entrada no está indexada por período. Cuando la
 * granularidad del caché y la de la escritura no coinciden, se tira todo. Es más caro y es
 * correcto, y en ese orden se decide.
 *
 * <p><strong>Y por qué está en otro bean.</strong> {@code @CacheEvict}, como {@code @Cacheable} y
 * {@code @Transactional}, lo aplica un PROXY. Una llamada entre métodos de la misma clase no pasa
 * por el proxy: la anotación no se dispara y nadie avisa. Separar el que escribe del que lee no es
 * ceremonia arquitectónica — es esquivar la trampa del Lab 09, otra vez.
 */
@Service
public class DeclaracionService {

    private final Formulario29Repository formularios;

    public DeclaracionService(Formulario29Repository formularios) {
        this.formularios = formularios;
    }

    /**
     * Registra una línea en el F29 del trámite y deja el caché de totales inservible a propósito.
     *
     * <p>El monto puede ser negativo: los créditos lo son. Lo que no puede ser es cero — lo impide
     * el {@code CHECK (monto <> 0)} de la V3, y por eso este método no lo revalida: el contrato
     * vive en la base, no en un {@code if} que alguien puede olvidar.
     *
     * @return el total del formulario después de agregar la línea (RN-06: derivado, jamás persistido)
     */
    @CacheEvict(cacheNames = ReporteService.CACHE_TOTALES, allEntries = true)
    @Transactional
    public long declararLinea(Long tramiteId, String codigo, long monto) {
        Formulario29 formulario = formularios.findByTramiteId(tramiteId)
                .orElseThrow(() -> new TramiteNoEncontradoException(tramiteId));

        // cascade = ALL en Formulario29.lineas: al agregar la línea al agregado y guardar el
        // formulario, la línea se persiste con él. El agregado manda; no se guarda la línea suelta.
        formulario.agregarLinea(new LineaF29(formulario, codigo, monto));
        formularios.save(formulario);

        return formulario.total();
    }
}
