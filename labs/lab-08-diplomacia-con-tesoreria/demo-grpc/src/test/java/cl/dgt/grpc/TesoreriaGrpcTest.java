package cl.dgt.grpc;

import static org.assertj.core.api.Assertions.assertThat;

import cl.dgt.grpc.teso.ConfirmacionRequest;
import cl.dgt.grpc.teso.ConfirmacionResponse;
import cl.dgt.grpc.teso.TesoreriaGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

/**
 * La demo, verificable: una llamada gRPC de verdad contra el servidor levantado.
 *
 * <p>Existe para que el relator no dependa de que «funcionaba la última vez». Un
 * {@code ./mvnw test} verde delante de la sala vale más que una diapositiva, y si algún día
 * el tren de dependencias rompe la generación de stubs, esto se pone rojo aquí y no en
 * clase.
 *
 * <p>Puerto 9099 y no 9090 para no chocar con una instancia que el relator haya dejado
 * corriendo mientras ensaya.
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@TestPropertySource(properties = "spring.grpc.server.port=9099")
@DisplayName("TESO confirma el pago por gRPC")
class TesoreriaGrpcTest {

    @Test
    @DisplayName("una llamada real al servicio devuelve el comprobante")
    void tesoConfirmaElPagoPorGrpc() {
        // `usePlaintext`: sin TLS. En producción gRPC va cifrado — aquí es una demo local
        // y añadir certificados no enseñaría nada sobre gRPC.
        ManagedChannel canal = ManagedChannelBuilder.forAddress("localhost", 9099)
                .usePlaintext()
                .build();
        try {
            ConfirmacionResponse respuesta = TesoreriaGrpc.newBlockingStub(canal)
                    .confirmar(ConfirmacionRequest.newBuilder()
                            .setFolio("DGT-00000042")
                            .setMonto(15_000)
                            .build());

            assertThat(respuesta.getPagado())
                    .as("el monto es positivo: TESO confirma")
                    .isTrue();
            assertThat(respuesta.getComprobante())
                    .as("el comprobante viaja en el mismo mensaje binario")
                    .isEqualTo("TESO-DGT-00000042");
        } finally {
            canal.shutdownNow();
        }
    }
}
