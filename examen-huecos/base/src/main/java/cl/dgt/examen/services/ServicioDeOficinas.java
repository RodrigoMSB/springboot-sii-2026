package cl.dgt.examen.services;

import cl.dgt.examen.dto.FichaOficina;
import cl.dgt.examen.dto.OficinaBreve;
import cl.dgt.examen.dto.ResumenOficina;
import cl.dgt.examen.entities.Oficina;
import cl.dgt.examen.exceptions.OficinaNoEncontrada;
import cl.dgt.examen.repositories.OficinaRepository;
import cl.dgt.examen.repositories.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioDeOficinas {

    private final OficinaRepository oficinas;
    private final SolicitudRepository solicitudes;

    public ServicioDeOficinas(OficinaRepository oficinas, SolicitudRepository solicitudes) {
        this.oficinas = oficinas;
        this.solicitudes = solicitudes;
    }

    /** Viene resuelto. Es el que usan la prueba de seguridad y la del 404. */
    @Transactional(readOnly = true)
    public FichaOficina ficha(String codigo) {
        return FichaOficina.de(buscar(codigo));
    }

    @Transactional(readOnly = true)
    public int conteoDeSolicitudes(String codigo) {
        // =========================================================================
        //  HUECO 01 · Cuantas solicitudes tiene la oficina
        // -------------------------------------------------------------------------
        //  Devuelve cuantas solicitudes tiene la oficina de ese codigo.
        //  Va con el hueco 01 de `Oficina`: sin la coleccion, esto no se puede escribir.
        //
        //  ESTA LISTO CUANDO · pasa el test H-01
        // =========================================================================
        throw new UnsupportedOperationException("HUECO 01");
    }

    @Transactional(readOnly = true)
    public List<FichaOficina> porComuna(String comuna) {
        // =========================================================================
        //  HUECO 02 · Las oficinas de una comuna, como fichas
        // -------------------------------------------------------------------------
        //  Devuelve las oficinas de esa comuna, cada una como `FichaOficina`.
        //  `FichaOficina` ya viene resuelta y sabe construirse desde una `Oficina`.
        //
        //  ESTA LISTO CUANDO · pasa el test H-02
        // =========================================================================
        throw new UnsupportedOperationException("HUECO 02");
    }

    @Transactional(readOnly = true)
    public ResumenOficina resumenDe(String codigo) {
        // =========================================================================
        //  HUECO 05 · El resumen de una oficina
        // -------------------------------------------------------------------------
        //  Arma el `ResumenOficina` de esa oficina. Si el codigo no existe, esto tiene
        //  que acabar en un 404 — mira como lo hacen los metodos de al lado.
        //
        //  Para cuantas solicitudes tiene, el repositorio ya trae una consulta resuelta.
        //
        //  ESTA LISTO CUANDO · pasa el test H-05
        // =========================================================================
        throw new UnsupportedOperationException("HUECO 05");
    }

    @Transactional(readOnly = true)
    public List<OficinaBreve> todas() {
        // =========================================================================
        //  HUECO 06 · Todas las oficinas, en version breve
        // -------------------------------------------------------------------------
        //  Devuelve TODAS las oficinas como `OficinaBreve`.
        //
        //  ESTA LISTO CUANDO · pasa el test H-06
        // =========================================================================
        throw new UnsupportedOperationException("HUECO 06");
    }

    /** Viene resuelto: buscar por código y fallar con la excepción del dominio. */
    private Oficina buscar(String codigo) {
        return oficinas.findByCodigo(codigo)
                .orElseThrow(() -> new OficinaNoEncontrada(codigo));
    }
}
