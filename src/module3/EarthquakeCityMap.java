package module3;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.GeoMapApp;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 *
 * @author Szabados Alpar
 *         Date: October 28, 2016
 */
public class EarthquakeCityMap extends PApplet {

    private static final long serialVersionUID = 1L;

    private static final float THRESHOLD_MODERATE = 5;
    private static final float THRESHOLD_LIGHT = 4;
    private UnfoldingMap map;
    private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

    public void setup() {
        size(950, 600, OPENGL);

        map = new UnfoldingMap(this, 200, 50, 700, 500, new GeoMapApp.TopologicalGeoMapProvider());

        map.zoomToLevel(2);
        MapUtils.createDefaultEventDispatcher(this, map);

        int yellow = color(255, 255, 0), blue = color(0, 0, 255), red = color(255, 0, 0);
        int blueRadius = 10, yellowRadius = 15, redRadius = 20;
        List<Marker> markers = ParseFeed.parseEarthquake(this, earthquakesURL).stream()
                                        .map(eq -> new SimplePointMarker(eq.getLocation(), eq.getProperties()))
                                        .peek(changeMarkerColorAndRadius(blue, yellow, red, blueRadius, yellowRadius, redRadius))
                                        .collect(Collectors.toList());
        map.addMarkers(markers);
    }

    private Consumer<SimplePointMarker> changeMarkerColorAndRadius(int blue, int yellow, int red, int blueRadius, int yellowRadius, int redRadius) {
        return marker -> {
            float magnitude = getMagnitude(marker);
            if (magnitude < THRESHOLD_LIGHT) {
                marker.setColor(blue);
                marker.setRadius(blueRadius);
            } else if (magnitude < THRESHOLD_MODERATE) {
                marker.setColor(yellow);
                marker.setRadius(yellowRadius);
            } else {
                marker.setColor(red);
                marker.setRadius(redRadius);
            }
        };
    }

    private float getMagnitude(Marker magnitude) {
        return Float.parseFloat(magnitude.getProperty("magnitude").toString());
    }

    public void draw() {
        background(10);
        map.draw();
        addKey();
    }

    private void addKey() {
        fill(255, 250, 240);
        rect(25, 50, 150, 250, 7);

        fill(0);
        textAlign(LEFT, CENTER);
        textSize(12);
        text("Earthquake Key", 50, 75);

        fill(255, 0, 0);
        ellipse(50, 125, 20, 20);
        fill(250, 250, 0);
        ellipse(50, 160, 17, 17);
        fill(0, 0, 250);
        ellipse(50, 190, 10, 10);

        fill(0);
        textSize(12);
        text("5.0+ Magnitude", 72, 125);
        text("4.0+ Magnitude", 72, 160);
        text("Below 4.0", 72, 190);
    }

    static public void main(String... args) {
        PApplet.main("module3.EarthquakeCityMap");
    }
}
