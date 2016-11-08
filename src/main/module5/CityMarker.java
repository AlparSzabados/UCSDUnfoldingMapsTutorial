package main.module5;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * Implements a visual marker for cities on an earthquake map
 *
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 */
public class CityMarker extends CommonMarker {

    public static int TRI_SIZE = 5;  // The size of the triangle marker

    public CityMarker(Location location) {
        super(location);
    }


    public CityMarker(Feature city) {
        super(((PointFeature) city).getLocation(), city.getProperties());
        // Cities have properties: "name" (city name), "country" (country name)
        // and "population" (population, in millions)
    }


    /**
     * Implementation of method to draw marker on the map.
     */
    @Override
    public void drawMarker(PGraphics pg, float x, float y) {
        // Save previous drawing style
        pg.pushStyle();

        // IMPLEMENT: drawing triangle for each city
        pg.fill(150, 30, 30);
        pg.triangle(x, y - TRI_SIZE, x - TRI_SIZE, y + TRI_SIZE, x + TRI_SIZE, y + TRI_SIZE);

        // Restore previous drawing style
        pg.popStyle();
    }

    /**
     * Show the title of the city if this marker is selected
     */
    public void showTitle(PGraphics pg, float x, float y) {
        String title = "City: " + getCity() + ", Country: " + getCountry() + ", Population: " + getPopulation() + " Mill.";
        pg.pushStyle();
        pg.fill(255, 255, 255);
        pg.rectMode(PConstants.CORNER);
        pg.rect(x + 9, y - 15, Math.max(pg.textWidth(title), 0) + 2, 20);
        pg.fill(0);
        pg.textSize(14);
        pg.text(title, x + 10, y);
        pg.popStyle();
    }


    /* Local getters for some city properties.
     */
    public String getCity() {
        return getStringProperty("name");
    }

    public String getCountry() {
        return getStringProperty("country");
    }

    public float getPopulation() {
        return Float.parseFloat(getStringProperty("population"));
    }
}
