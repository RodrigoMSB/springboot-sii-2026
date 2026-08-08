package cl.dgt.tramites.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;

/**
 * Enciende las dos mitades del Módulo 10: el reloj ({@code @EnableScheduling}) y la asincronía
 * ({@code @EnableAsync}). Sin estas anotaciones, {@code @Scheduled} y {@code @Async} son
 * comentarios decorativos: <strong>no fallan, no avisan, y no hacen nada</strong>. Es el mismo
 * silencio del {@code @EnableCaching} olvidado del Lab 10.
 */
@Configuration
@EnableScheduling
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /** Nombre del executor. Constante y no literal suelto: lo nombra cada {@code @Async}. */
    public static final String EJECUTOR_VIRTUAL = "ejecutorVirtual";

    /**
     * El executor de las notificaciones: <strong>un hilo virtual por tarea</strong>.
     *
     * <p>{@code setVirtualThreads(true)} cambia la naturaleza del recurso. Un pool clásico
     * ({@code ThreadPoolTaskExecutor}) tiene un número de hilos y una cola, y ese número es una
     * apuesta: pocos y las tareas hacen fila; muchos y la máquina se ahoga en cambios de contexto y
     * memoria de pilas. Con hilos virtuales no hay tal apuesta — se crea uno por tarea, cuestan
     * kilobytes en vez de megabytes, y el que se bloquea esperando devuelve su hilo portador.
     *
     * <p><strong>Lo que sigue siendo tuyo:</strong> el límite del otro lado. Diez mil hilos
     * virtuales contra un servidor de correo que aguanta cincuenta no es paralelismo, es una
     * denegación de servicio con tu firma. Si eso te preocupa, {@code setConcurrencyLimit(n)} pone
     * el freno donde corresponde: en la salida, no en el pool.
     */
    @Bean(EJECUTOR_VIRTUAL)
    public Executor ejecutorVirtual() {
        SimpleAsyncTaskExecutor ejecutor = new SimpleAsyncTaskExecutor("dgt-notif-");
        ejecutor.setVirtualThreads(true);
        return ejecutor;
    }

    /**
     * Qué hacer con las excepciones de un {@code @Async} que devuelve {@code void}.
     *
     * <p>Sin esto, esa excepción se pierde en el aire: el llamador ya siguió su camino y no hay
     * nadie a quien propagársela. Este manejador es la última red — registra el método, sus
     * argumentos y la causa, para que al menos quede rastro. Es exactamente el {@code catch} que
     * traga del Lab 09, pero al revés: aquí el {@code catch} es lo que impide que el error
     * desaparezca.
     *
     * <p>Cuando el llamador SÍ necesita enterarse, la firma debe devolver
     * {@code CompletableFuture<T>}: la excepción viaja dentro del futuro y quien lo componga la
     * recibe.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
