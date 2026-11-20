package com.fs.starfarer.api.alcoholism.patches;

import com.fs.starfarer.api.impl.campaign.submarkets.OpenMarketPlugin;
import patchlib.api.context.AfterContext;
import patchlib.api.match.ClassMatch;
import patchlib.api.match.MethodMatch;
import patchlib.api.patch.After;
import patchlib.api.patch.Patch;

@Patch(target = @ClassMatch(subtype = OpenMarketPlugin.class))
public class OpenMarketTariffPatch {

    @After(target = @MethodMatch(methodName = "getTariff", parameterCount = 0))
    public static void afterGetTariff(AfterContext context) {
        SurvivalTariff.apply(context);
    }
}
