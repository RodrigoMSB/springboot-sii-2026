package cl.dgt.tramites.config;

/**
 * La configuración de la DGT, tipada y validada.
 *
 * <p>Hoy este record existe pero <strong>nadie lo usa</strong>: {@code SelloService} lee la
 * configuración con {@code @Value}, uno por uno. Eso funciona… hasta que alguien escribe
 * {@code dgt.folio.largo: ocho} y la aplicación arranca igual, y falla al emitir el primer
 * folio, en producción.
 *
 * <p><strong>TODO_3 — Convierte esto en configuración tipada y validada (≈15 min).</strong>
 *
 * <p><em>Qué:</em> haz que Spring enlace el prefijo {@code dgt} a este record, y que valide
 * sus campos al arrancar.
 *
 * <p><em>Por qué:</em> un record es inmutable (nadie cambia el prefijo del folio en
 * caliente) y la validación convierte un error de configuración en un arranque fallido, no
 * en un folio de tres dígitos emitido a un contribuyente. Un invariante que se descubre en
 * producción ya perdió.
 *
 * <p><em>Reglas que sostiene:</em> el largo del folio no es un detalle de formato; es una
 * decisión de negocio disfrazada de configuración.
 *
 * <p>Pista 2: necesitas {@code @ConfigurationProperties(prefix = "dgt")} y
 * {@code @Validated} sobre el record; {@code @NotBlank} sobre los textos; {@code @Min(6)}
 * sobre el largo; y {@code @Valid} sobre el record anidado para que la validación entre en
 * él. La aplicación ya tiene {@code @ConfigurationPropertiesScan}: no hace falta declarar
 * ningún bean a mano.
 *
 * <p>Cuando termines, {@code T3_DgtPropertiesTest} se pondrá verde y podrás hacer que
 * {@code SelloService} lo consuma.
 */
public record DgtProperties(

        String institucion,

        Folio folio) {

    public record Folio(
            String prefijo,
            int largo) {
    }
}
