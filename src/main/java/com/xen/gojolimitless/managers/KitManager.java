package com.xen.gojolimitless.managers;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import com.xen.gojolimitless.util.SkillItemFactory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;


public class KitManager {

    public static final int SLOT_LAPSE_BLUE = 0;       
    public static final int SLOT_REVERSAL_RED = 1;     
    public static final int SLOT_RAPID_PUNCHES = 2;    
    public static final int SLOT_TWOFOLD_KICK = 3;     
    public static final int SLOT_LIMITLESS = 4;        

    private final GojoLimitlessPlugin plugin;
    private final SkillItemFactory factory;

    public KitManager(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
        this.factory = plugin.getSkillItemFactory();
    }

    

    public ItemStack lapseBlue() {
        return factory.create(Material.LAPIS_LAZULI, "lapse_blue", "§b§lLapse Blue",
                List.of(
                        "§7Negative cursed energy pull.",
                        "§7Yank a distant enemy toward you,",
                        "§7then strike with an unblockable kick.",
                        "",
                        "§8Right-Click to cast"
                ), "base", false);
    }

    public ItemStack reversalRed() {
        return factory.create(Material.REDSTONE, "reversal_red", "§c§lReversal Red",
                List.of(
                        "§7Positive cursed energy sphere.",
                        "§7Launches forward and detonates",
                        "§7in a wide blast radius.",
                        "",
                        "§8Right-Click to cast"
                ), "base", false);
    }

    public ItemStack rapidPunches() {
        return factory.create(Material.IRON_NUGGET, "rapid_punches", "§f§lRapid Punches",
                List.of(
                        "§7A flurry of close-range strikes,",
                        "§7each connecting with cursed force.",
                        "",
                        "§8Right-Click to cast"
                ), "base", false);
    }

    public ItemStack twofoldKick() {
        return factory.create(Material.FEATHER, "twofold_kick", "§e§lTwofold Kick",
                List.of(
                        "§7Kick the enemy airborne, then slam",
                        "§7them back down — a combo extender.",
                        "",
                        "§8Right-Click to cast"
                ), "base", false);
    }

    public ItemStack limitlessTeleport() {
        return factory.create(Material.ENDER_PEARL, "limitless_teleport", "§9§lLimitless: Teleport",
                List.of(
                        "§7Lock onto a target and close the",
                        "§7distance instantly through Limitless.",
                        "",
                        "§8Right-Click to cast"
                ), "base", false);
    }

    

    public ItemStack lapseBlueMax() {
        return factory.create(Material.LAPIS_BLOCK, "lapse_blue_max", "§b§lLapse Blue: MAX",
                List.of(
                        "§7Six Eyes amplified pull —",
                        "§7longer range, no escape, harder kick.",
                        "",
                        "§8Right-Click to cast"
                ), "awaken", true);
    }

    public ItemStack reversalRedMax() {
        return factory.create(Material.REDSTONE_BLOCK, "reversal_red_max", "§c§lReversal Red: MAX",
                List.of(
                        "§7An overwhelming positive-energy blast,",
                        "§7far larger than its base form.",
                        "",
                        "§8Right-Click to cast"
                ), "awaken", true);
    }

    public ItemStack hollowPurple() {
        return factory.create(Material.AMETHYST_SHARD, "hollow_purple", "§5§lHollow Purple",
                List.of(
                        "§7Fuse Lapse Blue and Reversal Red into",
                        "§7an imaginary mass that erases everything",
                        "§7caught in its path.",
                        "",
                        "§8Right-Click to charge & release"
                ), "awaken", true);
    }

    public ItemStack infiniteVoid() {
        return factory.create(Material.ENDER_EYE, "infinite_void", "§d§lDomain Expansion: Infinite Void",
                List.of(
                        "§7Unlimited Void. Targets caught inside",
                        "§7are overwhelmed with infinite information",
                        "§7and locked in place, taking steady damage.",
                        "",
                        "§8Right-Click to expand"
                ), "awaken", true);
    }

    public ItemStack hollowPurpleNuke() {
        return factory.create(Material.NETHER_STAR, "hollow_purple_nuke", "§4§l§nHollow Purple: Nuke",
                List.of(
                        "§7§lSECRET TECHNIQUE",
                        "§7The true imaginary mass — a guaranteed,",
                        "§7world-ending detonation. Single use.",
                        "",
                        "§8Right-Click to unleash (once per Awaken)"
                ), "awaken", true);
    }

    

    
    public void giveBaseKit(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.setItem(SLOT_LAPSE_BLUE, lapseBlue());
        inv.setItem(SLOT_REVERSAL_RED, reversalRed());
        inv.setItem(SLOT_RAPID_PUNCHES, rapidPunches());
        inv.setItem(SLOT_TWOFOLD_KICK, twofoldKick());
        inv.setItem(SLOT_LIMITLESS, limitlessTeleport());
    }

    
    public void swapToAwakenKit(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.setItem(SLOT_LAPSE_BLUE, lapseBlueMax());
        inv.setItem(SLOT_REVERSAL_RED, reversalRedMax());
        inv.setItem(SLOT_RAPID_PUNCHES, hollowPurple());
        inv.setItem(SLOT_TWOFOLD_KICK, infiniteVoid());
        inv.setItem(SLOT_LIMITLESS, hollowPurpleNuke());
    }

    
    public void revertToBaseKit(Player player) {
        giveBaseKit(player);
    }

    public SkillItemFactory getFactory() {
        return factory;
    }
}
