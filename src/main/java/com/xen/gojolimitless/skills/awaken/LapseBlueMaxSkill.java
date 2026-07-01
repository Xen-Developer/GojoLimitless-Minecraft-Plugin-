package com.xen.gojolimitless.skills.awaken;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import com.xen.gojolimitless.skills.Skill;
import com.xen.gojolimitless.util.TargetUtil;
import com.xen.gojolimitless.util.VfxUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;


public class LapseBlueMaxSkill implements Skill {

    private final GojoLimitlessPlugin plugin;

    public LapseBlueMaxSkill(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "lapse_blue_max";
    }

    @Override
    public String getCooldownConfigKey() {
        return "lapse-blue-max";
    }

    @Override
    public boolean cast(Player caster) {
        var world = caster.getWorld();
        
        
        LivingEntity initialTarget = TargetUtil.getTargetEntity(caster, 30.0);
        
        
        final Location singularityCenter = initialTarget != null ? 
                initialTarget.getLocation().add(0, 2.0, 0) : 
                caster.getEyeLocation().add(caster.getLocation().getDirection().multiply(15.0));

        
        world.playSound(caster.getEyeLocation(), Sound.ENTITY_WARDEN_SONIC_CHARGE, 2.0f, 0.5f);
        world.playSound(singularityCenter, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 0.5f);

        double pullDmgTick = plugin.getConfig().getDouble("damage.lapse-blue-max-pull-tick", 3.0);
        double finalImplosionDmg = plugin.getConfig().getDouble("damage.lapse-blue-max-implode", 20.0);

        
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 40;

            @Override
            public void run() {
                if (!caster.isOnline()) {
                    cancel();
                    return;
                }

                
                caster.setVelocity(new Vector(caster.getVelocity().getX(), 0.02, caster.getVelocity().getZ()));

                
                
                
                Block targetBlock = caster.getTargetBlockExact(30);
                Location desiredDest = targetBlock != null ? 
                        targetBlock.getLocation().add(0, 2.5, 0) : 
                        caster.getEyeLocation().add(caster.getLocation().getDirection().multiply(25.0));

                
                singularityCenter.add(desiredDest.subtract(singularityCenter).toVector().multiply(0.25));

                
                Location hand = caster.getEyeLocation().add(caster.getLocation().getDirection().multiply(0.8));
                if (ticks % 2 == 0) {
                    VfxUtil.beam(world, hand, singularityCenter, Particle.DUST, new Particle.DustOptions(Color.fromRGB(0, 120, 255), 1.4f));
                }

                
                world.spawnParticle(Particle.DUST, singularityCenter, 35, 0.2, 0.2, 0.2, 0,
                        new Particle.DustOptions(Color.fromRGB(0, 10, 255), 3.2f));
                world.spawnParticle(Particle.DUST, singularityCenter, 15, 0.4, 0.4, 0.4, 0,
                        new Particle.DustOptions(Color.fromRGB(120, 220, 255), 1.4f));
                world.spawnParticle(Particle.REVERSE_PORTAL, singularityCenter, 20, 1.2, 1.2, 1.2, 0.25);
                
                
                double radius = 4.5 * (1.0 - ((double) ticks / maxTicks) * 0.4);
                VfxUtil.ring(world, singularityCenter, radius, 28, Particle.DUST, new Particle.DustOptions(Color.fromRGB(0, 70, 255), 1.3f));

                if (ticks % 3 == 0) {
                    world.playSound(singularityCenter, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.4f, 0.5f);
                    world.playSound(singularityCenter, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.2f, 0.4f);
                }

                
                List<LivingEntity> nearby = TargetUtil.getNearbyEnemies(singularityCenter, 11.0, caster);
                for (LivingEntity entity : nearby) {
                    Vector pullDirection = singularityCenter.toVector().subtract(entity.getLocation().toVector());
                    double distance = pullDirection.length();

                    if (distance > 0.4) {
                        pullDirection.normalize();
                        
                        double force = Math.max(0.5, (11.0 - distance) * 0.32);
                        entity.setVelocity(pullDirection.multiply(force));
                    }

                    
                    if (ticks % 4 == 0) {
                        entity.damage(pullDmgTick, caster);
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 15, 4, false, false));
                    }
                }

                
                int groundCheckRadius = 3;
                for (int x = -groundCheckRadius; x <= groundCheckRadius; x++) {
                    for (int z = -groundCheckRadius; z <= groundCheckRadius; z++) {
                        
                        Location blockLoc = singularityCenter.clone().add(x, -2.5 + (Math.random() * 1.5), z);
                        Block block = blockLoc.getBlock();
                        if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                            if (Math.random() < 0.5) {
                                world.spawnParticle(Particle.BLOCK_CRUMBLE, blockLoc.clone().add(0, 1, 0), 4, 0.2, 0.2, 0.2, 0.15, block.getBlockData());
                                block.setType(Material.AIR, false); 
                            }
                        }
                    }
                }

                ticks++;

                
                if (ticks >= maxTicks) {
                    world.spawnParticle(Particle.SONIC_BOOM, singularityCenter, 4, 0.4, 0.4, 0.4, 0);
                    world.spawnParticle(Particle.EXPLOSION_EMITTER, singularityCenter, 4, 1.0, 1.0, 1.0, 0);
                    world.playSound(singularityCenter, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.5f);
                    world.playSound(singularityCenter, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.2f, 0.5f);

                    
                    List<LivingEntity> targetsToBlast = TargetUtil.getNearbyEnemies(singularityCenter, 8.5, caster);
                    for (LivingEntity entity : targetsToBlast) {
                        entity.damage(finalImplosionDmg, caster);
                        
                        Vector blastAway = entity.getLocation().toVector().subtract(singularityCenter.toVector());
                        blastAway.setY(0.55);
                        blastAway.normalize().multiply(2.4);
                        entity.setVelocity(blastAway);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }
}
