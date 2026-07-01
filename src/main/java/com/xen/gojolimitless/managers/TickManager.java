package com.xen.gojolimitless.managers;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class TickManager {

    private final GojoLimitlessPlugin plugin;

    public TickManager(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerDataManager pdm = plugin.getPlayerDataManager();
                boolean decay = pdm.decayEnabled();
                double decayAmount = pdm.decayPerSecond();
                long graceMillis = pdm.decayGraceSeconds() * 1000L;

                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (!pdm.has(player.getUniqueId())) continue;
                    PlayerSkillData data = pdm.get(player);

                    if (decay && !data.isAwakened() && !data.isReadyToAwaken()) {
                        long sinceCombat = System.currentTimeMillis() - data.getLastCombatActionMillis();
                        if (sinceCombat > graceMillis && data.getAwakenBar() > 0) {
                            data.addAwakenBar(-decayAmount, pdm.maxBar());
                        }
                    }

                    pdm.refreshBossBar(player, data);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
