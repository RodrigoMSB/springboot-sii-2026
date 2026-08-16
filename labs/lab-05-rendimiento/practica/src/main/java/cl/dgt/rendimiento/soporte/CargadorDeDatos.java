package cl.dgt.rendimiento.soporte;

import cl.dgt.rendimiento.entities.Contribuyente;
import cl.dgt.rendimiento.entities.Tramite;
import cl.dgt.rendimiento.repositories.ContribuyenteRepository;
import cl.dgt.rendimiento.repositories.TramiteRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Siembra la base con <strong>200 contribuyentes y 5 trámites cada uno</strong> — 1.000 trámites.
 * <strong>Viene dado.</strong>
 *
 * <p>Ese tamaño no es capricho: con tres filas el N+1 no se nota, y ese es exactamente el problema
 * que tiene en la vida real. Con 200 se nota, y sigue cabiendo en un laboratorio.
 *
 * <p>Solo siembra <strong>si la base está vacía</strong>. Así los datos persisten entre arranques y
 * las mediciones se repiten iguales.
 */
@Component
public class CargadorDeDatos {

    private static final int CUANTOS_CONTRIBUYENTES = 200;
    private static final int TRAMITES_POR_CONTRIBUYENTE = 5;

    private static final List<String> TIPOS = List.of(
            "Declaración F29", "Certificado de situación", "Inicio de actividades",
            "Cambio de domicilio", "Término de giro");
    private static final List<String> ESTADOS = List.of("RECIBIDO", "EMITIDO", "APROBADO", "OBSERVADO");

    private final ContribuyenteRepository contribuyentes;
    private final TramiteRepository tramites;

    public CargadorDeDatos(ContribuyenteRepository contribuyentes, TramiteRepository tramites) {
        this.contribuyentes = contribuyentes;
        this.tramites = tramites;
    }

    @Transactional
    public void sembrarSiHaceFalta() {
        long yaHay = contribuyentes.count();
        if (yaHay >= CUANTOS_CONTRIBUYENTES) {
            System.out.println("  base ya sembrada: " + yaHay + " contribuyentes y "
                    + tramites.count() + " trámites (persisten de la corrida anterior)");
            return;
        }

        System.out.println("  sembrando " + CUANTOS_CONTRIBUYENTES + " contribuyentes con "
                + TRAMITES_POR_CONTRIBUYENTE + " trámites cada uno…");
        tramites.deleteAll();
        contribuyentes.deleteAll();

        List<Tramite> lote = new ArrayList<>();
        for (int i = 1; i <= CUANTOS_CONTRIBUYENTES; i++) {
            Contribuyente c = contribuyentes.save(new Contribuyente(
                    String.format("7%d.%03d.%03d-%d", i % 10, i % 1000, (i * 7) % 1000, i % 10),
                    "Contribuyente " + String.format("%03d", i) + " Ltda."));
            for (int j = 0; j < TRAMITES_POR_CONTRIBUYENTE; j++) {
                lote.add(new Tramite(
                        TIPOS.get((i + j) % TIPOS.size()),
                        ESTADOS.get((i + j) % ESTADOS.size()),
                        LocalDate.of(2026, 1 + ((i + j) % 12), 1 + ((i * j) % 28)),
                        c));
            }
        }
        tramites.saveAll(lote);
        System.out.println("  sembrados: " + contribuyentes.count() + " contribuyentes, "
                + tramites.count() + " trámites");
    }
}
