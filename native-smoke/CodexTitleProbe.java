import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.hullmods.BaseAlcoholHullmodEffect;
import com.fs.starfarer.api.alcoholism.memory.Alcohol;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

// changelog row 20: the tooltip entry points the title screen codex uses, no campaign and no player fleet
public class CodexTitleProbe extends BaseModPlugin {
    @Override
    public void onApplicationLoad() {
        NativeSmokeSupport.run(CodexTitleProbe::probe);
    }

    private static void probe() throws Exception {
        CustomPanelAPI root = Global.getSettings().createCustom(600f, 800f, null);
        boolean mounted = false;
        Global.CODEX_TOOLTIP_MODE = true;
        try {
            for (Alcohol a : AlcoholRepo.ALCOHOL_MAP.values()) {
                TooltipMakerAPI tt = root.createUIElement(500f, 800f, false);
                a.addStatusTooltip(tt, false);
                a.addEffectTooltip(tt, false);
                NativeSmokeSupport.expect(tt.getHeightSoFar() > 0f, a.getId() + " item tooltip");

                BaseAlcoholHullmodEffect effect = (BaseAlcoholHullmodEffect) Global.getSettings().getHullModSpec(a.getEffectHullmodId()).getEffect();
                TooltipMakerAPI hullmod = root.createUIElement(500f, 800f, false);
                effect.addPostDescriptionSection(hullmod, ShipAPI.HullSize.CRUISER, null, 480f, true);
                NativeSmokeSupport.expect(hullmod.getHeightSoFar() > 0f, a.getId() + " hullmod tooltip");
                if (!mounted) root.addUIElement(hullmod).inTL(0f, 0f);
                mounted = true;
            }
        } finally {
            Global.CODEX_TOOLTIP_MODE = false;
        }
        NativeSmokeSupport.beginFrame();
        NativeSmokeSupport.advance(root, 30);
        NativeSmokeSupport.render(root);
        NativeSmokeSupport.screenshot("codex-title");
    }
}
