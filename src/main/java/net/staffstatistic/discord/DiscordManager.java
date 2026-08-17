package net.staffstatistic.discord;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import net.staffstatistic.StaffStatisticPlugin;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DiscordManager {

    private final StaffStatisticPlugin plugin;
    private FileConfiguration discordConfig;
    private boolean enabled;
    private String webhookUrl;
    private final HttpClient httpClient;

    public DiscordManager(StaffStatisticPlugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        loadConfig();
    }

    public void loadConfig() {
        File file = new File(plugin.getDataFolder(), "discord.yml");
        if (!file.exists()) {
            plugin.saveResource("discord.yml", false);
        }
        this.discordConfig = YamlConfiguration.loadConfiguration(file);
        this.enabled = discordConfig.getBoolean("discord.enabled", false);
        this.webhookUrl = discordConfig.getString("discord.webhook-url", "");

        if (!enabled) {
            plugin.getLogger().info("[Discord] Синхронизация с Discord выключена (discord.enabled: false).");
        } else if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.contains("your_webhook_here")) {
            plugin.getLogger().warning("[Discord] Внимание! Включен discord.enabled: true, но URL вебхука не настроен!");
        } else {
            plugin.getLogger().info("[Discord] Модуль Discord успешно подключен.");
        }
    }

    public void sendEmbed(String title, String description, int color) {
        if (!enabled) {
            return;
        }

        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.contains("your_webhook_here")) {
            plugin.getLogger().warning("[Discord] Ошибка: URL вебхука пуст или содержит дефолтный шаблон!");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String escapedTitle = escapeJson(title);
                String escapedDesc = escapeJson(description);

                String jsonBody = "{\"embeds\":[{" +
                        "\"title\":\"" + escapedTitle + "\"," +
                        "\"description\":\"" + escapedDesc + "\"," +
                        "\"color\":" + color +
                        "}]}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl.trim()))
                        .timeout(Duration.ofSeconds(5))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("User-Agent", "DiscordBot (https://github.com, 1.0.0)")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                int statusCode = response.statusCode();
                if (statusCode == 200 || statusCode == 204) {
                    plugin.getLogger().info("[Discord] Сообщение успешно отправлено в вебхук.");
                } else {
                    plugin.getLogger().warning("[Discord] Ошибка отправки! Код: " + statusCode + " | Ответ: " + response.body());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[Discord] Не удалось отправить сообщение в Discord: " + e.getMessage());
            }
        });
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}