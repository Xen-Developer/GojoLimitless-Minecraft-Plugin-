package com.xen.gojolimitless.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;


public class SkillItemFactory {

    private final NamespacedKey skillKey;
    private final NamespacedKey kitKey; 

    public SkillItemFactory(org.bukkit.plugin.Plugin plugin) {
        this.skillKey = new NamespacedKey(plugin, "gojo_skill_id");
        this.kitKey = new NamespacedKey(plugin, "gojo_kit");
    }

    public NamespacedKey getSkillKey() {
        return skillKey;
    }

    public NamespacedKey getKitKey() {
        return kitKey;
    }

    
    public ItemStack create(Material material, String skillId, String displayName, List<String> lore, String kit, boolean glow) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(skillKey, PersistentDataType.STRING, skillId);
        meta.getPersistentDataContainer().set(kitKey, PersistentDataType.STRING, kit);
        if (glow) {
            meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
        }
        item.setItemMeta(meta);
        return item;
    }

    
    public String getSkillId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(skillKey, PersistentDataType.STRING);
    }

    public String getKit(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(kitKey, PersistentDataType.STRING);
    }

    public boolean isSkillItem(ItemStack item) {
        return getSkillId(item) != null;
    }
}
