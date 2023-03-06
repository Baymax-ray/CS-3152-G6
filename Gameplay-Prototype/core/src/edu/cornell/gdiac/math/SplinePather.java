/*
 * SplinePather.java
 *
 * This class is a factory for producing Path2 and Poly2 objects from a Spline2. In previous interations, this
 * functionality was embedded in the Spline2 class.  That made that class much more heavyweight than we wanted for
 * a simple math class.  By separating this out as a factory, we allow ourselves the option of moving these calculations
 * to a worker thread if necessary.
 *
 * @author Crystal Jin
 * @date   12/5/2022
 */
package edu.cornell.gdiac.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * A factory class for producing Poly2 objects from a Spline2.
 *
 * In order to draw a cubic spline, we must first convert it to a Poly2 object. All of our rendering tools are designed
 * around the basic Poly2 class. In addition to generating a Poly2 for the spline path, this class can also generate
 * Poly2 objects for UI elements such as handles and anchors.
 *
 * As with many of our factories, the methods are broken up into three phases: initialization, calculation, and
 * materialization. To use the factory, you first set the data (in this case a pointer to a Spline2) with the
 * initialization methods. You then call the calculation method. Finally, you use the materialization methods to
 * access the data in several different ways. This division allows us to support multithreaded calculation if the data
 * generation takes too long. However, not that this factory keeps a pointer to the spline, and it is unsafe to modify
 * the spline while the calculation is ongoing. If you do multithread the calculation, you should force the user to
 * copy the spline first.
 */
public class SplinePather {
    /** The spline data */
    private Spline2 spline;
    /** The control data created by the approximation */
    private ArrayList<Float> pointBuff;
    /** The parameter data created by the approximation */
    private ArrayList<Float> paramBuff;
    /** The anchor indicators */
    private HashMap<Integer,Integer> anchorPts;
    /** Whether the approximation curve is closed */
    private boolean closed;
    /** Whether the calculation has been run */
    private boolean calculated;
    /** The flatness tolerance for generating paths */
    private float tolerance;

    private float[] vertBuff;
    private short[] indBuff;

    /** Tolerance to identify a point as "smooth" */
    private final float SMOOTH_TOLERANCE = 0.0001f;

    /** The default tolerance for the polygon approximation functions */
    private final float DEFAULT_FLATNESS = 0.5f;

    /**
     * Creates a spline approximator with no spline data.
     */
    public SplinePather(){
        spline = null;
        calculated = false;
        tolerance = DEFAULT_FLATNESS;
    }

    /**
     * Creates a spline approximator with the given spline as its initial data.
     *
     * @param spline    The spline to approximate
     */
    public SplinePather(Spline2 spline){
        this.spline = spline;
        calculated = false;
        tolerance = DEFAULT_FLATNESS;
    }

    /**
     * Sets the given spline as the data for this spline approximator.
     *
     * This method resets all interal data.  You will need to reperform the
     * calculation before accessing data.
     *
     * @param spline    The spline to approximate
     */
    public void set(Spline2 spline){
        reset();
        this.spline = spline;
    }

    /**
     * Clears all internal data, but still maintains a reference to the spline.
     *
     * Use this method when you want to reperform the approximation at a
     * different resolution.
     */
    public void reset(){
        calculated = false;
        pointBuff = new ArrayList<>();
        paramBuff = new ArrayList<>();
        anchorPts = new HashMap<>();
    }

    /**
     * Clears all internal data, including the spline data.
     *
     * When this method is called, you will need to set a new spline before
     * calling calculate.
     */
    public void clear(){
        reset();
        spline = null;
    }

    /**
     * Performs an approximation of the current spline
     *
     * A polygon approximation is creating by recursively calling de Castlejau's
     * until we reach a stopping condition.
     *
     * Hence this method is not thread-safe.  If you are using this method in
     * a task thread, you should copy the spline first before starting the
     * calculation.
     */
    public void calculate(){
        reset();
        if(spline == null) return;

        int size = spline.size();
        if(!(size>0)) return;

        float[] points = spline.getControlPoints();

        for(int i=0; i<size; i++){
            anchorPts.put(pointBuff.size()/2,i);
            generate((float)i, points[3*2*i], points[3*2*i+1], points[3*2*i+2], points[3*2*i+3],
                    points[3*2*i+4], points[3*2*i+5], points[3*2*i+6], points[3*2*i+7], 0);

        }
        // Push back last point and parameter
        anchorPts.put(pointBuff.size()/2,size);
        pointBuff.add(points[3*2*size]);
        pointBuff.add(points[3*2*size+1]);
        paramBuff.add((float) size);
        closed = spline.isClosed();
        calculated = true;
    }

    /**
     * Generates data via recursive use of de Castlejau's
     *
     * This method subdivides the spline at the given segment. The results
     * are put in the output buffers.
     *
     * @param  t        the parameter for the (start of) this segment
     * @param  p0x       the x coordinate of left anchor of this segment
     * @param  p0y       the y coordinate of left anchor of this segment
     * @param  p1x       the x coordinate of left tangent of this segment
     * @param  p1y       the y coordinate of left tangent of this segment
     * @param  p2x       the x coordinate of right tangent of this segment
     * @param  p2y       the y coordinate of right tangent of this segment
     * @param  p3x       the x coordinate of right anchor of this segment
     * @param  p3y       the y coordinate of right anchor of this segment
     * @param  depth    the current depth of the recursive call
     *
     * @return The number of (anchor) points generated by this recursive call.
     */
    private int generate(float t, float p0x, float p0y, float p1x, float p1y,float p2x,
                         float p2y, float p3x, float p3y, int depth){
        // Do not go to far
        boolean terminate = false;
        if(depth>=8){
            terminate = true;
        } else if(p0x == p1x && p0y == p1y && p2x == p3x && p2y == p3y){
            terminate = true;
        } else {
            float dx = p3x - p0x;
            float dy = p3y - p0y;
            float d2 = (((p1x - p3x) * dy - (p1y - p3y) * dx));
            float d3 = (((p2x - p3x) * dy - (p2y - p3y) * dx));
            d2 = d2 > 0 ? d2 : -d2;
            d3 = d3 > 0 ? d3 : -d3;
            if ((d2 + d3)*(d2 + d3) < tolerance * (dx*dx + dy*dy)) {
                terminate = true;
            }
        }

        // Add the first point if terminating.
        int result=0;
        if (terminate) {
            paramBuff.add(t);
            pointBuff.add(p0x);
            pointBuff.add(p0y);
            pointBuff.add(p1x);
            pointBuff.add(p1y);
            pointBuff.add(p2x);
            pointBuff.add(p2y);
            return 1;
        }

        // Cross bar
        float hx = (p1x+p2x)*0.5f;
        float hy = (p1y+p2y)*0.5f;
        float l1x = (p0x+p1x)*0.5f;
        float l1y = (p0y+p1y)*0.5f;
        float l2x = (l1x+hx)*0.5f;
        float l2y = (l1y+hy)*0.5f;
        float r2x = (p2x+p3x)*0.5f;
        float r2y = (p2y+p3y)*0.5f;
        float r1x = (r2x+hx)*0.5f;
        float r1y = (r2y+hy)*0.5f;
        float cx = (l2x+r1x)*0.5f;
        float cy = (l2y+r1y)*0.5f;

        // Recursive calls
        float s = t + 1.0f / (1 << (depth + 1));
        result =  generate(t, p0x, p0y, l1x, l1y, l2x, l2y, cx, cy, depth + 1);
        result += generate(s, cx, cy, r1x, r1y, r2x, r2y, p3x, p3y, depth + 1);
        return result;
    }

    /**
     * Returns a new polygon approximating this spline.
     *
     * The Poly2 indices will define a path traversing the vertices of the
     * polygon.  Hence this Poly2 may be drawn as a wireframe.  The indices
     * will define a closed path if the spline is itself closed, and an open
     * path otherwise.
     *
     * The resolution of the polygon is determined by the calculate
     * method.  See the description of that method for the various options.
     * If calculate has not been called, this method will create a polygon
     * from the control points on the original spline.
     *
     * @return a new polygon approximating this spline.
     */
    public Path2 getPath(){
        float[] points = getActivePoints();
        if(points == null) return new Path2();

        Path2 path = new Path2();
        int size  = points.length/2;
        int limit = isClosed() ? size-4 : size-1;
        path.vertices = new float[(int) (Math.floor(limit/3)+1)*2];
        int ind = 0;
        for(int i=0; i*3<=limit; i++){
            path.vertices[ind] = points[3*2*i];
            path.vertices[ind+1] = points[3*2*i+1];
            ind+=2;
            for (Entry<Integer, Integer> cur : anchorPts.entrySet()) {
                if (cur.getKey() % 3 == 0 && !(spline.smooth[cur.getValue()])) {
                    path.corners.add(cur.getKey() / 3);
                }
            }
        }
        path.closed = isClosed();
        return path;
    }

    /**
     * Returns a list of parameters for a polygon approximation
     *
     * The parameters correspond to the generating values in the spline
     * polynomial.  That is, if you evaluate the polynomial on the parameters,
     * {via Bezerier.getPoint(), you will get the points in the
     * approximating polygon.
     *
     * The resolution of the polygon is determined by the calculate
     * method.  See the description of that method for the various options.
     * If calculate has not been called, this method will choose parameters
     * for the control points on the original spline.
     *
     * @return a list of parameters for a polygon approximation
     */
    public float[] getParameters(){
        float[] result;
        if(calculated){
            result = new float[paramBuff.size()];
            for (int i=0; i<paramBuff.size(); i++){
                result[i] = paramBuff.get(i);
            }
        }else if (spline!=null){
            result = new float[spline.size()+1];
            for(int i=0; i<=spline.size(); i++){
                result[i] = i;
            }
        } else {
            result = new float[0];
        }
        return result;
    }

    /**
     * Returns a list of tangents for a polygon approximation
     *
     * These tangent vectors are presented in control point order.  First, we
     * have the right tangent of the first point, then the left tangent of the
     * second point, then the right, and so on.  Hence if the polygon contains
     * n points, this method will return 2(n-1) tangents.
     *
     * The resolution of the polygon is determined by the calculate()
     * method.  See the description of that method for the various options.
     * If calculate has not been called, this method will choose tangents
     * for the control points on the original spline.  This latter option
     * is useful when you want to draw a UI for the control point tangents.
     *
     * @return a list of tangents for a polygon approximation
     */
    public float[] getTangents(){
        float[] points = getActivePoints();
        float[] result = new float[0];

        if(points == null) return result;

        int size = points.length/2;
        result = new float[(int) (Math.floor((size-1)/3)*4+2)];

        int ind = 0;
        for(int i=0; 3*i < size-1; i++){
            result[ind] = points[3*2*i+2] - points[3*2*i];
            result[ind+1] = points[3*2*i+3] - points[3*2*i+1];
            result[ind+2] = points[3*2*i+4] - points[3*2*i+6];
            result[ind+3] = points[3*2*i+5] - points[3*2*i+7];
            ind += 4;
        }

        result[ind] = points[(size-2)*2] - points[(size-1)*2];
        result[ind+1] = points[(size-2)*2+1] - points[(size-1)*2+1];

        return result;
    }

    /**
     * Returns a list of normals for a polygon approximation
     *
     * There is one normal per control point. If polygon contains n points,
     * this method will also return n normals. The normals are determined by the
     * right tangents.  If the spline is open, then the normal of the last point
     * is determined by its left tangent.
     *
     * The resolution of the polygon is determined by the calculate
     * method.  See the description of that method for the various options.
     * If calculate has not been called, this method will choose normals
     * for the control points on the original spline.  This latter option
     * is useful when you want to draw a UI for the control point normals.
     *
     * @return a list of normals for a polygon approximation
     */
    public float[] getNormals(){
        float[] points = getActivePoints();
        float[] result = new float[0];

        if(points == null) return result;

        int size = points.length/2;

        result = new float[(int) (Math.floor((size-1)/3)*2+2)];

        float tx,ty,t;
        int ind = 0;
        for (int i=0; 3*i<size-1; i++){
            tx = points[(3*i+1)*2] - points[3*i*2];
            ty = points[(3*i+1)*2+1] - points[3*i*2+1];
            t = tx;
            tx = -ty;
            ty = t;
            result[ind] = tx;
            result[ind+1] = ty;
            ind += 2;
        }

        tx = points[(size-1)*2] - points[(size-2)*2];
        ty = points[(size-1)*2+1] - points[(size-2)*2+1];
        t = tx;
        tx = -ty;
        ty = t;
        result[ind] = tx;
        result[ind+1] = ty;
        return result;
    }

    /**
     * Fills in the vertex and index data for a single handle.
     *
     * Handles are circular shapes of a given radius. This information may be
     * passed to a PolygonNode to provide a visual representation of the
     * anchor points (as seen in Adobe Illustrator).
     *
     * @param  px    The x location of the handle point
     * @param  py    The y location of the handle point
     * @param  radius   the radius of each handle
     * @param  segments the number of segments in the handle "circle"
     */
    private void fillHandle(float px, float py, float radius, int segments){
        // Figure out the starting vertex
        int offset = vertBuff.length/2;

        float[] newV = new float[vertBuff.length+4+segments*2];
        System.arraycopy(vertBuff, 0, newV, 0, vertBuff.length);
        newV[vertBuff.length] = px;
        newV[vertBuff.length+1] = py;

        float coef = (float) (2.0f * Math.PI / segments);
        float tx, ty;
        for(int i=0; i<=segments*2; i+=2){
            float rads = i*coef;
            tx = (float) (0.5f * radius * Math.cos(rads));
            ty = (float) (0.5f * radius * Math.sin(rads));
            newV[vertBuff.length+2+i] = tx;
            newV[vertBuff.length+2+i+1] = ty;
        }
        vertBuff = newV;

        short[] newI = new short[indBuff.length+3*segments];
        System.arraycopy(indBuff, 0, newI, 0, indBuff.length);
        for(int i=0; i<segments*3; i+=3){
            newI[indBuff.length+i] = (short) offset;
            newI[indBuff.length+i+1] = (short) (offset+i+1);
            newI[indBuff.length+i+2] = (short) (offset+i+2);
        }
        indBuff = newI;
    }

    /**
     * Returns a Poly2 representing handles for the anchor points
     *
     * This method returns a collection of vertex information for handles at
     * the anchor points.  Handles are circular shapes of a given radius. This
     * information may be drawn to provide a visual representation of the
     * anchor points (as seen in Adobe Illustrator).
     *
     * The resolution of the polygon is determined by the calculate
     * method.  See the description of that method for the various options.
     * If calculate has not been called, this method will choose anchors
     * for the control points on the original spline.  This latter option
     * is useful when you want to draw a UI for the original control points.
     *
     * @param  radius   the radius of each handle
     * @param  segments the number of segments in the handle "circle"
     *
     * @return a Poly2 representing handles for the anchor points
     */
    public Poly2 getAnchors(float radius, int segments){
        float[] points = getActivePoints();
        if(points == null) return new Poly2(new float[0]);

        int size = points.length/2;
        int last = (closed ? (size - 4)/3 : (size - 1)/3);

        vertBuff = new float[0];
        indBuff = new short[0];
        for (int i=0; i<=last; i++){
            fillHandle(points[3*2*i], points[3*2*i+1], radius, segments);
        }
        return new Poly2(vertBuff,indBuff);
    }

    /**
     * Returns a Poly2 representing handles for the anchor points
     *
     * This method returns a collection of vertex information for handles at
     * the anchor points.  Handles are circular shapes of a given radius. This
     * information may be drawn to provide a visual representation of the
     * anchor points (as seen in Adobe Illustrator).
     *
     * The resolution of the polygon is determined by the calculate
     * method.  See the description of that method for the various options.
     * If calculate has not been called, this method will choose anchors
     * for the control points on the original spline.  This latter option
     * is useful when you want to draw a UI for the original control points.
     *
     * @param  radius   the radius of each handle
     *
     * @return a Poly2 representing handles for the anchor points
     */
    public Poly2 getAnchors(float radius){
        return getAnchors(radius,4);
    }

    /**
     * Returns a Poly2 representing handles for the tangent points
     *
     * This method returns vertex information for handles at the tangent
     * points.  Handles are circular shapes of a given radius. This information
     * may be passed to a PolygonNode to provide a visual representation of the
     * tangent points (as seen in Adobe Illustrator).
     *
     * The resolution of the polygon is determined by the calculate
     * method.  See the description of that method for the various options.
     * If calculate has not been called, this method will choose the tangents
     * from the control points on the original spline.  This latter option
     * is useful when you want to draw a UI for the original tangent points.
     *
     * @param  radius   the radius of each handle
     * @param  segments the number of segments in the handle "circle"
     *
     * @return a Poly2 representing handles for the tangent points
     */
    public Poly2 getHandle(float radius, int segments){
        float[] points = getActivePoints();
        if(points == null) return new Poly2(new float[0]);

        int size = points.length/2;

        vertBuff = new float[0];
        indBuff = new short[0];
        for (int i=0; 3*i<size; i++){
            fillHandle(points[(3*i+1)*2], points[(3*i+1)*2+1], radius, segments);
            fillHandle(points[(3*i+2)*2], points[(3*i+2)*2+1], radius, segments);
        }
        return new Poly2(vertBuff,indBuff);
    }

    /**
     * Returns a Poly2 representing handles for the tangent points
     *
     * This method returns vertex information for handles at the tangent
     * points.  Handles are circular shapes of a given radius. This information
     * may be passed to a PolygonNode to provide a visual representation of the
     * tangent points (as seen in Adobe Illustrator).
     *
     * The resolution of the polygon is determined by the calculate
     * method.  See the description of that method for the various options.
     * If calculate has not been called, this method will choose the tangents
     * from the control points on the original spline.  This latter option
     * is useful when you want to draw a UI for the original tangent points.
     *
     * @param  radius   the radius of each handle
     *
     * @return a Poly2 representing handles for the tangent points
     */
    public Poly2 getHandle(float radius){
        return getHandle(radius,4);
    }

    /**
     * Returns an expanded version of this spline
     *
     * When we use de Castlejau's to approximate the spline, it produces a list
     * of control points that are geometrically equal to this spline (e.g. ignoring
     * parameterization). Instead of flattening this information to a polygon,
     * this method presents this data as a new spline.
     *
     * The resolution of the polygon is determined by the calculate
     * method.  See the description of that method for the various options.
     * If calculate has not been called, this method will copy the original spline.
     *
     * @return an expanded version of this spline
     */
    public Spline2 getRefinement(){
        float[] points = getActivePoints();
        if(points == null) return new Spline2(new float[0]);

        Spline2 result = new Spline2(points);
        result.setClosed(isClosed());
        return result;
    }

    /**
     * Returns the currently "active" control points.
     *
     * If the calculation has been run, this is the data for the calculation.
     * Otherwise, it is the control points of the original spline
     */
    private float[] getActivePoints(){
        if (calculated){
            float[] res = new float[pointBuff.size()];
            for(int i=0; i<pointBuff.size(); i++){
                res[i] = pointBuff.get(i);
            }
            return res;
        } else if(spline!=null){
            return spline.getControlPoints();
        } else {
            return null;
        }
    }

    /**
     * Returns true if the current approximation is closed.
     *
     * @return true if the current approximation is closed.
     */
    private boolean isClosed(){
        return (calculated ? closed : (spline != null && spline.isClosed()));
    }

    /**
     * Returns true if the point at the given position is an anchor
     *
     * This value is always true if calculate has not been called
     *
     * @param pos   The position to check
     *
     * @return true if the point at the given position is an anchor
     */
    private boolean isAnchor(int pos){
        return (!calculated || anchorPts.containsKey(pos));
    }

}
