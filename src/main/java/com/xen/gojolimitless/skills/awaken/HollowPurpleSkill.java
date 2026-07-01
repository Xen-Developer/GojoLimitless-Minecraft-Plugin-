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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class HollowPurpleSkill implements Skill {

    private final GojoLimitlessPlugin plugin;

    public HollowPurpleSkill(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "hollow_purple";
    }

    @Override
    public String getCooldownConfigKey() {
        return "hollow-purple";
    }

    @Override
    public boolean cast(Player caster) {
        var world = caster.getWorld();
        Location initialCastLoc = caster.getLocation().clone();

        
        world.playSound(caster.getEyeLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 3.0f, 0.5f);
        world.playSound(caster.getEyeLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.5f);
        world.playSound(caster.getEyeLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);

        
        new BukkitRunnable() {
            int tick = 0;
            final int chargeTicks = 35;

            @Override
            public void run() {
                if (!caster.isOnline()) {
                    cancel();
                    return;
                }

                
                caster.setVelocity(new Vector(0, 0.05, 0));

                Location handTarget = caster.getEyeLocation().add(caster.getLocation().getDirection().multiply(1.5));
                double chargeProgress = (double) tick / chargeTicks;
                double outerOrbitRadius = 2.2 * (1.0 - chargeProgress * 0.8);

                
                double angleOffset = tick * 0.45;
                for (int i = 0; i < 2; i++) {
                    double currentAngle = angleOffset + (i * Math.PI);
                    Vector offset = new Vector(Math.cos(currentAngle) * outerOrbitRadius, (Math.sin(tick * 0.2) * 0.3), Math.sin(currentAngle) * outerOrbitRadius);
                    
                    
                    world.spawnParticle(Particle.DUST, handTarget.clone().add(offset), 6, 0.05, 0.05, 0.05, 0,
                            new Particle.DustOptions(Color.fromRGB(0, 80, 255), 1.6f));
                    
                    world.spawnParticle(Particle.DUST, handTarget.clone().subtract(offset), 6, 0.05, 0.05, 0.05, 0,
                            new Particle.DustOptions(Color.fromRGB(255, 10, 50), 1.6f));
                }

                
                if (tick > 15) {
                    world.spawnParticle(Particle.REVERSE_PORTAL, handTarget, 10, 0.3, 0.3, 0.3, 0.4);
                    world.spawnParticle(Particle.DUST, handTarget, 8, 0.2, 0.2, 0.2, 0,
                            new Particle.DustOptions(Color.fromRGB(150, 0, 255), 1.2f));
                }

                if (tick % 4 == 0) {
                    world.playSound(handTarget, Sound.BLOCK_BEACON_AMBIENT, 1.2f, 0.6f + (tick * 0.03f));
                    world.playSound(handTarget, Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 0.5f + (tick * 0.02f));
                }

                if (tick >= chargeTicks) {
                    
                    firePurpleSingularity(caster, world, caster.getEyeLocation().add(caster.getLocation().getDirection().multiply(1.5)));
                    cancel();
                    return;
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private void firePurpleSingularity(Player caster, org.bukkit.World world, Location startLocation) {
        Vector travelDirection = caster.getLocation().getDirection().normalize();
        
        double maxRange = 60.0;
        double destructionRadius = 3.2; 
        double damageValue = plugin.getConfig().getDouble("damage.hollow-purple-hit", 45.0);
        
        
        world.playSound(startLocation, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.5f);
        world.playSound(startLocation, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.5f, 0.5f);
        world.playSound(startLocation, Sound.ENTITY_WITHER_DEATH, 1.5f, 0.5f);

        Set<LivingEntity> deathRegistry = new HashSet<>();

        new BukkitRunnable() {
            double distanceTraveled = 0;
            final Location currentCenter = startLocation.clone();
            final double stepIncrement = 1.8; 

            @Override
            public void run() {
                if (distanceTraveled >= maxRange) {
                    cancel();
                    return;
                }

                currentCenter.add(travelDirection.clone().multiply(stepIncrement));
                distanceTraveled += stepIncrement;

                
                world.spawnParticle(Particle.FLASH, currentCenter, 2, 0.5, 0.5, 0.5, 0);
                world.spawnParticle(Particle.EXPLOSION_EMITTER, currentCenter, 1, 0.2, 0.2, 0.2, 0);
                
                
                world.spawnParticle(Particle.DUST, currentCenter, 45, 0.6, 0.6, 0.6, 0,
                        new Particle.DustOptions(Color.fromRGB(110, 0, 190), 3.5f));
                world.spawnParticle(Particle.DUST, currentCenter, 30, 0.4, 0.4, 0.4, 0,
                        new Particle.DustOptions(Color.fromRGB(240, 140, 255), 2.0f));

                
                for (int d = 0; d < 3; d++) {
                    VfxUtil.ring(world, currentCenter, destructionRadius - (d * 0.8), 20, Particle.DUST,
                            new Particle.DustOptions(Color.fromRGB(140, 20, 220), 1.5f));
                }
                world.spawnParticle(Particle.END_ROD, currentCenter, 8, 1.2, 1.2, 1.2, 0.05);

                
                int checkRadius = (int) Math.ceil(destructionRadius);
                for (int x = -checkRadius; x <= checkRadius; x++) {
                    for (int y = -checkRadius; y <= checkRadius; y++) {
                        for (int z = -checkRadius; z <= checkRadius; z++) {
                            Location blockLoc = currentCenter.clone().add(x, y, z);
                            
                            if (currentCenter.distance(blockLoc) <= destructionRadius) {
                                Block targetBlock = blockLoc.getBlock();
                                
                                
                                if (targetBlock.getType() != Material.AIR && targetBlock.getType() != Material.BEDROCK) {
                                    
                                    if (targetBlock.getType().isSolid()) {
                                        world.spawnParticle(Particle.BLOCK_CRUMBLE, blockLoc, 3, 0.2, 0.2, 0.2, 0.1, targetBlock.getBlockData());
                                    }
                                    targetBlock.setType(Material.AIR, true); 
                                }
                            }
                        }
                    }
                }

                
                List<LivingEntity> preyEnemies = TargetUtil.getNearbyEnemies(currentCenter, destructionRadius + 1.0, caster);
                for (LivingEntity target : preyEnemies) {
                    if (deathRegistry.contains(target)) continue;
                    deathRegistry.add(target);

                    target.damage(damageValue, caster);
                    
                    
                    Vector ejectionVector = travelDirection.clone().multiply(2.5).add(new Vector(0, 0.6, 0));
                    target.setFallDistance(0);
                    target.setVelocity(ejectionVector);

                    
                    world.spawnParticle(Particle.SONIC_BOOM, target.getEyeLocation(), 2, 0.2, 0.2, 0.2, 0);
                    world.playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 0.5f);
                }

                
                if (Math.floor(distanceTraveled) % 6 == 0) {
                    world.playSound(currentCenter, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 0.5f);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
