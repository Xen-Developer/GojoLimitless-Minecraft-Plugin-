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

import java.util.List;


public class ReversalRedSkill implements Skill {

    private final GojoLimitlessPlugin plugin;

    public ReversalRedSkill(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "reversal_red";
    }

    @Override
    public String getCooldownConfigKey() {
        return "reversal-red";
    }

    @Override
    public boolean cast(Player caster) {
        double travel = plugin.getConfig().getDouble("ranges.reversal-red-travel", 40.0);
        double blastRadius = plugin.getConfig().getDouble("ranges.reversal-red-blast-radius", 6.5);
        double hitDamage = plugin.getConfig().getDouble("damage.reversal-red-hit", 12.5);

        var world = caster.getWorld();
        Location start = caster.getEyeLocation().add(caster.getLocation().getDirection().multiply(1.0));
        Vector direction = caster.getLocation().getDirection().normalize();

        
        world.playSound(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.4f, 0.8f);
        world.playSound(start, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.2f, 0.5f);
        world.playSound(start, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.6f);

        new BukkitRunnable() {
            double traveled = 0;
            Location current = start.clone();
            final double speed = 1.6; 
            int rotationTicks = 0;

            @Override
            public void run() {
                if (traveled >= travel) {
                    detonate(world, current, caster, blastRadius, hitDamage);
                    cancel();
                    return;
                }

                current.add(direction.clone().multiply(speed));
                traveled += speed;

                
                
                world.spawnParticle(Particle.DUST, current, 12, 0.15, 0.15, 0.15, 0,
                        new Particle.DustOptions(Color.fromRGB(255, 10, 10), 1.8f));
                
                world.spawnParticle(Particle.CRIT, current, 5, 0.1, 0.1, 0.1, 0.1);
                world.spawnParticle(Particle.SWEEP_ATTACK, current, 1, 0.1, 0.1, 0.1, 0);

                
                rotationTicks++;
                double angle = rotationTicks * 0.5;
                for (int i = 0; i < 4; i++) {
                    double finalAngle = angle + (i * Math.PI / 2);
                    Vector offset = new Vector(Math.cos(finalAngle), 0, Math.sin(finalAngle)).multiply(0.6);
                    
                    Location particleLoc = current.clone().add(offset);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(180, 0, 30), 1.2f));
                }

                
                for (var entity : world.getNearbyEntities(current, 1.3, 1.3, 1.3)) {
                    if (entity instanceof LivingEntity living && !living.equals(caster)) {
                        detonate(world, current, caster, blastRadius, hitDamage);
                        cancel();
                        return;
                    }
                }

                
                if (!current.getBlock().isPassable() && current.getBlock().getType().isSolid()) {
                    detonate(world, current, caster, blastRadius, hitDamage);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private void detonate(org.bukkit.World world, Location at, Player caster, double radius, double hitDamage) {
        
        world.playSound(at, Sound.ENTITY_GENERIC_EXPLODE, 1.6f, 0.7f);
        world.playSound(at, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 0.6f);
        world.playSound(at, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.5f);

        
        world.spawnParticle(Particle.SONIC_BOOM, at, 3, 0.4, 0.4, 0.4, 0.1);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, at, 2, 0.2, 0.2, 0.2, 0);
        world.spawnParticle(Particle.REVERSE_PORTAL, at, 100, 1.5, 1.5, 1.5, 0.4);

        
        new BukkitRunnable() {
            int tick = 0;
            final int maxTicks = 8; 

            @Override
            public void run() {
                if (tick > maxTicks) {
                    cancel();
                    return;
                }
                
                double currentRadius = radius * ((double) tick / maxTicks);
                
                
                VfxUtil.ring(world, at, currentRadius, 40, Particle.DUST,
                        new Particle.DustOptions(Color.fromRGB(255, 0, 50), (float)(2.2 - (tick * 0.2))));
                
                
                for (int i = 0; i < 360; i += 12) {
                    double rad = Math.toRadians(i);
                    Vector vertVector = new Vector(0, Math.cos(rad), Math.sin(rad)).multiply(currentRadius);
                    world.spawnParticle(Particle.DUST, at.clone().add(vertVector), 1, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(255, 60, 0), (float)(1.6 - (tick * 0.15))));
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        
        List<LivingEntity> nearby = TargetUtil.getNearbyEnemies(at, radius, caster);
        for (LivingEntity entity : nearby) {
            entity.damage(hitDamage, caster);

            
            double entityDist = entity.getLocation().distance(at);
            double proximityFactor = Math.max(0.3, 1.0 - (entityDist / radius)); 

            Vector repulsionVector = entity.getLocation().toVector().subtract(at.toVector());
            
            
            if (repulsionVector.lengthSquared() == 0) {
                repulsionVector = new Vector(0, 0, 1);
            }
            
            repulsionVector.normalize();
            
            
            double launchStrength = 1.6 * proximityFactor + 0.6;
            repulsionVector.multiply(launchStrength);
            
            
            repulsionVector.setY(0.48 * proximityFactor + 0.2);

            
            entity.setFallDistance(0);
            entity.setVelocity(repulsionVector);
        }
    }
}
