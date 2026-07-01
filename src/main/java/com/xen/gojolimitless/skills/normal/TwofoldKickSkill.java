package com.xen.gojolimitless.skills.normal;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import com.xen.gojolimitless.skills.Skill;
import com.xen.gojolimitless.util.TargetUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


public class TwofoldKickSkill implements Skill {

    private final GojoLimitlessPlugin plugin;

    public TwofoldKickSkill(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "twofold_kick";
    }

    @Override
    public String getCooldownConfigKey() {
        return "twofold-kick";
    }

    @Override
    public boolean cast(Player caster) {
        LivingEntity target = TargetUtil.getTargetEntity(caster, 4.5);
        if (target == null) {
            caster.sendMessage(plugin.getPlayerDataManager().message("no-target"));
            return false;
        }

        var world = caster.getWorld();
        double hit1 = plugin.getConfig().getDouble("damage.twofold-kick-hit1", 4.0);
        double hit2 = plugin.getConfig().getDouble("damage.twofold-kick-hit2", 6.0);

        
        Location launchLoc = target.getLocation().add(0, 0.5, 0);
        world.playSound(launchLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.3f);
        world.spawnParticle(Particle.CLOUD, launchLoc, 20, 0.3, 0.1, 0.3, 0.15);
        world.spawnParticle(Particle.CRIT, launchLoc, 15, 0.3, 0.2, 0.3, 0.2);

        target.damage(hit1, caster);
        target.setVelocity(new Vector(0, 1.1, 0));

        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!target.isValid() || target.isDead()) return;

                Location slamStart = target.getLocation().add(0, 1, 0);
                world.playSound(slamStart, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 1.0f, 1.2f);
                world.spawnParticle(Particle.DRAGON_BREATH, slamStart, 18, 0.2, 0.2, 0.2, 0.05);

                target.damage(hit2, caster);
                target.setVelocity(new Vector(0, -1.4, 0));

                
                
                new BukkitRunnable() {
                    int tick = 0;

                    @Override
                    public void run() {
                        tick++;
                        if (!target.isValid() || target.isDead() || tick > 14) {
                            cancel();
                            return;
                        }
                        world.spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 4, 0.15, 0.15, 0.15, 0.05);

                        if (target.isOnGround()) {
                            Location impact = target.getLocation();
                            world.playSound(impact, Sound.ENTITY_GENERIC_BIG_FALL, 1.0f, 1.0f);
                            world.spawnParticle(Particle.EXPLOSION, impact, 1);
                            world.spawnParticle(Particle.BLOCK_CRUMBLE, impact, 25, 0.5, 0.1, 0.5, 0.1,
                                    impact.getBlock().getRelative(0, -1, 0).getBlockData());
                            
                            target.setVelocity(new Vector(0, 0.35, 0));
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
        }.runTaskLater(plugin, 8L);

        return true;
    }
}
