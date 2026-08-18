package cl.dgt.resiliencia.services;

import cl.dgt.resiliencia.tesoreria.ClienteTesoreria;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PagoService {

    private final ClienteTesoreria cliente;

    public PagoService(ClienteTesoreria cliente) {
        this.cliente = cliente;
    }

    // Paso 3 · declara un Retry. Paso 4 · declara un CircuitBreaker y publica sus transiciones.
    // escribe aquí

    public Map<String, Object> consultar(String id) {
        // Paso 3 y 4 · envuelve la llamada. Paso 5 · devuelve una respuesta degradada si falla.
        // escribe aquí
        return cliente.consultarPago(id);
    }

    // Paso 4 · devuelve el estado del circuito y sus métricas.
    // escribe aquí
}
