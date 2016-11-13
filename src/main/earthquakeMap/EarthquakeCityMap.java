package main.earthquakeMap;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.providers.GeoMapApp;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import main.parsing.ParseFeed;
import processing.core.PApplet;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static main.earthquakeMap.AddCountryParam.addCountryParameter;
import static main.earthquakeMap.CityMarker.TRI_SIZE;

/**
 * EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 *
 * @author Szabados Alpar
 *         Date: November 13, 2016
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

        map = new UnfoldingMap(this, 0, 0, 900, 700, new GeoMapApp.TopologicalGeoMapProvider());
        MapUtils.createDefaultEventDispatcher(this, map);
        map.zoomLevel(1);

        countryMarkers = MapUtils.createSimpleMarkers(GeoJSONReader.loadData(this, countryFile));

        cityMarkers = GeoJSONReader.loadData(this, cityFile)
                                   .stream()
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

    private boolean isOnLand(PointFeature earthquake) {
        return earthquake.getProperty("country") != null;
    }

    private Map<String, Long> printQuakes(List<Marker> markers) {
        return markers.stream()
                      .map(this::getQuakes)
                      .collect(groupingBy(identity(), counting()));
    }

    private String getQuakes(Marker marker) {
        return marker instanceof LandQuakeMarker ? marker.getStringProperty("country")
                                                 : "OCEAN QUAKE";
    }

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
        if (lastSelectedMarker != null) return;

        for (Marker m : markers) {
            CommonMarker marker = (CommonMarker) m;
            if (marker.isInside(map, mouseX, mouseY)) {
                lastSelectedMarker = marker;
                marker.setSelected(true);
                return;
            }
        }
    }

    @Override
    public void mouseClicked() {
        if (lastClickedMarker != null) {
            lastClickedMarker.setClicked(false);
            lastClickedMarker = null;
        }

        findLastClickedMarker(quakeMarkers);
        findLastClickedMarker(cityMarkers);

        if (lastClickedMarker != null) {
            hideAllMarkers(quakeMarkers);
            hideAllMarkers(cityMarkers);
            unhideMarkersInsideThreatZone();
        }

        if (lastClickedMarker == null) {
            unhideAllMarkers(quakeMarkers);
            unhideAllMarkers(cityMarkers);
        }
    }

    private void findLastClickedMarker(List<Marker> markers) {
        markers.stream()
               .filter(mk -> mk.isInside(map, mouseX, mouseY))
               .map(mk -> (CommonMarker) mk)
               .forEach(mk -> {
                   lastClickedMarker = mk;
                   lastClickedMarker.setClicked(true);
               });
    }

    private void hideAllMarkers(List<Marker> markers) {
        markers.forEach(mk -> mk.setHidden(true));
    }

    private void unhideMarkersInsideThreatZone() {
        Location lastClickedLocation = lastClickedMarker.getLocation();
        if (lastClickedMarker instanceof CityMarker) {
            quakeMarkers.stream()
                        .filter(mk -> mk instanceof EarthquakeMarker)
                        .forEach(mk -> setUnhidden(mk, lastClickedLocation, ((EarthquakeMarker) mk).threatCircle()));
        } else {
            cityMarkers.stream()
                       .filter(mk -> mk instanceof CityMarker)
                       .forEach(mk -> setUnhidden(mk, lastClickedLocation, ((EarthquakeMarker) lastClickedMarker).threatCircle()));
        }
        lastClickedMarker.setHidden(false);
    }

    private void setUnhidden(Marker marker, Location location, double threatCircle) {
        if (marker.getDistanceTo(location) < threatCircle) {
            marker.setHidden(false);
        }
    }

    private void unhideAllMarkers(List<Marker> markers) {
        markers.forEach(mk -> mk.setHidden(false));
    }

    public void draw() {
        background(220, 220, 220);
        map.draw();
        addLine();
        addLegend();
    }

    private void addLine() {
        if (lastClickedMarker instanceof CityMarker) {
            quakeMarkers.stream()
                        .filter(mk -> mk instanceof OceanQuakeMarker)
                        .filter(mk -> !mk.isHidden())
                        .forEach(this::createLineBetweenMarkers);
        } else if (lastClickedMarker instanceof OceanQuakeMarker) {
            cityMarkers.stream()
                       .filter(mk -> !mk.isHidden())
                       .forEach(this::createLineBetweenMarkers);
        }
    }

    private void createLineBetweenMarkers(Marker marker) {
        ScreenPosition cityLocation = map.getScreenPosition(marker.getLocation());
        ScreenPosition eqLocation = map.getScreenPosition(lastClickedMarker.getLocation());
        line(cityLocation.x, cityLocation.y, eqLocation.x, eqLocation.y);
    }

    private void addLegend() {
        createLegendBase();
        addTitle();
        addMarkerAndText();
        addCirclesAndText();
    }

    private void createLegendBase() {
        int baseColor = color(255, 250, 240);
        fill(baseColor);
        rect(0, 0, 150, 250);
    }

    private void addTitle() {
        fill(0);
        textAlign(LEFT, CENTER);
        textSize(12);
        int textX = 25;
        int textY = 25;
        text("Earthquake Key", textX, textY);
    }

    private void addMarkerAndText() {
        int triSize = TRI_SIZE;
        int markerX = 35;
        int triY = 50;
        int ellipseY = 70;
        int rectY = 90;
        int markerSize = 10;
        int triColor = color(150, 30, 30);
        int markerColor = color(255, 255, 255);

        fill(triColor);
        triangle(markerX, triY - triSize, markerX - triSize, triY + triSize, markerX + triSize, triY + triSize);

        fill(markerColor);
        ellipse(markerX, ellipseY, markerSize, markerSize);

        int rectOffset = 5;
        rect(markerX - rectOffset, rectY - rectOffset, markerSize, markerSize);

        int textX = 50;
        fill(0);
        textAlign(LEFT, CENTER);
        text("City Marker", textX, triY);
        text("Land Quake", textX, ellipseY);
        text("Ocean Quake", textX, rectY);
        text("Size ~ Magnitude", 25, 110);
    }

    private void addCirclesAndText() {
        int textX = 50;
        int ellipseX = 35;
        int ellipseY = 140;
        int ellipseSize = 12;
        int lineX = 35;
        int lineY = 200;
        int lineLength = 8;

        int yellow = color(255, 255, 0);
        int blue = color(0, 0, 255);
        int red = color(255, 0, 0);
        int white = color(255, 255, 255);

        fill(yellow);
        ellipse(ellipseX, ellipseY, ellipseSize, ellipseSize);

        fill(blue);
        ellipse(ellipseX, ellipseY + 20, ellipseSize, ellipseSize);

        fill(red);
        ellipse(ellipseX, ellipseY + 40, ellipseSize, ellipseSize);

        fill(white);
        ellipse(ellipseX, ellipseY + 60, ellipseSize, ellipseSize);

        strokeWeight(2);
        line(lineX - lineLength, lineY - lineLength, lineX + lineLength, lineY + lineLength);
        line(lineX - lineLength, lineY + lineLength, lineX + lineLength, lineY - lineLength);

        textAlign(LEFT, CENTER);
        fill(0, 0, 0);
        text("Shallow", textX, ellipseY);
        text("Intermediate", textX, ellipseY + 20);
        text("Deep", textX, ellipseY + 40);
        text("Past hour", textX, ellipseY + 60);
    }

    public static void main(String[] args) {
        PApplet.main("main.earthquakeMap.EarthquakeCityMap");
    }
}