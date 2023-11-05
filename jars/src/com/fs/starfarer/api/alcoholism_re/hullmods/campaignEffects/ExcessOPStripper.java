package com.fs.starfarer.api.alcoholism_re.hullmods.campaignEffects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism_re.listeners.NewDayListener;
import com.fs.starfarer.api.alcoholism_re.memory.AddictionStatus;
import com.fs.starfarer.api.alcoholism_re.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import java.util.ArrayList;

public class ExcessOPStripper implements NewDayListener, ColonyInteractionListener, EconomyTickListener {
    public static final String HAS_SHOWN_WARNING_KEY = "$AlcoholWarningHasBeenShow";

    //Corrects member OP every day when not addicted, or after 7 days if in withdrawal.
    //Also corrects OP when a ship is sold or bought and the player is not under the influence of Abynth.

    public static void register(){
        ListenerManagerAPI m = Global.getSector().getListenerManager();

        if(Global.getSettings().getBoolean("ADJUST_OP_WHEN_OVER_LIMIT")){
            if(m.hasListenerOfClass(ExcessOPStripper.class)) return;
            m.addListener( new ExcessOPStripper());
        } else m.removeListenerOfClass(ExcessOPStripper.class);
    }

    public int countdown = 7;

    public ExcessOPStripper(){
    }

    @Override
    public void onNewDay() {
        AddictionStatus status = AlcoholRepo.get(AlcoholRepo.ABSYNTH).getAddictionStatus();
        if(!status.isAddicted()) return;

        if(status.isWithdrawal()) {
            showMessageIfNeeded();
            countdown--;
        } else if (status.isConsuming()) countdown = 7;

        if(countdown < 0){
            correctPlayerFleetOP();
        }
    }

    @Override
    public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
        AddictionStatus status = AlcoholRepo.get(AlcoholRepo.ABSYNTH).getAddictionStatus();
        if(status.isWithdrawal() || !status.isAddicted()){
            MutableCharacterStatsAPI stats = Global.getSector().getPlayerPerson().getStats();

            if(!transaction.getShipsSold().isEmpty()){
                for (PlayerMarketTransaction.ShipSaleInfo info : transaction.getShipsSold()){
                    correctMember(info.getMember(), stats, transaction.getSubmarket().getPlugin().isFreeTransfer());
                }
            }

            if(!transaction.getShipsBought().isEmpty()){
                for (PlayerMarketTransaction.ShipSaleInfo info : transaction.getShipsBought()){
                    correctMember(info.getMember(), stats, transaction.getSubmarket().getPlugin().isFreeTransfer());
                }
            }
        }
    }

    public void correctPlayerFleetOP(){
        MutableCharacterStatsAPI stats = Global.getSector().getPlayerPerson().getStats();

        boolean notify = false;

        for (FleetMemberAPI m : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()){
            boolean corrected;
            corrected = correctMember(m, stats, true);

            if(corrected) notify = true;
        }

        if (notify) Global.getSector().getCampaignUI().addMessage("Some of your ship loadouts have been changed to account for OP deficits.");
    }

    public boolean correctMember(FleetMemberAPI m, MutableCharacterStatsAPI stats, boolean toCargo){
        ShipVariantAPI var = m.getVariant();
        boolean corrected;
        corrected = correctVariant(var, stats, toCargo);

        for (String s : var.getModuleSlots()){
            boolean corr;
            corr = correctVariant(var.getModuleVariant(s), stats, toCargo);

            if(corr) corrected = corr;
        }

        if(corrected) m.updateStats();

        return corrected;
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

    @Override
    public void reportEconomyTick(int iterIndex) {
        AddictionStatus status = AlcoholRepo.get(AlcoholRepo.ABSYNTH).getAddictionStatus();
        if(!status.isAddicted()) correctPlayerFleetOP();
    }

    @Override
    public void reportEconomyMonthEnd() {

    }
}
