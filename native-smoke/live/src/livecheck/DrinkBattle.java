package livecheck;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.combat.CombatEngine;

import java.util.ArrayList;
import java.util.List;

// changelog rows 7, 9, 10 in a real battle, DrinkBattleDriver asserts inside combat
public final class DrinkBattle {
    private static final String[] DRINKS = {AlcoholRepo.FREEDOM, AlcoholRepo.BLOOD, AlcoholRepo.STOUT};

    private DrinkBattle() {
    }

    public static List<Step> steps(boolean withdrawal) {
        List<Step> steps = new ArrayList<>();
        steps.add(c -> {
            Kit.reset();
            CampaignFleetAPI player = Kit.fleet();
            player.getCargo().addCrew(2000);
            player.getCargo().addFuel(500);
            Global.getSector().getPlayerFaction().getDoctrine().setAggression(2);
            Kit.ship("condor_Strike");
            FleetMemberAPI withOfficer = Kit.ship("hammerhead_Balanced");
            withOfficer.setCaptain(Kit.officer().getPerson());
            DrinkBattleDriver.officerMember = withOfficer.getId();

            for (String id : DRINKS) Kit.consume(id, 1f, 500f);
            Kit.manager().onNewDay();
            if (withdrawal) for (String id : DRINKS) Kit.withdraw(id, 1f);
            Kit.manager().reapply();
            player.getFleetData().setSyncNeeded();
            player.getFleetData().syncIfNeeded();
            for (FleetMemberAPI m : player.getFleetData().getMembersListCopy()) m.getRepairTracker().setCR(m.getRepairTracker().getMaxCR());

            CampaignFleetAPI enemy = Global.getFactory().createEmptyFleet(Factions.PIRATES, "Live check enemy", true);
            for (int i = 0; i < 4; i++) Kit.ship(enemy, "wolf_CS");
            player.getContainingLocation().addEntity(enemy);
            enemy.setLocation(player.getLocation().x + 150f, player.getLocation().y);

            ((CombatEngine) CombatEngine.getInstance()).setShowDeploymentDialog(false);
            Global.getSector().getCampaignUI().startBattle(new BattleCreationContext(player, FleetGoal.ATTACK, enemy, FleetGoal.ATTACK));
            c.note("battle started, withdrawal " + withdrawal);
            // the campaign Runner does not tick in combat, the driver ends the run
            c.frames(1000000);
        });
        return steps;
    }
}
