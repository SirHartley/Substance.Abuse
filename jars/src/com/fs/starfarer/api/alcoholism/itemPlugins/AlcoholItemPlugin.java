package com.fs.starfarer.api.alcoholism.itemPlugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.listeners.AlcoholConsumptionManager;
import com.fs.starfarer.api.alcoholism.memory.AddictionStatus;
import com.fs.starfarer.api.alcoholism.memory.AlcoholAPI;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.alcoholism.scripts.IntervalTracker;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class AlcoholItemPlugin extends BaseSpecialItemPlugin {

    private transient SpriteAPI frame;
    private transient SpriteAPI mask;

    @Override
    public void init(CargoStackAPI stack) {
        super.init(stack);
        frame = Global.getSettings().getSprite("alcohol", "frame");
        mask = Global.getSettings().getSprite("alcohol", "bg");
    }

    @Override
    public int getPrice(MarketAPI market, SubmarketAPI submarket) {
        return super.getPrice(market, submarket);
    }

    @Override
    public String getDesignType() {
        return null;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
        //super.createTooltip(tooltip, expanded, transferHandler, stackSource);

        float pad = 3f;
        float opad = 10f;
        float small = 5f;
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color b = Misc.getButtonTextColor();
        b = Misc.getPositiveHighlightColor();

        AlcoholAPI alcohol = AlcoholRepo.get(getId());
        FactionAPI faction = alcohol.getFaction();

        //title and origin
        tooltip.addTitle(getName(), faction.getColor());
        tooltip.addPara( Misc.ucFirst(faction.getPersonNamePrefixAOrAn()) + " " + Misc.ucFirst(faction.getDisplayName()) + " original", Misc.getGrayColor(), pad);

        tooltip.addPara(Global.getSettings().getDescription(alcohol.getCommodityId(), Description.Type.RESOURCE).getText1(), 10f);
        alcohol.addStatusTooltip(tooltip, false);
        alcohol.addEffectTooltip(tooltip, false);

        addCostLabel(tooltip, opad, transferHandler, stackSource);

        if(alcohol.getAddictionStatus().isConsuming()) tooltip.addPara("Right-click to stop distribution", b, opad);
        else tooltip.addPara("Right-click to distribute", b, opad);
    }

    @Override
    public void render(float x, float y, float w, float h, float alphaMult, float glowMult, SpecialItemRendererAPI renderer) {
        if(!isActive()) return;

        final SpriteAPI sprite = frame;
        final SpriteAPI mask = this.mask;
        float cx = x + w / 2f;
        float cy = y + h / 2f;
        float secondFraction = IntervalTracker.getInstance().getIntervalFraction();

        if (sprite != null) {
            Color baseColor = Misc.getBasePlayerColor();
            sprite.setSize(90f,90f);

            //sprite render
            sprite.setColor(baseColor);
            sprite.setNormalBlend();
            sprite.setAlphaMult(alphaMult * 0.8f);
            sprite.renderAtCenter(cx, cy);

            // mask
            GL11.glColorMask(false, false, false, true);
            GL11.glPushMatrix();
            GL11.glTranslatef(cx, cy, 0);
            Misc.renderQuadAlpha(x * 3f, y * 3f, w * 3f, h * 3f, Misc.zeroColor, 0f);
            GL11.glPopMatrix();
            sprite.setBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);
            sprite.renderAtCenter(cx, cy);

            mask.setAlphaMult(alphaMult * 0.9f);
            mask.setAngle(-secondFraction * 90f);
            mask.setBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_ALPHA);
            mask.renderAtCenter(cx, cy);

            GL11.glColorMask(true, true, true, false);
            mask.setBlendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);
            mask.renderAtCenter(cx, cy);
        }
    }

    @Override
    public boolean hasRightClickAction() {
        return true;
    }

    @Override
    public boolean shouldRemoveOnRightClickAction() {
        return false;
    }

    @Override
    public void performRightClickAction() {
        if(isActive()) {
            Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
                    "Stopping " + getName() + " distribution. Beware of withdrawal!");//,
            setConsuming(false);
        }
        else {
            Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
                    "Distributing " + getName() + " to the crews.");//,
            setConsuming(true);
        }

        Global.getSoundPlayer().playUISound(getSpec().getSoundId(), 1f, 1f);
    }

    private boolean isActive(){
        return AlcoholRepo.get(getId()).getAddictionStatus().isConsuming();
    }

    private void setConsuming(boolean consuming){
        AddictionStatus status = AlcoholRepo.get(getId()).getAddictionStatus();
        status.setConsuming(consuming);

        if(consuming) AlcoholConsumptionManager.getInstanceOrRegister().applHullmodToFleet(getId());
        else if(!status.isAddicted()) AlcoholConsumptionManager.getInstanceOrRegister().unapplyHullmodFromFleet(getId());
    }
}
