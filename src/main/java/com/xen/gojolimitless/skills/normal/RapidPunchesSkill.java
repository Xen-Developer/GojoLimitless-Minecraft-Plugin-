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


public class RapidPunchesSkill implements Skill {

    private final GojoLimitlessPlugin plugin;

    public RapidPunchesSkill(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "rapid_punches";
    }

    @Override
    public String getCooldownConfigKey() {
        return "rapid-punches";
    }

    @Override
    public boolean cast(Player caster) {
        LivingEntity target = TargetUtil.getTargetEntity(caster, 4.0);
        if (target == null) {
            caster.sendMessage(plugin.getPlayerDataManager().message("no-target"));
            return false;
        }

        var world = caster.getWorld();
        double perHit = plugin.getConfig().getDouble("damage.rapid-punches-per-hit", 2.5);
        int hitCount = plugin.getConfig().getInt("damage.rapid-punches-hit-count", 5);

        new BukkitRunnable() {
            int hitsLanded = 0;

            @Override
            public void run() {
                if (hitsLanded >= hitCount || !target.isValid() || target.isDead()
                        || caster.getLocation().distance(target.getLocation()) > 5.5) {
                    cancel();
                    return;
                }

                Location hitLoc = target.getLocation().add(0, 1.1, 0);
                float pitch = 1.0f + (hitsLanded * 0.08f);
                world.playSound(hitLoc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, pitch);
                world.spawnParticle(Particle.CRIT, hitLoc, 8, 0.3, 0.3, 0.3, 0.2);
                world.spawnParticle(Particle.SWEEP_ATTACK, hitLoc, 1, 0.2, 0.1, 0.2, 0);

                target.damage(perHit, caster);
                Vector tinyKnock = target.getLocation().toVector().subtract(caster.getLocation().toVector());
                tinyKnock.setY(0.05);
                tinyKnock.normalize().multiply(0.25);
                target.setVelocity(target.getVelocity().add(tinyKnock));

                hitsLanded++;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        return true;
    }
}
