package com.xen.gojolimitless.listeners;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinQuitListener implements Listener {

    private final GojoLimitlessPlugin plugin;

    public PlayerJoinQuitListener(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        
        plugin.getPlayerDataManager().get(player);
        plugin.getKitManager().giveBaseKit(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().remove(event.getPlayer());
    }
}
