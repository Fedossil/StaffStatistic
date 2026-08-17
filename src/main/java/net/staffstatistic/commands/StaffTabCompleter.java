package net.staffstatistic.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import net.staffstatistic.StaffStatisticPlugin;
import net.staffstatistic.model.StaffMember;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StaffTabCompleter implements TabCompleter {

    private final StaffStatisticPlugin plugin;

    public StaffTabCompleter(StaffStatisticPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("staff")) {
            if (!sender.hasPermission("staffstatistic.admin")) {
                return Collections.emptyList();
            }

            if (args.length == 1) {
                List<String> subCommands = Arrays.asList("add", "kick", "up", "down", "warn", "rebuke", "stats", "createnew");
                StringUtil.copyPartialMatches(args[0], subCommands, completions);
                Collections.sort(completions);
                return completions;
            }

            if (args.length == 2) {
                String sub = args[0].toLowerCase();

                // Для add предлагаем список всех игроков онлайн
                if (sub.equals("add")) {
                    List<String> players = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        players.add(p.getName());
                    }
                    StringUtil.copyPartialMatches(args[1], players, completions);
                    return completions;
                }

                // Для управления персоналом предлагаем ники действующих сотрудников
                if (sub.equals("kick") || sub.equals("up") || sub.equals("down") || sub.equals("warn") || sub.equals("rebuke") || sub.equals("stats")) {
                    List<String> staffNames = new ArrayList<>();
                    for (StaffMember member : plugin.getStaffManager().getAllStaff()) {
                        staffNames.add(member.getName());
                    }
                    StringUtil.copyPartialMatches(args[1], staffNames, completions);
                    return completions;
                }

                // Для создания новой должности
                if (sub.equals("createnew")) {
                    completions.add("<название_должности>");
                    return completions;
                }
            }

            if (args.length == 3) {
                String sub = args[0].toLowerCase();

                // Подсказка доступных должностей из конфига
                if (sub.equals("add") || sub.equals("up") || sub.equals("down")) {
                    List<String> roles = new ArrayList<>();
                    if (plugin.getConfig().contains("roles")) {
                        roles.addAll(plugin.getConfig().getConfigurationSection("roles").getKeys(false));
                    }
                    StringUtil.copyPartialMatches(args[2], roles, completions);
                    return completions;
                }

                if (sub.equals("createnew")) {
                    completions.add("<приоритет>");
                    return completions;
                }

                if (sub.equals("warn") || sub.equals("rebuke")) {
                    completions.add("[причина]");
                    return completions;
                }
            }
        }

        return Collections.emptyList();
    }
}