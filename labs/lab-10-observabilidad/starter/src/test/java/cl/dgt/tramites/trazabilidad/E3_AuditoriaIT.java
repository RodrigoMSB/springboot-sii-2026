package cl.dgt.tramites.trazabilidad;

import ch.qos.logback.classic.spi.ILoggingEvent;
import cl.dgt.tramites.application.ContribuyenteService;
import cl.dgt.tramites.application.EnsayoDeProxyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_3 · El auditor invisible (AOP). Invocar un servicio deja un rastro de auditoría —método,
 * argumentos ENMASCARADOS, tiempo— sin tocar la lógica. Y respeta el límite del proxy: una
 * autoinvocación ({@code this.otro()}) no se intercepta, igual que {@code @Transactional}.
 */
class E3_AuditoriaIT extends BaseTrazabilidadIT {

    @Autowired ContribuyenteService contribuyentes;
    @Autowired EnsayoDeProxyService ensayo;

    @Test
    @DisplayName("el aspecto audita la invocación y enmascara el RUT")
    void auditaYEnmascara() {
        List<ILoggingEvent> logs = capturarLogs("AUDITORIA",
                () -> contribuyentes.buscarPorRut("12345678-5"));

        assertThat(logs).isNotEmpty();
        String mensaje = logs.get(0).getFormattedMessage();
        assertThat(mensaje)
                .contains("ContribuyenteService.buscarPorRut")
                .contains("ms=");
        assertThat(mensaje)
                .as("el RUT jamás va completo al log")
                .doesNotContain("12345678-5")
                .contains("123***");
    }

    @Test
    @DisplayName("el aspecto NO intercepta la autoinvocación (límite del proxy)")
    void noInterceptaAutoinvocacion() {
        List<ILoggingEvent> logs = capturarLogs("AUDITORIA", () -> ensayo.externo());

        long externos = logs.stream().filter(e -> e.getFormattedMessage().contains(".externo")).count();
        long internos = logs.stream().filter(e -> e.getFormattedMessage().contains(".interno")).count();
        assertThat(externos).as("la puerta de entrada se audita").isEqualTo(1);
        assertThat(internos).as("la autoinvocación NO pasa por el proxy: no se audita").isZero();
    }
}
