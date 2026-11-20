package livecheck;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.hullmods.alcoholEffectHullmods.Stout_HullmodEffect;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.combat.CombatEngine;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.List;

// registered under plugins in data/config/settings.json, only active in the two battle groups
public class DrinkBattleDriver extends BaseEveryFrameCombatPlugin {
    private static final Logger log = Global.getLogger(DrinkBattleDriver.class);
    static final float ARMOR_AT = 5f;
    static final float ARMOR_READ_AT = 15f;
    static final float FIGHTERS_BY = 40f;
    static final float POOL_READ_AT = 22f;
    static final float POOL_LEFT = 40f;
    public static String officerMember = null;

    private CombatEngineAPI engine;
    private boolean active;
    private boolean withdrawal;
    private int frame = 0;
    private float time = 0f;
    private boolean aiChecked = false;
    private boolean fightersChecked = false;
    private boolean armorDone = false;
    private ShipAPI armorShip = null;
    private float armorSum = 0f;
    private boolean poolDone = false;
    private VarHandle pool = null;
    private Object regen = null;

    private static void expect(boolean ok, String id) {
        log.info("LIVE_CHECK " + (ok ? "ok" : "FAIL") + ": " + id);
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        List<String> groups = Arrays.asList(System.getProperty("live.check", "").split(","));
        withdrawal = groups.contains("battle_withdrawal");
        active = (withdrawal || groups.contains("battle_drinking")) && !engine.isSimulation() && engine.isInCampaign();
        if (active) ((CombatEngine) engine).setShowDeploymentDialog(false);
    }

    private int deploy(int owner, float y, float facing) {
        CombatFleetManagerAPI fm = engine.getFleetManager(owner);
        int i = 0;
        for (FleetMemberAPI m : fm.getReservesCopy()) {
            fm.spawnFleetMember(m, new Vector2f((i - 4) * 500f, y), facing, 0f);
            i++;
        }
        return i;
    }

    private static float armor(ShipAPI ship) {
        ArmorGridAPI grid = ship.getArmorGrid();
        float[][] cells = grid.getGrid();
        float sum = 0f;
        for (float[] row : cells) for (float v : row) sum += v;
        return sum;
    }

    private static void halveArmor(ShipAPI ship) {
        ArmorGridAPI grid = ship.getArmorGrid();
        float[][] cells = grid.getGrid();
        for (int x = 0; x < cells.length; x++) {
            for (int y = 0; y < cells[x].length; y++) grid.setArmorValue(x, y, cells[x][y] * 0.5f);
        }
    }

    private void checkAi() {
        String want = withdrawal ? Personalities.CAUTIOUS : Personalities.AGGRESSIVE;
        int plain = 0, officered = 0;
        for (ShipAPI s : engine.getShips()) {
            if (s.getOwner() != 0 || s.isFighter() || s == engine.getPlayerShip() || s.getShipAI() == null) continue;
            String got = s.getShipAI().getConfig().personalityOverride;
            String name = s.getHullSpec().getHullId();
            // the flagship carries the player, also not a default captain
            if (s.getCaptain() != null && !s.getCaptain().isDefault()) {
                FleetMemberAPI m = engine.getFleetManager(0).getDeployedFleetMember(s) == null ? null : engine.getFleetManager(0).getDeployedFleetMember(s).getMember();
                if (m != null && m.getId().equals(officerMember)) officered++;
                expect(got == null, "row9 ship with an officer " + name + " has no override, got " + got);
            } else {
                plain++;
                expect(want.equals(got), "row9 officerless " + name + " is " + want + ", got " + got);
            }
        }
        expect(plain >= 2 && officered == 1, "row9 checked " + plain + " officerless ships and " + officered + " with an officer");
    }

    private void checkFighters() {
        float want = withdrawal ? 1.2f : 0.9f;
        int found = 0;
        for (ShipAPI s : engine.getShips()) {
            if (s.getOwner() != 0 || !s.isFighter()) continue;
            found++;
            if (found > 3) continue;
            for (MutableStat stat : new MutableStat[]{s.getMutableStats().getHullDamageTakenMult(), s.getMutableStats().getArmorDamageTakenMult(), s.getMutableStats().getShieldDamageTakenMult()}) {
                MutableStat.StatMod mod = stat.getMultStatMod(AlcoholRepo.FREEDOM);
                expect(mod != null && Math.abs(mod.getValue() - want) <= 0.02f, "row7 fighter damage taken mod " + (mod == null ? "missing" : mod.getValue()) + ", want " + want);
            }
        }
        expect(found > 0, "row7 fighters launched, got " + found);
        for (ShipAPI s : engine.getShips()) {
            if (s.getOwner() == 0 && !s.isFighter() && s.getNumFighterBays() > 0) {
                expect(s.getMutableStats().getDamageToFighters().getMultStatMod(AlcoholRepo.FREEDOM) == null
                        && s.getMutableStats().getDamageToFighters().getFlatStatMod(AlcoholRepo.FREEDOM) == null, "row7 carrier has no sweet freedom damage to fighters mod");
            }
        }
    }

    private void startArmor() {
        for (ShipAPI s : engine.getShips()) {
            if (s.getOwner() == 0 && !s.isFighter() && s != engine.getPlayerShip()) {
                armorShip = s;
                break;
            }
        }
        boolean listener = armorShip.hasListenerOfClass(Stout_HullmodEffect.AlcoholArmorRegen.class);
        expect(listener != withdrawal, "row10 regen listener " + listener + " with withdrawal " + withdrawal);
        halveArmor(armorShip);
        armorSum = armor(armorShip);
    }

    // 1.1.f caps regen per deployment: shrink the pool, damage the ship again and see regen stop there
    private void startPool() {
        if (withdrawal) {
            poolDone = true;
            return;
        }
        try {
            pool = MethodHandles.privateLookupIn(Stout_HullmodEffect.AlcoholArmorRegen.class, MethodHandles.lookup())
                    .findVarHandle(Stout_HullmodEffect.AlcoholArmorRegen.class, "pool", float.class);
        } catch (ReflectiveOperationException e) {
            expect(false, "stout regen has a pool, " + e);
            poolDone = true;
            return;
        }
        regen = armorShip.getListeners(Stout_HullmodEffect.AlcoholArmorRegen.class).get(0);
        pool.set(regen, POOL_LEFT);
        halveArmor(armorShip);
        armorSum = armor(armorShip);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (!active) return;
        frame++;
        if (frame == 2) {
            engine.setDoNotEndCombat(true);
            int a = deploy(0, -4000f, 90f), b = deploy(1, 9000f, 270f);
            expect(a > 2 && b > 0, "row7 deployed " + a + " and " + b + " ships");
        }
        if (frame < 3) return;
        if (engine.isPaused()) engine.setPaused(false);
        ShipAPI player = engine.getPlayerShip();
        if (player != null && player.getShipAI() == null) player.resetDefaultAI();
        time += amount;

        if (!aiChecked && time >= 2f) {
            aiChecked = true;
            checkAi();
        }
        if (!armorDone && armorShip == null && time >= ARMOR_AT) startArmor();
        if (!armorDone && time >= ARMOR_READ_AT) {
            armorDone = true;
            float now = armor(armorShip);
            if (withdrawal) expect(now <= armorSum + 0.01f, "row10 armor flat in withdrawal, " + armorSum + " to " + now);
            else expect(now > armorSum, "row10 armor regen while drinking, " + armorSum + " to " + now);
        }
        if (armorDone && !poolDone && pool == null) startPool();
        if (pool != null && !poolDone && time >= POOL_READ_AT) {
            poolDone = true;
            float gain = armor(armorShip) - armorSum;
            float left = (float) pool.get(regen);
            expect(gain <= POOL_LEFT + 0.5f, "stout regen stops at the pool, " + gain + " regenerated from " + POOL_LEFT);
            expect(left == 0f, "stout pool used up, " + left + " left");
        }
        if (!fightersChecked) {
            boolean any = false;
            for (ShipAPI s : engine.getShips()) any |= s.getOwner() == 0 && s.isFighter();
            if ((any && time >= 8f) || time >= FIGHTERS_BY) {
                fightersChecked = true;
                checkFighters();
            }
        }
        if (aiChecked && armorDone && poolDone && fightersChecked) {
            log.info("LIVE_CHECK shot battle");
            log.info("LIVE_CHECK COMPLETE");
            System.exit(0);
        }
    }
}
