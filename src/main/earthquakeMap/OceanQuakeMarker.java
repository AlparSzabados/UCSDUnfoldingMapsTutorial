package main.earthquakeMap;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

/**
 * Implements a visual marker for ocean earthquakes on an earthquake map
 *
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Szabados Alpar
 */
public class OceanQuakeMarker extends EarthquakeMarker {

    public OceanQuakeMarker(PointFeature quake) {
        super(quake);
        isOnLand = false;
    }

    @Override
    public void drawEarthquake(PGraphics pg, float x, float y) {
        int buffer = 2;
        pg.rect(x - radius, y - radius, buffer * radius, buffer * radius);
    }
}
