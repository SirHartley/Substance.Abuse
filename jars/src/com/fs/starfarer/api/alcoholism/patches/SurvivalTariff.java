package com.fs.starfarer.api.alcoholism.patches;

import com.fs.starfarer.api.alcoholism.hullmods.campaignEffects.TariffModifier;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.util.Misc;
import patchlib.api.context.AfterContext;

// survival getTariff overrides return flat ext_ values and skip market.getTariff(), which TariffModifier edits in normal mode
public class SurvivalTariff {

    public static void apply(AfterContext context) {
        if (!context.isMostDerivedCall() || !Misc.isExtended()) return;

        MarketAPI market = ((BaseSubmarketPlugin) context.getSelf()).getMarket();
        if (market == null || market.isPlayerOwned()) return;

        float mult = TariffModifier.getTariffMult();
        if (mult == 1f) return;

        float tariff = context.getInferredReturnValue();
        context.setReturnValue(tariff * mult);
    }
}
