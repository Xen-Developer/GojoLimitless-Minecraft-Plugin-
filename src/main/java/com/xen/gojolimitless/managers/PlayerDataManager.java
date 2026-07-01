package com.xen.gojolimitless.managers;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class PlayerDataManager {

    private final GojoLimitlessPlugin plugin;
    private final Map<UUID, PlayerSkillData> dataMap = new ConcurrentHashMap<>();

    public PlayerDataManager(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    public PlayerSkillData get(Player player) {
        return dataMap.computeIfAbsent(player.getUniqueId(), id -> {
            PlayerSkillData data = new PlayerSkillData(id);
            data.getAwakenBossBar().addPlayer(player);
            return data;
        });
    }

    public boolean has(UUID id) {
        return dataMap.containsKey(id);
    }

    public PlayerSkillData getIfPresent(UUID id) {
        return dataMap.get(id);
    }

    public void remove(Player player) {
        PlayerSkillData data = dataMap.remove(player.getUniqueId());
        if (data != null) {
            data.cleanup();
        }
    }

    public void removeAll() {
        for (PlayerSkillData data : dataMap.values()) {
            data.cleanup();
        }
        dataMap.clear();
    }

    public Map<UUID, PlayerSkillData> all() {
        return dataMap;
    }

    

    public double maxBar() {
        return plugin.getConfig().getDouble("awaken.max-bar", 100.0);
    }

    public double barGainPerPlayerDamage() {
        return plugin.getConfig().getDouble("awaken.bar-gain-per-player-damage", 2.2);
    }

    public double barGainOnPlayerKill() {
        return plugin.getConfig().getDouble("awaken.bar-gain-on-player-kill", 15.0);
    }

    public double barGainOnMobKill() {
        return plugin.getConfig().getDouble("awaken.bar-gain-on-mob-kill", 4.0);
    }

    public double barGainOnPassiveKill() {
        return plugin.getConfig().getDouble("awaken.bar-gain-on-passive-kill", 1.0);
    }

    public int awakenDurationSeconds() {
        return plugin.getConfig().getInt("awaken.duration-seconds", 60);
    }

    public double healPercentOnAwaken() {
        return plugin.getConfig().getDouble("awaken.heal-percent-on-awaken", 25.0);
    }

    public boolean decayEnabled() {
        return plugin.getConfig().getBoolean("awaken.decay-enabled", true);
    }

    public double decayPerSecond() {
        return plugin.getConfig().getDouble("awaken.decay-per-second", 0.15);
    }

    public int decayGraceSeconds() {
        return plugin.getConfig().getInt("awaken.decay-grace-seconds", 12);
    }

    public long globalSkillCooldownMillis() {
        return (long) (plugin.getConfig().getDouble("cooldowns.global-skill-cooldown-seconds", 1.6) * 1000L);
    }

    public long skillCooldownMillis(String configKey) {
        return (long) (plugin.getConfig().getDouble("cooldowns." + configKey, 10.0) * 1000L);
    }

    public String message(String key) {
        String raw = plugin.getConfig().getString("messages." + key, "");
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        return ChatColor.translateAlternateColorCodes('&', prefix + raw);
    }

    public String rawMessage(String key) {
        String raw = plugin.getConfig().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    

    
    public void chargeBar(Player player, double amount) {
        if (amount <= 0) return;
        PlayerSkillData data = get(player);
        if (data.isAwakened()) return; 

        double max = maxBar();
        data.addAwakenBar(amount, max);
        data.markCombatAction();
        refreshBossBar(player, data);

        if (data.getAwakenBar() >= max && !data.isReadyToAwaken()) {
            data.setReadyToAwaken(true);
            player.sendTitle(ChatColor.translateAlternateColorCodes('&', "&b&lREADY"),
                    rawMessage("awaken-ready"), 10, 60, 20);
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.6f);
        }
    }

    
    public void refreshBossBar(Player player, PlayerSkillData data) {
        double max = maxBar();
        double pct = max <= 0 ? 0 : Math.max(0.0, Math.min(1.0, data.getAwakenBar() / max));
        var bar = data.getAwakenBossBar();
        bar.setProgress(pct);

        if (data.isAwakened()) {
            long remaining = Math.max(0, data.getAwakenEndsAtMillis() - System.currentTimeMillis()) / 1000L;
            bar.setColor(BarColor.PURPLE);
            bar.setTitle("§d§lAWAKENED §7- §f" + remaining + "s remaining");
        } else if (data.isReadyToAwaken()) {
            bar.setColor(BarColor.WHITE);
            bar.setTitle(ChatColor.translateAlternateColorCodes('&', "&b&lPress Q To Awaken"));
        } else {
            bar.setColor(BarColor.BLUE);
            int pctInt = (int) Math.round(pct * 100);
            bar.setTitle("§b§lAwaken: §f" + pctInt + "%");
        }
    }

    public void resetPlayer(Player player) {
        PlayerSkillData data = get(player);
        data.clearAllCooldowns();
        data.setAwakenBar(0, maxBar());
        data.setAwakened(false);
        data.setReadyToAwaken(false);
        data.setAwakenEndsAtMillis(0);
        refreshBossBar(player, data);
    }
}
