package com.xen.gojolimitless.listeners;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import com.xen.gojolimitless.managers.PlayerSkillData;
import com.xen.gojolimitless.skills.Skill;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;


public class SkillCastListener implements Listener {

    private final GojoLimitlessPlugin plugin;

    public SkillCastListener(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        String skillId = plugin.getSkillItemFactory().getSkillId(item);
        if (skillId == null) return; 

        event.setCancelled(true); 

        if (!player.hasPermission("gojolimitless.use")) {
            return;
        }

        PlayerSkillData data = plugin.getPlayerDataManager().get(player);

        
        if (data.isOnGlobalCooldown()) {
            double secs = data.getGlobalCooldownRemainingMillis() / 1000.0;
            player.sendMessage(plugin.getPlayerDataManager()
                    .message("global-cooldown").replace("{time}", String.format("%.1f", secs)));
            return;
        }

        
        if (data.isSkillOnCooldown(skillId)) {
            double secs = data.getSkillCooldownRemainingMillis(skillId) / 1000.0;
            player.sendMessage(plugin.getPlayerDataManager()
                    .message("on-cooldown").replace("{time}", String.format("%.1f", secs)));
            return;
        }

        
        
        if (skillId.equals("hollow_purple_nuke") && data.isHollowPurpleNukeUsedThisAwaken()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&cThe Nuke has already been unleashed this Awaken."));
            return;
        }

        Skill skill = plugin.getSkillRegistry().get(skillId);
        if (skill == null) {
            player.sendMessage(plugin.getPlayerDataManager().message("not-skill-item"));
            return;
        }

        boolean fired = skill.cast(player);
        if (!fired) {
            return; 
        }

        
        long cd = plugin.getPlayerDataManager().skillCooldownMillis(skill.getCooldownConfigKey());
        data.triggerSkillCooldown(skillId, cd);
        data.triggerGlobalCooldown(plugin.getPlayerDataManager().globalSkillCooldownMillis());

        if (skillId.equals("hollow_purple_nuke")) {
            data.setHollowPurpleNukeUsedThisAwaken(true);
        }

        data.markCombatAction();
    }
}
