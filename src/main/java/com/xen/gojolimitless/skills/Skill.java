package com.xen.gojolimitless.skills;

import org.bukkit.entity.Player;


public interface Skill {

    
    String getId();

    
    String getCooldownConfigKey();

    
    boolean cast(Player caster);
}
