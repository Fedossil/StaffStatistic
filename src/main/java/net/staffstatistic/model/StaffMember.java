package net.staffstatistic.model;

import java.util.UUID;

public class StaffMember {
    private final UUID uuid;
    private String name;
    private String roleKey;
    private int warns;
    private int rebukes;
    private long totalWorkTimeSeconds;
    private transient long sessionStartTime = -1;

    public StaffMember(UUID uuid, String name, String roleKey, int warns, int rebukes, long totalWorkTimeSeconds) {
        this.uuid = uuid;
        this.name = name;
        this.roleKey = roleKey;
        this.warns = warns;
        this.rebukes = rebukes;
        this.totalWorkTimeSeconds = totalWorkTimeSeconds;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRoleKey() { return roleKey; }
    public void setRoleKey(String roleKey) { this.roleKey = roleKey; }
    public int getWarns() { return warns; }
    public void setWarns(int warns) { this.warns = warns; }
    public int getRebukes() { return rebukes; }
    public void setRebukes(int rebukes) { this.rebukes = rebukes; }
    public long getTotalWorkTimeSeconds() { return totalWorkTimeSeconds; }
    public void setTotalWorkTimeSeconds(long totalWorkTimeSeconds) { this.totalWorkTimeSeconds = totalWorkTimeSeconds; }

    public boolean isOnDuty() { return sessionStartTime > 0; }
    public void startDuty() { this.sessionStartTime = System.currentTimeMillis(); }

    public long endDuty() {
        if (!isOnDuty()) return 0;
        long duration = (System.currentTimeMillis() - sessionStartTime) / 1000;
        this.totalWorkTimeSeconds += duration;
        this.sessionStartTime = -1;
        return duration;
    }
}