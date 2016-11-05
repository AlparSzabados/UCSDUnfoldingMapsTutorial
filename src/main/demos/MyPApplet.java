package main.demos;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to illustrate some use of the PApplet class
 * Used in module 3 of the UC San Diego MOOC Object Oriented Programming in Java
 *
 * @author UC San Diego Intermediate Software Development MOOC team
 */
public class MyPApplet extends PApplet {
    PImage img;

    public void setup() {
        //Add setup code for MyPApplet
        size(400, 400);                //set canvas size
        background(255);            //set canvas color
        stroke(0);                //set pen color
        img = loadImage("palmTrees.jpg", "jpg");
    }

    public void draw() {
        //Add drawing code for MyPApplet
        img.resize(0, height);            //resize loaded image to full height of canvas
        image(img, 0, 0);            //display image
        int color = sunColorSec(second());        //calculate color code for sun
        fill(60504);    //set sun color
        ellipse(width / 5, height / 5, width / 4, height / 5);    //draw sun

    }

    /**
     * Return the RGB color of the sun at this number of seconds in the minute
     */
    public int sunColorSec(float seconds) {
//        Map<String, Integer> rgb = new HashMap<>();
//
        float ratio = Math.abs(30 - seconds) / 30;
//        rgb.put("red", (int) (255 * ratio));
//        rgb.put("green", (int) (255 * ratio));
//        rgb.put("blue", 0);


        int a = 255;
        int b = 255;
        int c = 0;

        int abc = a * 100 + b * 10 + c;

        return (int) (abc*ratio);
    }

    public static void main(String[] args) {
        //Add main method for running as application
        PApplet.main("MyPApplet");
    }
}