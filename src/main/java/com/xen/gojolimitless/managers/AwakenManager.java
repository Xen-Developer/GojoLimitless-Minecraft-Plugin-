package com.xen.gojolimitless.managers;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import com.xen.gojolimitless.util.VfxUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class AwakenManager {

    private final GojoLimitlessPlugin plugin;
    private final ConcurrentHashMap<UUID, BukkitRunnable> activeAuras = new ConcurrentHashMap<>();

    public AwakenManager(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean tryAwaken(Player player) {
        var pdm = plugin.getPlayerDataManager();
        var data = pdm.get(player);

        if (!data.isReadyToAwaken() || data.isAwakened()) {
            return false;
        }

        
        data.setReadyToAwaken(false);

        
        new CinematicTransformationTask(player).runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    
    private class CinematicTransformationTask extends BukkitRunnable {
        private final Player player;
        private final World world;
        
        private int ticks = 0;
        private ArmorStand hologram;
        private final List<FallingBlock> debrisList = new ArrayList<>();
        private Location floatLoc;

        public CinematicTransformationTask(Player player) {
            this.player = player;
            this.world = player.getWorld();
        }

        @Override
        public void run() {
            if (!player.isOnline() || player.isDead()) {
                cleanup();
                cancel();
                return;
            }

            Location currentLoc = player.getLocation();

            
            
            
            if (ticks == 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 180, 255, false, false));
                
                world.playSound(player.getEyeLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 3.0f, 0.5f);
                world.playSound(player.getEyeLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 2.0f, 0.5f);
            }

            if (ticks > 0 && ticks < 30) {
                if (ticks % 10 == 0) {
                    world.playSound(player.getEyeLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 3.0f, 0.6f);
                }
            }

            
            
            
            if (ticks == 30) {
                Location holoLoc = currentLoc.clone().add(currentLoc.getDirection().multiply(2.5)).add(0, 1.5, 0);
                hologram = (ArmorStand) world.spawnEntity(holoLoc, EntityType.ARMOR_STAND);
                hologram.setInvisible(true);
                hologram.setInvulnerable(true);
                hologram.setGravity(false);
                hologram.setMarker(true);
                hologram.setCustomNameVisible(true);
                world.playSound(holoLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 2.0f, 0.5f);
            }

            if (ticks >= 30 && ticks < 100) {
                
                String prefix = "§b§l";
                String text = "It's time to get a little crazy...";
                int charsToType = Math.min(text.length(), (ticks - 30)); 
                
                if (hologram != null && hologram.isValid()) {
                    hologram.setCustomName(prefix + text.substring(0, charsToType));
                    if (ticks % 3 == 0 && charsToType < text.length()) {
                        world.playSound(hologram.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 2.0f);
                    }
                    
                    Location targetHolo = player.getEyeLocation().add(player.getLocation().getDirection().multiply(2.5)).subtract(0, 0.5, 0);
                    hologram.teleport(targetHolo);
                }

                
                player.setVelocity(new Vector(0, 0.08, 0)); 

                
                double yOffset = (ticks % 40) * 0.1;
                double angle1 = ticks * 0.3;
                double angle2 = angle1 + Math.PI; 
                
                Vector p1 = new Vector(Math.cos(angle1) * 1.5, yOffset, Math.sin(angle1) * 1.5);
                Vector p2 = new Vector(Math.cos(angle2) * 1.5, yOffset, Math.sin(angle2) * 1.5);

                world.spawnParticle(Particle.DUST, currentLoc.clone().add(p1), 2, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(0, 150, 255), 1.5f));
                world.spawnParticle(Particle.DUST, currentLoc.clone().add(p2), 2, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(150, 0, 255), 1.5f));
            }

            
            
            
            if (ticks == 100) {
                world.playSound(currentLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
                world.playSound(currentLoc, Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 2.0f);
                floatLoc = currentLoc.clone(); 

                
                for (int i = 0; i < 12; i++) {
                    Location blockLoc = currentLoc.clone().add(Math.random() * 8 - 4, -5, Math.random() * 8 - 4);
                    if (blockLoc.getBlock().getType().isSolid()) {
                        FallingBlock fb = world.spawnFallingBlock(blockLoc.add(0, 1, 0), blockLoc.getBlock().getBlockData());
                        fb.setDropItem(false);
                        fb.setGlowing(true);
                        debrisList.add(fb);
                    }
                }
            }

            if (ticks >= 100 && ticks < 150) {
                
                player.setVelocity(new Vector(0, 0.01, 0));

                
                for (double y = -2; y < 15; y += 1.5) {
                    VfxUtil.ring(world, currentLoc.clone().add(0, y, 0), 2.5, 20, Particle.ENCHANT, null);
                    world.spawnParticle(Particle.FIREWORK, currentLoc.clone().add(0, y, 0), 5, 0.5, 0.1, 0.5, 0.1);
                }

                
                for (FallingBlock fb : debrisList) {
                    if (fb.isValid()) {
                        Vector pull = floatLoc.clone().toVector().subtract(fb.getLocation().toVector());
                        Vector orbit = pull.clone().crossProduct(new Vector(0, 1, 0)).normalize().multiply(1.5);
                        fb.setVelocity(pull.normalize().multiply(0.2).add(orbit));
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, fb.getLocation(), 2, 0.2, 0.2, 0.2, 0);
                    }
                }
            }

            
            
            
            if (ticks == 150) {
                if (hologram != null) hologram.remove();
                for (FallingBlock fb : debrisList) {
                    if (fb.isValid()) {
                        world.spawnParticle(Particle.BLOCK_CRUMBLE, fb.getLocation(), 10, 0.5, 0.5, 0.5, 0, fb.getBlockData());
                        fb.remove();
                    }
                }
                world.playSound(currentLoc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 3.0f, 0.5f);
                
                
                player.setVelocity(new Vector(0, -2.5, 0));
            }

            
            
            
            if (ticks >= 165) {
                cleanup();
                executeTrueAwakening(player); 
                cancel();
            }

            ticks++;
        }

        private void cleanup() {
            if (hologram != null && hologram.isValid()) hologram.remove();
            for (FallingBlock fb : debrisList) {
                if (fb.isValid()) fb.remove();
            }
        }
    }

    
    private void executeTrueAwakening(Player player) {
        var pdm = plugin.getPlayerDataManager();
        var data = pdm.get(player);
        var world = player.getWorld();
        Location burstLoc = player.getLocation().add(0, 1, 0);

        
        world.playSound(burstLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 4.0f, 0.5f);
        world.playSound(burstLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 3.0f, 0.5f);
        world.playSound(burstLoc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.4f);
        world.playSound(burstLoc, Sound.BLOCK_GLASS_BREAK, 2.5f, 0.5f);
        
        world.strikeLightningEffect(burstLoc.clone().add(2, 0, 2));
        world.strikeLightningEffect(burstLoc.clone().add(-2, 0, -2));

        world.spawnParticle(Particle.FLASH, burstLoc, 6, 1.0, 1.0, 1.0, 0);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, burstLoc, 3, 0.5, 0.5, 0.5, 0);
        world.spawnParticle(Particle.END_ROD, burstLoc, 200, 0.5, 1.5, 0.5, 0.35);

        
        new BukkitRunnable() {
            int wave = 0;
            final int maxWaves = 25;

            @Override
            public void run() {
                if (!player.isOnline() || wave > maxWaves) {
                    cancel();
                    return;
                }

                double radius = wave * 0.65;
                
                VfxUtil.ring(world, burstLoc, radius, 45, Particle.DUST, new Particle.DustOptions(Color.fromRGB(0, 150, 255), 2.2f));
                VfxUtil.ring(world, burstLoc, radius * 0.8, 30, Particle.DUST, new Particle.DustOptions(Color.fromRGB(200, 0, 255), 1.8f));
                VfxUtil.ring(world, burstLoc, radius, 15, Particle.SWEEP_ATTACK, null);

                
                for (var entity : world.getNearbyEntities(burstLoc, radius, 3, radius)) {
                    if (entity instanceof LivingEntity living && !living.equals(player)) {
                        Vector push = living.getLocation().toVector().subtract(burstLoc.toVector());
                        push.setY(0.4);
                        living.setVelocity(push.normalize().multiply(1.8));
                    }
                }
                wave += 2;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        
        
        
        data.setAwakened(true);
        int durationSeconds = pdm.awakenDurationSeconds();
        data.setAwakenEndsAtMillis(System.currentTimeMillis() + durationSeconds * 1000L);

        
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        double healAmount = maxHealth * (pdm.healPercentOnAwaken() / 100.0);
        player.setHealth(Math.min(maxHealth, player.getHealth() + healAmount));

        
        plugin.getKitManager().swapToAwakenKit(player);
        player.sendMessage(pdm.message("awaken-activated"));
        pdm.refreshBossBar(player, data);

        
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.BLINDNESS);

        
        startGodAura(player);

        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && data.isAwakened()) {
                    endAwaken(player);
                }
            }
        }.runTaskLater(plugin, durationSeconds * 20L);
    }

    
    private void startGodAura(Player player) {
        if (activeAuras.containsKey(player.getUniqueId())) {
            activeAuras.get(player.getUniqueId()).cancel();
        }

        BukkitRunnable auraTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cancel();
                    activeAuras.remove(player.getUniqueId());
                    return;
                }
                
                World w = player.getWorld();
                Location eyeLoc = player.getEyeLocation();
                Vector dir = eyeLoc.getDirection().normalize().multiply(0.4);
                Vector cross = dir.clone().crossProduct(new Vector(0, 1, 0)).normalize().multiply(0.15);
                
                
                w.spawnParticle(Particle.DUST, eyeLoc.clone().add(dir).add(cross), 1, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(0, 255, 255), 0.8f));
                w.spawnParticle(Particle.DUST, eyeLoc.clone().add(dir).subtract(cross), 1, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(0, 255, 255), 0.8f));

                
                for (int i = 0; i < 2; i++) {
                    double x = player.getLocation().getX() + (Math.random() - 0.5) * 2;
                    double y = player.getLocation().getY() + (Math.random() * 2);
                    double z = player.getLocation().getZ() + (Math.random() - 0.5) * 2;
                    Color c = Math.random() > 0.5 ? Color.fromRGB(0, 150, 255) : Color.fromRGB(150, 0, 255);
                    w.spawnParticle(Particle.DUST, x, y, z, 1, 0, 0, 0, 0, new Particle.DustOptions(c, 1.2f));
                }
            }
        };

        auraTask.runTaskTimer(plugin, 0L, 2L);
        activeAuras.put(player.getUniqueId(), auraTask);
    }

    
    public void endAwaken(Player player) {
        var data = plugin.getPlayerDataManager().get(player);
        data.setAwakened(false);
        data.setAwakenBar(0, plugin.getPlayerDataManager().maxBar());
        data.setAwakenEndsAtMillis(0);

        plugin.getKitManager().revertToBaseKit(player);
        player.sendMessage(plugin.getPlayerDataManager().message("awaken-ended"));
        
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f, 1.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.5f, 0.5f);
        
        player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1, 0), 80, 0.6, 1.2, 0.6, 0.03);
        player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation().add(0, 1, 0), 30, 0.5, 1.0, 0.5, 0.01);

        if (activeAuras.containsKey(player.getUniqueId())) {
            activeAuras.get(player.getUniqueId()).cancel();
            activeAuras.remove(player.getUniqueId());
        }

        plugin.getPlayerDataManager().refreshBossBar(player, data);
    }
}
