-- El cinturón, del paso 5.
--
-- El turno del paso 4 vive en el código: protege mientras TODAS las emisiones
-- pasen por ese método. Esto vive en la base y protege siempre, venga de donde
-- venga — de otra aplicación, de un script, de alguien con un cliente SQL.
-- Dos defensas para el mismo invariante. No sobra ninguna.

-- Pero antes hay que limpiar, y ese detalle enseña más que la restricción:
-- la demo 2 ya dejó folios repetidos en la tabla, y PostgreSQL no deja crear
-- una restricción que los datos existentes ya incumplen. Se queda el primero
-- de cada (anio, numero) y se borran los demás.
--
-- Así es exactamente como duele en un sistema real: la restricción que faltaba
-- no se puede poner hasta haber arreglado a mano lo que se coló sin ella.
delete from folio f
where f.id > (select min(f2.id)
              from folio f2
              where f2.anio = f.anio
                and f2.numero = f.numero);

alter table folio
    add constraint folio_anio_numero_unico unique (anio, numero);
