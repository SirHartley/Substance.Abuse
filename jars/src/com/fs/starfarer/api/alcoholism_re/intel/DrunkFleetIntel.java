package com.fs.starfarer.api.alcoholism_re.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.misc.FleetLogIntel;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.Set;

public class DrunkFleetIntel extends FleetLogIntel {

    public static final String MEMORY_KEY_PRE = "$DrunkFleetAlive_";

    protected CampaignFleetAPI fleet;

    public DrunkFleetIntel(CampaignFleetAPI fleet) {
        this.fleet = fleet;
        this.important = true;
    }

    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3f;
        float opad = 10f;

        bullet(info);
        info.addPara("Find back to your main fleet.", pad);
        unindent(info);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float small = 3f;
        float opad = 10f;

        //info.addSectionHeading("Accidental Separation", Alignment.MID, opad);

        info.addPara("You got separated from your fleet during a party.", opad);
        info.addPara("Head there to merge with it.", opad);
        info.addPara("", opad);
        info.addPara("This was caused by consuming too much alcohol at once.", opad);

        //addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_IMPORTANT);
        tags.add("player");
        tags.add(Tags.INTEL_FLEET_LOG);
        return tags;
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("alcohol", "intel");
    }

    public String getName() {
        return "Drunken Fleet";
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return fleet;
    }

    @Override
    public boolean shouldRemoveIntel() {
        return canRemove();
    }

    private boolean canRemove(){
        return !Global.getSector().getMemoryWithoutUpdate().getBoolean(MEMORY_KEY_PRE + fleet.getId());
    }
}
