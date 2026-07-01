package com.xen.gojolimitless.listeners;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;


public class CombatListener implements Listener {

    private final GojoLimitlessPlugin plugin;

    public CombatListener(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (attacker.equals(victim)) return;

        double damage = event.getFinalDamage();
        if (damage <= 0) return;

        double gain = damage * plugin.getPlayerDataManager().barGainPerPlayerDamage();
        plugin.getPlayerDataManager().chargeBar(attacker, gain);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Player killer = dead.getKiller();
        if (killer == null) return;

        var pdm = plugin.getPlayerDataManager();

        if (dead instanceof Player) {
            pdm.chargeBar(killer, pdm.barGainOnPlayerKill());
            return;
        }

        List<String> hostileList = plugin.getConfig().getStringList("mobs.hostile");
        String typeName = dead.getType().name();

        if (hostileList.contains(typeName)) {
            pdm.chargeBar(killer, pdm.barGainOnMobKill());
        } else {
            
            pdm.chargeBar(killer, pdm.barGainOnPassiveKill());
        }
    }
}
