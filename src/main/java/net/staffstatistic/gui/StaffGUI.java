package net.staffstatistic.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import net.staffstatistic.StaffStatisticPlugin;
import net.staffstatistic.locale.LocaleManager;
import net.staffstatistic.model.StaffMember;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffGUI {

    private static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public static void openStaffList(Player viewer, StaffStatisticPlugin plugin) {
        LocaleManager loc = plugin.getLocaleManager();
        String title = LocaleManager.colorize(loc.getRaw("gui.title"));
        Inventory inv = Bukkit.createInventory(null, 54, title);

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, border);
        }

        int totalStaff = plugin.getStaffManager().getAllStaff().size();
        long onlineCount = plugin.getStaffManager().getAllStaff().stream()
                .filter(m -> {
                    Player p = Bukkit.getPlayer(m.getUuid());
                    return p != null && p.isOnline();
                }).count();

        Map<String, String> summaryPh = Map.of(
                "total", String.valueOf(totalStaff),
                "online", String.valueOf(onlineCount),
                "offline", String.valueOf(totalStaff - onlineCount)
        );

        ItemStack summary = createItem(Material.NETHER_STAR,
                loc.getRaw("gui.summary.title"),
                loc.getMessageList("gui.summary.lore", summaryPh)
        );
        inv.setItem(4, summary);

        List<StaffMember> members = plugin.getStaffManager().getSortedStaff();
        for (int i = 0; i < members.size() && i < CONTENT_SLOTS.length; i++) {
            StaffMember member = members.get(i);
            int slot = CONTENT_SLOTS[i];

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                Player onlinePlayer = Bukkit.getPlayer(member.getUuid());
                boolean isOnline = onlinePlayer != null && onlinePlayer.isOnline();

                String statusTag = isOnline ? loc.getRaw("gui.status-online-tag") : loc.getRaw("gui.status-offline-tag");
                meta.setDisplayName(LocaleManager.colorize(statusTag + " &#00D2FF" + member.getName()));
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(member.getUuid()));

                String roleName = plugin.getStaffManager().getRoleDisplayName(member.getRoleKey());
                int priority = plugin.getStaffManager().getRolePriority(member.getRoleKey());
                int maxWarns = plugin.getConfig().getInt("punishments.max-warns", 3);
                int maxRebukes = plugin.getConfig().getInt("punishments.max-rebukes", 2);

                Map<String, String> ph = new HashMap<>();
                ph.put("role", roleName);
                ph.put("priority", String.valueOf(priority));
                ph.put("duty_status", member.isOnDuty() ? loc.getRaw("gui.duty-active") : loc.getRaw("gui.duty-inactive"));
                ph.put("work_time", loc.formatTime(member.getTotalWorkTimeSeconds()));
                ph.put("warn_bar", getProgressBar(member.getWarns(), maxWarns, "&e"));
                ph.put("warns", String.valueOf(member.getWarns()));
                ph.put("max_warns", String.valueOf(maxWarns));
                ph.put("rebuke_bar", getProgressBar(member.getRebukes(), maxRebukes, "&c"));
                ph.put("rebukes", String.valueOf(member.getRebukes()));
                ph.put("max_rebukes", String.valueOf(maxRebukes));

                meta.setLore(loc.getMessageList("gui.head-lore", ph));
                head.setItemMeta(meta);
            }
            inv.setItem(slot, head);
        }

        inv.setItem(49, createItem(Material.BARRIER, loc.getRaw("gui.close-button"), null));
        viewer.openInventory(inv);
    }

    public static void openMemberManage(Player viewer, StaffMember member, StaffStatisticPlugin plugin) {
        LocaleManager loc = plugin.getLocaleManager();
        String title = LocaleManager.colorize(loc.getRaw("gui.member-title").replace("{player}", member.getName()));
        Inventory inv = Bukkit.createInventory(null, 27, title);

        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, border);
        }

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LocaleManager.colorize("&#00D2FF&l" + member.getName()));
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(member.getUuid()));
            meta.setLore(Arrays.asList(
                    LocaleManager.colorize("&7" + plugin.getStaffManager().getRoleDisplayName(member.getRoleKey())),
                    LocaleManager.colorize("&7" + loc.formatTime(member.getTotalWorkTimeSeconds()))
            ));
            head.setItemMeta(meta);
        }
        inv.setItem(4, head);

        inv.setItem(10, createItem(Material.ORANGE_DYE, loc.getRaw("gui.actions.add-warn"), loc.getMessageList("gui.actions.add-warn-lore", null)));
        inv.setItem(12, createItem(Material.RED_DYE, loc.getRaw("gui.actions.add-rebuke"), loc.getMessageList("gui.actions.add-rebuke-lore", null)));
        inv.setItem(14, createItem(Material.LIME_DYE, loc.getRaw("gui.actions.clear-punishments"), loc.getMessageList("gui.actions.clear-punishments-lore", null)));
        inv.setItem(16, createItem(Material.TNT, loc.getRaw("gui.actions.kick"), loc.getMessageList("gui.actions.kick-lore", null)));
        inv.setItem(22, createItem(Material.ARROW, loc.getRaw("gui.back-button"), null));

        viewer.openInventory(inv);
    }

    private static ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LocaleManager.colorize(name));
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String getProgressBar(int current, int max, String activeColor) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < max; i++) {
            if (i < current) {
                sb.append(activeColor).append("■");
            } else {
                sb.append("&7□");
            }
        }
        return sb.toString();
    }
}