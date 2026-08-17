package net.staffstatistic.hooks;

import litebans.api.Database;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LiteBansHook {

    private final boolean enabled;

    public LiteBansHook() {
        this.enabled = Bukkit.getPluginManager().isPluginEnabled("LiteBans");
    }

    public CompletableFuture<Integer> getBansCount(UUID staffUuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (enabled) {
                try {
                    String query = "SELECT COUNT(*) FROM {bans} WHERE banned_by_uuid = ?";
                    try (PreparedStatement st = Database.get().prepareStatement(query)) {
                        st.setString(1, staffUuid.toString());
                        try (ResultSet rs = st.executeQuery()) {
                            if (rs.next()) return rs.getInt(1);
                        }
                    }
                } catch (Exception ignored) {}
            }
            return 0;
        });
    }

    public CompletableFuture<Integer> getMutesCount(UUID staffUuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (enabled) {
                try {
                    String query = "SELECT COUNT(*) FROM {mutes} WHERE banned_by_uuid = ?";
                    try (PreparedStatement st = Database.get().prepareStatement(query)) {
                        st.setString(1, staffUuid.toString());
                        try (ResultSet rs = st.executeQuery()) {
                            if (rs.next()) return rs.getInt(1);
                        }
                    }
                } catch (Exception ignored) {}
            }
            return 0;
        });
    }
}