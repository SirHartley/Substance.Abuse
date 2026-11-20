package com.fs.starfarer.api.alcoholism.hullmods.campaignEffects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.hullmods.alcoholEffectHullmods.Freedom_HullmodEffect;
import com.fs.starfarer.api.alcoholism.memory.AddictionStatus;
import com.fs.starfarer.api.alcoholism.memory.Alcohol;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI;


public class TariffModifier extends BaseCampaignEventListener {

    public TariffModifier() {
        super(false);
    }

    private String alcoholId = AlcoholRepo.FREEDOM;

    public static void register(){
        for (CampaignEventListener l : Global.getSector().getAllListeners()){
            if(l instanceof TariffModifier) return;
        }

        TariffModifier script = new TariffModifier();
        Global.getSector().addTransientListener(script);
    }

    @Override
    public void reportPlayerOpenedMarket(MarketAPI market) {
        super.reportPlayerOpenedMarket(market);

        if(market.isPlayerOwned()) return;

        Alcohol alcohol = AlcoholRepo.get(alcoholId);
        AddictionStatus status = alcohol.getAddictionStatus();

        if(status.isConsuming()) market.getTariff().modifyMult(alcohol.getId(), getTariffMult(), alcohol.getName());
        else if (status.isWithdrawal()) market.getTariff().modifyMult(alcohol.getId(), getTariffMult(), alcohol.getName() + " withdrawal");
    }

    public static float getTariffMult(){
        AddictionStatus status = AlcoholRepo.get(AlcoholRepo.FREEDOM).getAddictionStatus();

        if(status.isConsuming()) return 1 + ((Freedom_HullmodEffect.MAX_TARIFF_DECREASE_PERCENT * status.getAddictionValue()) / 100f);
        if(status.isWithdrawal()) return 1 + ((Freedom_HullmodEffect.WITHDRAWAL_TARIFF_INCREASE_PERCENT * status.getAddictionValue()) / 100f);
        return 1f;
    }

    @Override
    public void reportPlayerClosedMarket(MarketAPI market) {
        super.reportPlayerClosedMarket(market);
        if(market.isPlayerOwned()) return;

        market.getTariff().unmodify(alcoholId);
    }
}
