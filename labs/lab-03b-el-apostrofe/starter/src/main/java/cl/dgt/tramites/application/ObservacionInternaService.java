package cl.dgt.tramites.application;

import cl.dgt.tramites.infrastructure.legacy.ReporteInternoLegacyDao;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Las observaciones internas de un contribuyente.
 *
 * <p>Hoy este servicio le pregunta al {@code ReporteInternoLegacyDao}, que habla JDBC crudo y
 * trae el apóstrofe puesto. Ábrelo y cuéntale los cuatro pecados antes de tocar nada:
 * {@code infrastructure/legacy/ReporteInternoLegacyDao.java}.
 *
 * <p><strong>{{TODO_3}} · ~15 min · Cambiar el mecanismo, no el contrato.</strong>
 *
 * <p><em>Qué hacer.</em>
 * <ol>
 *   <li>Reemplazar la dependencia: en vez de {@code ReporteInternoLegacyDao}, este servicio
 *       recibe {@code ObservacionInternaRepository} (el que armaste en el TODO_2).</li>
 *   <li>Llamar a {@code findByContribuyenteRut(rut)} y convertir cada
 *       {@code ObservacionInterna} en un {@code ObservacionInternaVista} — el RUT sale de
 *       {@code o.getContribuyente().getRut()}.</li>
 *   <li>Anotar el método con {@code @Transactional(readOnly = true)}. Hace falta porque el
 *       {@code @ManyToOne} es LAZY: leer el RUT del contribuyente necesita que la sesión de
 *       persistencia siga abierta. Sin eso verás una {@code LazyInitializationException}, y
 *       encontrártela una vez vale más que leer sobre ella.</li>
 * </ol>
 *
 * <p>Fíjate en lo que <em>no</em> hay que tocar: el controlador. El endpoint se ve igual desde
 * fuera; cambia por dónde salen los datos.
 *
 * <p><strong>Y de paso, mira el SQL.</strong> Arranca con
 * {@code ./bin/start-lab.sh --dir starter --ver-sql} y pide las observaciones: en el log va a
 * aparecer el SELECT que Hibernate generó por ti, con su JOIN y con un {@code ?} donde va el
 * RUT. Ese signo de pregunta es el final del apóstrofe.
 *
 * <p><em>Lo verifica:</em> {@code E3_EndpointMigradoIT}.
 */
@Service
@Profile("dev")
public class ObservacionInternaService {

    private final ReporteInternoLegacyDao dao;

    public ObservacionInternaService(ReporteInternoLegacyDao dao) {
        this.dao = dao;
    }

    public List<ObservacionInternaVista> porRut(String rut) {
        return dao.observacionesDe(rut);
    }
}
