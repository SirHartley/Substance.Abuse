package livecheck;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.hullmods.BaseAlcoholHullmodEffect;
import com.fs.starfarer.api.alcoholism.memory.Alcohol;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.impl.codex.CodexEntryPlugin;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// changelog rows 21 to 23
public final class CodexChecks {
    private CodexChecks() {
    }

    private static TooltipMakerAPI tooltip() {
        CustomPanelAPI panel = Global.getSettings().createCustom(500f, 800f, null);
        return panel.createUIElement(500f, 800f, false);
    }

    private static BaseAlcoholHullmodEffect effect(String id) {
        return (BaseAlcoholHullmodEffect) Global.getSettings().getHullModSpec(id).getEffect();
    }

    private static float height(String id) {
        TooltipMakerAPI tt = tooltip();
        effect(id).addPostDescriptionSection(tt, ShipAPI.HullSize.CRUISER, null, 480f, true);
        return tt.getHeightSoFar();
    }

    private static String lastLine(TooltipMakerAPI tt) {
        UIComponentAPI last = tt.getPrev();
        return last instanceof LabelAPI ? ((LabelAPI) last).getText() : String.valueOf(last);
    }

    private static void row21(Ctx c) {
        for (Alcohol a : AlcoholRepo.ALCOHOL_MAP.values()) {
            String id = a.getId();
            HullModSpecAPI spec = Global.getSettings().getHullModSpec(a.getEffectHullmodId());
            c.expect(spec.hasTag("codex_require_related"), "row21 " + id + " hullmod has codex_require_related");

            CodexEntryPlugin entry = CodexDataV2.getEntry(CodexDataV2.getHullmodEntryId(a.getEffectHullmodId()));
            Set<String> related = new HashSet<>();
            if (entry != null) for (CodexEntryPlugin r : entry.getRelatedEntries()) related.add(r.getId());
            c.expect(related.contains(CodexDataV2.getCommodityEntryId(a.getCommodityId())), "row21 " + id + " links the commodity");
            c.expect(related.contains(CodexDataV2.getItemEntryId(id)), "row21 " + id + " links the item");
            c.expect(related.contains(CodexDataV2.getItemEntryId(a.getIndustryItemId())), "row21 " + id + " links the recipe");

            float normal = height(id);
            Global.CODEX_TOOLTIP_MODE = true;
            try {
                float zero = height(id);
                Kit.addiction(id, 1f);
                float full = height(id);
                Kit.addiction(id, 0f);
                c.near(zero, full, 0.5f, "row21 " + id + " codex tooltip same at addiction 0 and 1");
                c.expect(zero > normal, "row21 " + id + " codex tooltip " + zero + " above normal " + normal);
            } finally {
                Global.CODEX_TOOLTIP_MODE = false;
            }
        }
    }

    public static List<Step> steps() {
        List<Step> steps = new ArrayList<>();
        steps.add(c -> {
            Kit.reset();
            row21(c);
            c.shot("codex");
        });
        steps.add(c -> {
            TooltipMakerAPI tt = tooltip();
            effect(AlcoholRepo.FUEL).addWithdrawalEffectTooltip(tt, 0.9f);
            String line = lastLine(tt);
            c.expect(line.contains("Reduces max. burn by 2") && !line.contains("-"), "row22 burn line: " + line);
        });
        steps.add(c -> {
            TooltipMakerAPI tt = tooltip();
            effect(AlcoholRepo.WATER).addPositiveEffectTooltip(tt, 1f);
            String line = lastLine(tt);
            c.expect(line.contains("1.5x") && !line.contains("2x"), "row23 hangover cure line: " + line);
        });
        return steps;
    }
}
