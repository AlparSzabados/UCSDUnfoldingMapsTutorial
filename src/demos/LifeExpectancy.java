package demos;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.providers.GeoMapApp;
import de.fhpotsdam.unfolding.utils.MapUtils;
import processing.core.PApplet;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

/**
 * Visualizes life expectancy in different countries.
 * <p>
 * It loads the country shapes from a GeoJSON file via a data reader, and loads the population density values from
 * another CSV file (provided by the World Bank). The data value is encoded to transparency via a simplistic linear
 * mapping.
 */
public class LifeExpectancy extends PApplet {

    private UnfoldingMap map;
    private Map<String, Float> lifeExpByCountry;
    private List<Marker> countryMarkers;

    public void setup() {
        size(800, 600, OPENGL);
        map = new UnfoldingMap(this, 50, 50, 700, 500, new GeoMapApp.TopologicalGeoMapProvider());
        MapUtils.createDefaultEventDispatcher(this, map);

        lifeExpByCountry = loadLifeExpectancyFromCSV();
        println("Loaded " + lifeExpByCountry.size() + " data entries");

        List<Feature> countries = GeoJSONReader.loadData(this, "countries.geo.json");
        countryMarkers = MapUtils.createSimpleMarkers(countries);
        map.addMarkers(countryMarkers);

        shadeCountries();
    }

    public void draw() {
        map.draw();
    }

    private void shadeCountries() {
        for (Marker marker : countryMarkers) {
            if (idPresentInCSV(marker)) {
                int colorLevel = (int) map(getId(marker), 40, 90, 10, 255);
                marker.setColor(color(255 - colorLevel, 100, colorLevel));
            } else
                marker.setColor(color(150, 150, 150));
        }
    }

    private Float getId(Marker marker) {
        return lifeExpByCountry.get(marker.getId());
    }

    private boolean idPresentInCSV(Marker marker) {
        return lifeExpByCountry.containsKey(marker.getId());
    }

    private Map<String, Float> loadLifeExpectancyFromCSV() {
        return stream(loadStrings("LifeExpectancyWorldBankModule3.csv"))
                .map(row -> row.split(","))
                .filter(this::filter)
                .collect(toMap(k -> k[4], v -> parseFloat(v[5])));
    }

    private boolean filter(String[] column) {
        return column.length == 6 && !column[5].equals("..");
    }

    static public void main(String args[]) {
        PApplet.main("demos.LifeExpectancy");
    }
}
