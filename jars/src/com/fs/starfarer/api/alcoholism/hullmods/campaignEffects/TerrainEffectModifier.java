package com.fs.starfarer.api.alcoholism.hullmods.campaignEffects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.hullmods.alcoholEffectHullmods.Freedom_HullmodEffect;
import com.fs.starfarer.api.alcoholism.listeners.NewDayListener;
import com.fs.starfarer.api.alcoholism.memory.AddictionStatus;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.DynamicStatsAPI;

public class TerrainEffectModifier implements NewDayListener {

    private String alcoholId = AlcoholRepo.FREEDOM;

    //transient
    public static void register() {
        ListenerManagerAPI manager = Global.getSector().getListenerManager();
        if(!manager.hasListenerOfClass(TerrainEffectModifier.class)) {
            manager.addListener(new TerrainEffectModifier(), true);
        }
    }
    
    @Override
    public void onNewDay() {
        AddictionStatus status = AlcoholRepo.get(alcoholId).getAddictionStatus();

        DynamicStatsAPI stats = Global.getSector().getPlayerPerson().getStats().getDynamic();
        stats.getMod(Stats.NAVIGATION_PENALTY_MULT).unmodify(alcoholId);

        if(status.isAddicted()){
            stats.getMod(Stats.NAVIGATION_PENALTY_MULT).modifyFlat(alcoholId, (Freedom_HullmodEffect.NEG_TERRAIN_EFFECT_INCREASE * status.getAddictionValue()) / 100f, AlcoholRepo.get(alcoholId).getName());
        }
    }
}
