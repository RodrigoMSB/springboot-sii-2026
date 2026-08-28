package cl.dgt.examen.dto;

import cl.dgt.examen.entities.Oficina;

/** Viene resuelto: es el cuerpo de la ficha, que llega hecha. */
public record FichaOficina(String codigo, String nombre, String comuna) {

    public static FichaOficina de(Oficina oficina) {
        return new FichaOficina(oficina.getCodigo(), oficina.getNombre(), oficina.getComuna());
    }
}
