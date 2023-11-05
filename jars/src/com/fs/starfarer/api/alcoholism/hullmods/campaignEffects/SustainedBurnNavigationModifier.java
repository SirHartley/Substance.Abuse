package com.fs.starfarer.api.alcoholism.hullmods.campaignEffects;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.hullmods.alcoholEffectHullmods.Fuel_HullmodEffect;
import com.fs.starfarer.api.alcoholism.memory.AddictionStatus;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.impl.campaign.abilities.SustainedBurnAbility;

import java.awt.*;

public class SustainedBurnNavigationModifier extends BaseCampaignEventListener implements EveryFrameScript {

    private AbilityPlugin ability = null;
    private boolean active = false;

    public static void register(){
        for (CampaignEventListener l : Global.getSector().getAllListeners()){
            if(l instanceof SustainedBurnNavigationModifier) return;
        }

        SustainedBurnNavigationModifier script = new SustainedBurnNavigationModifier();
        Global.getSector().addTransientListener(script);
        Global.getSector().addTransientScript(script);
    }

    public SustainedBurnNavigationModifier() {
        super(false);
    }

    @Override
    public void reportPlayerActivatedAbility(AbilityPlugin ability, Object param) {
        super.reportPlayerActivatedAbility(ability, param);

        AddictionStatus status = AlcoholRepo.get(getModId()).getAddictionStatus();

        if(status.isConsuming()){
            if(ability instanceof SustainedBurnAbility){
                this.active = true;
                this.ability = ability;
                CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
                fleet.getStats().getAccelerationMult().modifyMult(getModId(),  1 + (Fuel_HullmodEffect.MAX_SBURN_MANEUV_IMPROVE_MULT * status.getAddictionValue()));

                for (FleetMemberViewAPI view : fleet.getViews()) {
                    view.getContrailColor().shift(getModId(), new Color(255 - (int) Math.round(135 * status.getAddictionValue()),150,100,255), 1f, 1f, .75f);
                    view.getEngineGlowSizeMult().shift(getModId(), 2f, 1f, 1f, 1f);
                    view.getEngineHeightMult().shift(getModId(), 5f, 1f, 1f, 1f);
                    view.getEngineWidthMult().shift(getModId(), 3f, 1f, 1f, 1f);
                }
            }
        }
    }

    @Override
    public void reportPlayerDeactivatedAbility(AbilityPlugin ability, Object param) {
        super.reportPlayerDeactivatedAbility(ability, param);
        unapply();
    }

    public void unapply(){
        if(ability != null && ability instanceof SustainedBurnAbility){
            active = false;
            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            fleet.getStats().getAccelerationMult().unmodify(getModId());
        }
    }

    public static String getModId(){
        return AlcoholRepo.FUEL;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    //this is just insurance because I don't trust alex to always call reportPlayerDeactivatedAbility
    @Override
    public void advance(float amount) {
        if(ability != null && !ability.isActive() && active){
            unapply();
        }
    }
}
