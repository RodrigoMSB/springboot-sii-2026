package cl.dgt.tramites.application;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Guardar una observación interna y recuperarla.
 *
 * <p><strong>{{TODO_3}} · ~20 min · Conectar el servicio al repositorio.</strong> Los dos
 * métodos de abajo están vacíos: hoy explotan. El controlador ya está escrito y ya llama aquí,
 * así que en cuanto esto funcione, la aplicación entera funciona.
 *
 * <p><em>Qué hacer.</em>
 * <ol>
 *   <li>Recibir por constructor {@code ObservacionInternaRepository} (el del TODO_2) y también
 *       {@code ContribuyenteRepository}, que ya existe desde el Lab 02 — hace falta para
 *       encontrar al contribuyente al que se le anota la observación.</li>
 *   <li><strong>guardar:</strong> buscar el contribuyente por su RUT
 *       ({@code findByRut}, y si no está, lanzar {@code ContribuyenteNoEncontradoException}),
 *       construir una {@code ObservacionInterna} y pasarla por {@code save}. Devolver un
 *       {@code ObservacionInternaVista} con lo guardado.</li>
 *   <li><strong>porRut:</strong> llamar a {@code findByContribuyenteRut} y convertir cada
 *       entidad en un {@code ObservacionInternaVista} — el RUT sale de
 *       {@code o.getContribuyente().getRut()}.</li>
 *   <li>Anotar {@code guardar} con {@code @Transactional} y {@code porRut} con
 *       {@code @Transactional(readOnly = true)}.</li>
 * </ol>
 *
 * <p><em>Por qué la anotación importa, y no es decoración:</em> la relación con el contribuyente
 * es LAZY, así que leer su RUT necesita que la sesión de persistencia siga abierta. Sin
 * {@code @Transactional} verás una {@code LazyInitializationException} — y encontrártela una vez
 * vale más que leer sobre ella.
 *
 * <p><strong>Y ahora mira el SQL.</strong> Arranca con
 * {@code ./bin/start-lab.sh --ver-sql}, guarda una observación y pídela de vuelta. En
 * {@code .estado/dgt.log} van a aparecer el {@code INSERT} y el {@code SELECT} que Hibernate
 * escribió a partir de tu entidad. Tú escribiste cero líneas de SQL.
 *
 * <p><em>Lo verifica:</em> {@code E3_ServicioConectadoIT}.
 */
@Service
@Profile("dev")
public class ObservacionInternaService {

    public ObservacionInternaVista guardar(String rut, String texto, String autor) {
        throw new UnsupportedOperationException("{{TODO_3}}");
    }

    public List<ObservacionInternaVista> porRut(String rut) {
        throw new UnsupportedOperationException("{{TODO_3}}");
    }
}
