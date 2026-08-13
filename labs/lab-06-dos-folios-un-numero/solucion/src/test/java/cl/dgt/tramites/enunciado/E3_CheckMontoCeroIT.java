package cl.dgt.tramites.enunciado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * M8 · la restricción como contrato. La V3 agregó {@code CHECK (monto <> 0)} a {@code linea_f29}:
 * ninguna línea vale cero (un débito es positivo, un crédito es negativo; el cero es ruido).
 *
 * <p>Este test inserta una línea de monto CERO por JDBC CRUDO, saltándose toda validación de Java
 * a propósito: la base la tocan también scripts y cargas masivas. La última línea de defensa vive
 * en el motor. Si la V3 no está (o está mal), la base ACEPTA el 0 y este test se pone rojo: el
 * contrato no existe.
 */
@SpringBootTest(properties = "dgt.base-embebida.enabled=false")
@Import(BaseConcurrenciaIT.class)
class E3_CheckMontoCeroIT {

    @Autowired DataSource dataSource;

    @Test
    @DisplayName("la base rechaza una línea de monto cero aunque se salte la validación de Java")
    void elCheckDeLaBaseRechazaMontoCero() {
        assertThatThrownBy(() -> {
            try (Connection cn = dataSource.getConnection();
                 Statement st = cn.createStatement()) {
                // formulario29 id=1 existe en la semilla (V2). Insertamos DIRECTO, sin pasar por Java.
                st.executeUpdate(
                        "INSERT INTO linea_f29 (formulario29_id, codigo, monto) VALUES (1, '999', 0)");
            }
        })
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("ck_linea_f29_monto_no_cero");
    }
}
