/*
 * PathSmoother.java
 *
 * This class factory taking a continuous path of points and smoothing it into a path with less points. Notice that
 * smoothing is spread out over multiple methods: initialization, calculation, and materialization. This is to
 * enable this calculation to take place in a separate thread.
 *
 * @author Crystal Jin
 * @date   12/5/2022
 */
package edu.cornell.gdiac.math;

import java.util.ArrayList;


/**
 * A factory class to smooth a continuous path of points, reducing the number needed.
 *
 * A common temptation with mobile games is to track the player's finger gesture by recording all of the
 * finger positions over time. Except that this is a lot of points (and attempting to draw all these points
 * exposed some serious flaws in earlier versions of SpriteBatch). If points are too close together, then some
 * of them can be safely removed without altering the shape of the path.
 *
 * This class uses the Douglas-Peuker algorithm, as described here:
 *  https://en.wikipedia.org/wiki/Ramer–Douglas–Peucker_algorithm
 *
 * The correct epsilon value to use should be found with experimentation. In particular, it depends on the scale of
 * the path being smoothed.
 *
 * As with many of our factory classes, the methods are broken up into three phases: initialization, calculation, and
 * materialization. To use the factory, you first set the data (in this case a set of vertices or another Poly2) with
 * the initialization methods. You then call the calculation method. Finally, you use the materialization methods to
 * access the data in several different ways. This division allows us to support multithreaded calculation if the data
 * generation takes too long. However, note that this factory is not thread safe in that you cannot access data while
 * it is still in mid-calculation.
 */
public class PathSmoother {
    /** The set of vertices to use in the calculation */
    private ArrayList<Float> input;
    /** The set of vertices after smoothing */
    private ArrayList<Float> output;
    /** The epsilon value of the Douglas-Peucker algorithm */
    private float epsilon;
    /** Whether or not the calculation has been run */
    private boolean calculated;

    /** This makes sense as default for touch coordinates */
    private float DEFAULT_EPSILON=1;

    /**
     * Creates a path smoother with no vertex data.
     */
    public PathSmoother(){
        clear();
        epsilon = DEFAULT_EPSILON;
    }

    /**
     * Creates a path smoother with the given vertex data.
     *
     * The vertex data is copied.  The smother does not retain any references
     * to the original data.
     *
     * @param points    The vertices to triangulate
     */
    public PathSmoother(float[] points){
        reset();
        epsilon = DEFAULT_EPSILON;
        set(points);
    }

    /**
     * Sets the vertex data for this path smoother.
     *
     * The vertex data is copied. The smother does not retain any references
     * to the original data.
     *
     * This method resets all interal data. You will need to reperform the
     * calculation before accessing data.
     *
     * @param points    The vertices to triangulate
     */
    public void set(float[] points){
        clear();
        for (float i : points){
            input.add(i);
        }
    }

    /**
     * Sets the vertex data for this path smoother.
     *
     * The vertex data is copied. The smother does not retain any references
     * to the original data.  In addition, only the vertex data is copied.
     * Whether or not the path is closed is ignored.
     *
     * This method resets all interal data. You will need to reperform the
     * calculation before accessing data.
     *
     * @param path    The path to smooth
     */
    public void set(Path2 path){
        clear();
        for (float i : path.vertices){
            input.add(i);
        }
    }

    /**
     * Sets the epsilon value for the smoothing algorithm.
     *
     * The epsilon value specifies the tolerance for the algorithm.  At each
     * step, any point that is with epsilon of a line segment is considered
     * to be part of that line segment.
     *
     * Typically this value is found by experimentation. However, because this
     * is typically used to smooth touch paths (which have integer coordinates),
     * the value should be at least 1 (which is the default).
     *
     * @param epsilon   The epsilon value for the smoothing algorithm.
     */
    public void setEpsilon(float epsilon){
        this.epsilon = epsilon;
    }

    /**
     * Returns the epsilon value for the smoothing algorithm.
     *
     * The epsilon value specifies the tolerance for the algorithm.  At each
     * step, any point that is with epsilon of a line segment is considered
     * to be part of that line segment.
     *
     * Typically this value is found by experimentation. However, because this
     * is typically used to smooth touch paths (which have integer coordinates),
     * the value should be at least 1 (which is the default).
     *
     * @return the epsilon value for the smoothing algorithm.
     */
    public float getEpsilon(){
        return epsilon;
    }

    /**
     * Clears all internal data, but still maintains the initial vertex data.
     */
    public void reset(){
        output = new ArrayList<>();
        calculated = false;
    }

    /**
     * Clears all internal data, the initial vertex data.
     *
     * When this method is called, you will need to set a new vertices before
     * calling calculate.
     */
    public void clear(){
        reset();
        input = new ArrayList<>();
    }

    /**
     * Performs a triangulation of the current vertex data.
     */
    public void calculate(){
        if (!calculated){
            douglasPeucker(0,input.size()/2-1);
            calculated = true;
        }
    }

    /**
     * Recursively performs Douglas-Peuker on the given input segment
     *
     * The results will be pulled from _input and placed in _output.
     *
     * @param start The first position in _input to process
     * @param end   The last position in _input to process
     *
     * @return the number of points preserved in smoothing
     */
    private int douglasPeucker(int start, int end){
        int over = -1;
        float sx = input.get(start*2);
        float sy = input.get(start*2+1);
        float ex = (end == over? input.get(0) : input.get(end*2));
        float ey = (end == over? input.get(1) : input.get(end*2+1));

        if(end - start <= 1 || (end == over && start == input.size()/2-1)){
            output.add(sx);
            output.add(sy);
            output.add(ex);
            output.add(ey);
            return 2;
        } else if(sx == ex && sy == ey){
            output.add(sx);
            output.add(sy);
            int index = over;
            for(int i=start+1; index == over && i<end; i++){
                float vx = input.get(i*2);
                float vy = input.get(i*2+1);
                if(vx!= sx || vy!=sy){
                    index = i;
                }
            }
            if (index != over){
                return douglasPeucker(index,end)+1;
            } else{
                output.add(ex);
                output.add(ey);
                return 2;
            }
        }

        float dMax = 0;
        int index = 0;
        for(int i=start+1; i<end; i++){
            float vx = input.get(i*2);
            float vy = input.get(i*2+1);
            float ux = ex - sx;
            float uy = ey - sy;
            float dist = (float) Math.abs((uy*vx-ux*vy+ex*sy-ey*sx)/Math.sqrt(ux*ux+uy*uy));
            if(dist>dMax){
                index = i;
                dMax = dist;
            }
        }

        if(dMax > epsilon){
            int result = 0;
            result += douglasPeucker(start,index);
            output.remove(output.size()-1);
            output.remove(output.size()-1);
            result += douglasPeucker(index,end);
            return result;
        } else {
            output.add(sx);
            output.add(sy);
            output.add(ex);
            output.add(ey);
        }
        return 0;
    }

    /**
     * Returns a list of points representing the smoothed path.
     *
     * The result is guaranteed to be a subset of the original vertex path,
     * order preserved. The smoother does not retain a reference to the returned
     * list; it is safe to modify it.
     *
     * If the calculation is not yet performed, this method will return the
     * empty list.
     *
     * @return a list of indices representing the triangulation.
     */
    public float[] getPoints(){
        float[] res = new float[output.size()];
        for (int i=0; i<output.size(); i++){
            res[i] = output.get(i);
        }
        return res;
    }

    /**
     * Returns a path object representing the smoothed result.
     *
     * The resulting path is open.
     *
     * If the calculation is not yet performed, this method will return the
     * empty path.
     *
     * @return a polygon representing the triangulation.
     */
    public Path2 getPath(){
        float[] res = new float[output.size()];
        for (int i=0; i<output.size(); i++){
            res[i] = output.get(i);
        }
        return new Path2(res);
    }
}
