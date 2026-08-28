package cl.dgt.examen;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Imprime la tabla de huecos al terminar la suite.
 *
 * <p>No se mide sin mostrar: la nota del examen es «huecos resueltos sobre el total», así que la
 * suite no se limita a decir cuántos tests fallaron — dice qué hueco está verde y cuál no, y
 * termina con la cifra que se entrega.
 */
public class RegistroDeHuecos implements TestWatcher, AfterAllCallback {

    private static final Map<String, Boolean> RESULTADO = new LinkedHashMap<>();

    @Override
    public void testSuccessful(ExtensionContext contexto) {
        RESULTADO.put(contexto.getDisplayName(), true);
    }

    @Override
    public void testFailed(ExtensionContext contexto, Throwable causa) {
        RESULTADO.put(contexto.getDisplayName(), false);
    }

    @Override
    public void testAborted(ExtensionContext contexto, Throwable causa) {
        RESULTADO.put(contexto.getDisplayName(), false);
    }

    @Override
    public void testDisabled(ExtensionContext contexto, Optional<String> motivo) {
        RESULTADO.put(contexto.getDisplayName(), false);
    }

    @Override
    public void afterAll(ExtensionContext contexto) {
        int resueltos = (int) RESULTADO.values().stream().filter(Boolean::booleanValue).count();
        int total = RESULTADO.size();

        StringBuilder salida = new StringBuilder("\n")
                .append("=============================================================================\n")
                .append(" HUECOS\n")
                .append("-----------------------------------------------------------------------------\n");

        RESULTADO.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> salida.append(e.getValue() ? "  [OK]    " : "  [FALTA] ")
                        .append(e.getKey()).append('\n'));

        salida.append("-----------------------------------------------------------------------------\n")
                .append(" RESUELTOS: ").append(resueltos).append(" de ").append(total).append('\n')
                .append("=============================================================================\n");

        System.out.println(salida);
    }
}
