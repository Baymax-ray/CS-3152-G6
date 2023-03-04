package edu.cornell.gdiac.physics;

import java.util.ArrayList;

public class Board {
    private float width;
    private float height;
    private ArrayList<float[][]> arrays;
    public Board(float width, float height) {
        this.width = width;
        this.height = height;
        this.arrays = new ArrayList<>();}
    /**
     * Add a list of points to the board
     * @param list is a list of float, respectively x and y
     */
    public void Add(float[] list){
        int len=list.length/2;
        float[][] points=new float[len][2];
        for (int ii = 0; ii < len; ii++){
            points[ii][0]=list[ii*2];
            points[ii][1]=list[ii*2+1];
        }
        arrays.add(points);
    }

    /**
     * If (x,y) is inside this polygon
     * @param x
     * @param y
     * @param polygon
     * @return whether this point is in this polygon
     */
    public boolean isPointInsidePolygon(float x, float y, float[][] polygon) {
        int intersections = 0;
        int n = polygon.length;

        for (int i = 0; i < n; i++) {
            float[] p1 = polygon[i];
            float[] p2 = polygon[(i + 1) % n];

            if (p1[1] == p2[1]) { // skip horizontal lines
                continue;
            }

            float xIntersect = (y - p1[1]) * (p2[0] - p1[0]) / (p2[1] - p1[1]) + p1[0];
            if (xIntersect > x) { // the line intersects the ray
                if (p1[1] > p2[1]) { // the line is going down
                    intersections++;
                }
            }
        }
        return intersections % 2 == 1;
    }


    /**
     * The platform I am on
     */
    public float[][] StandOn(float x, float y){
        for (int ii=0; ii< arrays.size();ii++){
            if (isPointInsidePolygon(x,y, arrays.get(ii))){
                return arrays.get(ii);
            }
        }
        return new float[0][];
    }
}
