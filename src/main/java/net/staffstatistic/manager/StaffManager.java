package net.staffstatistic.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import net.staffstatistic.StaffStatisticPlugin;
import net.staffstatistic.model.StaffMember;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StaffManager {

    private final StaffStatisticPlugin plugin;
    private final Map<UUID, StaffMember> staffMembers = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public StaffManager(StaffStatisticPlugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    public void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        staffMembers.clear();

        if (dataConfig.contains("staff")) {
            for (String key : dataConfig.getConfigurationSection("staff").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                String name = dataConfig.getString("staff." + key + ".name");
                String role = dataConfig.getString("staff." + key + ".role");
                int warns = dataConfig.getInt("staff." + key + ".warns", 0);
                int rebukes = dataConfig.getInt("staff." + key + ".rebukes", 0);
                long time = dataConfig.getLong("staff." + key + ".work-time", 0);

                staffMembers.put(uuid, new StaffMember(uuid, name, role, warns, rebukes, time));
            }
        }
    }

    public void saveData() {
        dataConfig.set("staff", null);
        for (StaffMember member : staffMembers.values()) {
            String path = "staff." + member.getUuid().toString();
            dataConfig.set(path + ".name", member.getName());
            dataConfig.set(path + ".role", member.getRoleKey());
            dataConfig.set(path + ".warns", member.getWarns());
            dataConfig.set(path + ".rebukes", member.getRebukes());
            dataConfig.set(path + ".work-time", member.getTotalWorkTimeSeconds());
        }
        try { dataConfig.save(dataFile); } catch (IOException ignored) {}
    }

    public void addStaff(UUID uuid, String name, String role) {
        staffMembers.put(uuid, new StaffMember(uuid, name, role, 0, 0, 0));
        saveData();
    }

    public void removeStaff(UUID uuid) {
        staffMembers.remove(uuid);
        saveData();
    }

    public StaffMember getMember(UUID uuid) { return staffMembers.get(uuid); }

    public StaffMember getMemberByName(String name) {
        for (StaffMember m : staffMembers.values()) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    public Collection<StaffMember> getAllStaff() {
        return staffMembers.values();
    }

    public List<StaffMember> getSortedStaff() {
        List<StaffMember> list = new ArrayList<>(staffMembers.values());
        list.sort((m1, m2) -> Integer.compare(getRolePriority(m2.getRoleKey()), getRolePriority(m1.getRoleKey())));
        return list;
    }

    public boolean isRoleValid(String roleKey) {
        return plugin.getConfig().contains("roles." + roleKey);
    }

    public int getRolePriority(String roleKey) {
        return plugin.getConfig().getInt("roles." + roleKey + ".priority", 0);
    }

    public String getRoleDisplayName(String roleKey) {
        return plugin.getConfig().getString("roles." + roleKey + ".display-name", roleKey);
    }

    public List<String> getRolePermissions(String roleKey) {
        return plugin.getConfig().getStringList("roles." + roleKey + ".permissions");
    }

    public int getRoleNormHours(String roleKey) {
        return plugin.getConfig().getInt("roles." + roleKey + ".requirements.min-weekly-hours", 0);
    }

    public int getRoleNormBans(String roleKey) {
        return plugin.getConfig().getInt("roles." + roleKey + ".requirements.min-bans-week", 0);
    }

    public int getRoleNormMutes(String roleKey) {
        return plugin.getConfig().getInt("roles." + roleKey + ".requirements.min-mutes-week", 0);
    }

    public void createRole(String roleKey, int priority) {
        plugin.getConfig().set("roles." + roleKey + ".display-name", "&b" + roleKey);
        plugin.getConfig().set("roles." + roleKey + ".priority", priority);
        plugin.getConfig().set("roles." + roleKey + ".requirements.min-weekly-hours", 10);
        plugin.getConfig().set("roles." + roleKey + ".requirements.min-bans-week", 0);
        plugin.getConfig().set("roles." + roleKey + ".requirements.min-mutes-week", 0);
        plugin.getConfig().set("roles." + roleKey + ".permissions", Collections.singletonList("example.permission"));
        plugin.saveConfig();
    }

    public static String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d ч. %02d мин. %02d сек.", hours, minutes, seconds);
    }
}