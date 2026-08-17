package net.staffstatistic.sheets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import net.staffstatistic.StaffStatisticPlugin;
import net.staffstatistic.model.StaffMember;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class GoogleSheetsManager {

    private final StaffStatisticPlugin plugin;
    private FileConfiguration sheetsConfig;
    private boolean enabled;
    private String webAppUrl;
    private boolean syncOnDutyEnd;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#[a-fA-F0-9]{6}|#[a-fA-F0-9]{6}");

    public GoogleSheetsManager(StaffStatisticPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        File file = new File(plugin.getDataFolder(), "sheets.yml");
        if (!file.exists()) {
            plugin.saveResource("sheets.yml", false);
        }
        this.sheetsConfig = YamlConfiguration.loadConfiguration(file);
        this.enabled = sheetsConfig.getBoolean("sheets.enabled", false);
        this.webAppUrl = sheetsConfig.getString("sheets.webapp-url", "");
        this.syncOnDutyEnd = sheetsConfig.getBoolean("sheets.sync-on-duty-end", true);
    }

    public void sendDutyRecord(StaffMember member, long sessionSeconds) {
        if (!enabled || !syncOnDutyEnd || webAppUrl == null || webAppUrl.isEmpty() || webAppUrl.contains("your_script_id")) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(webAppUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String rawRole = plugin.getStaffManager().getRoleDisplayName(member.getRoleKey());
                String cleanRole = stripAllColors(rawRole);
                String cleanPlayer = stripAllColors(member.getName());

                String session = plugin.getLocaleManager().formatTime(sessionSeconds);
                String total = plugin.getLocaleManager().formatTime(member.getTotalWorkTimeSeconds());

                String json = String.format(
                        "{\"date\":\"%s\",\"player\":\"%s\",\"role\":\"%s\",\"sessionTime\":\"%s\",\"totalTime\":\"%s\",\"warns\":%d,\"rebukes\":%d}",
                        dateStr, cleanPlayer, cleanRole, session, total, member.getWarns(), member.getRebukes()
                );

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception ignored) {}
        });
    }

    private String stripAllColors(String text) {
        if (text == null) return "";
        String withoutHex = HEX_PATTERN.matcher(text).replaceAll("");
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', withoutHex)).trim();
    }
}