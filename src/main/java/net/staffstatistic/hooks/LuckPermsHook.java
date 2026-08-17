package net.staffstatistic.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.entity.Player;

import java.util.List;

public class LuckPermsHook {

    private LuckPerms luckPerms;

    public LuckPermsHook() {
        try {
            this.luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            this.luckPerms = null;
        }
    }

    public void setPermissions(Player player, List<String> permissions, boolean give) {
        if (luckPerms == null || permissions == null || permissions.isEmpty()) return;

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        for (String perm : permissions) {
            Node node = Node.builder(perm).value(true).build();
            if (give) {
                user.data().add(node);
            } else {
                user.data().remove(node);
            }
        }
        luckPerms.getUserManager().saveUser(user);
    }
}