# Desafío 99 · Liquibase y el rollback declarativo (opcional)

> Esto es **opcional** (P-15). Si no lo haces, no pierdes nada del lab. Si lo haces, marca la
> casilla en tu reporte con honestidad — un "no lo hice" vale más que un "sí" inventado.

## El reto

Nuestra V3 agrega un `CHECK` con Flyway y SQL plano. Flyway va **hacia adelante**: si quieres
deshacer, escribes otra migración (`V4`) que haga `DROP CONSTRAINT`. No hay "undo" declarado.

Liquibase hace lo mismo con changelogs (XML/YAML/JSON) y ofrece **rollback declarativo**: en el
mismo changeset defines cómo deshacerlo, y `liquibase rollback` lo aplica.

Replica la V3 como un changeset de Liquibase con su `rollback`, y compáralos:

```yaml
databaseChangeLog:
  - changeSet:
      id: check-linea-f29-monto-no-cero
      author: tu-nombre
      changes:
        - sql:
            sql: ALTER TABLE linea_f29 ADD CONSTRAINT ck_linea_f29_monto_no_cero CHECK (monto <> 0)
      rollback:
        - sql:
            sql: ALTER TABLE linea_f29 DROP CONSTRAINT ck_linea_f29_monto_no_cero
```

## Las preguntas

1. Con Flyway, ¿cómo deshaces la V3? ¿Cuántos archivos toca?
2. Con Liquibase, el `rollback` vive junto al cambio. ¿Qué ganas? ¿Qué cuesta esa comodidad?
3. La `flyway_schema_history` es una bitácora **inmutable**. ¿El rollback de Liquibase la
   contradice, o es coherente? (pista: un rollback también es un hecho que se registra)
4. Para un equipo casado con PostgreSQL, ¿qué elegirías, y por qué? No hay respuesta única —
   hay un criterio que defender.

No hay validador para esto: es una reflexión. Si lo hiciste, resume tu conclusión en el reporte.
