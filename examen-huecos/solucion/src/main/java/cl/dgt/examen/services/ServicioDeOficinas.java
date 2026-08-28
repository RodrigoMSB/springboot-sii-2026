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
        return buscar(codigo).getSolicitudes().size();
    }

    @Transactional(readOnly = true)
    public List<FichaOficina> porComuna(String comuna) {
        return oficinas.findByComuna(comuna).stream()
                .map(FichaOficina::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumenOficina resumenDe(String codigo) {
        Oficina oficina = buscar(codigo);
        long cuantas = solicitudes.countByOficinaCodigo(codigo);
        return new ResumenOficina(oficina.getCodigo(), oficina.getNombre(), oficina.getComuna(), cuantas);
    }

    @Transactional(readOnly = true)
    public List<OficinaBreve> todas() {
        return oficinas.findAll().stream()
                .map(o -> new OficinaBreve(o.getCodigo(), o.getComuna()))
                .toList();
    }

    /** Viene resuelto: buscar por código y fallar con la excepción del dominio. */
    private Oficina buscar(String codigo) {
        return oficinas.findByCodigo(codigo)
                .orElseThrow(() -> new OficinaNoEncontrada(codigo));
    }
}
