package main.earthquakeMap;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PConstants;
import processing.core.PGraphics;

public abstract class EarthquakeMarker extends CommonMarker {

    protected boolean isOnLand;

    protected float radius;

    protected static final float kmPerMile = 1.6f;

    public static final float THRESHOLD_INTERMEDIATE = 70;

    public static final float THRESHOLD_DEEP = 300;

    public abstract void drawEarthquake(PGraphics pg, float x, float y);

    public EarthquakeMarker(PointFeature feature) {
        super(feature.getLocation());
        // Add a radius property and then set the properties
        java.util.HashMap<String, Object> properties = feature.getProperties();
        float magnitude = Float.parseFloat(properties.get("magnitude").toString());
        properties.put("radius", 2 * magnitude);
        setProperties(properties);
        this.radius = 1.75f * getMagnitude();
    }

    /* calls abstract method drawEarthquake and then checks age and draws X if needed */
    @Override
    public void drawMarker(PGraphics pg, float x, float y) {
        pg.pushStyle();

        colorDetermine(pg);

        drawEarthquake(pg, x, y);

        String age = getStringProperty("age");
        if ("Past Hour".equals(age) || "Past Day".equals(age)) {
            pg.strokeWeight(2);
            int buffer = 2;
            pg.line(x - (radius + buffer),
                    y - (radius + buffer),
                    x + radius + buffer,
                    y + radius + buffer);
            pg.line(x - (radius + buffer),
                    y + (radius + buffer),
                    x + radius + buffer,
                    y - (radius + buffer));
        }

        pg.popStyle();
    }

    /* Show the title of the earthquake if this marker is selected */
    @Override
    public void showTitle(PGraphics pg, float x, float y) {
        String title = getTitle() + ", Magnitude: " + getMagnitude() + ", Depth: " + getDepth() + " Km.";
        pg.pushStyle();
        pg.fill(255, 255, 255);
        pg.rectMode(PConstants.CORNER);
        pg.rect(x + 9, y - 15, Math.max(pg.textWidth(title), 0) + 2, 20);
        pg.fill(0);
        pg.textSize(14);
        pg.text(title, x + 10, y);
        pg.popStyle();
    }

    public double threatCircle() {
        double miles = 20.0f * Math.pow(1.8, 2 * getMagnitude() - 5);
        double km = (miles * kmPerMile);
        return km;
    }

    private void colorDetermine(PGraphics pg) {
        float depth = getDepth();
        if (depth < THRESHOLD_INTERMEDIATE) {
            pg.fill(255, 255, 0);
        } else if (depth < THRESHOLD_DEEP) {
            pg.fill(0, 0, 255);
        } else {
            pg.fill(255, 0, 0);
        }
    }

    public float getMagnitude() {
        return Float.parseFloat(getProperty("magnitude").toString());
    }

    public float getDepth() {
        return Float.parseFloat(getProperty("depth").toString());
    }

    public String getTitle() {
        return (String) getProperty("title");
    }

    public float getRadius() {
        return Float.parseFloat(getProperty("radius").toString());
    }

    public boolean isOnLand() {
        return isOnLand;
    }
}
