package cl.dgt.grpc;

import cl.dgt.grpc.teso.ConfirmacionRequest;
import cl.dgt.grpc.teso.ConfirmacionResponse;
import cl.dgt.grpc.teso.TesoreriaGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

/**
 * TESO, hablando gRPC. Demo del relator del Lab 08 — no es material del alumno.
 *
 * <p>Fíjate en lo que <strong>no</strong> hay: ni un {@code @RestController}, ni una ruta,
 * ni un verbo HTTP, ni un código de estado. No hay URL que acordar y no hay JSON que
 * serializar. Lo que hay es un contrato ({@code src/main/proto/teso.proto}) del que se
 * generan las clases de los dos lados.
 *
 * <p>Arranca en el puerto <strong>9090</strong> y no publica nada por HTTP: gRPC va sobre
 * HTTP/2 con cuerpos binarios, así que un navegador no le sirve de cliente. Esa es
 * justamente una de las cosas que hay que saber antes de elegirlo.
 */
@SpringBootApplication
public class DemoGrpcApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoGrpcApplication.class, args);
    }
}

/**
 * La implementación del servicio.
 *
 * <p>{@code TesoreriaGrpc.TesoreriaImplBase} <strong>no existe en este repositorio</strong>:
 * la genera el compilador de Protocol Buffers a partir del {@code .proto}, en
 * {@code target/generated-sources/}. Si borras el {@code .proto}, esta clase deja de
 * compilar — que es exactamente la garantía que REST no te da.
 *
 * <p>El {@code StreamObserver} es la forma en que gRPC modela la respuesta, y no es un
 * capricho: el mismo mecanismo sirve para devolver <em>un</em> valor ({@code onNext} una
 * vez y {@code onCompleted}) o un flujo de miles. El streaming bidireccional no es una
 * extensión de gRPC; es la forma normal de su API.
 */
@Component
class TesoreriaGrpcService extends TesoreriaGrpc.TesoreriaImplBase {

    @Override
    public void confirmar(ConfirmacionRequest peticion, StreamObserver<ConfirmacionResponse> respuesta) {
        respuesta.onNext(ConfirmacionResponse.newBuilder()
                .setPagado(peticion.getMonto() > 0)
                .setComprobante("TESO-" + peticion.getFolio())
                .build());
        respuesta.onCompleted();
    }
}
