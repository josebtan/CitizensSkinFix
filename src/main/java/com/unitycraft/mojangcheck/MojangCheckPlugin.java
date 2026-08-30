package com.unitycraft.mojangcheck;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

public class MojangCheckPlugin extends JavaPlugin implements CommandExecutor {

    // Endpoints reales que Citizens (y el propio servidor) usan para obtener skins.
    private static final String[] ENDPOINTS = {
            "https://api.mojang.com/users/profiles/minecraft/Notch",
            "https://sessionserver.mojang.com/session/minecraft/profile/069a79f444e94726a5befca90e38aaf5",
            "https://api.minecraftservices.com/minecraft/profile/lookup/name/Notch",
            "https://textures.minecraft.net/"
    };

    @Override
    public void onEnable() {
        getLogger().info("========================================");
        getLogger().info(" MojangCheck cargado correctamente.");
        getLogger().info(" Usa /mojangcheck en consola o en el juego");
        getLogger().info(" para probar la conectividad con Mojang.");
        getLogger().info("========================================");

        if (getCommand("mojangcheck") != null) {
            getCommand("mojangcheck").setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("mojangcheck")) {
            return false;
        }

        sender.sendMessage("§6[MojangCheck] §fIniciando prueba de conectividad con Mojang...");
        sender.sendMessage("§7Esto puede tardar unos segundos.");

        // Corremos la prueba en un hilo aparte para no congelar el servidor
        // mientras esperamos respuesta (o timeout) de cada endpoint.
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> runChecks(sender));

        return true;
    }

    private void runChecks(CommandSender sender) {
        int okCount = 0;
        int failCount = 0;

        for (String urlStr : ENDPOINTS) {
            boolean success = checkEndpoint(sender, urlStr);
            if (success) {
                okCount++;
            } else {
                failCount++;
            }
        }

        sender.sendMessage("§6[MojangCheck] §fPrueba finalizada: §a" + okCount + " OK §7/ §c" + failCount + " fallidos");

        if (failCount > 0) {
            sender.sendMessage("§c[MojangCheck] Hay endpoints que fallaron. Esto explicaría por qué");
            sender.sendMessage("§clas skins de NPCs (Citizens) no cargan correctamente.");
        } else {
            sender.sendMessage("§a[MojangCheck] Todos los endpoints respondieron bien.");
            sender.sendMessage("§aEl problema de las skins probablemente NO es de red/firewall.");
        }
    }

    private boolean checkEndpoint(CommandSender sender, String urlStr) {
        String host;
        try {
            URL url = new URL(urlStr);
            host = url.getHost();
        } catch (MalformedURLException e) {
            sender.sendMessage("§c[MojangCheck] URL invalida: " + urlStr);
            return false;
        }

        sender.sendMessage("§7--------------------------------------");
        sender.sendMessage("§eProbando: §f" + host);

        // 1. Resolucion DNS
        InetAddress address;
        try {
            long dnsStart = System.currentTimeMillis();
            address = InetAddress.getByName(host);
            long dnsTime = System.currentTimeMillis() - dnsStart;
            sender.sendMessage("  §a[DNS OK] §f" + host + " -> " + address.getHostAddress() + " (" + dnsTime + "ms)");
        } catch (UnknownHostException e) {
            sender.sendMessage("  §c[DNS FAIL] §f" + host + " -> " + e.getMessage());
            sender.sendMessage("  §c(El servidor no puede ni siquiera resolver este dominio)");
            return false;
        }

        // 2. Conexion HTTP real
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "MojangCheck-Diagnostic-Plugin/1.0");

            long start = System.currentTimeMillis();
            int code = conn.getResponseCode();
            long time = System.currentTimeMillis() - start;

            sender.sendMessage("  §a[HTTP OK] §f" + host + " -> Codigo " + code + " (" + time + "ms)");
            conn.disconnect();
            return true;

        } catch (SocketTimeoutException e) {
            sender.sendMessage("  §c[HTTP TIMEOUT] §f" + host + " -> No respondio a tiempo");
            sender.sendMessage("  §c(Esto es tipico de un firewall bloqueando la conexion en silencio)");
            return false;
        } catch (IOException e) {
            sender.sendMessage("  §c[HTTP FAIL] §f" + host + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }
}
