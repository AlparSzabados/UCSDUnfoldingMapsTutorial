package module4;

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
import parsing.ParseFeed;
import processing.core.PApplet;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.toList;

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
        cityMarkers = cities.stream().map(CityMarker::new).collect(toList());

        quakeMarkers = ParseFeed.parseEarthquake(this, earthquakesURL).stream()
                                .map(f -> isLand(f) ? new LandQuakeMarker(f) : new OceanQuakeMarker(f))
                                .collect(toList());

        map.addMarkers(quakeMarkers);
        map.addMarkers(cityMarkers);

        printQuakes();
    }

    private void printQuakes() {

        quakeMarkers.stream()
                    .map(mk -> mk instanceof LandQuakeMarker ? mk.getProperty("country") : "OCEAN QUAKE")
                    .collect(groupingBy(identity(), counting()))
                    .forEach((k, v) -> System.out.println(k + " : " + v + " earthquake(s)"));
    }

    public void draw() {
        background(0);
        map.draw();
        addKey();
    }

    // helper method to draw key in GUI
    // TODO: Update this method as appropriate
    private void addKey() {
        // Remember you can use Processing's graphics methods here
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

    private boolean isLand(PointFeature earthquake) {
        return countryMarkers.stream().anyMatch(mk -> isInCountry(earthquake, mk));
    }

    private boolean isInCountry(PointFeature earthquake, Marker country) {
        Location checkLoc = earthquake.getLocation();

        if (country.getClass() == MultiMarker.class) {
            for (Marker marker : ((MultiMarker) country).getMarkers()) {
                if (((AbstractShapeMarker) marker).isInsideByLocation(checkLoc)) {
                    earthquake.addProperty("country", country.getProperty("name"));
                    return true;
                }
            }
        } else if (((AbstractShapeMarker) country).isInsideByLocation(checkLoc)) {
            earthquake.addProperty("country", country.getProperty("name"));
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        PApplet.main("module4.EarthquakeCityMap");
    }
}
