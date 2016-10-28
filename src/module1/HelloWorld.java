package module1;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.AbstractMapProvider;
import de.fhpotsdam.unfolding.providers.GeoMapApp;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import processing.core.PApplet;

public class HelloWorld extends PApplet {
    private static final long serialVersionUID = 1L;

    public static String mbTilesString = "blankLight-1-3.mbtiles";
    private static final boolean offline = false;

    UnfoldingMap map1;

    UnfoldingMap map2;

    public void setup() {
        size(800, 600, P2D);
        this.background(200, 200, 200);
        AbstractMapProvider provider = new GeoMapApp.TopologicalGeoMapProvider();
        int zoomLevel = 10;

        if (offline) {
            provider = new MBTilesMapProvider(mbTilesString);
            zoomLevel = 3;
        }

        map1 = new UnfoldingMap(this, 25, 50, 350, 500, provider);
        map1.zoomAndPanTo(zoomLevel, new Location(32.9f, -117.2f));

        map2 = new UnfoldingMap(this, 425, 50, 350, 500, provider);
        map2.zoomAndPanTo(zoomLevel, new Location(46.7712101f, 23.623635299999933f));

        MapUtils.createDefaultEventDispatcher(this, map1);
        MapUtils.createDefaultEventDispatcher(this, map2);
    }

    public void draw() {
        map1.draw();
        map2.draw();
    }

    static public void main(String args[]) {
        PApplet.main("module1.HelloWorld");
    }
}