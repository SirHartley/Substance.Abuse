package com.fs.starfarer.api.alcoholism.memory;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface AlcoholAPI {
    String getId();
    String getCommodityId();
    String getName();
    String getEffectHullmodId();
    FactionAPI getFaction();
    float getMult();
    float incrementAddiction(float days);
    AddictionStatus getAddictionStatus();
    void addEffectTooltip(TooltipMakerAPI tt, boolean forHullmod);
    void addStatusTooltip(TooltipMakerAPI tt);
}
