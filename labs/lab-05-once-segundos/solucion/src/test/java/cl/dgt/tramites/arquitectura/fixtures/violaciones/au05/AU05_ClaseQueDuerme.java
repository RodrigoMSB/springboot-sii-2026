package cl.dgt.tramites.arquitectura.fixtures.violaciones.au05;

/** Viola AU-05: duerme un número de milisegundos en vez de esperar una condición. */
public class AU05_ClaseQueDuerme {
    public void esperarAlBackend() throws InterruptedException {
        Thread.sleep(600L);
    }
}
