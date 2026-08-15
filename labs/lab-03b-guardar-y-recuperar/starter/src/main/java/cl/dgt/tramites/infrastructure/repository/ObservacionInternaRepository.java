package cl.dgt.tramites.infrastructure.repository;

import cl.dgt.tramites.domain.entity.ObservacionInterna;

import java.util.List;

/**
 * El repositorio de observaciones internas: guardar y buscar.
 *
 * <p><strong>{{TODO_2}} · ~20 min · Esto todavía no es un repositorio.</strong> Es una interfaz
 * suelta: nadie la implementa y Spring no crea ningún bean con ella, así que el método de abajo
 * no lo puede llamar nadie.
 *
 * <p><em>Qué hacer.</em> Dos cambios, y ninguno es escribir SQL:
 * <ul>
 *   <li>Que la interfaz <strong>extienda</strong>
 *       {@code JpaRepository<ObservacionInterna, Long>}. Con eso Spring Data genera la
 *       implementación al arrancar — no hay clase que escribir— y con ella llegan gratis
 *       {@code save}, {@code findById}, {@code findAll} y {@code delete}.</li>
 *   <li>Dejar declarado {@code findByContribuyenteRut(String rut)}. <strong>El nombre ES la
 *       consulta</strong>: Spring Data lo parte en {@code findBy} + {@code Contribuyente} +
 *       {@code Rut}, sigue la relación que declaraste en la entidad y arma el {@code SELECT}
 *       con su {@code JOIN}.</li>
 * </ul>
 *
 * <p><em>El que hace la magia visible es {@code save}:</em> le pasas un objeto y le hace el
 * {@code INSERT}; después te devuelve la instancia con el {@code id} que generó el motor ya
 * puesto. Un objeto entra, una fila aparece.
 *
 * <p><em>Pista.</em> Si el nombre del método no cuadra con los campos de la entidad, la
 * aplicación no arranca y el error dice exactamente qué propiedad no encontró. Es un error de
 * arranque, no de ejecución: falla temprano y falla claro.
 *
 * <p><em>Lo verifica:</em> {@code E2_GuardarYRecuperarIT}.
 *
 * <p>Vive en {@code infrastructure} y no en {@code domain} por lo que AU-03 hace cumplir: va a
 * extender Spring Data, y el dominio no conoce a Spring.
 */
public interface ObservacionInternaRepository {

    /**
     * Declarado aquí solo para que los tests compilen mientras el TODO_2 no está hecho. Cuando
     * la interfaz extienda {@code JpaRepository}, esta firma la hereda de ahí — puedes borrarla
     * o dejarla, es exactamente la misma.
     */
    <S extends ObservacionInterna> S save(S observacion);

    List<ObservacionInterna> findByContribuyenteRut(String rut);
}
