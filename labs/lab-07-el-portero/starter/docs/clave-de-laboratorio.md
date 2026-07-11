# Clave de laboratorio

Los tres usuarios semilla (`V2__datos_semilla.sql`) comparten la misma clave:

```
dgt-2026
```

Su hash BCrypt (cost 10) está versionado en la migración. **Un hash no es un secreto**:
no permite autenticarse, y versionarlo hace el entorno reproducible.

Esta clave abre una base desechable que vive en tu portátil y muere con
`docker compose down -v`. No abre nada más. Si algún día un `application.yml` de este
repo contiene una credencial de producción, ese será el crimen del Lab 01 — y no es este
archivo.

| RUT | Nombre | Rol |
|---|---|---|
| `11111111-1` | Valentina Rojas | `CONTRIBUYENTE` |
| `9876543-2` | Carolina Espinoza | `FUNCIONARIO` |
| `8765432-1` | Ignacio Bravo | `FISCALIZADOR` |
