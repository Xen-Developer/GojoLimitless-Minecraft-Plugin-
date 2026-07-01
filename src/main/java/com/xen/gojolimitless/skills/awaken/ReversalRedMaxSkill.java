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
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ReversalRedMaxSkill implements Skill {

    private final GojoLimitlessPlugin plugin;

    public ReversalRedMaxSkill(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "reversal_red_max";
    }

    @Override
    public String getCooldownConfigKey() {
        return "reversal-red-max";
    }

    @Override
    public boolean cast(Player caster) {
        double maxTravelDistance = 65.0;
        double blastRadius = plugin.getConfig().getDouble("ranges.reversal-red-blast-radius", 6.0) * 2.2;
        double initialHitDmg = plugin.getConfig().getDouble("damage.reversal-red-max-hit", 25.0);

        var world = caster.getWorld();
        Location startLocation = caster.getEyeLocation().add(caster.getLocation().getDirection().multiply(1.0));
        Vector travelDirection = caster.getLocation().getDirection().normalize();

        
        world.playSound(startLocation, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.5f, 0.5f);
        world.playSound(startLocation, Sound.BLOCK_BEACON_DEACTIVATE, 2.0f, 0.6f);
        world.playSound(startLocation, Sound.ENTITY_WITHER_SHOOT, 1.5f, 0.5f);

        Set<LivingEntity> piercedTargets = new HashSet<>();

        new BukkitRunnable() {
            double distanceTraveled = 0;
            Location currentCenter = startLocation.clone();
            final double speedIncrement = 2.4; 
            final double repelPathRadius = 3.5; 

            @Override
            public void run() {
                
                if (distanceTraveled >= maxTravelDistance || currentCenter.getY() < world.getMinHeight() || currentCenter.getY() > world.getMaxHeight()) {
                    triggerMaxDetonation(world, currentCenter, caster, blastRadius, initialHitDmg);
                    cancel();
                    return;
                }

                currentCenter.add(travelDirection.clone().multiply(speedIncrement));
                distanceTraveled += speedIncrement;

                
                world.spawnParticle(Particle.DUST, currentCenter, 40, 0.4, 0.4, 0.4, 0,
                        new Particle.DustOptions(Color.fromRGB(255, 0, 40), 3.0f)); 
                world.spawnParticle(Particle.DUST, currentCenter, 25, 0.7, 0.7, 0.7, 0,
                        new Particle.DustOptions(Color.fromRGB(255, 120, 140), 1.8f)); 
                world.spawnParticle(Particle.FLASH, currentCenter, 1, 0.1, 0.1, 0.1, 0);
                world.spawnParticle(Particle.SWEEP_ATTACK, currentCenter, 2, 0.5, 0.5, 0.5, 0.1);

                
                VfxUtil.ring(world, currentCenter, repelPathRadius, 18, Particle.DUST, new Particle.DustOptions(Color.fromRGB(200, 10, 30), 1.3f));

                if (Math.floor(distanceTraveled) % 5 == 0) {
                    world.playSound(currentCenter, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 0.6f);
                }

                
                List<LivingEntity> pathEnemies = TargetUtil.getNearbyEnemies(currentCenter, repelPathRadius + 1.0, caster);
                for (LivingEntity target : pathEnemies) {
                    if (piercedTargets.contains(target)) continue;
                    piercedTargets.add(target);

                    target.damage(initialHitDmg * 0.6, caster); 
                    
                    Vector midAirRepel = target.getLocation().toVector().subtract(currentCenter.toVector());
                    midAirRepel.setY(0.4);
                    midAirRepel.normalize().multiply(1.8);
                    target.setVelocity(target.getVelocity().add(midAirRepel));
                }

                
                int floorCheck = (int) Math.ceil(repelPathRadius);
                boolean structuralImpactTrigger = false;

                for (int x = -floorCheck; x <= floorCheck; x++) {
                    for (int y = -floorCheck; y <= floorCheck; y++) {
                        for (int z = -floorCheck; z <= floorCheck; z++) {
                            Location blockLoc = currentCenter.clone().add(x, y, z);
                            if (currentCenter.distance(blockLoc) <= repelPathRadius) {
                                Block block = blockLoc.getBlock();
                                if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                                    structuralImpactTrigger = true;
                                    
                                    if (block.getType().isSolid() && Math.random() < 0.25) {
                                        
                                        try {
                                            FallingBlock debris = world.spawnFallingBlock(blockLoc.clone().add(0, 0.5, 0), block.getBlockData());
                                            debris.setDropItem(false);
                                            Vector bounceForce = blockLoc.toVector().subtract(currentCenter.toVector()).normalize().multiply(0.6);
                                            bounceForce.setY(Math.random() * 0.4 + 0.2);
                                            debris.setVelocity(bounceForce);
                                        } catch (Exception ignored) {}
                                    }
                                    block.setType(Material.AIR, true);
                                }
                            }
                        }
                    }
                }

                
                if (structuralImpactTrigger && Math.random() < 0.35) {
                    triggerMaxDetonation(world, currentCenter, caster, blastRadius, initialHitDmg);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private void triggerMaxDetonation(org.bukkit.World world, Location detonateLoc, Player caster, double baseRadius, double maxDamage) {
        
        world.playSound(detonateLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 3.0f, 0.5f);
        world.playSound(detonateLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.5f, 0.5f);
        world.playSound(detonateLoc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2.0f, 0.5f);

        world.spawnParticle(Particle.SONIC_BOOM, detonateLoc, 4, 1.0, 1.0, 1.0, 0);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, detonateLoc, 6, 1.5, 1.5, 1.5, 0);

        
        new BukkitRunnable() {
            int wavePhase = 0;

            @Override
            public void run() {
                if (wavePhase > 1) {
                    cancel();
                    return;
                }

                
                double currentWaveRadius = baseRadius * (wavePhase == 0 ? 0.6 : 1.2);
                int densityMultiplier = wavePhase == 0 ? 250 : 400;

                
                for (int h = -1; h <= 1; h++) {
                    VfxUtil.ring(world, detonateLoc.clone().add(0, h * 0.8, 0), currentWaveRadius, densityMultiplier / 6, 
                            Particle.DUST, new Particle.DustOptions(Color.fromRGB(255, wavePhase == 0 ? 10 : 80, 40), 2.5f));
                }
                
                world.spawnParticle(Particle.CLOUD, detonateLoc, 80, currentWaveRadius * 0.5, 0.5, currentWaveRadius * 0.5, 0.3);

                
                List<LivingEntity> shockwaveVictims = TargetUtil.getNearbyEnemies(detonateLoc, currentWaveRadius, caster);
                for (LivingEntity entity : shockwaveVictims) {
                    
                    double computationalDamage = wavePhase == 0 ? maxDamage : maxDamage * 0.4;
                    entity.damage(computationalDamage, caster);

                    Vector repulsionForce = entity.getLocation().toVector().subtract(detonateLoc.toVector());
                    double scaleFactor = repulsionForce.length();
                    repulsionForce.normalize();

                    
                    double directionalVelocityMultiplier = Math.max(1.5, (currentWaveRadius - scaleFactor) * 0.7);
                    Vector terminalVector = repulsionForce.multiply(directionalVelocityMultiplier * 2.4).setY(0.75);
                    
                    entity.setFallDistance(0);
                    entity.setVelocity(terminalVector);
                    
                    world.playSound(entity.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.5f, 0.5f);
                }

                wavePhase++;
            }
        }.runTaskTimer(plugin, 0L, 4L); 
    }
}
