package com.xen.gojolimitless.listeners;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import com.xen.gojolimitless.managers.PlayerSkillData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;


public class AwakenListener implements Listener {

    private final GojoLimitlessPlugin plugin;

    public AwakenListener(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getPlayerDataManager().has(player.getUniqueId())) return;

        PlayerSkillData data = plugin.getPlayerDataManager().get(player);
        if (!data.isReadyToAwaken() || data.isAwakened()) return;

        event.setCancelled(true);
        plugin.getAwakenManager().tryAwaken(player);
    }
}
