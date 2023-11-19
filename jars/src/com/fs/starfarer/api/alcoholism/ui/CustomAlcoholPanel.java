package com.fs.starfarer.api.alcoholism.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.alcoholism.memory.CustomAlcohol;
import com.fs.starfarer.api.alcoholism.memory.CustomAlcoholMemory;
import com.fs.starfarer.api.alcoholism.memory.Ingredient;
import com.fs.starfarer.api.alcoholism.ui.basepanel.FramedCustomPanelPlugin;
import com.fs.starfarer.api.alcoholism.ui.basepanel.FramedInteractionDialogCustomPanelPlugin;
import com.fs.starfarer.api.alcoholism.ui.basepanel.MiddleCircleCustomPanelPlugin;
import com.fs.starfarer.api.alcoholism.ui.basepanel.VisualCustomPanel;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;

import javax.tools.Tool;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CustomAlcoholPanel {
    protected static final float PANEL_WIDTH_1 = 240;
    protected static final float PANEL_WIDTH_2 = VisualCustomPanel.PANEL_WIDTH - PANEL_WIDTH_1 - 8;
    protected static final float SHIP_ICON_WIDTH = 48;
    protected static final float ARROW_BUTTON_WIDTH = 20, BUTTON_HEIGHT = 30;
    protected static final float SELECT_BUTTON_WIDTH = 95f;
    protected static final float TEXT_FIELD_WIDTH = 80f;

    protected static final float BASE_PANEL_MAX_HEIGHT = 700f;
    protected static final float BASE_PANEL_MAX_WIDTH = 1180f;

    protected static final float RECIPE_SELECTION_PANEL_WIDTH = 250f;
    protected static final float RECIPE_SELECTION_PANEL_HEIGHT = 70f;
    protected static final float RECIPE_SELECTION_ENTRY_HEIGHT = 85f;

    protected static final float EFFECT_DISPLAY_PANEL_WIDTH = 250f;

    private Industry industry;
    private List<Ingredient> ingredients = new LinkedList<>();
    private AlcoholListFilter currentFilter = AlcoholListFilter.ACTIVE;
    Map<AlcoholListFilter, ScrollPanelAPI> filterScrollPanels = new HashMap<>();

    public enum AlcoholListFilter{
        ACTIVE,
        ARCHIVE
    }

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

    public void display(CustomAlcohol custom){
        if (custom == null) //clear panel

        //todo make this

        ModPlugin.log("cleared alcohol panel");
    }

    private void showCustomPanel(final InteractionDialogAPI dialogue) {
        final float opad = 10f;
        float spad = 5f;

        fixBasePanelOffsets();

        CustomPanelAPI visualCustomPanel = VisualCustomPanel.getPanel();
        CustomPanelAPI basePanel = visualCustomPanel.createCustomPanel(BASE_PANEL_MAX_WIDTH, BASE_PANEL_MAX_HEIGHT, new FramedInteractionDialogCustomPanelPlugin(true));

        TooltipMakerAPI lastUsedVariableButtonAnchor;
        TooltipMakerAPI variableButtonAnchor;
        String buttonId;

        boolean prerequisiteForActive = true;
        Color baseColor;
        Color bgColour;
        Color textColor;

        //"Create New" Button and confirm popup

        CustomPanelAPI createNewAlcoholButtonPanel = visualCustomPanel.createCustomPanel(RECIPE_SELECTION_PANEL_WIDTH, RECIPE_SELECTION_PANEL_HEIGHT, new FramedInteractionDialogCustomPanelPlugin(true));

        buttonId = "new_alcohol_button";
        prerequisiteForActive = CustomAlcoholMemory.getInstanceOrRegister().getAll().size() <= 100;
        baseColor = prerequisiteForActive ? Misc.getButtonTextColor() : Misc.getTextColor();
        bgColour = prerequisiteForActive ? Misc.getDarkPlayerColor() : Misc.getGrayColor();
        textColor = prerequisiteForActive ? Misc.getTextColor() : Color.WHITE;

        variableButtonAnchor = createNewAlcoholButtonPanel.createUIElement(RECIPE_SELECTION_PANEL_WIDTH, BUTTON_HEIGHT, false);
        ButtonAPI currentButton = variableButtonAnchor.addButton("+ New Recipe", buttonId, baseColor, bgColour, Alignment.MID, CutStyle.ALL, RECIPE_SELECTION_PANEL_WIDTH - opad, BUTTON_HEIGHT, 0);
        currentButton.setEnabled(prerequisiteForActive);
        FramedInteractionDialogCustomPanelPlugin.ButtonEntry entry = new FramedInteractionDialogCustomPanelPlugin.ButtonEntry(currentButton, buttonId) {
            @Override
            public void onToggle() {
               dialogue.showCustomDialog(400f, 100f, new BaseCustomDialogDelegate() {
                   public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
                       TooltipMakerAPI info = panel.createUIElement(400f, 100f, false);
                       info.addSectionHeading("Confirm or return", Alignment.MID, opad);
                       info.addPara("Are you sure? Your current changes will not be saved.", opad);
                       panel.addUIElement(info).inMid();
                   }

                   public boolean hasCancelButton() {
                       return true;
                   }

                   public void customDialogConfirm() {
                       display(null);
                       showPanel(dialogue);
                   }
               });
            }
        };

        VisualCustomPanel.getPlugin().addButton(entry);
        createNewAlcoholButtonPanel.addUIElement(variableButtonAnchor).inTMid(spad);
        lastUsedVariableButtonAnchor = variableButtonAnchor;

        buttonId = "set_filter_active";
        prerequisiteForActive = currentFilter == AlcoholListFilter.ACTIVE;
        baseColor = prerequisiteForActive ? Misc.getButtonTextColor() : Misc.getTextColor();
        bgColour = prerequisiteForActive ? Misc.getDarkPlayerColor() : Misc.getGrayColor();
        textColor = prerequisiteForActive ? Misc.getTextColor() : Color.WHITE;

        variableButtonAnchor = createNewAlcoholButtonPanel.createUIElement(RECIPE_SELECTION_PANEL_WIDTH / 2, BUTTON_HEIGHT, false);
        currentButton = variableButtonAnchor.addButton("Active", buttonId, baseColor, bgColour, Alignment.MID, CutStyle.TOP, RECIPE_SELECTION_PANEL_WIDTH / 2 - opad, BUTTON_HEIGHT - 1, 0);
        entry = new FramedInteractionDialogCustomPanelPlugin.ButtonEntry(currentButton, buttonId) {
            @Override
            public void onToggle() {
                currentFilter = AlcoholListFilter.ACTIVE;
                showPanel(dialogue);
            }
        };

        VisualCustomPanel.getPlugin().addButton(entry);
        createNewAlcoholButtonPanel.addUIElement(variableButtonAnchor).belowLeft(lastUsedVariableButtonAnchor, spad);
        //lastUsedVariableButtonAnchor = variableButtonAnchor;

        buttonId = "set_filter_archive";
        prerequisiteForActive = currentFilter == AlcoholListFilter.ARCHIVE;
        baseColor = prerequisiteForActive ? Misc.getButtonTextColor() : Misc.getTextColor();
        bgColour = prerequisiteForActive ? Misc.getDarkPlayerColor() : Misc.getGrayColor();
        textColor = prerequisiteForActive ? Misc.getTextColor() : Color.WHITE;

        variableButtonAnchor = createNewAlcoholButtonPanel.createUIElement(RECIPE_SELECTION_PANEL_WIDTH / 2, BUTTON_HEIGHT, false);
        currentButton = variableButtonAnchor.addButton("Archived", buttonId, baseColor, bgColour, Alignment.MID, CutStyle.TOP, RECIPE_SELECTION_PANEL_WIDTH  / 2 - opad, BUTTON_HEIGHT - 1, 0);
        entry = new FramedInteractionDialogCustomPanelPlugin.ButtonEntry(currentButton, buttonId) {
            @Override
            public void onToggle() {
                currentFilter = AlcoholListFilter.ARCHIVE;
                showPanel(dialogue);
            }
        };

        VisualCustomPanel.getPlugin().addButton(entry);
        createNewAlcoholButtonPanel.addUIElement(variableButtonAnchor).belowRight(lastUsedVariableButtonAnchor, spad);
        lastUsedVariableButtonAnchor = variableButtonAnchor;

        basePanel.addComponent(createNewAlcoholButtonPanel).inTL(0f,0f);

        //"Select Existing" List
        CustomPanelAPI selectExistingAlcoholListBasePanel = visualCustomPanel.createCustomPanel(RECIPE_SELECTION_PANEL_WIDTH, BASE_PANEL_MAX_HEIGHT - RECIPE_SELECTION_PANEL_HEIGHT, new FramedInteractionDialogCustomPanelPlugin(true));
        TooltipMakerAPI selectExistingAlcoholListTooltip = selectExistingAlcoholListBasePanel.createUIElement(RECIPE_SELECTION_PANEL_WIDTH, BASE_PANEL_MAX_HEIGHT - RECIPE_SELECTION_PANEL_HEIGHT, true);

        for (final CustomAlcohol alcohol : CustomAlcoholMemory.getInstanceOrRegister().getAll()){
            if (currentFilter == AlcoholListFilter.ACTIVE && alcohol.hidden) continue;
            if (currentFilter == AlcoholListFilter.ARCHIVE && !alcohol.hidden) continue;

            CustomPanelAPI alcoholEntry = selectExistingAlcoholListBasePanel.createCustomPanel(RECIPE_SELECTION_PANEL_WIDTH - opad, RECIPE_SELECTION_ENTRY_HEIGHT, new FramedInteractionDialogCustomPanelPlugin(true));
            TooltipMakerAPI image = alcoholEntry.createUIElement(80f, 80f, false);
            image.addImage(alcohol.iconName, RECIPE_SELECTION_PANEL_HEIGHT, 0f);
            alcoholEntry.addUIElement(image).inTL(0f, spad);

            float entryButtonHeight = 20f;
            float entryButtonWidth = 80f;

            TooltipMakerAPI name = alcoholEntry.createUIElement(RECIPE_SELECTION_PANEL_WIDTH - opad - RECIPE_SELECTION_PANEL_HEIGHT, RECIPE_SELECTION_PANEL_HEIGHT - entryButtonHeight - spad, false);
            name.addPara("%s", 0f, Misc.getHighlightColor(), alcohol.getName());

            int limit = alcohol.name.length() < 25 ? 40 : 20;
            String shortDesc = alcohol.shortDesc.length() > limit ? alcohol.shortDesc.substring(0, limit) + "..." : alcohol.shortDesc;
            name.addPara(shortDesc, Misc.getGrayColor(), 0f);
            alcoholEntry.addUIElement(name).rightOfTop(image, 0f);

            //entry buttons

            buttonId = "filter_entry_" + alcohol.uid;
            prerequisiteForActive = currentFilter == AlcoholListFilter.ARCHIVE;
            baseColor = prerequisiteForActive ? Misc.getButtonTextColor() : Misc.getTextColor();
            bgColour = prerequisiteForActive ? Misc.getDarkPlayerColor() : Misc.getGrayColor();
            textColor = prerequisiteForActive ? Misc.getTextColor() : Color.WHITE;

            variableButtonAnchor = alcoholEntry.createUIElement(entryButtonWidth, entryButtonHeight, false);
            currentButton = variableButtonAnchor.addButton((alcohol.hidden ? "Unarchive" : "Archive"), buttonId, baseColor, bgColour, Alignment.MID, CutStyle.ALL, entryButtonWidth, entryButtonHeight, 0);

            entry = new FramedInteractionDialogCustomPanelPlugin.ButtonEntry(currentButton, buttonId) {
                @Override
                public void onToggle() {
                    alcohol.setHidden(!alcohol.hidden);
                    showPanel(dialogue);
                }
            };

            VisualCustomPanel.getPlugin().addButton(entry);
            alcoholEntry.addUIElement(variableButtonAnchor).inBR(opad, opad);
            lastUsedVariableButtonAnchor = variableButtonAnchor;

            buttonId = "edit_entry_" + alcohol.uid;
            prerequisiteForActive = true;
            baseColor = prerequisiteForActive ? Misc.getButtonTextColor() : Misc.getTextColor();
            bgColour = prerequisiteForActive ? Misc.getDarkPlayerColor() : Misc.getGrayColor();
            textColor = prerequisiteForActive ? Misc.getTextColor() : Color.WHITE;
            entryButtonWidth = 70f;

            variableButtonAnchor = alcoholEntry.createUIElement(entryButtonWidth, entryButtonHeight, false);
            currentButton = variableButtonAnchor.addButton("Edit", buttonId, baseColor, bgColour, Alignment.MID, CutStyle.ALL, entryButtonWidth, entryButtonHeight, 0);
            entry = new FramedInteractionDialogCustomPanelPlugin.ButtonEntry(currentButton, buttonId) {
                @Override
                public void onToggle() {
                    display(alcohol);
                    showPanel(dialogue);
                }
            };

            VisualCustomPanel.getPlugin().addButton(entry);
            alcoholEntry.addUIElement(variableButtonAnchor).leftOfMid(lastUsedVariableButtonAnchor, spad);
            lastUsedVariableButtonAnchor = variableButtonAnchor;

            selectExistingAlcoholListTooltip.addCustom(alcoholEntry, spad);
        }

        selectExistingAlcoholListBasePanel.addUIElement(selectExistingAlcoholListTooltip).inTMid(0f);

        /*if (filterScrollPanels.containsKey(currentFilter)) selectExistingAlcoholListTooltip.setExternalScroller(filterScrollPanels.get(currentFilter)); //regenerates the scroller to be in the same pos as the player left it
        else filterScrollPanels.put(currentFilter, selectExistingAlcoholListTooltip.getExternalScroller());*/ //doesnt work and I dont care

        basePanel.addComponent(selectExistingAlcoholListBasePanel).belowLeft(createNewAlcoholButtonPanel, 0f);

        //"Make your Alcohol" Panel

        float width = BASE_PANEL_MAX_WIDTH - EFFECT_DISPLAY_PANEL_WIDTH - RECIPE_SELECTION_PANEL_WIDTH;
        CustomPanelAPI alcoholCreatorBasePanel = visualCustomPanel.createCustomPanel(width, BASE_PANEL_MAX_HEIGHT, new FramedInteractionDialogCustomPanelPlugin(true));
        CustomPanelAPI centerCirclePanel = visualCustomPanel.createCustomPanel(width, BASE_PANEL_MAX_HEIGHT, new MiddleCircleCustomPanelPlugin(Color.MAGENTA, (width - 100f) / 2, 0.5f)); //make the background circle
        alcoholCreatorBasePanel.addComponent(centerCirclePanel).inMid(); //add the background circle

        variableButtonAnchor = alcoholCreatorBasePanel.createUIElement(BASE_PANEL_MAX_WIDTH - EFFECT_DISPLAY_PANEL_WIDTH - RECIPE_SELECTION_PANEL_WIDTH, BUTTON_HEIGHT, false);
        variableButtonAnchor.addSectionHeading("Alcohol Creator Panel", Alignment.MID, 0f);
        alcoholCreatorBasePanel.addUIElement(variableButtonAnchor).inBMid(0f);

        basePanel.addComponent(alcoholCreatorBasePanel).rightOfTop(createNewAlcoholButtonPanel, 0f);

        //"Show Effect" Panel

        CustomPanelAPI showEffectBasePanel = visualCustomPanel.createCustomPanel(EFFECT_DISPLAY_PANEL_WIDTH, BASE_PANEL_MAX_HEIGHT, new FramedInteractionDialogCustomPanelPlugin(true));
        variableButtonAnchor = showEffectBasePanel.createUIElement(EFFECT_DISPLAY_PANEL_WIDTH, BUTTON_HEIGHT, false);
        variableButtonAnchor.addSectionHeading("Effect Display", Alignment.MID, 0f);
        showEffectBasePanel.addUIElement(variableButtonAnchor).inBMid(0f);
        basePanel.addComponent(showEffectBasePanel).rightOfTop(alcoholCreatorBasePanel, 0f);


        //finalize

        visualCustomPanel.addComponent(basePanel).inMid();
        VisualCustomPanel.addTooltipToPanel();
    }
}