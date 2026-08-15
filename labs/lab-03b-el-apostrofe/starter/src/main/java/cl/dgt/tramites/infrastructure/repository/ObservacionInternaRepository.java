package cl.dgt.tramites.infrastructure.repository;

import cl.dgt.tramites.domain.entity.ObservacionInterna;

import java.util.List;

/**
 * El repositorio de observaciones internas.
 *
 * <p><strong>{{TODO_2}} · ~15 min · Esto todavía no es un repositorio.</strong> Es una interfaz
 * suelta: nadie la implementa y Spring no crea ningún bean con ella. Por eso el método de abajo
 * no lo puede llamar nadie.
 *
 * <p><em>Qué hacer.</em> Dos cambios, y ninguno es escribir SQL:
 * <ul>
 *   <li>Que la interfaz <strong>extienda</strong>
 *       {@code JpaRepository<ObservacionInterna, Long>}. Con eso Spring Data genera la
 *       implementación al arrancar: no hay clase que escribir.</li>
 *   <li>Dejar declarado {@code findByContribuyenteRut(String rut)}. <strong>El nombre ES la
 *       consulta</strong>: Spring Data lo parte en {@code findBy} + {@code Contribuyente} +
 *       {@code Rut}, sigue la relación de la entidad y arma el SELECT con su JOIN.</li>
 * </ul>
 *
 * <p><em>Pista.</em> Si el nombre del método no cuadra con los campos de la entidad, la
 * aplicación no arranca y el error te dice exactamente qué propiedad no encontró. Es un error
 * de arranque, no de ejecución: falla temprano y falla claro.
 *
 * <p><em>Y esto es lo importante:</em> tú no escribes la consulta, así que no hay dónde
 * concatenar nada. El RUT viaja como PARÁMETRO. Ahí es donde muere el apóstrofe.
 *
 * <p><em>Lo verifica:</em> {@code E2_ConsultaDerivadaIT}, incluido el test
 * {@code el_apostrofe_ya_no_es_codigo}.
 *
 * <p>Vive en {@code infrastructure} y no en {@code domain} por lo que AU-03 hace cumplir: va a
 * extender Spring Data, y el dominio no conoce a Spring.
 */
public interface ObservacionInternaRepository {

    List<ObservacionInterna> findByContribuyenteRut(String rut);
}
