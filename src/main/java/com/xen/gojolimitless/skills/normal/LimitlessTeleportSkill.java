package com.xen.gojolimitless.skills.normal;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import com.xen.gojolimitless.skills.Skill;
import com.xen.gojolimitless.util.TargetUtil;
import com.xen.gojolimitless.util.VfxUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


public class LimitlessTeleportSkill implements Skill {

    private final GojoLimitlessPlugin plugin;
    private static final float SPIN_THRESHOLD_DEGREES = 35.0f;

    public LimitlessTeleportSkill(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "limitless_teleport";
    }

    @Override
    public String getCooldownConfigKey() {
        return "limitless-teleport";
    }

    @Override
    public boolean cast(Player caster) {
        double range = plugin.getConfig().getDouble("ranges.limitless-teleport-range", 30.0);
        LivingEntity target = TargetUtil.getTargetEntity(caster, range);
        if (target == null) {
            caster.sendMessage(plugin.getPlayerDataManager().message("no-target"));
            return false;
        }

        var data = plugin.getPlayerDataManager().get(caster);
        if (data.isChannelingTeleport()) return false;
        data.setChannelingTeleport(true);

        var world = caster.getWorld();
        float startYaw = caster.getLocation().getYaw();

        world.playSound(caster.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.7f, 0.6f);
        world.playSound(caster.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.9f, 0.4f);

        
        new BukkitRunnable() {
            int tick = 0;
            final int channelTicks = 20;

            @Override
            public void run() {
                if (tick >= channelTicks || !caster.isOnline() || !target.isValid() || target.isDead()) {
                    finish();
                    cancel();
                    return;
                }

                VfxUtil.sphere(world, caster.getLocation().add(0, 1, 0), 1.0, 5, 8, Particle.DUST,
                        new Particle.DustOptions(Color.fromRGB(60, 140, 255), 0.9f));
                if (tick % 4 == 0) {
                    world.spawnParticle(Particle.END_ROD, caster.getLocation().add(0, 1, 0), 6, 0.3, 0.5, 0.3, 0.02);
                }
                tick++;
            }

            void finish() {
                data.setChannelingTeleport(false);
                if (!caster.isOnline() || !target.isValid() || target.isDead()) return;

                float endYaw = caster.getLocation().getYaw();
                float yawDelta = Math.abs(normalizeAngle(endYaw - startYaw));
                boolean spun = yawDelta > SPIN_THRESHOLD_DEGREES;

                Vector targetFacing = target.getLocation().getDirection().normalize();
                Location landSpot;

                if (spun) {
                    
                    landSpot = target.getLocation().clone().add(targetFacing.clone().multiply(-1.2));
                } else {
                    
                    landSpot = target.getLocation().clone().add(targetFacing.clone().multiply(1.2));
                }
                landSpot.setDirection(target.getLocation().toVector().subtract(landSpot.toVector()));
                landSpot.setY(target.getLocation().getY());

                
                world.spawnParticle(Particle.FLASH, caster.getLocation().add(0, 1, 0), 1);
                world.playSound(caster.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1.0f, 1.5f);

                caster.teleport(landSpot);

                world.spawnParticle(Particle.FLASH, landSpot.clone().add(0, 1, 0), 1);
                world.spawnParticle(Particle.DUST, landSpot.clone().add(0, 1, 0), 40, 0.4, 0.6, 0.4, 0,
                        new Particle.DustOptions(Color.fromRGB(80, 160, 255), 1.2f));
                world.playSound(landSpot, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

                if (spun) {
                    caster.sendActionBar(net.kyori.adventure.text.Component.text("§b§lBehind strike!"));
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private float normalizeAngle(float angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
}
