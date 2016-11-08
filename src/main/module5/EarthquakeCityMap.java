package main.module5;

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

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

/**
 * EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 *
 * @author Your name here
 *         Date: July 17, 2015
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

    private CommonMarker lastSelected;
    private CommonMarker lastClicked;

    public void setup() {
        size(900, 700, OPENGL);

        map = new UnfoldingMap(this, 200, 50, 650, 600, new GeoMapApp.TopologicalGeoMapProvider());
        MapUtils.createDefaultEventDispatcher(this, map);

        countryMarkers = MapUtils.createSimpleMarkers(GeoJSONReader.loadData(this, countryFile));

        cityMarkers = GeoJSONReader.loadData(this, cityFile).stream()
                                   .map(main.module4.CityMarker::new)
                                   .collect(toList());

        quakeMarkers = ParseFeed.parseEarthquake(this, earthquakesURL).stream()
                                .peek(f -> addCountryParameter(f, countryMarkers))
                                .map(f -> createMarker(f))
                                .collect(toList());

        map.addMarkers(quakeMarkers);
        map.addMarkers(cityMarkers);

    }

    private EarthquakeMarker createMarker(PointFeature f) {
        return isOnLand(f) ? new main.module5.LandQuakeMarker(f) : new OceanQuakeMarker(f);
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

    private Map<String, Long> printQuakes(List<Marker> markers) {
        return markers.stream()
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

    /**
     * Event handler that gets called automatically when the
     * mouse moves.
     */
    @Override
    public void mouseMoved() {
        // clear the last selection
        if (lastSelected != null) {
            lastSelected.setSelected(false);
            lastSelected = null;

        }
        selectMarkerIfHover(quakeMarkers);
        selectMarkerIfHover(cityMarkers);
    }

    // If there is a marker under the cursor, and lastSelected is null
    // set the lastSelected to be the first marker found under the cursor
    // Make sure you do not select two markers.
    //
    private void selectMarkerIfHover(List<Marker> markers) {
        // TODO: Implement this method
    }

    /**
     * The event handler for mouse clicks
     * It will display an earthquake and its threat circle of cities
     * Or if a city is clicked, it will display all the earthquakes
     * where the city is in the threat circle
     */
    @Override
    public void mouseClicked() {
        // TODO: Implement this method
        // Hint: You probably want a helper method or two to keep this code
        // from getting too long/disorganized
    }


    // loop over and unhide all markers
    private void unhideMarkers() {
        for (Marker marker : quakeMarkers) {
            marker.setHidden(false);
        }

        for (Marker marker : cityMarkers) {
            marker.setHidden(false);
        }
    }

    public void draw() {
        background(0);
        map.draw();
        addKey();
    }

    private void addKey() {
        fill(255, 250, 240);

        int xbase = 25;
        int ybase = 50;

        rect(xbase, ybase, 150, 250);

        fill(0);
        textAlign(LEFT, CENTER);
        textSize(12);
        text("Earthquake Key", xbase + 25, ybase + 25);

        fill(150, 30, 30);
        int tri_xbase = xbase + 35;
        int tri_ybase = ybase + 50;
        triangle(tri_xbase, tri_ybase - CityMarker.TRI_SIZE, tri_xbase - CityMarker.TRI_SIZE,
                tri_ybase + CityMarker.TRI_SIZE, tri_xbase + CityMarker.TRI_SIZE,
                tri_ybase + CityMarker.TRI_SIZE);

        fill(0, 0, 0);
        textAlign(LEFT, CENTER);
        text("City Marker", tri_xbase + 15, tri_ybase);

        text("Land Quake", xbase + 50, ybase + 70);
        text("Ocean Quake", xbase + 50, ybase + 90);
        text("Size ~ Magnitude", xbase + 25, ybase + 110);

        fill(255, 255, 255);
        ellipse(xbase + 35,
                ybase + 70,
                10,
                10);
        rect(xbase + 35 - 5, ybase + 90 - 5, 10, 10);

        fill(color(255, 255, 0));
        ellipse(xbase + 35, ybase + 140, 12, 12);
        fill(color(0, 0, 255));
        ellipse(xbase + 35, ybase + 160, 12, 12);
        fill(color(255, 0, 0));
        ellipse(xbase + 35, ybase + 180, 12, 12);

        textAlign(LEFT, CENTER);
        fill(0, 0, 0);
        text("Shallow", xbase + 50, ybase + 140);
        text("Intermediate", xbase + 50, ybase + 160);
        text("Deep", xbase + 50, ybase + 180);

        text("Past hour", xbase + 50, ybase + 200);

        fill(255, 255, 255);
        int centerx = xbase + 35;
        int centery = ybase + 200;
        ellipse(centerx, centery, 12, 12);

        strokeWeight(2);
        line(centerx - 8, centery - 8, centerx + 8, centery + 8);
        line(centerx - 8, centery + 8, centerx + 8, centery - 8);

    }


    // Checks whether this quake occurred on land.  If it did, it sets the
    // "country" property of its PointFeature to the country where it occurred
    // and returns true.  Notice that the helper method addCountry will
    // set this "country" property already.  Otherwise it returns false.
    private boolean isLand(PointFeature earthquake) {

        // IMPLEMENT THIS: loop over all countries to check if location is in any of them
        // If it is, add 1 to the entry in countryQuakes corresponding to this country.
        for (Marker country : countryMarkers) {
            if (isInCountry(earthquake, country)) {
                return true;
            }
        }

        // not inside any country
        return false;
    }

    // prints countries with number of earthquakes
    private void printQuakes() {
        int totalWaterQuakes = quakeMarkers.size();
        for (Marker country : countryMarkers) {
            int numQuakes = 0;
            for (Marker marker : quakeMarkers) {
                EarthquakeMarker eqMarker = (EarthquakeMarker) marker;
                if (eqMarker.isOnLand()) {
                    if (country.getStringProperty("name").equals(eqMarker.getStringProperty("country"))) {
                        numQuakes++;
                    }
                }
            }
            if (numQuakes > 0) {
                totalWaterQuakes -= numQuakes;
                System.out.println(country.getStringProperty("name") + ": " + numQuakes);
            }
        }
        System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
    }


    // helper method to test whether a given earthquake is in a given country
    // This will also add the country property to the properties of the earthquake feature if
    // it's in one of the countries.
    // You should not have to modify this code
    private boolean isInCountry(PointFeature earthquake, Marker country) {
        // getting location of feature
        Location checkLoc = earthquake.getLocation();

        // some countries represented it as MultiMarker
        // looping over SimplePolygonMarkers which make them up to use isInsideByLoc
        if (country.getClass() == MultiMarker.class) {

            // looping over markers making up MultiMarker
            for (Marker marker : ((MultiMarker) country).getMarkers()) {

                // checking if inside
                if (((AbstractShapeMarker) marker).isInsideByLocation(checkLoc)) {
                    earthquake.addProperty("country", country.getProperty("name"));

                    // return if is inside one
                    return true;
                }
            }
        }

        // check if inside country represented by SimplePolygonMarker
        else if (((AbstractShapeMarker) country).isInsideByLocation(checkLoc)) {
            earthquake.addProperty("country", country.getProperty("name"));

            return true;
        }
        return false;
    }

}
