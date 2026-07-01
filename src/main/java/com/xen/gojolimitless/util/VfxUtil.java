package com.xen.gojolimitless.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;


public final class VfxUtil {

    private VfxUtil() {}

    
    public static void dust(World world, Location loc, Color color, float size, int count) {
        Particle.DustOptions options = new Particle.DustOptions(color, size);
        world.spawnParticle(Particle.DUST, loc, count, 0, 0, 0, 0, options);
    }

    
    public static void ring(World world, Location center, double radius, int points, Particle particle, Object data) {
        double step = (2 * Math.PI) / points;
        for (int i = 0; i < points; i++) {
            double angle = step * i;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location p = new Location(world, x, center.getY(), z);
            spawn(world, particle, p, data);
        }
    }

    
    public static void sphere(World world, Location center, double radius, int latSteps, int lonSteps, Particle particle, Object data) {
        for (int i = 0; i <= latSteps; i++) {
            double lat = Math.PI * i / latSteps - Math.PI / 2;
            double y = radius * Math.sin(lat);
            double ringRadius = radius * Math.cos(lat);
            int thisRingPoints = Math.max(4, (int) (lonSteps * Math.cos(lat)));
            for (int j = 0; j < thisRingPoints; j++) {
                double lon = 2 * Math.PI * j / thisRingPoints;
                double x = ringRadius * Math.cos(lon);
                double z = ringRadius * Math.sin(lon);
                Location p = center.clone().add(x, y, z);
                spawn(world, particle, p, data);
            }
        }
    }

    
    public static void beam(World world, Location start, Location end, Particle particle, Object data) {
        Vector dir = end.toVector().subtract(start.toVector());
        double length = dir.length();
        if (length < 0.01) return;
        dir.normalize();
        double step = 0.3;
        for (double d = 0; d < length; d += step) {
            Location p = start.clone().add(dir.clone().multiply(d));
            spawn(world, particle, p, data);
        }
    }

    
    public static void helix(World world, Location base, double radius, double height, int turns, int pointsPerTurn, Particle particle, Object data) {
        int totalPoints = turns * pointsPerTurn;
        for (int strand = 0; strand < 2; strand++) {
            double offset = strand * Math.PI;
            for (int i = 0; i < totalPoints; i++) {
                double t = (double) i / totalPoints;
                double angle = t * turns * 2 * Math.PI + offset;
                double y = t * height;
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                Location p = base.clone().add(x, y, z);
                spawn(world, particle, p, data);
            }
        }
    }

    
    public static void burst(World world, Location center, double spread, int count, Particle particle, Object data) {
        world.spawnParticle(particle, center, count, spread, spread, spread, 0.05, data);
    }

    private static void spawn(World world, Particle particle, Location loc, Object data) {
        if (data != null) {
            world.spawnParticle(particle, loc, 1, 0, 0, 0, 0, data);
        } else {
            world.spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }
}
