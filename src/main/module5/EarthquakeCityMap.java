package main.module5;

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

    private CommonMarker lastSelectedMarker;
    private CommonMarker lastClickedMarker;

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
                                .map(this::createMarker)
                                .collect(toList());

        map.addMarkers(quakeMarkers);
        map.addMarkers(cityMarkers);

        printQuakes(quakeMarkers).forEach((k, v) -> System.out.println(k + " : " + v));
    }

    private EarthquakeMarker createMarker(PointFeature f) {
        return isOnLand(f) ? new LandQuakeMarker(f) : new OceanQuakeMarker(f);
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
        Map<String, Long> print;
        print = markers.stream()
                       .map(this::getQuakes)
                       .collect(groupingBy(identity(), counting()));
        return print;
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
        if (lastSelectedMarker != null) {
            lastSelectedMarker.setSelected(false);
            lastSelectedMarker = null;

        }
        selectMarkerIfHover(quakeMarkers);
        selectMarkerIfHover(cityMarkers);
    }

    private void selectMarkerIfHover(List<Marker> markers) {
        // Abort if there's already a marker selected
        if (lastSelectedMarker != null) {
            return;
        }

        for (Marker m : markers) {
            CommonMarker marker = (CommonMarker) m;
            if (marker.isInside(map, mouseX, mouseY)) {
                lastSelectedMarker = marker;
                marker.setSelected(true);
                return;
            }
        }
    }

    /**
     * The event handler for mouse clicks
     * It will display an earthquake and its threat circle of cities
     * Or if a city is clicked, it will display all the earthquakes
     * where the city is in the threat circle
     */
    @Override
    public void mouseClicked() {
        if (lastClickedMarker != null) {
            lastClickedMarker.setClicked(false);
            lastClickedMarker = null;
        }

        selectMarkerIfClicked(quakeMarkers);
        selectMarkerIfClicked(cityMarkers);

        if (lastClickedMarker != null) {
            hideQuakes(quakeMarkers);
            hideQuakes(cityMarkers);
        }
    }

    private void hideQuakes(List<Marker> markers) {
        Location lastClickedLocation = lastClickedMarker.getLocation();

        hideAllMarkers(markers);

        if (lastClickedMarker instanceof CityMarker) {
            markers.stream()
                   .filter(mk -> mk instanceof EarthquakeMarker)
                   .forEach(mk -> setUnhidden(mk, lastClickedLocation, ((EarthquakeMarker) mk).threatCircle()));
        } else {
            markers.stream()
                   .filter(mk -> mk instanceof CityMarker)
                   .forEach(mk -> setUnhidden(mk, lastClickedLocation, ((EarthquakeMarker) lastClickedMarker).threatCircle()));
        }
        lastClickedMarker.setHidden(false);
    }

    private void hideAllMarkers(List<Marker> markers) {
        markers.forEach(mk -> mk.setHidden(true));
    }

    private void setUnhidden(Marker marker, Location location, double threatCircle) {
        if (marker.getDistanceTo(location) < threatCircle) {
            marker.setHidden(false);
        }
    }

    private void selectMarkerIfClicked(List<Marker> markers) {
        markers.stream()
               .filter(marker -> marker.isInside(map, mouseX, mouseY))
               .forEach(marker -> {
                   ((CommonMarker) marker).setClicked(true);
                   lastClickedMarker = (CommonMarker) marker;
               });

        if (lastClickedMarker == null) {
            unhideMarkers();
        }
    }

    // loop over and unhide all markers
    private void unhideMarkers() {
        quakeMarkers.forEach(mk -> mk.setHidden(false));
        cityMarkers.forEach(mk -> mk.setHidden(false));
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

    public static void main(String[] args) {
        PApplet.main("main.module5.EarthquakeCityMap");
    }
}