package com.fs.starfarer.api.alcoholism.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.ModPlugin;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.alcoholism.memory.CustomAlcohol;
import com.fs.starfarer.api.alcoholism.memory.CustomAlcoholMemory;
import com.fs.starfarer.api.alcoholism.memory.Ingredient;
import com.fs.starfarer.api.alcoholism.ui.basepanel.FramedInteractionDialogCustomPanelPlugin;
import com.fs.starfarer.api.alcoholism.ui.basepanel.MiddleCircleCustomPanelPlugin;
import com.fs.starfarer.api.alcoholism.ui.basepanel.VariableBorderPanelPlugin;
import com.fs.starfarer.api.alcoholism.ui.basepanel.VisualCustomPanel;
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;

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
    protected static final float INNER_SELECTOR_SQUARE_SIZE = 80f;
    protected static final float OUTER_SELECTOR_SQUARE_SIZE = 89f;
    protected static final float SELECTOR_SQUARE_PAD_INCREMENT = 2f;

    private Industry industry;
    private List<Ingredient> ingredients = new LinkedList<>();
    private AlcoholListFilter currentFilter = AlcoholListFilter.ACTIVE;
    private Map<AlcoholListFilter, ScrollPanelAPI> filterScrollPanels = new HashMap<>();
    private String name = "...";

    public enum AlcoholListFilter {
        ACTIVE,
        ARCHIVE
    }

    public CustomAlcoholPanel(Industry industry) {
        this.industry = industry;
    }

    public void showPanel(InteractionDialogAPI dialogue) {
        VisualCustomPanel.createPanel(dialogue, true);
        showCustomPanel(dialogue);
    }

    private void fixBasePanelOffsets() {
        //this sets the base panel to 0,0
        //don't question it

        CustomPanelAPI basePanel = VisualCustomPanel.getPanel();
        PositionAPI pos = basePanel.getPosition();
        pos.setSize(Global.getSettings().getScreenWidth(), Global.getSettings().getScreenHeight());
        pos.setYAlignOffset(pos.getY() <= 0 ? -pos.getY() : pos.getY());
        pos.setXAlignOffset(pos.getX() >= 0 ? -pos.getX() + 10 : pos.getX() - 10);
    }

    public void display(CustomAlcohol custom) {
        if (custom == null) {
            ingredients.clear();
            name = "...";

            //clear panel
        }

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

        variableButtonAnchor = createNewAlcoholButtonPanel.createUIElement(RECIPE_SELECTION_PANEL_WIDTH / 2, BUTTON_HEIGHT, false);
        currentButton = variableButtonAnchor.addButton("Archived", buttonId, baseColor, bgColour, Alignment.MID, CutStyle.TOP, RECIPE_SELECTION_PANEL_WIDTH / 2 - opad, BUTTON_HEIGHT - 1, 0);
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

        basePanel.addComponent(createNewAlcoholButtonPanel).inTL(0f, 0f);

        //"Select Existing" List
        CustomPanelAPI selectExistingAlcoholListBasePanel = visualCustomPanel.createCustomPanel(RECIPE_SELECTION_PANEL_WIDTH, BASE_PANEL_MAX_HEIGHT - RECIPE_SELECTION_PANEL_HEIGHT, new FramedInteractionDialogCustomPanelPlugin(true));
        TooltipMakerAPI selectExistingAlcoholListTooltip = selectExistingAlcoholListBasePanel.createUIElement(RECIPE_SELECTION_PANEL_WIDTH, BASE_PANEL_MAX_HEIGHT - RECIPE_SELECTION_PANEL_HEIGHT, true);

        for (final CustomAlcohol alcohol : CustomAlcoholMemory.getInstanceOrRegister().getAll()) {
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
            baseColor = Misc.getButtonTextColor();
            bgColour = Misc.getDarkPlayerColor();
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
        CustomPanelAPI centerCirclePanel = visualCustomPanel.createCustomPanel(width - 100, width - 100, new MiddleCircleCustomPanelPlugin(Color.MAGENTA, (width - 100f) / 2, 0.5f)); //make the background circle

        int squareAmt = 4;
        float padBetweenSquares = (width - 100 - INNER_SELECTOR_SQUARE_SIZE * squareAmt) / (squareAmt - 1);
        CustomPanelAPI lastSiblingSquare = null;

        //add the center heat gauge
        float heatGaugeWidth = width - 200;
        float heatGaugeHeight = BUTTON_HEIGHT + opad + 1;
        CustomPanelAPI heatGaugePanel = centerCirclePanel.createCustomPanel(heatGaugeWidth, heatGaugeHeight, new FramedInteractionDialogCustomPanelPlugin(true));
        centerCirclePanel.addComponent(heatGaugePanel).inTMid(INNER_SELECTOR_SQUARE_SIZE * 2 + BUTTON_HEIGHT);

        CustomPanelAPI textLabel = heatGaugePanel.createCustomPanel(OUTER_SELECTOR_SQUARE_SIZE + opad, BUTTON_HEIGHT, new FramedInteractionDialogCustomPanelPlugin(false));
        TooltipMakerAPI label = textLabel.createUIElement(OUTER_SELECTOR_SQUARE_SIZE + opad, BUTTON_HEIGHT, false);
        label.addSectionHeading("Heat", Alignment.MID, 0f);
        textLabel.addUIElement(label).inMid();
        heatGaugePanel.addComponent(textLabel).inTMid(-BUTTON_HEIGHT);

        //add the ingredient squares
        for (int i = 0; i < squareAmt; i++) {

            CustomPanelAPI innerSquare = centerCirclePanel.createCustomPanel(INNER_SELECTOR_SQUARE_SIZE, INNER_SELECTOR_SQUARE_SIZE, new FramedInteractionDialogCustomPanelPlugin(false));

            //create inner button for ingredient
            buttonId = "ingredient_" + i;
            baseColor = Misc.getButtonTextColor();
            bgColour = Misc.getDarkPlayerColor();
            textColor = Misc.getTextColor();

            variableButtonAnchor = innerSquare.createUIElement(INNER_SELECTOR_SQUARE_SIZE, INNER_SELECTOR_SQUARE_SIZE, false);
            currentButton = variableButtonAnchor.addAreaCheckbox("", buttonId, baseColor, bgColour, textColor, INNER_SELECTOR_SQUARE_SIZE, INNER_SELECTOR_SQUARE_SIZE, 0f, false);

            final ButtonAPI finalCurrentButton = currentButton;
            entry = new FramedInteractionDialogCustomPanelPlugin.ButtonEntry(finalCurrentButton, buttonId) {
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

                    finalCurrentButton.setChecked(false);
                }
            };

            VisualCustomPanel.getPlugin().addButton(entry);
            innerSquare.addUIElement(variableButtonAnchor).inTL(-4f, -1f);

            //create outer square and add on top
            CustomPanelAPI outerSquare = innerSquare.createCustomPanel(OUTER_SELECTOR_SQUARE_SIZE, OUTER_SELECTOR_SQUARE_SIZE, new FramedInteractionDialogCustomPanelPlugin(true));
            innerSquare.addComponent(outerSquare).inMid();

            //text label
            textLabel = innerSquare.createCustomPanel(OUTER_SELECTOR_SQUARE_SIZE + opad, BUTTON_HEIGHT, new FramedInteractionDialogCustomPanelPlugin(false));
            label = textLabel.createUIElement(OUTER_SELECTOR_SQUARE_SIZE + opad, BUTTON_HEIGHT, false);
            label.addSectionHeading("Ingredient " + (i + 1), Alignment.MID, 0f);
            textLabel.addUIElement(label).inMid();
            innerSquare.addComponent(textLabel).inTMid(-BUTTON_HEIGHT - spad);

            //add line to heat gauge

            if (i == 0) centerCirclePanel.addComponent(innerSquare).inTL(0f, INNER_SELECTOR_SQUARE_SIZE / 2);
            else centerCirclePanel.addComponent(innerSquare).rightOfMid(lastSiblingSquare, padBetweenSquares);
            lastSiblingSquare = innerSquare;

            float squareCenterX = outerSquare.getPosition().getCenterX();
            float gaugeCenterX = heatGaugePanel.getPosition().getCenterX();

            float lineHeight;
            float lineWidth;

            if (squareCenterX < gaugeCenterX) {
                //pos is further to the left, |_

                boolean outsideGaugeBorder = squareCenterX < gaugeCenterX - heatGaugeWidth / 2;

                float squareBottomCenterX = squareCenterX - opad;
                float squareBottomCenterY = outerSquare.getPosition().getCenterY() - (OUTER_SELECTOR_SQUARE_SIZE - 1) / 2;

                float gaugeLeftBorderCenterX = gaugeCenterX - (heatGaugeWidth / 2);
                float gaugeCenterY = heatGaugePanel.getPosition().getCenterY();

                if (!outsideGaugeBorder) gaugeCenterY += heatGaugeHeight / 2;

                lineHeight = squareBottomCenterY - gaugeCenterY;
                lineWidth = outsideGaugeBorder ? gaugeLeftBorderCenterX - squareBottomCenterX : 1;

                CustomPanelAPI connectorLinePanel = innerSquare.createCustomPanel(lineWidth, lineHeight, new VariableBorderPanelPlugin(Misc.getButtonTextColor(), true, false, false, outsideGaugeBorder));
                innerSquare.addComponent(connectorLinePanel).belowMid(outerSquare, 0f);

            } else if (squareCenterX >= gaugeCenterX) {
                //pos is further to the right, _|

                boolean outsideGaugeBorder = squareCenterX > gaugeCenterX + heatGaugeWidth / 2;

                float squareBottomCenterX = squareCenterX + opad;
                float squareBottomCenterY = outerSquare.getPosition().getCenterY() - (OUTER_SELECTOR_SQUARE_SIZE - 1) / 2;

                float gaugeRightBorderCenterX = gaugeCenterX + (heatGaugeWidth / 2);
                float gaugeCenterY = heatGaugePanel.getPosition().getCenterY();

                if (!outsideGaugeBorder) gaugeCenterY += heatGaugeHeight / 2;

                lineHeight = squareBottomCenterY - gaugeCenterY;
                lineWidth = outsideGaugeBorder ? gaugeRightBorderCenterX - squareBottomCenterX : 1;

                CustomPanelAPI connectorLinePanel = innerSquare.createCustomPanel(lineWidth, lineHeight, new VariableBorderPanelPlugin(Misc.getButtonTextColor(), true, false, false, outsideGaugeBorder));
                innerSquare.addComponent(connectorLinePanel).belowMid(outerSquare, 0f);
            }
        }

        //add the image selector
        CustomPanelAPI visualChangerPanelInnerSquare = centerCirclePanel.createCustomPanel(INNER_SELECTOR_SQUARE_SIZE, INNER_SELECTOR_SQUARE_SIZE, new FramedInteractionDialogCustomPanelPlugin(false));

        //create inner button for ingredient
        buttonId = "image_selection";
        baseColor = Misc.getButtonTextColor();
        bgColour = Misc.getDarkPlayerColor();
        textColor = Misc.getTextColor();

        variableButtonAnchor = visualChangerPanelInnerSquare.createUIElement(INNER_SELECTOR_SQUARE_SIZE, INNER_SELECTOR_SQUARE_SIZE, false);
        currentButton = variableButtonAnchor.addAreaCheckbox("", buttonId, baseColor, bgColour, textColor, INNER_SELECTOR_SQUARE_SIZE, INNER_SELECTOR_SQUARE_SIZE, 0f, false);

        final ButtonAPI finalCurrentButton = currentButton;
        entry = new FramedInteractionDialogCustomPanelPlugin.ButtonEntry(finalCurrentButton, buttonId) {
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

                finalCurrentButton.setChecked(false);
            }
        };

        VisualCustomPanel.getPlugin().addButton(entry);
        visualChangerPanelInnerSquare.addUIElement(variableButtonAnchor).inTL(-4f, -1f);

        //create outer square and add on top
        CustomPanelAPI outerSquare = visualChangerPanelInnerSquare.createCustomPanel(OUTER_SELECTOR_SQUARE_SIZE, OUTER_SELECTOR_SQUARE_SIZE, new FramedInteractionDialogCustomPanelPlugin(true));
        visualChangerPanelInnerSquare.addComponent(outerSquare).inMid();

        //text label
        textLabel = visualChangerPanelInnerSquare.createCustomPanel(OUTER_SELECTOR_SQUARE_SIZE + opad, BUTTON_HEIGHT, new FramedInteractionDialogCustomPanelPlugin(false));
        label = textLabel.createUIElement(OUTER_SELECTOR_SQUARE_SIZE + opad, BUTTON_HEIGHT, false);
        label.addSectionHeading("Visuals", Alignment.MID, 0f);
        textLabel.addUIElement(label).inMid();
        visualChangerPanelInnerSquare.addComponent(textLabel).inTMid(-BUTTON_HEIGHT - spad);

        //add line to heat gauge

        centerCirclePanel.addComponent(visualChangerPanelInnerSquare).belowMid(heatGaugePanel, INNER_SELECTOR_SQUARE_SIZE);

        // todo add lines

        /*float squareCenterX = outerSquare.getPosition().getCenterX();
        float gaugeCenterX = heatGaugePanel.getPosition().getCenterX();

        float lineHeight;
        float lineWidth;

        //|_

        float gaugeLeftCenterX = gaugeCenterX - OUTER_SELECTOR_SQUARE_SIZE;
        float gaugeBottomY = heatGaugePanel.getPosition().getCenterY() - heatGaugeHeight / 2;

        float squareLeftCenterX = squareCenterX - (OUTER_SELECTOR_SQUARE_SIZE + 1) / 2 - opad;
        float squareCenterY = outerSquare.getPosition().getCenterY();

        lineHeight = gaugeBottomY - squareCenterY;
        lineWidth = gaugeLeftCenterX - squareLeftCenterX;

        CustomPanelAPI connectorLinePanel = centerCirclePanel.createCustomPanel(lineWidth, lineHeight, new VariableBorderPanelPlugin(Misc.getButtonTextColor(), true, false, false, true));
        innerSquare.addComponent(connectorLinePanel).leftOfMid(outerSquare, -OUTER_SELECTOR_SQUARE_SIZE / 2);

        //_|

        float gaugeRightCenterX = gaugeCenterX + OUTER_SELECTOR_SQUARE_SIZE;
        float squareRightCenterX = squareCenterX + (OUTER_SELECTOR_SQUARE_SIZE + 1) / 2;

        lineHeight = gaugeBottomY - squareCenterY;
        lineWidth = gaugeRightCenterX - squareRightCenterX;

        connectorLinePanel = innerSquare.createCustomPanel(lineWidth, lineHeight, new VariableBorderPanelPlugin(Misc.getButtonTextColor(), true, false, false, true));
        innerSquare.addComponent(connectorLinePanel).rightOfMid(outerSquare, OUTER_SELECTOR_SQUARE_SIZE / 2);*/

        //add name panel

        float namePanelWidth = width - 300;
        float namePanelHeight = BUTTON_HEIGHT + opad + 1;
        CustomPanelAPI namePanelPanel = centerCirclePanel.createCustomPanel(namePanelWidth, namePanelHeight, new FramedInteractionDialogCustomPanelPlugin(false));
        TooltipMakerAPI namePanelTextfieldHolder = namePanelPanel.createUIElement(namePanelWidth, namePanelHeight, false);

        TextFieldAPI namePanelTextfield = namePanelTextfieldHolder.addTextField(namePanelWidth, namePanelHeight, Fonts.ORBITRON_16, 0f); //todo save and read!
        namePanelTextfield.setMidAlignment();
        namePanelTextfield.setBgColor(Misc.getDarkPlayerColor().darker());
        namePanelTextfield.setText(AlcoholRepo.CUSTOM_ALCOHOL_NAME_LIST.get(MathUtils.getRandomNumberInRange(0, AlcoholRepo.CUSTOM_ALCOHOL_NAME_LIST.size() - 1))); //todo this is temp, replace with actual name algo

        //todo add randomization buttons for name and visuals

        namePanelPanel.addUIElement(namePanelTextfieldHolder);
        centerCirclePanel.addComponent(namePanelPanel).belowMid(visualChangerPanelInnerSquare, 50f);

        //BREW ! button

        CustomPanelAPI brewButtonPanel = centerCirclePanel.createCustomPanel(SELECT_BUTTON_WIDTH, BUTTON_HEIGHT, new FramedInteractionDialogCustomPanelPlugin(false));

        buttonId = "brew_button";
        prerequisiteForActive = true; //todo: All fields Filled
        baseColor = prerequisiteForActive ? Misc.getButtonTextColor() : Misc.getTextColor();
        bgColour = prerequisiteForActive ? new Color(50, 130, 0, 255) : Misc.getGrayColor();

        variableButtonAnchor = brewButtonPanel.createUIElement(SELECT_BUTTON_WIDTH, BUTTON_HEIGHT, false);
        currentButton = variableButtonAnchor.addButton("BREW!", buttonId, baseColor, bgColour, Alignment.MID, CutStyle.ALL, SELECT_BUTTON_WIDTH, BUTTON_HEIGHT, 0);
        currentButton.setEnabled(prerequisiteForActive);
        entry = new FramedInteractionDialogCustomPanelPlugin.ButtonEntry(currentButton, buttonId) {
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
        brewButtonPanel.addUIElement(variableButtonAnchor).inMid();

        centerCirclePanel.addComponent(brewButtonPanel).belowMid(namePanelPanel, 50f);

        //add the center circle panel

        alcoholCreatorBasePanel.addComponent(centerCirclePanel).inMid(); //add the background circle

        /*variableButtonAnchor = alcoholCreatorBasePanel.createUIElement(BASE_PANEL_MAX_WIDTH - EFFECT_DISPLAY_PANEL_WIDTH - RECIPE_SELECTION_PANEL_WIDTH, opad + 1, false);
        variableButtonAnchor.addSectionHeading("Alcohol Creator", Alignment.MID, 0f);
        alcoholCreatorBasePanel.addUIElement(variableButtonAnchor).inTMid(0f);*/

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