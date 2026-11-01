package com.fs.starfarer.api.alcoholism.hullmods.campaignEffects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.listeners.AlcoholConsumptionManager;
import com.fs.starfarer.api.alcoholism.listeners.NewDayListener;
import com.fs.starfarer.api.alcoholism.memory.AddictionStatus;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.alcoholism.memory.SettingsHandler;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import java.util.ArrayList;

// strips only ships tagged on a refit save while they carried the absynth hullmod
public class ExcessOPStripper implements NewDayListener, ColonyInteractionListener {
    public static final String HAS_SHOWN_WARNING_KEY = "$AlcoholWarningHasBeenShow";
    public static final String FITTED_ON_ABSYNTH_TAG = "alcoholism_absynth_fit";

    public static void register(){
        ListenerManagerAPI m = Global.getSector().getListenerManager();

        if(SettingsHandler.getInstance().adjustOP){
            if(m.hasListenerOfClass(ExcessOPStripper.class)) return;
            m.addListener( new ExcessOPStripper());
        } else m.removeListenerOfClass(ExcessOPStripper.class);
    }

    public int countdown = 7;
    // false on saves from before the tag existed
    public boolean taggedExistingFits = false;

    public ExcessOPStripper(){
    }

    public static void tagIfFittedOnAbsynth(ShipVariantAPI variant){
        if(variant.hasHullMod(getAbsynthHullmodId())) variant.addTag(FITTED_ON_ABSYNTH_TAG);
    }

    @Override
    public void onNewDay() {
        AddictionStatus status = AlcoholRepo.get(AlcoholRepo.ABSYNTH).getAddictionStatus();

        if(!taggedExistingFits){
            for (FleetMemberAPI m : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) tagIfFittedOnAbsynth(m.getVariant());
            taggedExistingFits = true;
        }

        if(status.isWithdrawal()) {
            showMessageIfNeeded();
            countdown--;
        } else if (status.isConsuming()) countdown = 7;

        if(!status.isAddicted() || countdown < 0){
            correctPlayerFleetOP();
        }
    }

    @Override
    public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
        AddictionStatus status = AlcoholRepo.get(AlcoholRepo.ABSYNTH).getAddictionStatus();
        if(status.isWithdrawal() || !status.isAddicted()){
            MutableCharacterStatsAPI stats = Global.getSector().getPlayerPerson().getStats();
            boolean toCargo = transaction.getSubmarket().getPlugin().isFreeTransfer();

            for (PlayerMarketTransaction.ShipSaleInfo info : transaction.getShipsSold()){
                if(correctTradedMember(info.getMember(), stats, toCargo, status, false)) info.getMember().updateStats();
            }

            boolean corrected = false;
            for (PlayerMarketTransaction.ShipSaleInfo info : transaction.getShipsBought()){
                if(correctTradedMember(info.getMember(), stats, toCargo, status, true)) corrected = true;
            }

            if(corrected) AlcoholConsumptionManager.syncFleetStats(Global.getSector().getPlayerFleet());
        }
    }

    // stored ships keep the absynth hullmod, so on bought ships it marks fits stored before the tag existed
    private boolean correctTradedMember(FleetMemberAPI m, MutableCharacterStatsAPI stats, boolean toCargo, AddictionStatus status, boolean isBought){
        ShipVariantAPI var = m.getVariant();
        if(!isFittedOnAbsynth(var) && !(isBought && hasAbsynthHullmod(var))) return false;

        // removing the mod also clears the variant's cached op cost stats, which can still hold the discount
        if(!status.isAddicted()) removeAbsynthHullmod(var);

        boolean corrected = correctMember(m, stats, toCargo);
        if(!status.isAddicted()) untag(var);

        return corrected;
    }

    public void correctPlayerFleetOP(){
        MutableCharacterStatsAPI stats = Global.getSector().getPlayerPerson().getStats();
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        boolean isAddicted = AlcoholRepo.get(AlcoholRepo.ABSYNTH).getAddictionStatus().isAddicted();

        boolean notify = false;

        for (FleetMemberAPI m : fleet.getFleetData().getMembersListCopy()){
            ShipVariantAPI var = m.getVariant();
            if(!isFittedOnAbsynth(var)) continue;

            if(correctMember(m, stats, true)) notify = true;
            if(!isAddicted) untag(var);
        }

        if (notify) {
            AlcoholConsumptionManager.syncFleetStats(fleet);
            Global.getSector().getCampaignUI().addMessage("Some of your ship loadouts have been changed to account for OP deficits.");
        }
    }

    public boolean correctMember(FleetMemberAPI m, MutableCharacterStatsAPI stats, boolean toCargo){
        ShipVariantAPI var = m.getVariant();
        boolean corrected;
        corrected = correctVariant(var, stats, toCargo);

        for (String s : var.getModuleSlots()){
            boolean corr;
            corr = correctVariant(AlcoholConsumptionManager.getEditableModuleVariant(var, s), stats, toCargo);

            if(corr) corrected = corr;
        }

        return corrected;
    }

    private static String getAbsynthHullmodId(){
        return AlcoholRepo.get(AlcoholRepo.ABSYNTH).getEffectHullmodId();
    }

    private static boolean isFittedOnAbsynth(ShipVariantAPI var){
        if(var.hasTag(FITTED_ON_ABSYNTH_TAG)) return true;

        for (String s : var.getModuleSlots()){
            if(var.getModuleVariant(s).hasTag(FITTED_ON_ABSYNTH_TAG)) return true;
        }

        return false;
    }

    private static boolean hasAbsynthHullmod(ShipVariantAPI var){
        return var.hasHullMod(getAbsynthHullmodId());
    }

    private static void removeAbsynthHullmod(ShipVariantAPI var){
        String id = getAbsynthHullmodId();

        for (String s : var.getModuleSlots()){
            ShipVariantAPI module = AlcoholConsumptionManager.getEditableModuleVariant(var, s);
            module.removeMod(id);
            module.removePermaMod(id);
        }

        var.removeMod(id);
        var.removePermaMod(id);
    }

    private static void untag(ShipVariantAPI var){
        var.removeTag(FITTED_ON_ABSYNTH_TAG);

        for (String s : var.getModuleSlots()){
            var.getModuleVariant(s).removeTag(FITTED_ON_ABSYNTH_TAG);
        }
    }

    public boolean correctVariant(ShipVariantAPI var, MutableCharacterStatsAPI stats, boolean toCargo){
        int availableOP = var.getUnusedOP(stats); //if negative we are over OP limit
        if(availableOP >= 0) return false;

        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        //vents, then caps, then hullmods, then fighters, then weapons

        int vents = var.getNumFluxVents();
        if(vents > 0){
            int ventsAfterCorrection = Math.max(0, availableOP + vents);
            var.setNumFluxVents(ventsAfterCorrection);
            availableOP += (vents - ventsAfterCorrection);

            if(availableOP >= 0) return true;
        }

        int caps = var.getNumFluxCapacitors();
        if(caps > 0){
            int capsAfterCorrection = Math.max(0, availableOP + caps);
            var.setNumFluxCapacitors(capsAfterCorrection);

            availableOP += (caps - capsAfterCorrection);
            if(availableOP >= 0) return true;
        }

        for (String s : new ArrayList<>(var.getNonBuiltInHullmods())) {
            int cost = Global.getSettings().getHullModSpec(s).getCostFor(var.getHullSize());
            if (cost <= 0) continue;

            var.removeMod(s);
            availableOP += cost;

            if (availableOP >= 0) return true;
        }

        int numBays = 20; // well above whatever it might actually be
        for (int i = 0; i < numBays; i++) {
            String id = var.getWingId(i);

            if (id != null && var.getNonBuiltInWings().contains(id)) {
                float cost = Global.getSettings().getFighterWingSpec(id).getOpCost(var.getStatsForOpCosts());
                var.setWingId(i, null);
                if(toCargo) cargo.addFighters(id, 1);

                availableOP += Math.round(cost);
                if (availableOP >= 0) return true;
            }
        }

        for (String id : var.getFittedWeaponSlots()) {
            WeaponSlotAPI slot = var.getSlot(id);
            if (slot.isDecorative() || slot.isBuiltIn() || slot.isHidden() || slot.isSystemSlot() || slot.isStationModule()) continue;

            String wepId = var.getWeaponId(id);
            float cost = Global.getSettings().getWeaponSpec(wepId).getOrdnancePointCost(stats, var.getStatsForOpCosts());
            var.clearSlot(id);

            if(toCargo) cargo.addWeapons(wepId, 1);

            availableOP += Math.round(cost);
            if (availableOP >= 0) return true;
        }

        return true;
    }

    public void showMessageIfNeeded(){
        MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();
        AddictionStatus status = AlcoholRepo.get(AlcoholRepo.ABSYNTH).getAddictionStatus();

        if(!mem.getBoolean(HAS_SHOWN_WARNING_KEY) && status.isWithdrawal()){
            if(!Global.getSector().getCampaignUI().isShowingDialog()){
                Global.getSector().getCampaignUI().showMessageDialog(
                        "You have gone into withdrawal for Absynth. As your ships might now be over the OP limit, you have 7 days to correct them or resume consumption" +
                                ", after which your ship loadouts will be automatically corrected."
                        + "\n\nThis warning will not be shown again.");

                Global.getSector().setPaused(true);
                mem.set(HAS_SHOWN_WARNING_KEY, true);
            }
        }
    }

    @Override
    public void reportPlayerOpenedMarket(MarketAPI market) {

    }

    @Override
    public void reportPlayerClosedMarket(MarketAPI market) {

    }

    @Override
    public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {

    }

}
