package net.staffstatistic.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.staffstatistic.StaffStatisticPlugin;
import net.staffstatistic.gui.StaffGUI;
import net.staffstatistic.model.StaffMember;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffCommand implements CommandExecutor {

    private final StaffStatisticPlugin plugin;

    public StaffCommand(StaffStatisticPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("staffstatistic.admin")) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player player) {
                StaffGUI.openStaffList(player, plugin);
            } else {
                sender.sendMessage("GUI доступно только игрокам.");
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "createnew": {
                if (args.length < 3) {
                    sender.sendMessage("§cИспользуйте: /staff createnew <должность> <приоритет>");
                    return true;
                }
                String role = args[1].toLowerCase();
                int priority;
                try {
                    priority = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cПриоритет должен быть числом!");
                    return true;
                }

                if (plugin.getStaffManager().isRoleValid(role)) {
                    sender.sendMessage(plugin.getLocaleManager().getMessage("role-exists"));
                    return true;
                }

                plugin.getStaffManager().createRole(role, priority);
                sender.sendMessage(plugin.getLocaleManager().getMessage("role-created", Map.of("role", role, "priority", String.valueOf(priority))));
                return true;
            }

            case "add": {
                if (args.length < 3) {
                    sender.sendMessage("§cИспользуйте: /staff add <ник> <должность>");
                    return true;
                }
                String name = args[1];
                String role = args[2].toLowerCase();

                if (!plugin.getStaffManager().isRoleValid(role)) {
                    sender.sendMessage(plugin.getLocaleManager().getMessage("role-not-found"));
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(name);
                if (plugin.getStaffManager().getMember(target.getUniqueId()) != null) {
                    sender.sendMessage(plugin.getLocaleManager().getMessage("already-staff"));
                    return true;
                }

                plugin.getStaffManager().addStaff(target.getUniqueId(), name, role);
                sender.sendMessage(plugin.getLocaleManager().getMessage("added", Map.of("player", name, "role", plugin.getStaffManager().getRoleDisplayName(role))));
                return true;
            }

            case "kick": {
                if (args.length < 2) {
                    sender.sendMessage("§cИспользуйте: /staff kick <ник>");
                    return true;
                }
                StaffMember member = plugin.getStaffManager().getMemberByName(args[1]);
                if (member == null) {
                    sender.sendMessage(plugin.getLocaleManager().getMessage("player-not-found"));
                    return true;
                }

                plugin.getStaffManager().removeStaff(member.getUuid());
                sender.sendMessage(plugin.getLocaleManager().getMessage("kicked", Map.of("player", member.getName())));
                return true;
            }

            case "warn": {
                if (args.length < 2) {
                    sender.sendMessage("§cИспользуйте: /staff warn <ник> [причина]");
                    return true;
                }
                StaffMember member = plugin.getStaffManager().getMemberByName(args[1]);
                if (member == null) {
                    sender.sendMessage(plugin.getLocaleManager().getMessage("player-not-found"));
                    return true;
                }

                String reason = args.length > 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "Не указана";
                member.setWarns(member.getWarns() + 1);
                plugin.getStaffManager().saveData();

                int max = plugin.getConfig().getInt("punishments.max-warns", 3);
                sender.sendMessage(plugin.getLocaleManager().getMessage("warn-added", Map.of("player", member.getName(), "reason", reason, "current", String.valueOf(member.getWarns()), "max", String.valueOf(max))));
                plugin.getDiscordManager().sendEmbed("⚠️ Выдано предупреждение", "Сотрудник: " + member.getName() + "\nПричина: " + reason + "\nПредов: " + member.getWarns() + "/" + max, 16753920);
                return true;
            }

            case "rebuke": {
                if (args.length < 2) {
                    sender.sendMessage("§cИспользуйте: /staff rebuke <ник> [причина]");
                    return true;
                }
                StaffMember member = plugin.getStaffManager().getMemberByName(args[1]);
                if (member == null) {
                    sender.sendMessage(plugin.getLocaleManager().getMessage("player-not-found"));
                    return true;
                }

                String reason = args.length > 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "Не указана";
                member.setRebukes(member.getRebukes() + 1);
                plugin.getStaffManager().saveData();

                int max = plugin.getConfig().getInt("punishments.max-rebukes", 2);
                sender.sendMessage(plugin.getLocaleManager().getMessage("rebuke-added", Map.of("player", member.getName(), "reason", reason, "current", String.valueOf(member.getRebukes()), "max", String.valueOf(max))));
                plugin.getDiscordManager().sendEmbed("🚨 Выдан выговор", "Сотрудник: " + member.getName() + "\nПричина: " + reason + "\nВыговоров: " + member.getRebukes() + "/" + max, 16711680);

                if (member.getRebukes() >= max && plugin.getConfig().getBoolean("punishments.auto-kick-on-limit", true)) {
                    plugin.getStaffManager().removeStaff(member.getUuid());
                    sender.sendMessage(plugin.getLocaleManager().getMessage("auto-kicked", Map.of("player", member.getName())));
                }
                return true;
            }

            case "up":
            case "down": {
                if (args.length < 3) {
                    sender.sendMessage("§cИспользуйте: /staff " + sub + " <ник> <новая_должность>");
                    return true;
                }
                StaffMember member = plugin.getStaffManager().getMemberByName(args[1]);
                String newRole = args[2].toLowerCase();

                if (member == null) {
                    sender.sendMessage(plugin.getLocaleManager().getMessage("player-not-found"));
                    return true;
                }
                if (!plugin.getStaffManager().isRoleValid(newRole)) {
                    sender.sendMessage(plugin.getLocaleManager().getMessage("role-not-found"));
                    return true;
                }

                member.setRoleKey(newRole);
                plugin.getStaffManager().saveData();

                String msg = sub.equals("up") ? "upgraded" : "downgraded";
                sender.sendMessage(plugin.getLocaleManager().getMessage(msg, Map.of("player", member.getName(), "role", plugin.getStaffManager().getRoleDisplayName(newRole))));
                return true;
            }

            case "stats":
            case "info": {
                if (args.length < 2) {
                    sender.sendMessage("§cИспользуйте: /staff stats <ник>");
                    return true;
                }
                StaffMember member = plugin.getStaffManager().getMemberByName(args[1]);
                if (member == null) {
                    sender.sendMessage(plugin.getLocaleManager().getMessage("player-not-found"));
                    return true;
                }

                Player onlineTarget = Bukkit.getPlayer(member.getUuid());
                String status;
                if (onlineTarget != null && onlineTarget.isOnline()) {
                    status = member.isOnDuty() ? plugin.getLocaleManager().getMessage("status-online") : plugin.getLocaleManager().getMessage("status-online-nowork");
                } else {
                    status = plugin.getLocaleManager().getMessage("status-offline");
                }

                plugin.getLiteBansHook().getBansCount(member.getUuid()).thenAccept(bans -> {
                    plugin.getLiteBansHook().getMutesCount(member.getUuid()).thenAccept(mutes -> {
                        Map<String, String> ph = new HashMap<>();
                        ph.put("player", member.getName());
                        ph.put("role", plugin.getStaffManager().getRoleDisplayName(member.getRoleKey()));
                        ph.put("online", status);
                        ph.put("work_time", plugin.getLocaleManager().formatTime(member.getTotalWorkTimeSeconds()));
                        ph.put("norm_hours", String.valueOf(plugin.getStaffManager().getRoleNormHours(member.getRoleKey())));
                        ph.put("norm_bans", String.valueOf(plugin.getStaffManager().getRoleNormBans(member.getRoleKey())));
                        ph.put("norm_mutes", String.valueOf(plugin.getStaffManager().getRoleNormMutes(member.getRoleKey())));
                        ph.put("warns", String.valueOf(member.getWarns()));
                        ph.put("max_warns", String.valueOf(plugin.getConfig().getInt("punishments.max-warns")));
                        ph.put("rebukes", String.valueOf(member.getRebukes()));
                        ph.put("max_rebukes", String.valueOf(plugin.getConfig().getInt("punishments.max-rebukes")));
                        ph.put("bans", String.valueOf(bans));
                        ph.put("mutes", String.valueOf(mutes));

                        List<String> lines = plugin.getLocaleManager().getMessageList("messages.stats-format", ph);
                        for (String line : lines) sender.sendMessage(line);
                    });
                });
                return true;
            }
        }

        return true;
    }
}