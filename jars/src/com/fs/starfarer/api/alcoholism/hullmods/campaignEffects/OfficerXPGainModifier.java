package com.fs.starfarer.api.alcoholism.hullmods.campaignEffects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.hullmods.alcoholEffectHullmods.Tears_HullmodEffect;
import com.fs.starfarer.api.alcoholism.memory.AddictionStatus;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

import java.util.HashMap;
import java.util.Map;

public class OfficerXPGainModifier extends BaseCampaignEventListener {

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

    // fires before vanilla grants battle xp, reportBattleOccurred fires after
    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {
        super.reportPlayerEngagement(result);
        if(!officerXPMemory.isEmpty() || !AlcoholRepo.get(AlcoholRepo.TEARS).getAddictionStatus().isAddicted()) return;

        for (OfficerDataAPI data : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()){
            PersonAPI person = data.getPerson();
            officerXPMemory.put(person.getId(), person.getStats().getXP());
        }
    }

    // autoresolved battles skip this, their xp counts towards the next battle
    @Override
    public void reportBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle) {
        super.reportBattleOccurred(primaryWinner, battle);
        if(!battle.isPlayerInvolved() || officerXPMemory.isEmpty()) return;

        AddictionStatus status = AlcoholRepo.get(AlcoholRepo.TEARS).getAddictionStatus();

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
            }
        }

        officerXPMemory.clear();
    }
}
