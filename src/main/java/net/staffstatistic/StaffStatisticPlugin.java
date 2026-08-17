package net.staffstatistic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import net.staffstatistic.commands.StaffCommand;
import net.staffstatistic.commands.StaffTabCompleter;
import net.staffstatistic.commands.StaffWorkCommand;
import net.staffstatistic.discord.DiscordManager;
import net.staffstatistic.gui.GUIListener;
import net.staffstatistic.hooks.LiteBansHook;
import net.staffstatistic.hooks.LuckPermsHook;
import net.staffstatistic.locale.LocaleManager;
import net.staffstatistic.manager.StaffManager;
import net.staffstatistic.model.StaffMember;
import net.staffstatistic.sheets.GoogleSheetsManager;

public final class StaffStatisticPlugin extends JavaPlugin implements Listener {

    private LocaleManager localeManager;
    private StaffManager staffManager;
    private DiscordManager discordManager;
    private GoogleSheetsManager googleSheetsManager;
    private LuckPermsHook luckPermsHook;
    private LiteBansHook liteBansHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.localeManager = new LocaleManager(this);
        this.staffManager = new StaffManager(this);
        this.discordManager = new DiscordManager(this);
        this.googleSheetsManager = new GoogleSheetsManager(this);
        this.luckPermsHook = new LuckPermsHook();
        this.liteBansHook = new LiteBansHook();

        // Регистрация команд и таб-комплитера
        if (getCommand("staff") != null) {
            getCommand("staff").setExecutor(new StaffCommand(this));
            getCommand("staff").setTabCompleter(new StaffTabCompleter(this));
        }
        if (getCommand("staffwork") != null) {
            getCommand("staffwork").setExecutor(new StaffWorkCommand(this));
        }

        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("StaffStatistic v1.0 successfully enabled.");
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            StaffMember member = staffManager.getMember(player.getUniqueId());
            if (member != null && member.isOnDuty()) {
                long sessionTime = member.endDuty();
                luckPermsHook.setPermissions(player, staffManager.getRolePermissions(member.getRoleKey()), false);
                googleSheetsManager.sendDutyRecord(member, sessionTime);
            }
        }
        if (staffManager != null) {
            staffManager.saveData();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        StaffMember member = staffManager.getMember(player.getUniqueId());
        if (member != null && member.isOnDuty()) {
            long sessionTime = member.endDuty();
            staffManager.saveData();
            luckPermsHook.setPermissions(player, staffManager.getRolePermissions(member.getRoleKey()), false);
            discordManager.sendEmbed("🔴 Завершение смены (Выход)", "Сотрудник: " + player.getName() + " покинул сервер. Отработано: " + StaffManager.formatTime(sessionTime), 16711680);
            googleSheetsManager.sendDutyRecord(member, sessionTime);
        }
    }

    public LocaleManager getLocaleManager() { return localeManager; }
    public StaffManager getStaffManager() { return staffManager; }
    public DiscordManager getDiscordManager() { return discordManager; }
    public GoogleSheetsManager getGoogleSheetsManager() { return googleSheetsManager; }
    public LuckPermsHook getLuckPermsHook() { return luckPermsHook; }
    public LiteBansHook getLiteBansHook() { return liteBansHook; }
}