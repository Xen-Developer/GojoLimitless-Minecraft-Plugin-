package com.xen.gojolimitless.skills.awaken;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import com.xen.gojolimitless.skills.Skill;
import com.xen.gojolimitless.util.TargetUtil;
import com.xen.gojolimitless.util.VfxUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class HollowPurpleNukeSkill implements Skill, Listener {

    private final GojoLimitlessPlugin plugin;
    private static final double APPLIED_DAMAGE = 45.0;
    private final Set<UUID> invulnerableCasters = new HashSet<>();

    public HollowPurpleNukeSkill(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getId() {
        return "hollow_purple_nuke";
    }

    @Override
    public String getCooldownConfigKey() {
        return "hollow-purple-nuke";
    }

    @Override
    public boolean cast(Player caster) {
        World world = caster.getWorld();
        
        Location targetDetonationLoc = caster.getTargetBlockExact(35) != null ? 
                caster.getTargetBlockExact(35).getLocation().add(0, 2, 0) : 
                caster.getEyeLocation().add(caster.getLocation().getDirection().multiply(25));

        UUID casterUUID = caster.getUniqueId();
        invulnerableCasters.add(casterUUID);

        
        world.playSound(caster.getLocation(), Sound.ENTITY_WITHER_SPAWN, 3.0f, 0.2f);
        world.playSound(targetDetonationLoc, Sound.ENTITY_WARDEN_SONIC_CHARGE, 3.0f, 0.5f);

        
        new BukkitRunnable() {
            int tick = 0;
            final int chargeTicks = 40;

            @Override
            public void run() {
                if (!caster.isOnline() || !invulnerableCasters.contains(casterUUID)) {
                    invulnerableCasters.remove(casterUUID);
                    cancel();
                    return;
                }

                
                caster.setVelocity(new Vector(0, 0.12, 0));

                double progress = (double) tick / chargeTicks;
                double pullRadius = 6.0 + (progress * 10.0);

                
                world.spawnParticle(Particle.REVERSE_PORTAL, targetDetonationLoc, 15, pullRadius * 0.4, 2, pullRadius * 0.4, 0.2);
                world.spawnParticle(Particle.DUST, targetDetonationLoc, 20, 1.0, 1.0, 1.0, 0,
                        new Particle.DustOptions(Color.fromRGB(130, 0, 255), 2.5f));

                
                List<LivingEntity> trappedEnemies = TargetUtil.getNearbyEnemies(targetDetonationLoc, pullRadius, caster);
                for (LivingEntity enemy : trappedEnemies) {
                    Vector dragVector = targetDetonationLoc.toVector().subtract(enemy.getLocation().toVector());
                    enemy.setVelocity(dragVector.normalize().multiply(0.45).setY(0.15));
                }

                
                if (tick % 4 == 0) {
                    world.playSound(targetDetonationLoc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 2.0f, 0.4f + (tick * 0.02f));
                }

                if (tick >= chargeTicks) {
                    detonateNuke(caster, world, targetDetonationLoc);
                    invulnerableCasters.remove(casterUUID);
                    cancel();
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private void detonateNuke(Player caster, World world, Location epicenter) {
        double explosionRadius = 15.0; 

        
        world.playSound(epicenter, Sound.ENTITY_GENERIC_EXPLODE, 5.0f, 0.4f);
        world.playSound(epicenter, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 4.0f, 0.5f);
        world.playSound(epicenter, Sound.ENTITY_WARDEN_SONIC_BOOM, 3.5f, 0.4f);

        
        world.spawnParticle(Particle.FLASH, epicenter, 5, 2.0, 2.0, 2.0, 0);

        
        int blockRadius = (int) Math.ceil(explosionRadius);
        for (int x = -blockRadius; x <= blockRadius; x++) {
            for (int y = -blockRadius; y <= blockRadius; y++) {
                for (int z = -blockRadius; z <= blockRadius; z++) {
                    Location targetBlockLoc = epicenter.clone().add(x, y, z);
                    double calculatedDistance = epicenter.distance(targetBlockLoc);

                    
                    if (calculatedDistance <= explosionRadius && targetBlockLoc.getY() >= epicenter.getY() - (explosionRadius * 0.6)) {
                        Block block = targetBlockLoc.getBlock();
                        if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                            if (block.getType().isSolid() && Math.random() < 0.15) {
                                world.spawnParticle(Particle.BLOCK_CRUMBLE, targetBlockLoc, 2, 0.1, 0.1, 0.1, 0.2, block.getBlockData());
                            }
                            block.setType(Material.AIR, false); 
                        }
                    }
                }
            }
        }

        
        new BukkitRunnable() {
            int waveTick = 0;
            final int waveDuration = 25;

            @Override
            public void run() {
                if (waveTick > waveDuration) {
                    cancel();
                    return;
                }

                double activeRadius = explosionRadius * ((double) waveTick / waveDuration);
                
                
                for (int layer = -2; layer <= 2; layer++) {
                    VfxUtil.ring(world, epicenter.clone().add(0, layer * 1.2, 0), activeRadius, 65, Particle.DUST,
                            new Particle.DustOptions(Color.fromRGB(150, 0, 255), 2.8f));
                }
                
                VfxUtil.ring(world, epicenter, activeRadius * 0.8, 40, Particle.DUST, new Particle.DustOptions(Color.fromRGB(255, 255, 255), 1.5f));
                world.spawnParticle(Particle.EXPLOSION, epicenter, 4, activeRadius * 0.5, 1.0, activeRadius * 0.5, 0.1);

                waveTick += 2;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        
        List<LivingEntity> targets = TargetUtil.getNearbyEnemies(epicenter, explosionRadius + 3.0, caster);
        for (LivingEntity entity : targets) {
            double distanceToEpicenter = entity.getLocation().distance(epicenter);
            
            
            double damageScale = Math.max(0.40, 1.0 - (distanceToEpicenter / explosionRadius));
            entity.damage(APPLIED_DAMAGE * damageScale, caster);

            
            Vector launchTrajectory = entity.getLocation().toVector().subtract(epicenter.toVector());
            launchTrajectory.setY(0.8); 
            launchTrajectory.normalize().multiply(3.0); 
            
            entity.setFallDistance(0);
            entity.setVelocity(launchTrajectory);

            world.spawnParticle(Particle.SONIC_BOOM, entity.getEyeLocation(), 1);
        }

        caster.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&5&l☄ &d&lCRITICAL OUTPUT: Unrestricted Hollow Purple shattered the landscape. &7(" + targets.size() + " vaporized)"));
    }

    @EventHandler
    public void onCasterDamage(EntityDamageEvent event) {
        
        if (invulnerableCasters.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
