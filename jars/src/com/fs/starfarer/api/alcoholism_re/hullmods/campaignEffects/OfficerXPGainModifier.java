package com.fs.starfarer.api.alcoholism_re.hullmods.campaignEffects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism_re.hullmods.alcoholEffectHullmods.Tears_HullmodEffect;
import com.fs.starfarer.api.alcoholism_re.listeners.NewDayListener;
import com.fs.starfarer.api.alcoholism_re.memory.AddictionStatus;
import com.fs.starfarer.api.alcoholism_re.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

import java.util.HashMap;
import java.util.Map;

public class OfficerXPGainModifier extends BaseCampaignEventListener implements NewDayListener {

    private Map<String, Long> officerXPMemory = new HashMap<>();

    public OfficerXPGainModifier() {
        super(false);
    }

    public static void register(){
        for (CampaignEventListener l : Global.getSector().getAllListeners()){
            if(l instanceof OfficerXPGainModifier) return;
        }

        OfficerXPGainModifier script = new OfficerXPGainModifier();
        Global.getSector().addTransientListener(script);
    }

    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {
        super.reportPlayerEngagement(result);
        AddictionStatus status = AlcoholRepo.get(AlcoholRepo.TEARS).getAddictionStatus();
        if(!status.isAddicted()) return;

        if(status.isConsuming()){
            float xpMult = 1 + ((status.getAddictionValue() * Tears_HullmodEffect.MAX_OFFICER_XP_INCREASE) / 100);

            for (OfficerDataAPI data : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()){
                PersonAPI person = data.getPerson();

                if(officerXPMemory.containsKey(person.getId())){
                    MutableCharacterStatsAPI stats = person.getStats();
                    float current = stats.getXP();
                    float old = officerXPMemory.get(person.getId());
                    float diff = current - old;
                    long increase = Math.round((diff * xpMult) - diff);

                    //remove bonus XP or it'll use it
                    long bonus = stats.getBonusXp();
                    stats.setBonusXp(0);

                    //don't add XP directly to stats, will cause officers to level up without being able to pick stats
                    data.addXP(increase);
                    stats.setBonusXp(bonus);
                }

                officerXPMemory.put(person.getId(), person.getStats().getXP());
            }

        } else if (status.isWithdrawal()){
            float xpMult = 1 + ((status.getAddictionValue() * Tears_HullmodEffect.WITHDRAWAL_OFFICER_XP_REDUCTION) / 100);

            for (OfficerDataAPI data : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()){
                PersonAPI person = data.getPerson();

                if(officerXPMemory.containsKey(person.getId())){
                    float current = person.getStats().getXP();
                    float old = officerXPMemory.get(person.getId());
                    float diff = current - old; //10 gewonnen
                    long decrease = Math.round(diff - (diff * xpMult));

                    //adding a negative XP number should work
                    person.getStats().addXP(-decrease, null, false, false);
                }

                officerXPMemory.put(person.getId(), person.getStats().getXP());
            }
        }
    }

    @Override
    public void onNewDay() {
        AddictionStatus status = AlcoholRepo.get(AlcoholRepo.TEARS).getAddictionStatus();
        if(!status.isAddicted()) return;

        updateOfficerXPStatus();
    }

    private void updateOfficerXPStatus(){
        officerXPMemory.clear();

        for (OfficerDataAPI data : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()){
            PersonAPI person = data.getPerson();
            officerXPMemory.put(person.getId(), person.getStats().getXP());
        }
    }
}
