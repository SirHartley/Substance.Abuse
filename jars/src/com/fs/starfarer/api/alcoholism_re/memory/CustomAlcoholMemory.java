package com.fs.starfarer.api.alcoholism_re.memory;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism_re.ModPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;

import java.util.HashMap;
import java.util.Map;

public class CustomAlcoholMemory {
    public static final String CUSTOM_ALCOHOL_MEMORY_KEY = "$customAlcoholMemory";
    protected Map<String, CustomAlcohol> alcoholMap;

    //permanent
    public static CustomAlcoholMemory getInstanceOrRegister() {
        MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();

        if (mem.contains(CUSTOM_ALCOHOL_MEMORY_KEY)) return (CustomAlcoholMemory) mem.get(CUSTOM_ALCOHOL_MEMORY_KEY);
        else {

            ModPlugin.log("creating new Custom Alcohol Memory");

            CustomAlcoholMemory memory = new CustomAlcoholMemory();
            mem.set(CUSTOM_ALCOHOL_MEMORY_KEY, memory);

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
        alcoholMap.put(alcohol.id, alcohol);
    }

    public void setup(){
        for (CustomAlcohol alcohol : alcoholMap.values()){
            AlcoholRepo.ALCOHOL_MAP.put(alcohol.id, alcohol);
            AddictionMemory.getInstanceOrRegister().addIfNeeded(alcohol);
            alcohol.overwriteSpec();
        }
    }
}
