package com.github.jamestkhan.recast.debug;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * @author JamesTKhan
 * @version August 22, 2022
 */
public class CustomShapeRenderer extends ShapeRenderer {

    public void circle (float x, float y, float z, float radius) {
        circle(x, y, z, radius, Math.max(1, (int)(6 * (float)Math.cbrt(radius))));
    }

    public void circle (float x, float y, float z, float radius, int segments) {
        if (segments <= 0) throw new IllegalArgumentException("segments must be > 0.");
        float colorBits = getColor().toFloatBits();
        float angle = 2 * MathUtils.PI / segments;
        float cos = MathUtils.cos(angle);
        float sin = MathUtils.sin(angle);
        float cx = radius, cy = 0;

        if (getCurrentType() == ShapeType.Line) {
            //check(ShapeType.Line, ShapeType.Filled, segments * 2 + 2);
            for (int i = 0; i < segments; i++) {
                getRenderer().color(colorBits);
                getRenderer().vertex(x + cx, y + cy, z);
                float temp = cx;
                cx = cos * cx - sin * cy;
                cy = sin * temp + cos * cy;
                getRenderer().color(colorBits);
                getRenderer().vertex(x + cx, y + cy, z);
            }
            // Ensure the last segment is identical to the first.
            getRenderer().color(colorBits);
            getRenderer().vertex(x + cx, y + cy, z);
        } else {
            //check(ShapeType.Line, ShapeType.Filled, segments * 3 + 3);
            segments--;
            for (int i = 0; i < segments; i++) {
                getRenderer().color(colorBits);
                getRenderer().vertex(x, y, z);
                getRenderer().color(colorBits);
                getRenderer().vertex(x + cx, y + cy, z);
                float temp = cx;
                cx = cos * cx - sin * cy;
                cy = sin * temp + cos * cy;
                getRenderer().color(colorBits);
                getRenderer().vertex(x + cx, y + cy, z);
            }
            // Ensure the last segment is identical to the first.
            getRenderer().color(colorBits);
            getRenderer().vertex(x, y, z);
            getRenderer().color(colorBits);
            getRenderer().vertex(x + cx, y + cy, z);
        }

        cx = radius;
        cy = 0;
        getRenderer().color(colorBits);
        getRenderer().vertex(x + cx, y + cy, z);
    }

}
