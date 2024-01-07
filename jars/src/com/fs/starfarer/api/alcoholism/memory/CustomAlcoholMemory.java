package com.fs.starfarer.api.alcoholism.memory;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * this remembers custom alcohol configs
 * its purpose is to gen and play CA into the repo at game start, everything else goes from there
 */

public class CustomAlcoholMemory {
    public static final String CUSTOM_ALCOHOL_MEMORY_KEY = "$customAlcoholMemory";
    protected Map<String, CustomAlcohol> alcoholMap;

    //permanent
    public static CustomAlcoholMemory getInstanceOrRegister() {
        Map<String, Object> mem = Global.getSector().getPersistentData();

        if (mem.containsKey(CUSTOM_ALCOHOL_MEMORY_KEY)) return (CustomAlcoholMemory) mem.get(CUSTOM_ALCOHOL_MEMORY_KEY);
        else {

            ModPlugin.log("creating new Custom Alcohol Memory");

            CustomAlcoholMemory memory = new CustomAlcoholMemory();
            mem.put(CUSTOM_ALCOHOL_MEMORY_KEY, memory);

            return memory;
        }
    }

    public CustomAlcoholMemory() {
        this.alcoholMap = new HashMap<>();
    }

    public CustomAlcohol get(String id){
        return alcoholMap.get(id);
    }

    public void add(CustomAlcohol alcohol){
        alcoholMap.put(alcohol.getId(), alcohol);
    }

    public void remove(CustomAlcohol alcohol){
        alcoholMap.remove(alcohol.id);
    }

    public List<CustomAlcohol> getAll() {
        return new ArrayList<>(alcoholMap.values());
    }

    public void initAll(){
        for (CustomAlcohol alcohol : alcoholMap.values()) alcohol.init();
    }
}
