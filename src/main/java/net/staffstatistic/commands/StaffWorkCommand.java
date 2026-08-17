package net.staffstatistic.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.staffstatistic.StaffStatisticPlugin;
import net.staffstatistic.model.StaffMember;

import java.util.List;
import java.util.Map;

public class StaffWorkCommand implements CommandExecutor {

    private final StaffStatisticPlugin plugin;

    public StaffWorkCommand(StaffStatisticPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }

        StaffMember member = plugin.getStaffManager().getMember(player.getUniqueId());
        if (member == null) {
            player.sendMessage(plugin.getLocaleManager().getMessage("not-staff"));
            return true;
        }

        List<String> permissions = plugin.getStaffManager().getRolePermissions(member.getRoleKey());

        if (member.isOnDuty()) {
            long sessionTime = member.endDuty();
            plugin.getStaffManager().saveData();
            plugin.getLuckPermsHook().setPermissions(player, permissions, false);

            player.sendMessage(plugin.getLocaleManager().getMessage("work-disabled", Map.of(
                    "session", plugin.getLocaleManager().formatTime(sessionTime),
                    "total", plugin.getLocaleManager().formatTime(member.getTotalWorkTimeSeconds())
            )));

            plugin.getDiscordManager().sendEmbed("🔴 Завершение смены", "Сотрудник: " + player.getName() + "\nОтработано: " + plugin.getLocaleManager().formatTime(sessionTime), 16711680);
            plugin.getGoogleSheetsManager().sendDutyRecord(member, sessionTime);
        } else {
            member.startDuty();
            plugin.getLuckPermsHook().setPermissions(player, permissions, true);

            player.sendMessage(plugin.getLocaleManager().getMessage("work-enabled"));
            plugin.getDiscordManager().sendEmbed("🟢 Выход на смену", "Сотрудник: " + player.getName() + " заступил на смену.", 65280);
        }

        return true;
    }
}