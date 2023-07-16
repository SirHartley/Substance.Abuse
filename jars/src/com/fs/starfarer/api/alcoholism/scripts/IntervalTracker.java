package com.fs.starfarer.api.alcoholism.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.util.IntervalUtil;

public class IntervalTracker implements EveryFrameScript {

    private IntervalUtil interval;

    public IntervalTracker(){
        interval = new IntervalUtil(1f, 1f);
    }

    @Override
    public void advance(float amount) {
        interval.advance(amount);
    }

    public float getIntervalFraction(){
        return Math.min(interval.getElapsed() / interval.getIntervalDuration(), 1f);
    }

    //transient
    public static IntervalTracker getInstance() {
        for (EveryFrameScript transientScript : Global.getSector().getTransientScripts()) {
            if (transientScript instanceof IntervalTracker) {
                return (IntervalTracker) transientScript;
            }
        }

        ModPlugin.log("creating timeManager instance");

        IntervalTracker script = new IntervalTracker();
        Global.getSector().addTransientScript(script);
        return script;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }
}
