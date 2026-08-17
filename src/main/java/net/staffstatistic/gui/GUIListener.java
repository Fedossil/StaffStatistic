package net.staffstatistic.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import net.staffstatistic.StaffStatisticPlugin;
import net.staffstatistic.model.StaffMember;

import java.util.Map;

public class GUIListener implements Listener {

    private final StaffStatisticPlugin plugin;

    public GUIListener(StaffStatisticPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getView().getTitle() == null) return;

        String title = e.getView().getTitle();
        String mainTitle = ChatColor.stripColor(plugin.getLocaleManager().colorize(plugin.getLocaleManager().getRaw("gui.title")));

        // Главное меню списка
        if (ChatColor.stripColor(title).equals(mainTitle)) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null) return;

            if (item.getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }

            if (item.getType() == Material.PLAYER_HEAD) {
                String rawName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                String targetName = rawName.replaceAll("\\[.*?\\]", "").trim();
                StaffMember member = plugin.getStaffManager().getMemberByName(targetName);
                if (member != null) {
                    StaffGUI.openMemberManage(player, member, plugin);
                }
            }
            return;
        }

        // Меню управления профилем
        if (title.contains(":") && e.getInventory().getSize() == 27) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;

            String targetName = ChatColor.stripColor(title.split(":")[1].trim());
            StaffMember member = plugin.getStaffManager().getMemberByName(targetName);
            if (member == null) return;

            int slot = e.getRawSlot();
            int maxWarns = plugin.getConfig().getInt("punishments.max-warns", 3);
            int maxRebukes = plugin.getConfig().getInt("punishments.max-rebukes", 2);

            if (slot == 10) { // Выдать пред
                member.setWarns(member.getWarns() + 1);
                plugin.getStaffManager().saveData();
                player.sendMessage(plugin.getLocaleManager().getMessage("warn-added", Map.of("player", member.getName(), "reason", "GUI Action", "current", String.valueOf(member.getWarns()), "max", String.valueOf(maxWarns))));
                StaffGUI.openMemberManage(player, member, plugin);
            } else if (slot == 12) { // Выдать выговор
                member.setRebukes(member.getRebukes() + 1);
                plugin.getStaffManager().saveData();
                player.sendMessage(plugin.getLocaleManager().getMessage("rebuke-added", Map.of("player", member.getName(), "reason", "GUI Action", "current", String.valueOf(member.getRebukes()), "max", String.valueOf(maxRebukes))));

                if (member.getRebukes() >= maxRebukes && plugin.getConfig().getBoolean("punishments.auto-kick-on-limit", true)) {
                    plugin.getStaffManager().removeStaff(member.getUuid());
                    player.sendMessage(plugin.getLocaleManager().getMessage("auto-kicked", Map.of("player", member.getName())));
                    StaffGUI.openStaffList(player, plugin);
                } else {
                    StaffGUI.openMemberManage(player, member, plugin);
                }
            } else if (slot == 14) { // Очистить
                member.setWarns(0);
                member.setRebukes(0);
                plugin.getStaffManager().saveData();
                player.sendMessage("§a" + plugin.getLocaleManager().getRaw("gui.actions.clear-punishments"));
                StaffGUI.openMemberManage(player, member, plugin);
            } else if (slot == 16) { // Снять со стаффа
                plugin.getStaffManager().removeStaff(member.getUuid());
                player.sendMessage(plugin.getLocaleManager().getMessage("kicked", Map.of("player", member.getName())));
                StaffGUI.openStaffList(player, plugin);
            } else if (slot == 22) { // Назад
                StaffGUI.openStaffList(player, plugin);
            }
        }
    }
}