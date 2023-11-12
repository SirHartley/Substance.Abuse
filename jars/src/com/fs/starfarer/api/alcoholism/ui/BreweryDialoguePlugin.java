package com.fs.starfarer.api.alcoholism.ui;

import com.fs.starfarer.api.alcoholism.ui.basepanel.VisualCustomPanel;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

import java.util.Map;

//todo on close: VisualCustomPanel.clearPanel();

public class BreweryDialoguePlugin implements InteractionDialogPlugin {

    public InteractionDialogAPI dialog;
    private Industry industry;

    public BreweryDialoguePlugin(Industry industry) {
        this.industry = industry;
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;

        dialog.setPromptText("");
        dialog.hideTextPanel();
        dialog.getOptionPanel().clearOptions();

        VisualCustomPanel.createPanel(dialog, true);
        refreshCustomPanel();
    }

    public void refreshCustomPanel(){
        new CustomAlcoholPanel().showPanel(dialog);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {
    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return null;
    }
}
