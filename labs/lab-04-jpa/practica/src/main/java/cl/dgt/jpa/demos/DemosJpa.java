package cl.dgt.jpa.demos;

import org.springframework.stereotype.Service;

@Service
public class DemosJpa {

    public void guardar() {
        seccion(1, "GUARDAR · save()");
        // Guarda dos observaciones con save() e imprime el id que asignó la base.
        // escribe aquí
    }

    public void buscarPorId() {
        seccion(2, "BUSCAR POR ID · findById()");
        // Busca por id con findById() y muestra qué devuelve cuando no existe.
        // escribe aquí
    }

    public void listarTodas() {
        seccion(3, "LISTAR TODAS · findAll()");
        // Trae todas con findAll() e imprime cuántas son.
        // escribe aquí
    }

    public void buscarPorAutor() {
        seccion(4, "BUSCAR POR AUTOR · findByAutor()");
        // Llama a findByAutor() e imprime lo que vuelve.
        // escribe aquí
    }

    public void buscarConDosCondiciones() {
        seccion(5, "DOS CONDICIONES · findByAutorAndFechaAfter()");
        // Llama a findByAutorAndFechaAfter() con una fecha de corte.
        // escribe aquí
    }

    public void actualizar() {
        seccion(6, "ACTUALIZAR SIN save() · dirty checking");
        // Cambia el texto de una observación dentro de la transacción, SIN llamar a save().
        // escribe aquí
    }

    public void borrar() {
        seccion(7, "BORRAR · deleteById()");
        // Borra una por id y comprueba con count() que ya no está.
        // escribe aquí
    }

    public void contar() {
        seccion(8, "CONTAR · count() vs findAll().size()");
        // Compara count() contra findAll().size() y mira el SQL de cada uno.
        // escribe aquí
    }

    private void seccion(int numero, String titulo) {
        System.out.println();
        System.out.println("=== " + numero + " · " + titulo + " ===");
    }
}
