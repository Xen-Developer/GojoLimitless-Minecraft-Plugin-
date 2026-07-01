package com.xen.gojolimitless.managers;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


public class DomainManager {

    private final GojoLimitlessPlugin plugin;

    
    private final List<DomainSession> activeSessions = new ArrayList<>();

    public DomainManager(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isBlockSafeToModify(World world, Block block) {
        if (block.getType() == Material.BEDROCK) return false;
        int minY = world.getMinHeight();
        
        if (block.getY() <= minY + 1) return false;
        return true;
    }

    
    public DomainSession placeTemporary(World world, List<Block> targets, Material material, long durationTicks) {
        DomainSession session = new DomainSession();
        for (Block block : targets) {
            if (!isBlockSafeToModify(world, block)) continue;
            BlockData original = block.getBlockData().clone();
            session.placedBlocks.add(block.getLocation());
            session.originalData.add(original);
            block.setType(material, false);
        }
        activeSessions.add(session);

        session.revertTask = new BukkitRunnable() {
            @Override
            public void run() {
                revert(session);
            }
        }.runTaskLater(plugin, durationTicks);

        return session;
    }

    
    public void revert(DomainSession session) {
        if (session.reverted) return;
        session.reverted = true;
        for (int i = 0; i < session.placedBlocks.size(); i++) {
            var loc = session.placedBlocks.get(i);
            Block block = loc.getBlock();
            
            if (block.getType() == Material.BEDROCK) continue;
            block.setBlockData(session.originalData.get(i), false);
        }
        if (session.revertTask != null && !session.revertTask.isCancelled()) {
            session.revertTask.cancel();
        }
        activeSessions.remove(session);
    }

    
    public void revertAll() {
        Deque<DomainSession> snapshot = new ArrayDeque<>(activeSessions);
        for (DomainSession session : snapshot) {
            revert(session);
        }
    }

    public static class DomainSession {
        private final List<org.bukkit.Location> placedBlocks = new ArrayList<>();
        private final List<BlockData> originalData = new ArrayList<>();
        private boolean reverted = false;
        private BukkitTask revertTask;
    }
}
