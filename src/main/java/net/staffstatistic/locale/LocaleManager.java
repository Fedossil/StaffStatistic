package net.staffstatistic.locale;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import net.staffstatistic.StaffStatisticPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocaleManager {

    private final StaffStatisticPlugin plugin;
    private FileConfiguration langConfig;
    private String prefix = "";
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})|#([A-Fa-f0-9]{6})");

    public LocaleManager(StaffStatisticPlugin plugin) {
        this.plugin = plugin;
        setupLocale();
    }

    public void setupLocale() {
        File localeDir = new File(plugin.getDataFolder(), "locale");
        if (!localeDir.exists()) localeDir.mkdirs();

        saveDefault("ru_RU.yml");
        saveDefault("en_US.yml");

        String selected = plugin.getConfig().getString("settings.language", "ru_RU");
        File file = new File(localeDir, selected + ".yml");
        if (!file.exists()) file = new File(localeDir, "ru_RU.yml");

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            this.langConfig = YamlConfiguration.loadConfiguration(reader);
        } catch (Exception e) {
            this.langConfig = YamlConfiguration.loadConfiguration(file);
        }

        this.prefix = colorize(langConfig.getString("prefix", ""));
    }

    private void saveDefault(String name) {
        File file = new File(plugin.getDataFolder(), "locale/" + name);
        if (!file.exists()) plugin.saveResource("locale/" + name, false);
    }

    public String getRaw(String path) {
        return langConfig.getString(path, path);
    }

    public String getMessage(String path, Map<String, String> placeholders) {
        String raw = langConfig.getString("messages." + path);
        if (raw == null) raw = langConfig.getString(path, "§cMissing: " + path);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                raw = raw.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return prefix + colorize(raw);
    }

    public String getMessage(String path) {
        return getMessage(path, null);
    }

    public List<String> getMessageList(String path, Map<String, String> placeholders) {
        List<String> list = langConfig.getStringList(path);
        List<String> result = new ArrayList<>();
        for (String line : list) {
            if (placeholders != null) {
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    line = line.replace("{" + entry.getKey() + "}", entry.getValue());
                }
            }
            result.add(colorize(line));
        }
        return result;
    }

    public String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String hStr = langConfig.getString("time-format.hours", "h.");
        String mStr = langConfig.getString("time-format.minutes", "min.");
        String sStr = langConfig.getString("time-format.seconds", "sec.");

        return String.format("%02d %s %02d %s %02d %s", hours, hStr, minutes, mStr, seconds, sStr);
    }

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) return "";
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}