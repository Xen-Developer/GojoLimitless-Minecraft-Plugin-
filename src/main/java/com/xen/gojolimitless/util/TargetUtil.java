package com.xen.gojolimitless.util;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.List;


public final class TargetUtil {

    private TargetUtil() {}

    
    public static LivingEntity getTargetEntity(Player player, double range) {
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                0.6,
                entity -> entity instanceof LivingEntity
                        && !entity.equals(player)
                        && !entity.isDead()
        );
        if (result == null || result.getHitEntity() == null) return null;
        return (LivingEntity) result.getHitEntity();
    }

    
    public static List<LivingEntity> getNearbyEnemies(Player caster, double radius) {
        List<LivingEntity> out = new ArrayList<>();
        for (var entity : caster.getWorld().getNearbyEntities(caster.getLocation(), radius, radius, radius)) {
            if (entity instanceof LivingEntity living && !living.equals(caster) && !living.isDead()) {
                out.add(living);
            }
        }
        return out;
    }

    
    public static List<LivingEntity> getNearbyEnemies(org.bukkit.Location center, double radius, Player exclude) {
        List<LivingEntity> out = new ArrayList<>();
        for (var entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity living && !living.equals(exclude) && !living.isDead()) {
                out.add(living);
            }
        }
        return out;
    }
}
