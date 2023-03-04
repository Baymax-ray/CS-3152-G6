package edu.cornell.gdiac.physics;

import java.util.ArrayList;

public class Board {
    private int width;
    private int height;
    private ArrayList<float[]> arrays;
    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.arrays = new ArrayList<>();}
    /**
     * Add a list of points to the board
     * @param points
     */
    public void Add(float[] points){
        arrays.add(points);
    }

    /**
     * If I can move to this point without pass the edge of a platform
     */
    public boolean CanGo(float x, float y){

    }
}
