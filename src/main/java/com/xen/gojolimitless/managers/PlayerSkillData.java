package com.xen.gojolimitless.managers;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class PlayerSkillData {

    private final UUID playerId;

    private double awakenBar = 0.0;
    private boolean awakened = false;
    private boolean readyToAwaken = false;
    private long awakenEndsAtMillis = 0L;
    private boolean hollowPurpleNukeUsedThisAwaken = false;

    private long lastCombatActionMillis = System.currentTimeMillis();
    private long globalSkillCooldownUntilMillis = 0L;

    
    private final Map<String, Long> skillCooldowns = new HashMap<>();

    
    
    private final BossBar awakenBossBar;

    
    private boolean channelingTeleport = false;

    public PlayerSkillData(UUID playerId) {
        this.playerId = playerId;
        this.awakenBossBar = org.bukkit.Bukkit.createBossBar(
                "§b§lAwaken: §f0%", BarColor.BLUE, BarStyle.SOLID);
        this.awakenBossBar.setProgress(0.0);
        this.awakenBossBar.setVisible(true);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public double getAwakenBar() {
        return awakenBar;
    }

    public void setAwakenBar(double value, double max) {
        this.awakenBar = Math.max(0.0, Math.min(value, max));
    }

    public void addAwakenBar(double amount, double max) {
        setAwakenBar(this.awakenBar + amount, max);
    }

    public boolean isAwakened() {
        return awakened;
    }

    public void setAwakened(boolean awakened) {
        this.awakened = awakened;
        if (awakened) {
            this.hollowPurpleNukeUsedThisAwaken = false;
        }
    }

    public boolean isReadyToAwaken() {
        return readyToAwaken;
    }

    public void setReadyToAwaken(boolean readyToAwaken) {
        this.readyToAwaken = readyToAwaken;
    }

    public long getAwakenEndsAtMillis() {
        return awakenEndsAtMillis;
    }

    public void setAwakenEndsAtMillis(long awakenEndsAtMillis) {
        this.awakenEndsAtMillis = awakenEndsAtMillis;
    }

    public boolean isHollowPurpleNukeUsedThisAwaken() {
        return hollowPurpleNukeUsedThisAwaken;
    }

    public void setHollowPurpleNukeUsedThisAwaken(boolean used) {
        this.hollowPurpleNukeUsedThisAwaken = used;
    }

    public long getLastCombatActionMillis() {
        return lastCombatActionMillis;
    }

    public void markCombatAction() {
        this.lastCombatActionMillis = System.currentTimeMillis();
    }

    public BossBar getAwakenBossBar() {
        return awakenBossBar;
    }

    public boolean isChannelingTeleport() {
        return channelingTeleport;
    }

    public void setChannelingTeleport(boolean channelingTeleport) {
        this.channelingTeleport = channelingTeleport;
    }

    

    public boolean isOnGlobalCooldown() {
        return System.currentTimeMillis() < globalSkillCooldownUntilMillis;
    }

    public long getGlobalCooldownRemainingMillis() {
        return Math.max(0L, globalSkillCooldownUntilMillis - System.currentTimeMillis());
    }

    public void triggerGlobalCooldown(long durationMillis) {
        this.globalSkillCooldownUntilMillis = System.currentTimeMillis() + durationMillis;
    }

    

    public boolean isSkillOnCooldown(String skillId) {
        Long until = skillCooldowns.get(skillId);
        return until != null && System.currentTimeMillis() < until;
    }

    public long getSkillCooldownRemainingMillis(String skillId) {
        Long until = skillCooldowns.get(skillId);
        if (until == null) return 0L;
        return Math.max(0L, until - System.currentTimeMillis());
    }

    public void triggerSkillCooldown(String skillId, long durationMillis) {
        skillCooldowns.put(skillId, System.currentTimeMillis() + durationMillis);
    }

    public void clearAllCooldowns() {
        skillCooldowns.clear();
        globalSkillCooldownUntilMillis = 0L;
    }

    
    public void cleanup() {
        awakenBossBar.removeAll();
    }
}
