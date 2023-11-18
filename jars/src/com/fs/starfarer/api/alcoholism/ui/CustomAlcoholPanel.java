package com.fs.starfarer.api.alcoholism.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.memory.Ingredient;
import com.fs.starfarer.api.alcoholism.ui.basepanel.InteractionDialogCustomPanelPlugin;
import com.fs.starfarer.api.alcoholism.ui.basepanel.VisualCustomPanel;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.*;

import java.util.LinkedList;
import java.util.List;

public class CustomAlcoholPanel {
    protected static final float PANEL_WIDTH_1 = 240;
    protected static final float PANEL_WIDTH_2 = VisualCustomPanel.PANEL_WIDTH - PANEL_WIDTH_1 - 8;
    protected static final float SHIP_ICON_WIDTH = 48;
    protected static final float ARROW_BUTTON_WIDTH = 20, BUTTON_HEIGHT = 30;
    protected static final float SELECT_BUTTON_WIDTH = 95f;
    protected static final float TEXT_FIELD_WIDTH = 80f;

    protected static final float PANEL_MAX_HEIGHT = 700f;
    protected static final float PANEL_MAX_WIDTH = 1180f;

    private Industry industry;
    private List<Ingredient> ingredients = new LinkedList<>();

    public CustomAlcoholPanel(Industry industry){
        this.industry = industry;
    }

    public void showPanel(InteractionDialogAPI dialogue) {
        VisualCustomPanel.createPanel(dialogue, true);
        showCustomPanel(dialogue);
    }

    private void fixBasePanelOffsets(){
        //this sets the base panel to 0,0
        //don't question it

        CustomPanelAPI basePanel = VisualCustomPanel.getPanel();
        PositionAPI pos = basePanel.getPosition();
        pos.setSize(Global.getSettings().getScreenWidth(), Global.getSettings().getScreenHeight());
        pos.setYAlignOffset(pos.getY() <= 0 ? -pos.getY() : pos.getY());
        pos.setXAlignOffset(pos.getX() >= 0 ? -pos.getX() + 10 : pos.getX() - 10);
    }

    private void showCustomPanel(InteractionDialogAPI dialogue) {
        float opad = 10f;
        float spad = 3f;

        fixBasePanelOffsets();

        CustomPanelAPI visualCustomPanel = VisualCustomPanel.getPanel();
        CustomPanelAPI basePanel = visualCustomPanel.createCustomPanel(PANEL_MAX_WIDTH, PANEL_MAX_HEIGHT, new InteractionDialogCustomPanelPlugin(false));

        /*//add header
        TooltipMakerAPI basePanelTooltip = basePanel.createUIElement(PANEL_MAX_WIDTH, PANEL_MAX_HEIGHT, false);
        basePanelTooltip.addSectionHeading("Brewery", Alignment.MID, 0f);
        basePanel.addUIElement(basePanelTooltip).inTL(0,0);*/


        TooltipMakerAPI lastUsedVariableButtonAnchor;


/*
        for (Map.Entry<String, ResearchProject> projectEntry : ResearchProjectTemplateRepo.RESEARCH_PROJECTS.entrySet()) {

            final ResearchProject project = projectEntry.getValue();
            final String projId = project.getId();
            final ResearchProject.Progress progress = project.getProgress();
            CargoAPI cargo = playerFleet.getCargo();
            float progressPercent = Math.min(1f, (progress.points * 1f) / (project.getRequiredPoints() * 1f));
            log.info("points " + progress.points + " req " + project.getRequiredPoints() + " % " + progressPercent);

            if (progress.redeemed || !project.display()) continue;

            boolean isFinished = progress.points >= project.getRequiredPoints() || progress.redeemed;
            boolean isRedeemed = progress.redeemed;

            int availableItemCount = 0;
            for (RequiredItem item : project.getRequiredItems()) {
                float count = cargo.getQuantity(item.type, item.id);
                if (count > 0f) availableItemCount += count;
            }

            boolean playerHasAnyInputInCargo = availableItemCount > 0;

            panelTooltip.addSectionHeading(project.getName(), Alignment.MID, opad);

            CustomPanelAPI buttonPanel = panel.createCustomPanel(PANEL_WIDTH_1, 50f, new NoFrameCustomPanelPlugin());

            //             NAME
            // CHECK INPUT - CONTRIBUTE - REDEEM
            // PROGRESS BAR SHOW % (+ X ITEMS AVAILABLE)
            // FLAVOUR TEXT

            //CHECK INPUT

            TooltipMakerAPI variableButtonAnchor = buttonPanel.createUIElement(SELECT_BUTTON_WIDTH, BUTTON_HEIGHT, false);

            String buttonId = "button_inputs_" + projId;

            boolean prerequisiteForActive = true;

            Color baseColor = prerequisiteForActive ? Misc.getButtonTextColor() : Misc.getTextColor();
            Color bgColour = prerequisiteForActive ? Misc.getDarkPlayerColor() : Misc.getGrayColor();

            ButtonAPI newLoadoutButton = variableButtonAnchor.addButton("Show inputs", buttonId, baseColor, bgColour, Alignment.MID, CutStyle.C2_MENU, SELECT_BUTTON_WIDTH, BUTTON_HEIGHT, 0);
            newLoadoutButton.setEnabled(prerequisiteForActive);
            InteractionDialogCustomPanelPlugin.ButtonEntry entry = new InteractionDialogCustomPanelPlugin.ButtonEntry(newLoadoutButton, buttonId) {
                @Override
                public void onToggle() {
                    ResearchProjectDialoguePlugin.getCurrentDialoguePlugin().setProjectIdForInputs(projId);
                }
            };

            VisualCustomPanel.getPlugin().addButton(entry);
            buttonPanel.addUIElement(variableButtonAnchor).inTL(spad, opad);       //first in row
            lastUsedVariableButtonAnchor = variableButtonAnchor;

            //CONTRIBUTE

            prerequisiteForActive = !isFinished && playerHasAnyInputInCargo;

            baseColor = prerequisiteForActive ? Misc.getButtonTextColor() : Misc.getTextColor();
            bgColour = prerequisiteForActive ? Misc.getDarkPlayerColor() : Misc.getGrayColor();

            buttonId = "button_cargo_" + projId;
            variableButtonAnchor = buttonPanel.createUIElement(SELECT_BUTTON_WIDTH, BUTTON_HEIGHT, false);
            newLoadoutButton = variableButtonAnchor.addButton("Cargo", buttonId, baseColor, bgColour, Alignment.MID, CutStyle.C2_MENU, SELECT_BUTTON_WIDTH, BUTTON_HEIGHT, 0);
            newLoadoutButton.setEnabled(prerequisiteForActive);
            entry = new InteractionDialogCustomPanelPlugin.ButtonEntry(newLoadoutButton, buttonId) {
                @Override
                public void onToggle() {
                    ResearchProjectDonationCargoPicker.init(ResearchProjectDialoguePlugin.getCurrentDialoguePlugin(), projId);
                }
            };

            VisualCustomPanel.getPlugin().addButton(entry);
            buttonPanel.addUIElement(variableButtonAnchor).rightOfMid(lastUsedVariableButtonAnchor, opad);         //second in row
            lastUsedVariableButtonAnchor = variableButtonAnchor;

            //REDEEM

            prerequisiteForActive = isFinished && !isRedeemed;

            baseColor = Misc.getTextColor();
            bgColour = prerequisiteForActive ? new Color(50, 130, 0, 255) : Misc.getGrayColor();

            buttonId = "button_redeem_" + projId;
            variableButtonAnchor = buttonPanel.createUIElement(SELECT_BUTTON_WIDTH, BUTTON_HEIGHT, false);
            newLoadoutButton = variableButtonAnchor.addButton("Redeem", buttonId, baseColor, bgColour, Alignment.MID, CutStyle.C2_MENU, SELECT_BUTTON_WIDTH, BUTTON_HEIGHT, 0);
            newLoadoutButton.setEnabled(prerequisiteForActive);
            entry = new InteractionDialogCustomPanelPlugin.ButtonEntry(newLoadoutButton, buttonId) {
                @Override
                public void onToggle() {
                    ResearchProject project = ResearchProjectTemplateRepo.RESEARCH_PROJECTS.get(projId);
                    CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
                    CargoAPI rewards = project.getRewards();
                    cargo.addAll(rewards.createCopy());

                    if (project.isRepeatable()) {
                        project.getProgress().points = 0;
                        project.getProgress().redeemed = false;
                    } else project.getProgress().redeemed = true;

                    ResearchProjectDialoguePlugin plugin = ResearchProjectDialoguePlugin.getCurrentDialoguePlugin();
                    plugin.setProjectIdForRewards(projId, rewards);
                    plugin.refreshCustomPanel();
                }
            };

            VisualCustomPanel.getPlugin().addButton(entry);
            buttonPanel.addUIElement(variableButtonAnchor).rightOfMid(lastUsedVariableButtonAnchor, opad);         //last in row
            lastUsedVariableButtonAnchor = variableButtonAnchor;

            panelTooltip.addCustom(buttonPanel, opad); //add panel

            // PROGRESS BAR SHOW % (+ X ITEMS AVAILABLE)

            //the most scuffed progress bar of all time
            CustomPanelAPI progressPanel = panel.createCustomPanel(VisualCustomPanel.PANEL_WIDTH - 20f, SHIP_ICON_WIDTH + 6f, new FramedCustomPanelPlugin(0.25f, Misc.getBasePlayerColor(), false));

            float holderWidth = VisualCustomPanel.PANEL_WIDTH - 33f;
            TooltipMakerAPI barHolder = progressPanel.createUIElement(holderWidth, 0, false);

            ButtonAPI checkbox = barHolder.addAreaCheckbox(StringHelper.getAbsPercentString(progressPercent, false), null, Misc.getDarkPlayerColor(), Misc.getDarkPlayerColor(), Misc.getTextColor(), Math.max(50f, holderWidth * progressPercent), BUTTON_HEIGHT, opad);
            checkbox.setEnabled(false);
            checkbox.setChecked(true);
            progressPanel.addUIElement(barHolder).inTL(1f, 1f); //add it to top left of fleet panel (?)

            panelTooltip.addCustom(progressPanel, 3); //add fleet panel

            // FLAVOUR TEXT
            panelTooltip.addPara("You have %S items available for this project.", opad, Misc.getHighlightColor(), availableItemCount + "");
            panelTooltip.addPara(project.getShortDesc(), Misc.getGrayColor(), opad);
        }*/

        visualCustomPanel.addComponent(basePanel).inMid();
        VisualCustomPanel.addTooltipToPanel();
    }
}