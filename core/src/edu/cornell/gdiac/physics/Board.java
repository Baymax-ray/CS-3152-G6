package edu.cornell.gdiac.physics;

import java.util.ArrayList;

public class Board {

    /** CURRENTLY NOT USED. THIS IS THE GAME BOARD'S INFO.*/
    private float width;
    private float height;

    /**
     * platformArrays is an array s.t. each element in this
     * array represents a platform using a 2-D float list.
     *
     * This float list contains a series of points, where
     * float[i][0] is the i-th point's x-coordinate, float[i][1] is the i-th point's y-coordinate.
     * */
    private ArrayList<float[][]> platformArrays;

    public Board(float width, float height) {
        this.width = width;
        this.height = height;
        this.platformArrays = new ArrayList<>();}
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
        platformArrays.add(points);
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
     * The platform I stand on
     */
    public float[][] StandOn(float x, float y){
        for (int ii=0; ii< platformArrays.size();ii++){
            if (isPointInsidePolygon(x,y, platformArrays.get(ii))){
                return platformArrays.get(ii);
            }
        }
        return new float[0][];
    }
}
