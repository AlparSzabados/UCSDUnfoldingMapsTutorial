package main.module4;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.GeoMapApp;
import de.fhpotsdam.unfolding.utils.MapUtils;
import main.parsing.ParseFeed;
import processing.core.PApplet;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

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

    private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

    private String cityFile = "city-data.json";
    private String countryFile = "countries.geo.json";

    private UnfoldingMap map;

    private List<Marker> cityMarkers;
    private List<Marker> quakeMarkers;
    private List<Marker> countryMarkers;

    public void setup() {
        size(900, 700, OPENGL);

        map = new UnfoldingMap(this, 200, 50, 650, 600, new GeoMapApp.TopologicalGeoMapProvider());
        MapUtils.createDefaultEventDispatcher(this, map);

        countryMarkers = MapUtils.createSimpleMarkers(GeoJSONReader.loadData(this, countryFile));

        cityMarkers = GeoJSONReader.loadData(this, cityFile).stream()
                                   .map(CityMarker::new)
                                   .collect(toList());

        quakeMarkers = ParseFeed.parseEarthquake(this, earthquakesURL).stream()
                                .peek(f -> addCountryParameter(f, countryMarkers))
                                .map(f -> isOnLand(f) ? new LandQuakeMarker(f) : new OceanQuakeMarker(f))
                                .collect(toList());

        map.addMarkers(quakeMarkers);
        map.addMarkers(cityMarkers);

        printQuakes().forEach((k, v) -> System.out.println(k + " : " + v));
    }

    //adds country parameter to PointFeatures located on land
    private void addCountryParameter(PointFeature earthquake, List<Marker> country) {
        Location checkLoc = earthquake.getLocation();
        for (Marker mark : country) {
            if (mark.getClass() == MultiMarker.class) {
                for (Marker marker : ((MultiMarker) mark).getMarkers()) {
                    if (((AbstractShapeMarker) marker).isInsideByLocation(checkLoc)) {
                        earthquake.addProperty("country", mark.getProperty("name"));
                    }
                }
            } else if (((AbstractShapeMarker) mark).isInsideByLocation(checkLoc)) {
                earthquake.addProperty("country", mark.getProperty("name"));
            }
        }
    }

    private boolean isOnLand(PointFeature earthquake) {
        return earthquake.getProperty("country") != null;
    }

    private Map<String, Long> printQuakes() {
        return quakeMarkers.stream()
                           .map(this::getQuakes)
                           .collect(groupingBy(identity(), counting()));
    }

    private String getQuakes(Marker mk) {
        return isLandMarker(mk) ? mk.getStringProperty("country")
                                : "OCEAN QUAKE";
    }

    private boolean isLandMarker(Marker mk) {
        return mk instanceof LandQuakeMarker;
    }

    public void draw() {
        background(150, 150, 150);
        map.draw();
        addLegend();
    }

    private void addLegend() {
        strokeWeight(2);
        fill(255, 250, 240);
        rect(25, 50, 150, 300);

        fill(0);
        textAlign(LEFT, CENTER);
        textSize(12);
        text("Earthquake Legend", 50, 75);

        fill(color(150, 30, 30));
        triangle(50, 125 - 5, 50 - 5, 125 + 5, 50 + 5, 125 + 5);
        fill(255);
        ellipse(50, 150, 12, 12);
        rect(44, 170, 12, 12);

        fill(0, 0, 0);
        text("City Marker", 75, 125);
        text("Land Quake", 75, 150);
        text("Ocean Quake", 75, 175);
        textAlign(LEFT, CENTER);
        text("Size ~ Magnitude", 50, 200);

        fill(250, 220, 0);
        ellipse(50, 230, 12, 12);
        fill(0, 0, 255);
        ellipse(50, 250, 12, 12);
        fill(255, 0, 0);
        ellipse(50, 270, 12, 12);
        fill(255);
        ellipse(50, 290, 12, 12);
        line(50 - 11, 290 - 11, 50 + 11, 290 + 11);
        line(50 - 11, 290 + 11, 50 + 11, 290 - 11);

        fill(0);
        text("Shallow", 75, 230);
        text("Intermediate", 75, 250);
        text("Deep", 75, 270);
        text("Pased", 75, 290);
    }

    public static void main(String[] args) {
        PApplet.main("main.module4.EarthquakeCityMap");
    }
}
