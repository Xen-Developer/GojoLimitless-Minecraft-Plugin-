package com.xen.gojolimitless.skills.awaken;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import com.xen.gojolimitless.managers.DomainManager;
import com.xen.gojolimitless.skills.Skill;
import com.xen.gojolimitless.util.TargetUtil;
import com.xen.gojolimitless.util.VfxUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class InfiniteVoidSkill implements Skill {

    private final GojoLimitlessPlugin plugin;
    private final Random random = new Random();

    public InfiniteVoidSkill(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "infinite_void";
    }

    @Override
    public String getCooldownConfigKey() {
        return "infinite-void";
    }

    @Override
    public boolean cast(Player caster) {
        World world = caster.getWorld();
        Location center = caster.getLocation();
        
        double radius = plugin.getConfig().getDouble("ranges.domain-radius", 22.0); 
        int durationSeconds = plugin.getConfig().getInt("domain.duration-seconds", 10);
        double tickDamage = plugin.getConfig().getDouble("damage.infinite-void-tick-damage", 6.0);
        long durationTicks = durationSeconds * 20L;

        
        world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 3.0f, 0.1f); 
        world.playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.5f);
        world.playSound(center, Sound.BLOCK_END_PORTAL_SPAWN, 1.5f, 0.8f);

        DomainManager domainManager = plugin.getDomainManager();
        
        List<Block> blackOuterShell = new ArrayList<>();
        List<Block> glassFloor = new ArrayList<>();
        List<Block> floorSubLayer = new ArrayList<>();
        List<Block> invisibleLightSources = new ArrayList<>();
        List<Block> cosmicDebris = new ArrayList<>();
        List<Block> interiorAir = new ArrayList<>(); 

        int blockRadius = (int) Math.ceil(radius);
        int floorY = center.getBlockY() - 1; 

        
        for (int x = -blockRadius; x <= blockRadius; x++) {
            for (int y = -blockRadius; y <= blockRadius; y++) {
                for (int z = -blockRadius; z <= blockRadius; z++) {
                    
                    int currentY = center.getBlockY() + y;
                    Block block = world.getBlockAt(center.getBlockX() + x, currentY, center.getBlockZ() + z);
                    
                    if (block.getType() == Material.BEDROCK || block.getY() <= world.getMinHeight()) {
                        continue;
                    }

                    double distance = Math.sqrt(x * x + y * y + z * z);

                    
                    if (currentY == floorY && distance < radius - 1.0) {
                        glassFloor.add(block);
                    } 
                    else if (currentY == floorY - 1 && distance < radius - 1.0) {
                        floorSubLayer.add(block); 
                    }
                    
                    else if (distance >= radius - 1.0 && distance <= radius) {
                        blackOuterShell.add(block);
                    }
                    
                    else if (distance >= radius - 2.0 && distance < radius - 1.0) {
                        if (block.getType() == Material.AIR || block.getType().isTransparent()) {
                            invisibleLightSources.add(block);
                        }
                    }
                    
                    else if (distance < radius - 3.0 && currentY > floorY + 3 && random.nextDouble() < 0.001) {
                        cosmicDebris.add(block);
                    }
                    
                    else if (distance < radius - 2.0 && currentY > floorY) {
                        if (block.getType() != Material.AIR) {
                            interiorAir.add(block);
                        }
                    }
                }
            }
        }

        
        domainManager.placeTemporary(world, interiorAir, Material.AIR, durationTicks); 
        domainManager.placeTemporary(world, blackOuterShell, Material.BLACK_CONCRETE, durationTicks);
        domainManager.placeTemporary(world, floorSubLayer, Material.CYAN_TERRACOTTA, durationTicks);
        domainManager.placeTemporary(world, glassFloor, Material.BLACK_STAINED_GLASS, durationTicks);
        domainManager.placeTemporary(world, cosmicDebris, Material.CRYING_OBSIDIAN, durationTicks);

        
        domainManager.placeTemporary(world, invisibleLightSources, Material.LIGHT, durationTicks);
        for (Block b : invisibleLightSources) {
            if (b.getBlockData() instanceof Light lightData) {
                lightData.setLevel(15);
                b.setBlockData(lightData, false);
            }
        }

        
        caster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) durationTicks, 9, false, false));
        caster.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, (int) durationTicks, 4, false, false));
        caster.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, (int) durationTicks, 3, false, false));

        
        new BukkitRunnable() {
            int elapsedTicks = 0;
            double spiralTheta = 0;

            @Override
            public void run() {
                if (elapsedTicks >= durationTicks || !caster.isOnline()) {
                    cancel();
                    return;
                }

                
                for (int i = 0; i < 20; i++) {
                    double offsetX = (random.nextDouble() * (radius - 4)) * (random.nextBoolean() ? 1 : -1);
                    double offsetY = (random.nextDouble() * (radius - 4)) * (random.nextBoolean() ? 1 : -1);
                    double offsetZ = (random.nextDouble() * (radius - 4)) * (random.nextBoolean() ? 1 : -1);
                    Location starLocation = center.clone().add(offsetX, offsetY + 2, offsetZ);

                    world.spawnParticle(Particle.FIREWORK, starLocation, 1, 0, 0, 0, 0);
                    if (random.nextFloat() < 0.15) {
                        world.spawnParticle(Particle.END_ROD, starLocation, 1, 0.1, 0.1, 0.1, 0.01);
                    }
                }

                
                Location eyeCenter = center.clone().add(0, radius - 4, 0);
                spiralTheta += Math.PI / 16; 
                
                for (int arm = 0; arm < 3; arm++) { 
                    double armOffset = (Math.PI * 2 / 3) * arm;
                    for (double r = 0.5; r < 7.0; r += 0.5) {
                        double x = r * Math.cos(spiralTheta + armOffset - (r * 0.4)); 
                        double z = r * Math.sin(spiralTheta + armOffset - (r * 0.4));
                        Location particleLoc = eyeCenter.clone().add(x, 0, z);
                        
                        Particle.DustTransition galaxyDust = new Particle.DustTransition(
                                Color.fromRGB(138, 43, 226), Color.fromRGB(0, 0, 0), 2.0f); 
                        world.spawnParticle(Particle.DUST_COLOR_TRANSITION, particleLoc, 1, 0.1, 0.1, 0.1, 0, galaxyDust);
                    }
                }

                
                VfxUtil.ring(world, caster.getLocation().add(0, 0.1, 0), 1.5, 15, Particle.END_ROD, null);
                
                if (elapsedTicks % 20 == 0) { 
                    world.playSound(center, Sound.AMBIENT_CRIMSON_FOREST_MOOD, 1.0f, 0.5f);
                }
                
                elapsedTicks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= durationTicks || !caster.isOnline()) {
                    cancel();
                    return;
                }

                
                Vector lookDir = caster.getLocation().getDirection().setY(0).normalize().multiply(1.5);
                caster.setVelocity(new Vector(lookDir.getX(), caster.getVelocity().getY(), lookDir.getZ()));

                
                for (int i = 0; i < 6; i++) {
                    Location slashLoc = caster.getLocation().add(
                            (random.nextDouble() - 0.5) * 16,
                            random.nextDouble() * 3,
                            (random.nextDouble() - 0.5) * 16
                    );
                    world.spawnParticle(Particle.SWEEP_ATTACK, slashLoc, 1);
                    if (i % 3 == 0) {
                        world.playSound(slashLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.6f);
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        
        List<LivingEntity> trapped = TargetUtil.getNearbyEnemies(center, radius - 2.0, caster);
        for (LivingEntity entity : trapped) {
            applyInfiniteVoidParalysis(entity, durationSeconds, tickDamage, caster);
        }

        caster.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                "&0[&d&lInfinite Void&0] &fThe universe is laid bare. &d" + trapped.size() + " &7minds caught in infinity."));

        return true;
    }

    private void applyInfiniteVoidParalysis(LivingEntity entity, int durationSeconds, double tickDamage, Player caster) {
        World world = entity.getWorld();
        Location lockLocation = entity.getLocation().clone();

        
        entity.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, durationSeconds * 20, 0, false, false));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationSeconds * 20, 255, false, false));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, durationSeconds * 20, 1, false, false));
        
        if (entity instanceof Player targetPlayer) {
            targetPlayer.sendTitle(
                org.bukkit.ChatColor.translateAlternateColorCodes('&', "&0&l- &f&lINFINITE VOID &0&l-"), 
                org.bukkit.ChatColor.translateAlternateColorCodes('&', "&dYour mind is flooding with infinite information."), 
                5, 70, 15
            );
        }

        
        new BukkitRunnable() {
            int ticksElapsed = 0;
            final int totalTicks = durationSeconds * 20;

            @Override
            public void run() {
                if (ticksElapsed >= totalTicks || !entity.isValid() || entity.isDead()) {
                    cancel();
                    return;
                }

                
                entity.setVelocity(new Vector(0, -0.1, 0)); 
                if (entity.getLocation().distanceSquared(lockLocation) > 0.1) {
                    entity.teleport(lockLocation);
                }

                
                
                for (int i = 0; i < 4; i++) {
                    Location streamStart = entity.getEyeLocation().add(
                            (random.nextDouble() - 0.5) * 4, 
                            (random.nextDouble() - 0.5) * 4 + 2, 
                            (random.nextDouble() - 0.5) * 4);
                    
                    Vector flowDir = entity.getEyeLocation().toVector().subtract(streamStart.toVector()).normalize().multiply(0.4);
                    world.spawnParticle(Particle.ENCHANT, streamStart, 0, flowDir.getX(), flowDir.getY(), flowDir.getZ(), 1.0);
                }

                
                if (ticksElapsed % 10 == 0) { 
                    
                    world.playSound(entity.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 2.0f);
                    world.playSound(entity.getEyeLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.3f, 2.0f);
                    
                    if (ticksElapsed % 20 == 0) { 
                        entity.damage(tickDamage, caster);
                        world.spawnParticle(Particle.REVERSE_PORTAL, entity.getEyeLocation(), 35, 0.4, 0.4, 0.4, 0.25);
                        world.playSound(entity.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.4f, 1.9f);
                    }
                }
                
                ticksElapsed++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
