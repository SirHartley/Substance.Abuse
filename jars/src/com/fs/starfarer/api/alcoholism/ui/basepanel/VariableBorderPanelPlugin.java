package com.fs.starfarer.api.alcoholism.ui.basepanel;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

public class VariableBorderPanelPlugin implements CustomUIPanelPlugin {

    protected PositionAPI pos;
    public Color color;
    public boolean[] lines;

    public VariableBorderPanelPlugin(Color color, boolean left, boolean top, boolean right, boolean bottom) {
        this.color = color;
        this.lines = new boolean[]{
                left,
                top,
                right,
                bottom
        };
    }

    @Override
    public void positionChanged(PositionAPI pos) {
        this.pos = pos;
    }

    public void renderBox(float x, float y, float w, float h, float alphaMult) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0.3f * alphaMult);

        float[] points = new float[]{
                0, 0, //bottom left
                0, h, //top left
                w, h, //top right
                w, 0, //bot right
                0, 0 //bottom left
        };

        for (int i = 0; i < 4; i++) {
            GL11.glBegin(GL11.GL_LINES);

            if (lines[i]) {
                //left line 0, 1 | 2, 3
                //top line 2, 3 | 4, 5
                //right line 4, 5 | 6, 7
                //bot line  6, 7 | 8, 9

                int index = i * 2;

                GL11.glVertex2f(points[index] + x, points[index + 1] + y);
                GL11.glVertex2f(points[index + 2] + x, points[index + 3] + y);
            }

            GL11.glEnd();
        }

        GL11.glPopMatrix();
    }

    @Override
    public void renderBelow(float alphaMult) {
    }

    @Override
    public void render(float alphaMult) {
        float x = pos.getX();
        float y = pos.getY();
        float w = pos.getWidth();
        float h = pos.getHeight();

        renderBox(x, y, w, h, alphaMult);
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void processInput(List<InputEventAPI> arg0) {
    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}