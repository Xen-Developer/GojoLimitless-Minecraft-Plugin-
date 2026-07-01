package com.xen.gojolimitless.managers;

import com.xen.gojolimitless.GojoLimitlessPlugin;
import com.xen.gojolimitless.skills.Skill;
import com.xen.gojolimitless.skills.awaken.HollowPurpleNukeSkill;
import com.xen.gojolimitless.skills.awaken.HollowPurpleSkill;
import com.xen.gojolimitless.skills.awaken.InfiniteVoidSkill;
import com.xen.gojolimitless.skills.awaken.LapseBlueMaxSkill;
import com.xen.gojolimitless.skills.awaken.ReversalRedMaxSkill;
import com.xen.gojolimitless.skills.normal.LapseBlueSkill;
import com.xen.gojolimitless.skills.normal.LimitlessTeleportSkill;
import com.xen.gojolimitless.skills.normal.RapidPunchesSkill;
import com.xen.gojolimitless.skills.normal.ReversalRedSkill;
import com.xen.gojolimitless.skills.normal.TwofoldKickSkill;

import java.util.HashMap;
import java.util.Map;


public class SkillRegistry {

    private final GojoLimitlessPlugin plugin;
    private final Map<String, Skill> skills = new HashMap<>();

    public SkillRegistry(GojoLimitlessPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        register(new LapseBlueSkill(plugin));
        register(new ReversalRedSkill(plugin));
        register(new RapidPunchesSkill(plugin));
        register(new TwofoldKickSkill(plugin));
        register(new LimitlessTeleportSkill(plugin));

        register(new LapseBlueMaxSkill(plugin));
        register(new ReversalRedMaxSkill(plugin));
        register(new HollowPurpleSkill(plugin));
        register(new InfiniteVoidSkill(plugin));
        register(new HollowPurpleNukeSkill(plugin));
    }

    private void register(Skill skill) {
        skills.put(skill.getId(), skill);
    }

    public Skill get(String id) {
        return skills.get(id);
    }
}
