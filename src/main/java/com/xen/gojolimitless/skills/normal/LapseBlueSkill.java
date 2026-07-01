package com.xen.gojolimitless.skills.normal;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import com.xen.gojolimitless.skills.Skill;
import com.xen.gojolimitless.util.TargetUtil;
import com.xen.gojolimitless.util.VfxUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


public class LapseBlueSkill implements Skill {

    private final GojoLimitlessPlugin plugin;

    public LapseBlueSkill(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "lapse_blue";
    }

    @Override
    public String getCooldownConfigKey() {
        return "lapse-blue";
    }

    @Override
    public boolean cast(Player caster) {
        var world = caster.getWorld();
        
        
        Vector forwardOffset = caster.getLocation().getDirection().normalize().multiply(3.5);
        Location anomalyCenter = caster.getEyeLocation().add(forwardOffset).add(0, 1.2, 0);

        double suctionRadius = plugin.getConfig().getDouble("ranges.lapse-blue-suction-radius", 12.0);
        double blastRadius = plugin.getConfig().getDouble("ranges.lapse-blue-blast-radius", 5.5);
        double tickDamage = plugin.getConfig().getDouble("damage.lapse-blue-tick-damage", 1.0);
        double finalDamage = plugin.getConfig().getDouble("damage.lapse-blue-final-explosion", 14.0);

        
        world.playSound(anomalyCenter, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.2f, 0.5f);
        world.playSound(anomalyCenter, Sound.BLOCK_BEACON_AMBIENT, 2.0f, 0.5f);

        
        new BukkitRunnable() {
            int durationTicks = 0;
            final int maxDurationTicks = 30; 

            @Override
            public void run() {
                if (durationTicks >= maxDurationTicks) {
                    detonateAnomaly(world, anomalyCenter, caster, blastRadius, finalDamage);
                    cancel();
                    return;
                }

                
                if (durationTicks % 4 == 0) {
                    world.playSound(anomalyCenter, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
                }

                
                
                world.spawnParticle(Particle.DUST, anomalyCenter, 15, 0.1, 0.1, 0.1, 0,
                        new Particle.DustOptions(Color.fromRGB(0, 0, 255), 2.0f));
                world.spawnParticle(Particle.REVERSE_PORTAL, anomalyCenter, 12, 0.2, 0.2, 0.2, 0.4);
                
                
                double sphereRadius = 0.5 + (Math.sin(durationTicks * 0.4) * 0.3); 
                VfxUtil.sphere(world, anomalyCenter, sphereRadius, 8, 8, Particle.DUST,
                        new Particle.DustOptions(Color.fromRGB(0, 150, 255), 1.2f));

                
                for (Entity entity : world.getNearbyEntities(anomalyCenter, suctionRadius, suctionRadius, suctionRadius)) {
                    if (entity instanceof LivingEntity victim && !victim.equals(caster)) {
                        
                        Location victimLoc = victim.getLocation();
                        Vector pullDirection = anomalyCenter.toVector().subtract(victimLoc.toVector());
                        double distance = pullDirection.length();

                        if (distance > 0.1) {
                            pullDirection.normalize();
                            
                            
                            double suctionForce = Math.min(1.4, 0.3 + (distance * 0.12));
                            Vector velocityAdjustment = pullDirection.multiply(suctionForce);
                            
                            
                            if (victimLoc.getY() < anomalyCenter.getY()) {
                                velocityAdjustment.setY(Math.min(0.45, 0.2 + (anomalyCenter.getY() - victimLoc.getY()) * 0.15));
                            }

                            
                            victim.setFallDistance(0);
                            victim.setVelocity(velocityAdjustment);

                            
                            if (durationTicks % 5 == 0) {
                                victim.damage(tickDamage, caster);
                                world.spawnParticle(Particle.CRIT, victim.getEyeLocation(), 4, 0.2, 0.2, 0.2, 0.1);
                            }
                        }
                    }
                }

                durationTicks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private void detonateAnomaly(org.bukkit.World world, Location at, Player caster, double radius, double damage) {
        
        world.playSound(at, Sound.ENTITY_GENERIC_EXPLODE, 1.8f, 0.7f);
        world.playSound(at, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 0.7f);
        world.playSound(at, Sound.BLOCK_ANVIL_LAND, 1.0f, 0.1f);

        
        world.spawnParticle(Particle.SONIC_BOOM, at, 4, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, at, 3, 0.3, 0.3, 0.3, 0);
        
        
        for (int i = 0; i < 360; i += 8) {
            double rad = Math.toRadians(i);
            Vector ringDir = new Vector(Math.cos(rad), 0, Math.sin(rad)).multiply(radius);
            world.spawnParticle(Particle.DUST, at.clone().add(ringDir), 2, 0.1, 0.1, 0.1, 
                    new Particle.DustOptions(Color.fromRGB(0, 191, 255), 1.8f));
            
            
            if (i % 24 == 0) {
                world.spawnParticle(Particle.SWEEP_ATTACK, at.clone().add(ringDir.multiply(0.4)), 1, 0, 0, 0, 0);
            }
        }

        
        for (Entity entity : world.getNearbyEntities(at, radius, radius, radius)) {
            if (entity instanceof LivingEntity victim && !victim.equals(caster)) {
                victim.damage(damage, caster);

                
                Vector pushAway = victim.getLocation().toVector().subtract(at.toVector());
                if (pushAway.lengthSquared() == 0) {
                    pushAway = new Vector(0, 0, 1);
                }
                
                pushAway.normalize().multiply(1.5); 
                pushAway.setY(0.45); 
                
                victim.setFallDistance(0);
                victim.setVelocity(pushAway);
            }
        }
    }
}
