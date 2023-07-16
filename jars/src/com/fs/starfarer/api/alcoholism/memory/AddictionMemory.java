package com.fs.starfarer.api.alcoholism.memory;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks the addiction status of any given alcohol separately
 */

public class AddictionMemory {
    public static final String ADDICTION_MEMORY_KEY = "$AddictionMemory";
    protected Map<String, AddictionStatus> statusList;
    protected Map<Integer, Float> monthlyAddictionIncreaseMap = new HashMap<>();
    private int lastDay = 0;

    //permanent
    public static AddictionMemory getInstanceOrRegister() {
        MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();

        if (mem.contains(ADDICTION_MEMORY_KEY)) return (AddictionMemory) mem.get(ADDICTION_MEMORY_KEY);
        else {

            ModPlugin.log("creating new AddictionMemory");

            AddictionMemory addictionMemory = new AddictionMemory();
            mem.set(ADDICTION_MEMORY_KEY, addictionMemory);

            return addictionMemory;
        }
    }

    public AddictionMemory() {
        this.statusList = new HashMap<>();

        for (String s : AlcoholRepo.ALCOHOL_MAP.keySet()) {
            statusList.put(s, new AddictionStatus());
        }
    }

    public boolean contains(AlcoholAPI alcohol){
        return statusList.containsKey(alcohol.getId());
    }

    public void addIfNeeded(AlcoholAPI alcohol){
        if (!statusList.containsKey(alcohol.getId())) statusList.put(alcohol.getId(), new AddictionStatus());
    }

    public void refresh(){
        for (AlcoholAPI alcohol : AlcoholRepo.ALCOHOL_MAP.values()) {
            addIfNeeded(alcohol);
        }
    }

    public AddictionStatus getStatusForId(String id) {
        return statusList.get(id);
    }

    public void addTotalDailyAddictionGainToMemory(float amt) {
        monthlyAddictionIncreaseMap.put(getNextDayForMap(), amt);
    }

    public float getTotaladdictionGainLastMonth() {
        float total = 0f;
        for (float f : monthlyAddictionIncreaseMap.values()) total += f;

        return total;
    }

    public void resetAddictionMap(){
        monthlyAddictionIncreaseMap.clear();
        lastDay = 0;
    }

    private int getNextDayForMap() {
        if (lastDay > AddictionBrain.DAYS_PER_MONTH) lastDay = 0;
        lastDay++;

        return lastDay;
    }
}
