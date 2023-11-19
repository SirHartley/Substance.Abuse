package com.fs.starfarer.api.alcoholism.ui.basepanel;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class MiddleCircleCustomPanelPlugin implements CustomUIPanelPlugin {

    protected PositionAPI pos;
    public Color color;
    public float radius;
    public float alpha;

    public MiddleCircleCustomPanelPlugin(Color color, float radius, float alpha) {
        this.alpha = alpha;
        this.color = color;
        this.radius = radius;
    }

    @Override
    public void positionChanged(PositionAPI pos) {
        this.pos = pos;
    }

    private void drawCircle(int numPoints, Vector2f center, float radius, float angleOffset, float alphaMult) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0.3f * alphaMult * this.alpha);

        float angleDiffBetweenPoints = 360 / (float) numPoints;
        glBegin(GL_LINE_LOOP);
        for (int i = 0; i < numPoints; i++) {
            Vector2f pointLoc3 = MathUtils.getPointOnCircumference(center, radius, (i * angleDiffBetweenPoints) + angleOffset);
            glVertex2f(pointLoc3.x, pointLoc3.y);

            Vector2f pointLoc4 = MathUtils.getPointOnCircumference(center, radius - 10, (i * angleDiffBetweenPoints) + angleOffset);
            glVertex2f(pointLoc4.x, pointLoc4.y);
        }
        glEnd();

        GL11.glPopMatrix();
    }

    @Override
    public void renderBelow(float alphaMult) {
    }

    @Override
    public void render(float alphaMult) {
        drawCircle(360, new Vector2f(pos.getCenterX(), pos.getCenterY()), radius, 0f, alphaMult);
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
