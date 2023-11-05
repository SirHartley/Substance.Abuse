package com.fs.starfarer.api.alcoholism.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.alcoholism.listeners.NewDayListener;

import java.util.List;

public class NewDayTracker implements EveryFrameScript {
    private float days = 0;
    private float lastDay = 0;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        days += Global.getSector().getClock().convertToDays(amount);

        if ((float) Math.floor(days) > lastDay) {
            lastDay = (float) Math.floor(days);

            List<NewDayListener> list = Global.getSector().getListenerManager().getListeners(NewDayListener.class);
            for (NewDayListener x : list) {
                x.onNewDay();
            }
        }
    }
    
    public static void register() {
        if(!Global.getSector().hasTransientScript(NewDayTracker.class)){
            ModPlugin.log("creating NewDayTracker instance");
            Global.getSector().addTransientScript(new NewDayTracker());
        }
    }
}

