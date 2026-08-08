package cl.dgt.tramites.config.observabilidad;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * El auditor invisible. Registra CADA invocación a un servicio de la aplicación —qué método, con
 * qué argumentos, cuánto tardó— sin que la lógica de negocio se entere. Eso es AOP: el "qué
 * registrar" (transversal) vive aquí, separado del "qué hace" (el servicio). El código de negocio
 * queda idéntico: byte a byte igual que el Lab 08.
 *
 * <p><strong>El límite del proxy.</strong> El aspecto se aplica sobre el PROXY del bean, igual que
 * {@code @Transactional}. Si un método de un servicio llama a OTRO método público del MISMO bean
 * con {@code this.otro(...)}, esa llamada NO pasa por el proxy: el aspecto no la ve. Es la misma
 * trampa del Lab 06. Se audita lo que entra por la puerta, no lo que se llama por dentro.
 *
 * <p><strong>Datos sensibles enmascarados.</strong> Los argumentos de tipo texto se registran
 * PARCIALES (un RUT queda como {@code 123***}). Y la clave jamás llega aquí: el emisor de tokens
 * no es un {@code *Service}, así que el pointcut no lo intercepta. Un log de auditoría que filtra
 * una credencial es peor que no tener log.
 */
@Aspect
@Component
public class AspectoDeAuditoria {

    private static final Logger log = LoggerFactory.getLogger("AUDITORIA");

    @Around("execution(* cl.dgt.tramites.application..*Service.*(..))")
    public Object auditar(ProceedingJoinPoint punto) throws Throwable {
        String metodo = punto.getSignature().getDeclaringType().getSimpleName()
                + "." + punto.getSignature().getName();
        String args = Arrays.toString(enmascarar(punto.getArgs()));

        long inicio = System.nanoTime();
        try {
            Object resultado = punto.proceed();
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            log.info("invocacion metodo={} args={} ms={} resultado=ok", metodo, args, ms);
            return resultado;
        } catch (Throwable e) {
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            // NO se traga la excepción: se registra CON CONTEXTO y se re-lanza. Ese es el
            // reemplazo del `catch` que traga del starter.
            log.warn("invocacion metodo={} args={} ms={} resultado=error tipo={}",
                    metodo, args, ms, e.getClass().getSimpleName());
            throw e;
        }
    }

    private Object[] enmascarar(Object[] args) {
        return Arrays.stream(args)
                .map(a -> (a instanceof String texto) ? enmascararTexto(texto) : a)
                .toArray();
    }

    /** Deja ver lo justo para reconocer, no lo suficiente para robar. */
    private String enmascararTexto(String texto) {
        return texto.length() <= 3 ? "***" : texto.substring(0, 3) + "***";
    }
}
