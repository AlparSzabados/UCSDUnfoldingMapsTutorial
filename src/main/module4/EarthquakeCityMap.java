package main.module4;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
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

import java.util.HashMap;
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

        List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
        countryMarkers = MapUtils.createSimpleMarkers(countries);

        List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
        cityMarkers = cities.stream()
                            .map(CityMarker::new)
                            .collect(toList());

        List<PointFeature> quakes = ParseFeed.parseEarthquake(this, earthquakesURL);

        List<PointFeature> quakesWithCountryParameter = quakes.stream()
                                                              .peek(f -> addCountryParameter(f, countryMarkers)) //TODO EVIL CODE!
                                                              .collect(toList());

        quakeMarkers = quakesWithCountryParameter.stream()
                                                 .map(f -> isOnLand(f) ? new LandQuakeMarker(f) : new OceanQuakeMarker(f))
                                                 .collect(toList());

        map.addMarkers(quakeMarkers);
        map.addMarkers(cityMarkers);

        printQuakes().forEach((k, v) -> System.out.println(k + " : " + v));
    }

    //adds country parameter to PointFeatures that or located on land
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
        Map<String, Long> quakes = new HashMap<>();
        quakeMarkers.stream()
                    .map(this::getQuakes)
                    .collect(groupingBy(identity(), counting()))
                    .forEach(quakes::put);

        return quakes;
    }

    private String getQuakes(Marker mk) {
        return isLandMarker(mk) ? mk.getStringProperty("country")
                                : "OCEAN QUAKE";
    }

    private boolean isLandMarker(Marker mk) {
        return mk instanceof LandQuakeMarker;
    }

    public void draw() {
        background(0);
        map.draw();
        addKey();
    }

    public static void main(String[] args) {
        PApplet.main("main.module4.EarthquakeCityMap");
    }

    // TODO: Update this method as appropriate
    private void addKey() {
        fill(255, 250, 240);
        rect(25, 50, 150, 250);

        fill(0);
        textAlign(LEFT, CENTER);
        textSize(12);
        text("Earthquake Key", 50, 75);

        fill(color(255, 0, 0));
        ellipse(50, 125, 15, 15);
        fill(color(255, 255, 0));
        ellipse(50, 175, 10, 10);
        fill(color(0, 0, 255));
        ellipse(50, 225, 5, 5);

        fill(0, 0, 0);
        text("5.0+ Magnitude", 75, 125);
        text("4.0+ Magnitude", 75, 175);
        text("Below 4.0", 75, 225);
    }
}
