package cl.dgt.rendimiento.demos;

import cl.dgt.rendimiento.repositories.ContribuyenteRepository;
import cl.dgt.rendimiento.soporte.ContadorDeConsultas;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemosRendimiento {

    private final ContribuyenteRepository contribuyentes;
    private final ContadorDeConsultas contador;

    public DemosRendimiento(ContribuyenteRepository contribuyentes, ContadorDeConsultas contador) {
        this.contribuyentes = contribuyentes;
        this.contador = contador;
    }

    @Transactional(readOnly = true)
    public void elCrimen() {
        seccion(1, "EL CRIMEN · findAll() y tocar la relación");
        // Trae todos con findAll() y toca la relación de cada uno; cuenta las consultas.
        // escribe aquí
    }

    @Transactional(readOnly = true)
    public void conJoinFetch() {
        seccion(2, "JOIN FETCH · traerlo todo de una vez");
        // Llama a la consulta con JOIN FETCH y vuelve a contar.
        // escribe aquí
    }

    @Transactional(readOnly = true)
    public void conEntityGraph() {
        seccion(3, "@EntityGraph · lo mismo, sin JPQL");
        // Haz lo mismo con @EntityGraph, sin escribir JPQL.
        // escribe aquí
    }

    @Transactional(readOnly = true)
    public void conProyeccion() {
        seccion(4, "PROYECCIÓN · traer solo lo que se muestra");
        // Trae sólo los campos que la pantalla muestra.
        // escribe aquí
    }

    @Transactional(readOnly = true)
    public void laPantallaQueNoNecesitaTramites() {
        seccion(5, "LA OTRA PANTALLA · solo razones sociales");
        // Trae sólo las razones sociales y compara el costo.
        // escribe aquí
    }

    private void seccion(int numero, String titulo) {
        System.out.println();
        System.out.println("=== " + numero + " · " + titulo + " ===");
    }
}
