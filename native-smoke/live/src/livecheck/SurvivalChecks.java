package livecheck;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.hullmods.campaignEffects.ExcessOPStripper;
import com.fs.starfarer.api.alcoholism.listeners.AlcoholConsumptionManager;
import com.fs.starfarer.api.alcoholism.memory.AddictionBrain;
import com.fs.starfarer.api.alcoholism.memory.Alcohol;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;

import java.util.ArrayList;
import java.util.List;

// 1.1.f survival mode and consumption changes. every step turns survival mode off again before it ends
public final class SurvivalChecks {
    private static final String OP_MOD = "live_check_op";
    private static final String ABSYNTH_FIT_TAG = "alcoholism_absynth_fit";

    private SurvivalChecks() {
    }

    private static MarketAPI jangala() {
        return Global.getSector().getEconomy().getMarket("jangala");
    }

    private static MarketAPI npcWith(String submarket) {
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!m.isPlayerOwned() && !m.isHidden() && m.hasSubmarket(submarket)) return m;
        }
        return null;
    }

    private static float tariff(MarketAPI market, String submarket) {
        return market.getSubmarket(submarket).getPlugin().getTariff();
    }

    private static int cargoWeapons() {
        int count = 0;
        for (CargoStackAPI stack : Kit.cargo().getStacksCopy()) if (stack.isWeaponStack()) count += (int) stack.getSize();
        return count;
    }

    private static int fittedWeapons(ShipVariantAPI v) {
        int count = 0;
        for (String slot : v.getFittedWeaponSlots()) if (!v.getSlot(slot).isBuiltIn()) count++;
        return count;
    }

    private static SpecialItemPlugin plugin(String itemId) {
        CargoAPI cargo = Global.getFactory().createCargo(true);
        cargo.addSpecial(new SpecialItemData(itemId, null), 1);
        return cargo.getStacksCopy().get(0).getPlugin();
    }

    private static float decay(boolean ext) {
        Kit.withdraw(AlcoholRepo.SUNSHINE, 0.5f);
        float before = Kit.drink(AlcoholRepo.SUNSHINE).getAddictionStatus().getAddictionValue();
        Kit.ext(ext);
        try {
            Kit.manager().onNewDay();
        } finally {
            Kit.ext(false);
        }
        return before - Kit.drink(AlcoholRepo.SUNSHINE).getAddictionStatus().getAddictionValue();
    }

    private static void freedomTariffs(Ctx c, String state, float mult) {
        MarketAPI open = jangala();
        MarketAPI military = npcWith(Submarkets.GENERIC_MILITARY);
        Kit.ext(true);
        try {
            c.near(tariff(open, Submarkets.SUBMARKET_OPEN), Global.getSettings().getFloat("ext_normalTariff") * mult, 0.001f, "freedom survival open market tariff " + state);
            if (military == null) c.note("freedom no npc military market found");
            else c.near(tariff(military, Submarkets.GENERIC_MILITARY), Global.getSettings().getFloat("ext_militaryMarketTariff") * mult, 0.001f,
                    "freedom survival military market tariff " + state + " at " + military.getId());
        } finally {
            Kit.ext(false);
        }
    }

    private static void floor(List<Step> steps) {
        steps.add(c -> {
            Kit.reset();
            float crew = AlcoholConsumptionManager.getPlayerSkeletonCrewAmount();
            float raw = AddictionBrain.getConsumptionPerCrewPerDayForAddiction(0f) * crew;
            c.note("floor skeleton crew " + crew + ", crew need " + raw + " units a day");
            c.near(AlcoholConsumptionManager.getRequiredUnitsPerDay(0f), Math.max(0.1f, raw), 0.0001f, "floor daily need is the crew need, at least 0.1");

            Alcohol a = Kit.drink(AlcoholRepo.STOUT);
            Kit.consume(a.getId(), 0f, 10f);
            float cargo = 10f;
            float opened = 0f;
            boolean whole = true;
            for (int day = 0; day < 3; day++) {
                float need = AlcoholConsumptionManager.getCurrentRequiredUnitsPerDay(a);
                float open = (float) Math.ceil(Math.max(0f, need - opened));
                cargo -= open;
                opened += open - need;
                Kit.manager().onNewDay();
                float left = Kit.cargo().getCommodityQuantity(a.getCommodityId());
                whole &= left == Math.round(left);
            }
            c.expect(whole, "buffer cargo holds whole units every day");
            c.near(Kit.cargo().getCommodityQuantity(a.getCommodityId()), cargo, 0.001f, "buffer cargo after 3 days");
            c.near(Kit.opened(a.getId()), opened, 0.001f, "buffer opened unit holds the rest");
            Kit.reset();
        });
    }

    private static void stock(List<Step> steps) {
        steps.add(c -> {
            MarketAPI market = jangala();
            SubmarketAPI open = market.getSubmarket(Submarkets.SUBMARKET_OPEN);
            int npc = 0;
            int own = 0;
            Kit.ext(true);
            try {
                for (Alcohol a : AlcoholRepo.ALCOHOL_MAP.values()) {
                    if (Math.abs(BaseSubmarketPlugin.getCommodityAvailabilityMult(market, open, a.getCommodityId()) - 0.35f) < 0.001f) npc++;
                }
                market.setPlayerOwned(true);
                for (Alcohol a : AlcoholRepo.ALCOHOL_MAP.values()) {
                    if (Math.abs(BaseSubmarketPlugin.getCommodityAvailabilityMult(market, open, a.getCommodityId()) - 0.65f) < 0.001f) own++;
                }
            } finally {
                market.setPlayerOwned(false);
                Kit.ext(false);
            }
            int all = AlcoholRepo.ALCOHOL_MAP.size();
            c.expect(npc == all, "stock survival npc markets at 35%, " + npc + " of " + all + " drinks");
            c.expect(own == all, "stock survival own colonies at 65%, " + own + " of " + all + " drinks");
            c.near(BaseSubmarketPlugin.getCommodityAvailabilityMult(market, open, Kit.drink(AlcoholRepo.STOUT).getCommodityId()), 1f, 0.001f, "stock normal mode keeps full stock");
        });
    }

    private static void withdrawal(List<Step> steps) {
        steps.add(c -> {
            Kit.reset();
            float normal = decay(false);
            float ext = decay(true);
            c.note("withdrawal one day: normal " + normal + ", survival " + ext);
            c.expect(normal > 0f, "withdrawal wears off in normal mode");
            c.near(ext, normal, 0.00001f, "withdrawal wears off at the same speed in survival");
            Kit.reset();
        });
    }

    private static void freedom(List<Step> steps) {
        steps.add(c -> {
            Kit.reset();
            Kit.consume(AlcoholRepo.FREEDOM, 1f, 500f);
            MarketAPI market = jangala();
            c.near(tariff(market, Submarkets.SUBMARKET_OPEN), market.getTariff().getModifiedValue(), 0.0001f, "freedom normal mode open market reads the market tariff");
            freedomTariffs(c, "while drinking", 0.8f);
            Kit.withdraw(AlcoholRepo.FREEDOM, 1f);
            freedomTariffs(c, "in withdrawal", 1.2f);
            Kit.reset();
            Kit.ext(true);
            try {
                c.near(tariff(market, Submarkets.SUBMARKET_OPEN), Global.getSettings().getFloat("ext_normalTariff"), 0.0001f, "freedom survival tariff untouched without the drink");
            } finally {
                Kit.ext(false);
            }
        });
    }

    private static void sunshineKing(List<Step> steps) {
        steps.add(c -> {
            Kit.reset();
            Kit.consume(AlcoholRepo.SUNSHINE, 1f, 500f);
            Kit.consume(AlcoholRepo.KING, 1f, 500f);
            Kit.manager().applyActiveAlcoholHullmods(Kit.fleet());
            Kit.manager().reapply();
            Kit.fleet().getFleetData().setSyncNeeded();
            Kit.fleet().getFleetData().syncIfNeeded();
            FleetMemberAPI m = Kit.fleet().getFleetData().getMembersListCopy().get(0);
            float sunshine = Kit.drink(AlcoholRepo.SUNSHINE).getAddictionStatus().getAddictionValue();
            float king = Kit.drink(AlcoholRepo.KING).getAddictionStatus().getAddictionValue();
            for (boolean ext : new boolean[]{false, true}) {
                String mode = ext ? "survival" : "normal mode";
                Kit.ext(ext);
                try {
                    m.updateStats();
                    MutableStat.StatMod fuel = m.getStats().getFuelUseMod().getMultBonus(AlcoholRepo.SUNSHINE);
                    MutableStat.StatMod crew = m.getStats().getCrewLossMult().getMultStatMod(AlcoholRepo.KING);
                    c.expect(fuel != null && crew != null, "sunshine and king mods present in " + mode);
                    if (fuel != null) c.near(fuel.getValue(), 1f - (ext ? 0.15f : 0.25f) * sunshine, 0.001f, "sunshine fuel use in " + mode);
                    if (crew != null) c.near(crew.getValue(), 1f - (ext ? 0.10f : 0.20f) * king, 0.001f, "king crew loss in " + mode);
                } finally {
                    Kit.ext(false);
                }
            }
            m.updateStats();
            Kit.reset();
        });
    }

    private static void recipes(List<Step> steps) {
        steps.add(c -> {
            MarketAPI market = npcWith(Submarkets.SUBMARKET_BLACK);
            c.expect(market != null && market.hasSubmarket(Submarkets.SUBMARKET_OPEN), "recipe market with open and black market");
            if (market == null || !market.hasSubmarket(Submarkets.SUBMARKET_OPEN)) return;
            SubmarketAPI open = market.getSubmarket(Submarkets.SUBMARKET_OPEN);
            SubmarketAPI black = market.getSubmarket(Submarkets.SUBMARKET_BLACK);
            SpecialItemPlugin recipe = plugin(Kit.drink(AlcoholRepo.STOUT).getIndustryItemId());
            float normalOpen = recipe.getPrice(market, open);
            float normalBlack = recipe.getPrice(market, black);
            float extOpen;
            float extBlack;
            Kit.ext(true);
            try {
                extOpen = recipe.getPrice(market, open);
                extBlack = recipe.getPrice(market, black);
            } finally {
                Kit.ext(false);
            }
            c.note("recipe prices at " + market.getId() + ": open " + normalOpen + " -> " + extOpen + ", black " + normalBlack + " -> " + extBlack);
            c.expect(normalOpen > 0f && normalBlack > 0f, "recipe has a price");
            if (normalOpen > 0f) c.near(extOpen / normalOpen, 0.3f, 0.01f, "recipe survival open market price");
            if (normalBlack > 0f) c.near(extBlack / normalBlack, 1f, 0.01f, "recipe survival black market price");
        });
    }

    private static void absynthBought(List<Step> steps) {
        steps.add(c -> {
            Kit.reset();
            FleetMemberAPI m = Kit.ship("hammerhead_Balanced");
            ShipVariantAPI v = m.getVariant();
            v.setNumFluxVents(0);
            v.setNumFluxCapacitors(0);
            for (String mod : new ArrayList<>(v.getNonBuiltInHullmods())) v.removeMod(mod);
            v.addTag(ABSYNTH_FIT_TAG);
            MutableCharacterStatsAPI stats = Global.getSector().getPlayerPerson().getStats();
            stats.getShipOrdnancePointBonus().modifyFlat(OP_MOD, -(Kit.unusedOp(m) + 10));
            try {
                c.expect(Kit.unusedOp(m) < 0, "absynth bought ship setup over the op limit");
                int fitted = fittedWeapons(v);
                int inCargo = cargoWeapons();
                MarketAPI market = jangala();
                PlayerMarketTransaction tx = new PlayerMarketTransaction(market, market.getSubmarket(Submarkets.SUBMARKET_OPEN), CoreUITradeMode.OPEN);
                tx.getShipsBought().add(new PlayerMarketTransaction.ShipSaleInfo(m, 0f));
                new ExcessOPStripper().reportPlayerMarketTransaction(tx);
                int stripped = fitted - fittedWeapons(v);
                c.expect(Kit.unusedOp(m) >= 0, "absynth bought ship stripped to its op limit");
                c.expect(stripped > 0, "absynth bought ship lost weapons, " + stripped);
                c.expect(cargoWeapons() - inCargo == stripped, "absynth stripped weapons in cargo, " + (cargoWeapons() - inCargo) + " of " + stripped);
            } finally {
                stats.getShipOrdnancePointBonus().unmodify(OP_MOD);
                Kit.fleet().getFleetData().removeFleetMember(m);
                Kit.reset();
            }
        });
    }

    private static void qaras(List<Step> steps) {
        steps.add(c -> {
            MarketAPI qaras = Global.getSector().getEconomy().getMarket("qaras");
            c.expect(qaras != null, "qaras market exists");
            if (qaras == null) return;
            int spools = 0;
            for (Industry ind : qaras.getIndustries()) {
                if (ind.getSpecialItem() != null && Items.FULLERENE_SPOOL.equals(ind.getSpecialItem().getId())) spools++;
            }
            c.expect(spools == 0, "qaras has no fullerene spool on a new game, got " + spools);
        });
    }

    public static List<Step> steps() {
        List<Step> steps = new ArrayList<>();
        floor(steps);
        stock(steps);
        withdrawal(steps);
        freedom(steps);
        sunshineKing(steps);
        recipes(steps);
        absynthBought(steps);
        qaras(steps);
        return steps;
    }
}
