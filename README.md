# MojangCheck

Plugin de diagnóstico para Paper/Spigot que prueba si el servidor puede conectarse
correctamente a los servidores de Mojang. Se creó para diagnosticar por qué las
skins de NPCs (plugin Citizens) no cargaban en un servidor con `online-mode=false`.

## Qué hace

Al ejecutar `/mojangcheck`, el plugin intenta:

1. Resolver DNS de cada dominio de Mojang relevante.
2. Hacer una petición HTTP real a cada uno.
3. Reportar en el chat/consola si cada paso tuvo éxito, falló, o dio timeout.

Dominios probados:
- `api.mojang.com`
- `sessionserver.mojang.com`
- `api.minecraftservices.com`
- `textures.minecraft.net`

## Instalación

1. Ve a la pestaña **Actions** de este repositorio.
2. Entra al workflow más reciente (**Build Plugin**) que haya corrido.
3. Descarga el artefacto **MojangCheck-plugin** (es un `.zip` que contiene el `.jar`).
4. Extrae el `.jar` y súbelo a la carpeta `plugins/` de tu servidor.
5. Reinicia el servidor (o usa un plugin de reload de plugins).

## Uso

```
/mojangcheck
```

Requiere el permiso `mojangcheck.use` (por defecto solo `op`).

## Interpretando el resultado

- **`[DNS OK]`** → el servidor puede resolver el dominio a una IP.
- **`[DNS FAIL]`** → problema de DNS del servidor/hosting.
- **`[HTTP OK]`** → la conexión y respuesta llegaron bien (el número es el código HTTP).
- **`[HTTP TIMEOUT]`** → típico de un firewall bloqueando la conexión sin avisar.
- **`[HTTP FAIL]`** → algún otro error de red (conexión rechazada, SSL, etc.).

Si ves `TIMEOUT` o `FAIL` en `sessionserver.mojang.com` específicamente, esa es
la causa más probable de que las skins de Citizens no carguen.
