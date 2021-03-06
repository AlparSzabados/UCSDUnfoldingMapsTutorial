package main.earthquakeMap;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

/**
 * Implements a visual marker for land earthquakes on an earthquake map
 *
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Szabados Alpar
 */
public class LandQuakeMarker extends EarthquakeMarker {

    public LandQuakeMarker(PointFeature quake) {
        super(quake);
        isOnLand = true;
    }

    @Override
    public void drawEarthquake(PGraphics pg, float x, float y) {
        int buffer = 2;
        pg.ellipse(x, y, buffer * radius, buffer * radius);
    }

    public String getCountry() {
        return (String) getProperty("country");
    }
}