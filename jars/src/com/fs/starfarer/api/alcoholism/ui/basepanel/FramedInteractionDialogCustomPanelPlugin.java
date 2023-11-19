package com.fs.starfarer.api.alcoholism.ui.basepanel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.ButtonAPI;

import java.util.*;

/**
 * @Author Histidine
 */
public class FramedInteractionDialogCustomPanelPlugin extends FramedCustomPanelPlugin {

    protected List<ButtonEntry> buttons = new LinkedList<>();
    protected boolean showBorder;

    public FramedInteractionDialogCustomPanelPlugin(boolean withBorder) {
        super(1f, Global.getSector().getPlayerFaction().getBaseUIColor(), false);
        this.showBorder = withBorder;
    }

    @Override
    public void render(float alphaMult) {
        if (showBorder) super.render(alphaMult);
    }

    public void addButton(ButtonEntry entry) {
        buttons.add(entry);
    }

    public void checkButtons() {
        for (ButtonEntry button : buttons) {
            button.checkButton();
        }
    }

    @Override
    public void advance(float amount) {
        checkButtons();
    }

    @Override
    public void processInput(List<InputEventAPI> input) {
    }

    public static abstract class ButtonEntry {
        public ButtonAPI button;
        public boolean state;
        public String id;

        public ButtonEntry() {
        }

        public ButtonEntry(ButtonAPI button, String id) {
            this.button = button;
            this.id = id;
            state = button.isChecked();
        }

        public void setState(boolean state) {
            this.state = state;
            button.setChecked(state);
        }

        public void checkButton() {
            if (state != button.isChecked()) {
                state = button.isChecked();
                onToggle();
            }
        }

        public abstract void onToggle();
    }

    public static class RadioButtonEntry extends ButtonEntry {

        public List<RadioButtonEntry> buttons;

        public RadioButtonEntry(ButtonAPI button, String id) {
            super(button, id);
        }

        @Override
        public void onToggle() {
            for (RadioButtonEntry entry : buttons) {
                if (entry != this) {
                    entry.setState(false);
                    //Global.getLogger(this.getClass()).info("Toggling other button " + entry.id);
                } else entry.setState(true);
            }
            onToggleImpl();
        }

        public void onToggleImpl() {

        }
    }
}
