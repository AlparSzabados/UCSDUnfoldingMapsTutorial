package main.module4;

import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PGraphics;

import java.util.HashMap;

import static java.lang.Float.parseFloat;

/**
 * Implements a visual marker for earthquakes on an earthquake map
 *
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Szabados Alpar
 */
public abstract class EarthquakeMarker extends SimplePointMarker {

    protected boolean isOnLand;

    protected float radius;

    public static final float DEPTH_INTERMEDIATE = 70;

    public static final float DEPTH_DEEP = 300;

    public abstract void drawEarthquake(PGraphics pg, float x, float y);

    public EarthquakeMarker(PointFeature feature) {
        super(feature.getLocation());
        HashMap<String, Object> properties = feature.getProperties();
        float magnitude = parseFloat(properties.get("magnitude").toString());
        properties.put("radius", 2 * magnitude);
        setProperties(properties);
        this.radius = 1.75f * getMagnitude();
    }

    public void draw(PGraphics pg, float x, float y) {
        pg.pushStyle();
        colorDetermine(pg);
        drawEarthquake(pg, x, y);

        String age = getStringProperty("age");
        if ("Past Hour".equals(age) || "Past Day".equals(age)) {
            pg.strokeWeight(2);
            int buffer = 2;
            float bufferAndRadius = radius + buffer;

            pg.line(x - bufferAndRadius,
                    y - bufferAndRadius,
                    x + bufferAndRadius,
                    y + bufferAndRadius);
            pg.line(x - bufferAndRadius,
                    y + bufferAndRadius,
                    x + bufferAndRadius,
                    y - bufferAndRadius);
        }

        pg.popStyle();
    }

    private void colorDetermine(PGraphics pg) {
        float magnitude = getDepth();
        if (magnitude < DEPTH_INTERMEDIATE) {
            pg.fill(250, 220, 0);
        } else if (magnitude < DEPTH_DEEP) {
            pg.fill(0, 0, 220);
        } else {
            pg.fill(220, 0, 0);
        }
    }

    public float getMagnitude() {
        return parseFloat(getProperty("magnitude").toString());
    }

    public float getDepth() {
        return parseFloat(getProperty("depth").toString());
    }

    public String getTitle() {
        return (String) getProperty("title");
    }

    public float getRadius() {
        return parseFloat(getProperty("radius").toString());
    }

    public boolean isOnLand() {
        return isOnLand;
    }


}
