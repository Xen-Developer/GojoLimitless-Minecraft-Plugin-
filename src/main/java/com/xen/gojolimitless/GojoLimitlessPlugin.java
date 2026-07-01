package com.xen.gojolimitless;

import com.xen.gojolimitless.listeners.AwakenListener;
import com.xen.gojolimitless.listeners.CombatListener;
import com.xen.gojolimitless.listeners.PlayerJoinQuitListener;
import com.xen.gojolimitless.listeners.SkillCastListener;
import com.xen.gojolimitless.managers.AwakenManager;
import com.xen.gojolimitless.managers.DomainManager;
import com.xen.gojolimitless.managers.KitManager;
import com.xen.gojolimitless.managers.PlayerDataManager;
import com.xen.gojolimitless.managers.SkillRegistry;
import com.xen.gojolimitless.util.SkillItemFactory;
import org.bukkit.plugin.java.JavaPlugin;

public class GojoLimitlessPlugin extends JavaPlugin {

    private static GojoLimitlessPlugin instance;

    private PlayerDataManager playerDataManager;
    private KitManager kitManager;
    private SkillItemFactory skillItemFactory;
    private SkillRegistry skillRegistry;
    private DomainManager domainManager;
    private AwakenManager awakenManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.skillItemFactory = new SkillItemFactory(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.kitManager = new KitManager(this);
        this.domainManager = new DomainManager(this);
        this.skillRegistry = new SkillRegistry(this);
        this.awakenManager = new AwakenManager(this);

        
        skillRegistry.registerAll();

        
        getServer().getPluginManager().registerEvents(new SkillCastListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new AwakenListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(this), this);

        
        com.xen.gojolimitless.commands.GojoCommand command = new com.xen.gojolimitless.commands.GojoCommand(this);
        getCommand("gojo").setExecutor(command);
        getCommand("gojo").setTabCompleter(command);

        
        new com.xen.gojolimitless.managers.TickManager(this).start();

        getLogger().info("GojoLimitless enabled — Six Eyes online.");
    }

    @Override
    public void onDisable() {
        if (domainManager != null) {
            domainManager.revertAll();
        }
        if (playerDataManager != null) {
            playerDataManager.removeAll();
        }
        getLogger().info("GojoLimitless disabled — all domains safely reverted.");
    }

    public static GojoLimitlessPlugin getInstance() {
        return instance;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public SkillItemFactory getSkillItemFactory() {
        return skillItemFactory;
    }

    public SkillRegistry getSkillRegistry() {
        return skillRegistry;
    }

    public DomainManager getDomainManager() {
        return domainManager;
    }

    public AwakenManager getAwakenManager() {
        return awakenManager;
    }
}
