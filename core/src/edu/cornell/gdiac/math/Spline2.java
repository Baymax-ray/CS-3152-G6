/*
 * Spline2.java
 *
 * This class represents a spline of cubic beziers. A bezier spline is just a sequence of beziers joined together,
 * so that the end of one is the beginning of the other. Cubic beziers have four control points, two for the vertex
 * anchors and two for their tangents.
 *
 * This class has been purposefully kept lightweight.  If you want to draw a Spline2, you will need to allocate a
 * Path2 value for the spline using the factory SplinePather.  We have to turn shapes into paths or polygons
 * to draw them anyway, and this allows us to do all of the cool things we can already do with paths, like extrude
 * them or create wireframes.
 *
 * @author Crystal Jin
 * @date   12/5/2022
 */
package edu.cornell.gdiac.math;

import com.badlogic.gdx.math.Vector2;

/**
 * A class representing a spline of cubic beziers.
 *
 * A bezier spline is a sequence of beziers, where the start of one is the begining of the other.
 * A bezier spline may be open or closed. In a closed spline, the end of the last bezier is the beginning of the first
 * (or in the case of a degenerate bezier, a bezier with the same beginning and end).
 *
 * A single cubic bezier is represented by the four points. There are the two anchor points P1 and P2.
 * These represent the start and end of the curve.
 * In addition, each of these points has a tangent: T1 and T2. The curve is defined so that P1 has a right tangent of T1,
 * and P2 has a left tangent of T2. The tangents themselves are given as points, not vectors (so the tangent vector is Tn-Pn).
 * These four points are known as the control points. When we represent a bezier, we typically represent it as
 * a list of four points in this order: P1, T1, T2, and P2.
 *
 * In a bezier spline, the first anchor point of the next curve is the same as the last anchor point of the previous one.
 * There is no need to duplicate this information. However, the tanget is not a duplicate, since anchor points on the
 * interior of the spline have both a left and right tangent. Therefore, the control point list always has two tangents
 * between any two anchors. Thus bezier spline of n beziers will contain 3n+1 control points.
 *
 * This class does not contain any drawing functionality at all. If you wish to draw a bezier, create a Path2 with
 * the SplinePather factory. This factory creates a line-segment approximation of the spline, in much the same way
 * that we approximate circles or ellipses when drawing them. You can then use the many features for drawing
 * a path object, such as extrusion or wireframes.
 *
 * This class has a lot of advanced methods to detect the nearest anchor, tanget, or curve location to a point.
 * These are designed so that you can edit a bezier in a level editor for your game. These methods determine
 * the part of the bezier closest to you mouse location, so that you can select and edit them.
 */
public class Spline2 {
    /** The number of segments in this spline */
    private int size;
    /**
     * The defining control points of this spline (both anchor points and tangents).
     *
     * The number of elements in this array is 6*size+2. Each point is an adjacent
     * pair in the array, and each segment has four points (anchor, tangent,
     * tangent, anchor).  The first and last anchor of each segment is shared
     * and not repeated.
     */
    private float[] points;
    /** For each anchor point in the spline, whether it is a smooth point or a hinge point. */
    public boolean[] smooth;
    /** Whether the spline is closed. This effects editing and polygon approximation */
    public boolean closed;

    /** Maximum recursion depth for de Castlejau's */
    private final int MAX_DEPTH = 8;
    /** Tolerance to identify a point as "smooth" */
    private final float SMOOTH_TOLERANCE = 0.0001f;

    /**
     * Creates a degenerate spline of one point
     *
     * This resulting spline consists of a single point, but it is still size
     * 0. That is because it has no segments (and as a degenerate spline, it
     * is open).
     *
     * This constructor is useful for building up a spline incrementally.
     *
     * @param  point    The bezier anchor point
     */
    public Spline2(Vector2 point){
        points = new float[2];
        smooth = new boolean[1];
        points[0] = point.x;
        points[1] = point.y;
        closed = false;
        size = 0;
    }

    /**
     * Creates a spline of two points
     *
     * The minimum spline possible has 4 points: two anchors and two tangents.
     * This sets the start to be the first anchor point, and end to be the
     * second.  The tangents, are the same as the anchor points, which means
     * that the tangents are degenerate.  This has the effect of making the
     * bezier a straight line from start to end. The spline is open, unless
     * start and end are the same.
     *
     * @param  start    The first bezier anchor point
     * @param  end      The second bezier anchor point
     */
    public Spline2(Vector2 start, Vector2 end) {
        points = new float[8];
        smooth = new boolean[2];
        points[0] = start.x;
        points[1] = start.y;
        points[2] = start.x;
        points[3] = start.y;
        points[4] = end.x;
        points[5] = end.y;
        points[6] = end.x;
        points[7] = end.y;
        closed = (start.equals(end));
        size = 1;
    }

    /**
     * Creates a spline from the given control points.
     *
     * The control points must be specified in the form
     *
     *      anchor, tangent, tangent, anchor, tangent ... anchor
     *
     * That is, starts and ends with anchors, and every two anchors have two
     * tangents (right of the first, left of the second) in between. The
     * size of this vector must be equal to 1 mod 3.
     *
     * The created spline is open.
     *
     * @param  points   The vector of control points
     */
    public Spline2(float[] points){
        if ((points.length/2) % 3 != 1) throw new IllegalArgumentException("Control point array is the wrong size.");
        size = (points.length/2 - 1) / 3;
        closed = false;
        this.points = points;
        smooth = new boolean[size+1];
        for(int i=1; i<size;i++){
            smooth[i] = checkSmooth(i);
        }
    }

    /**
     * Creates a copy of the given spline.
     *
     * @param  spline   The spline to copy
     */
    public Spline2(Spline2 spline) {
        size = spline.size;
        closed = spline.closed;
        points = spline.points;
        smooth = spline.smooth;
    }

    /**
     * Sets this spline to be a degenerate degenerate of one point
     *
     * This resulting spline consists of a single point, but it is still size
     * 0. That is because it has no segments (and as a degenerate spline, it
     * is open).
     *
     * This assignment is useful for building up a spline incrementally.
     *
     * @param  point    The bezier anchor point
     *
     * @return This spline, returned for chaining
     */
    public Spline2 set(Vector2 point) {
        points = new float[2];
        smooth = new boolean[1];
        points[0] = point.x;
        points[1] = point.y;
        closed = false;
        size = 0;
        return this;
    }

    /**
     * Sets this spline to be a line between two points
     *
     * The minimum spline possible has 4 points: two anchors and two tangents.
     * This sets the start to be the first anchor point, and end to be the
     * second.  The tangents, are the same as the anchor points, which means
     * that the tangents are degenerate.  This has the effect of making the
     * bezier a straight line from start to end. The spline is open, unless
     * start and end are the same.
     *
     * @param  start    The first bezier anchor point
     * @param  end      The second bezier anchor point
     *
     * @return This spline, returned for chaining
     */
    public Spline2 set(Vector2 start, Vector2 end) {
        points = new float[8];
        smooth = new boolean[2];
        points[0] = start.x;
        points[1] = start.y;
        points[2] = start.x;
        points[3] = start.y;
        points[4] = end.x;
        points[5] = end.y;
        points[6] = end.x;
        points[7] = end.y;
        closed = (start.equals(end));
        size = 1;
        return this;
    }

    /**
     * Sets this spline to have the given control points.
     *
     * The control points must be specified in the form
     *
     *      anchor, tangent, tangent, anchor, tangent ... anchor
     *
     * That is, starts and ends with anchors, and every two anchors have two
     * tangents (right of the first, left of the second) in between. The
     * size of this vector must be equal to 1 mod 3.
     *
     * This method makes the spline is open.
     *
     * @param  points   The vector of control points
     *
     * @return This spline, returned for chaining
     */
    public Spline2 set(float[] points) {
        if ((points.length/2) % 3 != 1) throw new IllegalArgumentException("Control point array is the wrong size.");
        size = (points.length/2 - 1) / 3;
        closed = false;
        this.points = points;
        smooth = new boolean[size+1];
        for(int i=1; i<size;i++){
            smooth[i] = checkSmooth(i);
        }
        return this;
    }

    /**
     * Sets this spline to be a copy of the given spline.
     *
     * @param  spline   The spline to copy
     *
     * @return This spline, returned for chaining
     */
    public Spline2 set(Spline2 spline) {
        size = spline.size;
        closed = spline.closed;
        points = spline.points;
        smooth = spline.smooth;
        return this;
    }

    /**
     * Sets whether the spline is closed.
     *
     * A closed spline is one where the first and last anchor are the same.
     * Hence the first and last tangents are tangents (right, and left,
     * respectively) of the same point. This is relevant for the setTangent()
     * method, particularly if the change is meant to be symmetric.
     *
     * When closing a spline, the end point is not a smooth point. This can
     * be changed by calling {@link #setSmooth}.
     *
     * A closed spline has no end. Therefore, anchors cannot be added to
     * a closed spline.  They may only be inserted between two other
     * anchors.
     *
     * @param flag      Whether the spline is closed
     */
    public void setClosed(boolean flag) {
        if (points.length==0){
            return;
        }

        if(flag && !closed) {
            if (!(points[0] == points[points.length-2] && points[1] == points[points.length-1])) {
                addAnchor(new Vector2(points[0], points[1]));
            }
        } else if (!flag && closed) {
            deleteAnchor(size);
        }
        closed = flag;
    }



    /**
     * Returns the number of segments in this spline
     *
     * Each segment is a bezier. To use the bezier methods associated with this
     * class, you will need to know the correct segment.
     *
     * @return the number of segments in this spline
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if the spline is closed.
     *
     * A closed spline is one where the first and last anchor are the same.
     * Hence the first and last tangents are tangents (right, and left,
     * respectively) of the same point.  This is relevant for the setTangent()
     * method, particularly if the change is meant to be symmetric.
     *
     * When closing a spline, the end point is not a smooth point. This can
     * be changed by calling {@link #setSmooth}.
     *
     * A closed spline has no end. Therefore, anchors cannot be added to
     * a closed spline.  They may only be inserted between two other
     * anchors.
     *
     * @return true if the spline is closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Returns the spline point for parameter tp.
     *
     * This method is like the public getPoint(), except that it is restricted
     * to a single bezier segment.  A bezier is parameterized with tp in 0..1,
     * with tp = 0 representing the first anchor and tp = 1 representing the
     * second. This method is used by the public getPoint() to compute its value.
     *
     * @param  segment  the bezier segment to select from
     * @param  tp       the parameterization value
     *
     * @return the spline point for parameter tp
     */
    private Vector2 getPoint(int segment, float tp){
        if (!(segment >= 0 && segment < size)) throw new IllegalArgumentException("Illegal spline segment");
        if (!(tp >= 0.0f && tp <= 1.0f)) throw new IllegalArgumentException("Illegal segment parameter");

        int index = 3*segment;
        float sp = 1-tp;
        float a = sp*sp;
        float d = tp*tp;
        float b = 3 * tp*a;
        float c = 3 * sp*d;
        a = a*sp;
        d = d*tp;

        float x = a*points[index*2] + b*points[index*2+2] + c*points[index*2+4] + d*points[index*2+6];
        float y = a*points[index*2+1] + b*points[index*2+3] + c*points[index*2+5] + d*points[index*2+7];

        return new Vector2(x,y);
    }

    /**
     * Returns the spline point for parameter tp.
     *
     * A bezier spline is a parameterized curve. For a single bezier, it is
     * parameterized with tp in 0..1, with tp = 0 representing the first
     * anchor and tp = 1 representing the second. In the spline, we generalize
     * this idea, where tp is an anchor if it is an int, and is inbetween
     * the anchors floor(tp) and ceil(tp) otherwise.
     *
     * @param  tp   the parameterization value
     *
     * @return the spline point for parameter tp
     */
    public Vector2 getPoint(float tp) {
        return getPoint((int)tp,tp-(int)tp);
    }

    /**
     * Sets the spline point at parameter tp.
     *
     * A bezier spline is a parameterized curve. For a single bezier, it is
     * parameterized with tp in 0..1, with tp = 0 representing the first
     * anchor and tp = 1 representing the second. In the spline, we generalize
     * this idea, where tp is an anchor if it is an int, and is inbetween
     * the anchors floor(tp) and ceil(tp) otherwise.
     *
     * In this method, if tp is an int, it will just reassign the associated
     * anchor value.  Otherwise, this will insert a new anchor point at
     * that parameter.  This has a side-effect of changing the parameterization
     * values for the curve, as the number of beziers has increased.
     *
     * @param  tp       the parameterization value
     * @param  point    the new value to assign
     */
    public void setPoint(float tp, Vector2 point) {
        if(!(tp >= 0 && tp <= size)) throw new IllegalArgumentException("Parameter out of bounds");
        if(!(!closed || tp < size)) throw new IllegalArgumentException("Parameter out of bounds for closed spline");
        int seg = (int) tp;
        if (seg == tp) {
            setAnchor(seg, point);
        }
        else {
            tp = tp - seg;
            insertAnchor(seg, tp);
            setAnchor(seg + 1, point);
        }
    }

    /**
     * Sets the anchor point at the given index.
     *
     * This method will change both the anchor and its associated tangets.
     * The new tangents will have the same relative change in position.
     * As a result, the bezier will still have the same shape locally.
     * This is the natural behavior for changing an anchor, as seen in
     * Adobe Illustrator.
     *
     * If an open spline has n segments, then it has n+1 anchors. Similiarly,
     * a closed spline had n anchors.  The value index should be in the
     * appropriate range.
     *
     * @param  index    the anchor index (0..n+1 or 0..n)
     * @param  point    the new value to assign
     */
    public void setAnchor(int index, Vector2 point) {
        if(!(index >= 0 && index <= size)) throw new IllegalArgumentException("Index out of bounds");
        if(!(!closed || index < size)) throw new IllegalArgumentException("Index out of bounds for closed spline");

        float dx = point.x - points[2*3*index];
        float dy = point.y - points[2*3*index+1];

        // Adjust left tangents
        if (index > 0) {
            points[3 * index*2 - 2] += dx;
            points[3 * index*2 - 1] += dy;
        } else if (closed) {
            points[3 * size * 2 - 2] += dx;
            points[3 * size * 2 - 1] += dy;
        }

        // Adjust right tangents
        if (index < size) {
            points[3 * index * 2 + 1] += dx;
            points[3 * index * 2 + 2] += dy;
        } else if (closed) {
            points[2] += dx;
            points[3] += dy;
        }

        points[3 * index * 2] = point.x;
        points[3 * index * 2+1] = point.y;
    }

    /**
     * Returns the smoothness for the anchor point at the given index.
     *
     * A smooth anchor is one in which the derivative of the curve at the
     * anchor is continuous.  Practically, this means that the left and
     * right tangents are always parallel.  Only a non-smooth anchor may
     * form a "hinge".
     *
     * If an open spline has n segments, then it has n+1 anchors. However,
     * the two end anchors can never be smooth. A closed spline with the
     * same number of segments has n anchors, but all anchors have the
     * potential to be smooth.
     *
     * @param  index    the anchor index (0..n+1 or 0..n)
     *
     * @return the smoothness for the anchor point at the given index.
     */
    public boolean isSmooth(int index) {
        if (!(index >= 0 && index < size)) throw new IllegalArgumentException("Index out of bounds");
        if (closed && index > size - 1) throw new IllegalArgumentException("Index out of bounds for closed spline");
        return smooth[index];
    }

    /**
     * Sets the smoothness for the anchor point at the given index.
     *
     * A smooth anchor is one in which the derivative of the curve at the
     * anchor is continuous.  Practically, this means that the left and
     * right tangents are always parallel.  Only a non-smooth anchor may
     * form a "hinge".
     *
     * If you set a non-smooth anchor to smooth, it will adjust the
     * tangents accordingly.  In particular, it will average the two
     * tangents, making them parallel. This is the natural behavior for
     * changing an smoothness, as seen in Adobe Illustrator.
     *
     * If an open spline has n segments, then it has n+1 anchors. However,
     * the two end anchors can never be smooth. A closed spline with the
     * same number of segments has n anchors, but all anchors have the
     * potential to be smooth.
     *
     * @param  index    the anchor index (0..n+1 or 0..n)
     * @param  flag     the anchor smoothness
     */
    public void setSmooth(int index, boolean flag) {
        if (!(!closed || index < size)) throw new IllegalArgumentException("Index out of bounds for closed spline");
        if (!(index >= 0 && index <= size)) throw new IllegalArgumentException("Index out of bounds");
        if (!(closed || (index > 0 && index < size))) throw new IllegalArgumentException("End point smoothness cannot be changed");

        smooth[index] = flag;
        if (flag) {
            int rindx = (index == 0 && closed) ? size : index;
            Vector2 temp0 = new Vector2(points[3 * rindx*2-2] - points[3 * index*2], points[3 * rindx*2-1] - points[3 * index*2+1]);
            Vector2 temp1 = new Vector2(points[3 * index*2] - points[3 * index*2+2], points[3 * index*2+1] - points[3 * index*2+3]);

            if(temp0.isZero()){
                temp0 = temp1;
            } else if (temp1.isZero()){
                temp1 = temp0;
            } else  {
                float scale0 = temp0.len();
                float scale1 = temp1.len();

                // Average the vectors
                temp0 = temp0.nor();
                temp1 = temp1.nor();
                Vector2 temp2 = new Vector2((temp0.x+temp1.x)/2.0f,(temp0.y+temp1.y)/2.0f);
                temp2 = temp2.nor();

                // Scale them appropriately
                temp0.set(temp2);
                temp0.scl(scale0);
                temp1.set(temp2);
                temp1.scl(scale1);
            }

            points[3*rindx*2-2] = points[3*index*2] + temp0.x;
            points[3*rindx*2-1] = points[3*index*2+1] + temp0.y;
            points[3*index*2+1] = points[3*index*2] - temp1.x;
            points[3*index*2+2] = points[3*index*2+1] - temp1.y;
        }
    }

    /**
     * Returns the tangent at the given index.
     *
     * Tangents are specified as points, not vectors.  To get the tangent
     * vector for an anchor, you must subtract the anchor from its tangent
     * point.  Hence a curve is degenerate when the tangent and the
     * anchor are the same.
     *
     * If a spline has n segments, then it has 2n tangents. This is true
     * regardless of whether it is open or closed. The value index should
     * be in the appropriate range. An even index is a right tangent,
     * while an odd index is a left tangent. If the spline is closed, then
     * 2n-1 is the left tangent of the first point.
     *
     * @param  index    the tangent index (0..2n)
     *
     * @return the tangent at the given index.
     */
    public Vector2 getTangent(int index) {
        if(!(index >= 0 && index < 2 * size)) throw new IllegalArgumentException("Index out of bounds");
        int spline = (index + 1) / 2;
        int anchor = 3 * spline;
        int tangt = (index % 2 == 1 ? anchor - 1 : anchor + 1);
        return new Vector2(points[tangt*2],points[tangt*2+1]);
    }

    /**
     * Sets the tangent at the given index.
     *
     * Tangents are specified as points, not vectors.  To get the tangent
     * vector for an anchor, you must subtract the anchor from its tangent
     * point.  Hence a curve is degenerate when the tangent and the
     * anchor are the same.
     *
     * If the associated anchor point is smooth, changing the direction
     * of the tangent vector will also change the direction of the other
     * tangent vector (so that they remain parallel).  However, changing
     * only the magnitude will have no effect, unless symmetric is true.
     * In that case, it will modify the other tangent so that it has the
     * same magnitude and parallel direction. This is the natural behavior
     * for changing a tangent, as seen in Adobe Illustrator.
     *
     * If a spline has n segments, then it has 2n tangents. This is true
     * regardless of whether it is open or closed. The value index should
     * be in the appropriate range. An even index is a right tangent,
     * while an odd index is a left tangent. If the spline is closed, then
     * 2n-1 is the left tangent of the first point.
     *
     * @param  index     the tangent index (0..2n)
     * @param  tang      the new value to assign
     * @param  symmetric whether to make the other tangent symmetric
     */
    public void setTangent(int index, Vector2 tang, boolean symmetric) {
        if(!(index >= 0 && index < 2 * size)) throw new IllegalArgumentException("Index out of bounds");

        int spline = (index + 1) / 2;
        int anchor = 3 * spline;
        int tangt1 = (index % 2 == 1 ? anchor - 1 : anchor + 1);
        int tangt2 = (index % 2 == 1 ? anchor + 1 : anchor - 1);

        if (spline == 0) {
            tangt2 = (closed ? 3 * size - 1 : -1);
        } else if (spline == size) {
            tangt2 = (closed ? 1 : -1);
        }

        if (symmetric && tangt2 != -1) {
            points[tangt2*2] = points[anchor*2] + points[anchor*2] - tang.x;
            points[tangt2*2+1] = points[anchor*2+1] + points[anchor*2+1] - tang.y;
        } else if (smooth[spline] && tangt2 != -1) {
            Vector2 temp0 = new Vector2(points[anchor*2]-points[tangt2*2], points[anchor*2+1]-points[tangt2*2+1]);
            float d = temp0.len();
            temp0 = new Vector2(points[anchor*2]-tang.x, points[anchor*2+1]-tang.y);
            temp0 = temp0.nor();
            temp0.scl(d);

            points[tangt2*2] = points[anchor*2] + temp0.x;
            points[tangt2*2+1] = points[anchor*2+1] + temp0.y;
        }

        points[tangt1*2] = tang.x;
        points[tangt1*2+1] = tang.y;
    }

    /**
     * Returns the spline control points.
     *
     * If the spline has n segments, then the list will have 6n+2 elements
     * in it, representing the n+1 anchor points and the 2n tangents.
     * The values will alternate
     *
     *      anchor, tangent, tangent, anchor, tangent ... anchor
     *
     * This is true even if the curve is closed.  In that case, the
     * first and last anchor points will be the same.
     *
     * @return the spline control points.
     */
    public float[] getControlPoints() {
        return points;
    }

    /**
     * Adds the given point to the end of the spline, creating a new segment.
     *
     * Assuming that the spline is open, the new segment will start at the end
     * of the previous last segment and extend it to the given point. The value
     * tang is the left tangent of the new anchor point.
     *
     * As the previous end point could not have been smooth, it will remain that
     * way (so the right tangent of that point will be degenerate -- equal to
     * the anchor). Use {@link #setSmooth} to change the characteristic of an
     * interior point. If the spline was super degenerate (there were no control
     * points at all), then this method will simply add the given anchor, but
     * keep the segment size as 0.
     *
     * As closed splines have no end, this method will fail on closed splines.
     * You should use {@link #insertAnchor} instead for closed splines.
     *
     * @param  point    the new anchor point to add to the end
     * @param  tang     the left tangent of the new anchor point
     *
     * @return the new number of segments in this spline
     */
    public int addAnchor(Vector2 point, Vector2 tang) {
        if (closed) throw new IllegalArgumentException("Cannot append to closed curve");

        if(points.length == 0){
            set(point);
        } else {
            float[] newP = new float[points.length+6];
            System.arraycopy(points, 0, newP, 0, points.length);
            boolean[] newS = new boolean[smooth.length+1];
            System.arraycopy(smooth, 0, newS, 0, smooth.length);
            smooth = newS;
            int pos = 3*size+1;
            if (smooth[size]) {
                newP[points.length] = points[(pos-1)*2]*2 - points[(pos-2)*2];
                newP[points.length+1] = points[(pos-1)*2+1]*2 - points[(pos-2)*2+1];
            } else {
                newP[points.length] = points[(pos-1)*2];
                newP[points.length+1] = points[(pos-1)*2+1];
            }
            newP[points.length+2] = tang.x;
            newP[points.length+3] = tang.y;
            newP[points.length+4] = point.x;
            newP[points.length+5] = point.y;
            points = newP;
            size++;
        }
        return size;
    }

    /**
     * Adds the given point to the end of the spline, creating a new segment.
     *
     * Assuming that the spline is open, the new segment will start at the end
     * of the previous last segment and extend it to the given point. This method
     * will add a degenerate left tangent (e.g the tangent point is the same as
     * an anchor point).
     *
     * As the previous end point could not have been smooth, it will remain that
     * way (so the right tangent of that point will be degenerate -- equal to
     * the anchor). Use {@link #setSmooth} to change the characteristic of an
     * interior point. If the spline was super degenerate (there were no control
     * points at all), then this method will simply add the given anchor, but
     * keep the segment size as 0.
     *
     * As closed splines have no end, this method will fail on closed splines.
     * You should use {@link #insertAnchor} instead for closed splines.
     *
     * @param point     The new anchor point to add to the end
     *
     * @return the new number of segments in this spline
     */
    public int addAnchor(Vector2 point) {
        return addAnchor(point,point);
    }

    /**
     * Adds a (cubic) bezier path from the end of the spline to point.
     *
     * Assuming that the spline is open, the new segment will start at the end
     * of the previous last segment and extend it to the given point. The given
     * control points will define the right tangent of the previous end point,
     * and the left tangent of point, respectively.
     *
     * This method will compute whether or not the previous end point is smooth
     * from the new right tangent. If the spline was super degenerate (there
     * were no control points at all), then this method will build the bezier
     * from the origin.
     *
     * As closed splines have no end, this method will fail on closed splines.
     *
     * @param control1  The first bezier control point
     * @param control2  The second bezier control point
     * @param point     The new anchor point
     *
     * @return the new number of segments in this spline
     */
    public int addBezier(Vector2 control1, Vector2 control2, Vector2 point){
        if (closed) throw new IllegalArgumentException("Cannot append to closed curve");
        if(points.length == 0){
            set(new Vector2());
        }

        float[] newP = new float[points.length+6];
        boolean[] newS = new boolean[smooth.length+1];
        System.arraycopy(points, 0, newP, 0, points.length);
        System.arraycopy(smooth, 0, newS, 0, smooth.length);
        smooth = newS;
        newP[points.length] = control1.x;
        newP[points.length+1] = control1.y;
        newP[points.length+2] = control2.x;
        newP[points.length+3] = control2.y;
        newP[points.length+4] = point.x;
        newP[points.length+5] = point.y;
        points = newP;
        size++;
        return size;
    }

    /**
     * Adds a (quadratic) bezier path from the end of the spline to point.
     *
     * Assuming that the spline is open, the new segment will start at the end
     * of the previous last segment and extend it to the given point. The new
     * right and left tangents will be computed from the given quadratic
     * control point.
     *
     * This method will compute whether or not the previous end point is smooth
     * from the new right tangent. If the spline was super degenerate (there
     * were no control points at all), then this method will build the bezier
     * from the origin.
     *
     * As closed splines have no end, this method will fail on closed splines.
     *
     * @param control   The quadratic control point
     * @param point     The new anchor point
     *
     * @return the new number of segments in this spline
     */
    public int addQuad(Vector2 control, Vector2 point) {
        if (closed) throw new IllegalArgumentException("Cannot append to closed curve");
        if(points.length == 0){
            set(new Vector2());
        }

        float[] newP = new float[points.length+6];
        boolean[] newS = new boolean[smooth.length+1];
        System.arraycopy(points, 0, newP, 0, points.length);
        System.arraycopy(smooth, 0, newS, 0, smooth.length);
        smooth = newS;

        newP[points.length] = points[points.length-2] + 2.0f/3.0f * (control.x - points[points.length-2]);
        newP[points.length+1] = points[points.length-1] + 2.0f/3.0f * (control.y - points[points.length-1]);
        newP[points.length+2] = point.x + 2.0f/3.0f * (control.x - point.x);
        newP[points.length+3] = point.y + 2.0f/3.0f * (control.y - point.y);
        points = newP;
        size++;
        return  size;
    }

    /**
     * Deletes the anchor point at the given index.
     *
     * The point is deleted as well as both of its tangents (left and right).
     * All remaining anchors after the deleted one will shift their indices
     * down by one. Deletion is allowed on closed splines; the spline will
     * remain closed after deletion.
     *
     * If an open spline has n segments, then it has n+1 anchors. Similiarly,
     * a closed spline had n anchors.  The value index should be in the
     * appropriate range.
     *
     * @param  index    the anchor index to delete
     */
    public void deleteAnchor(int index){
        if(closed && index > size) throw new IllegalArgumentException("Index out of bounds for closed spline");
        if(index < 0 || index > size) throw new IllegalArgumentException("Index out of bounds");

        float[] newP = new float[points.length-6];
        boolean[] newS = new boolean[smooth.length-1];
        if (index == size && !closed) {
            // Pop it off the back
            System.arraycopy(points, 0, newP, 0, newP.length);
            System.arraycopy(smooth, 0, newS, 0, newS.length);
        } else {
            // Shift everything left.
            System.arraycopy(points, 0, newP, 0, 3 * index * 2);
            System.arraycopy(points, (3 * (index + 1)) * 2, newP, 3 * index * 2, points.length - (3 * (index + 1)) * 2);
            System.arraycopy(smooth, 0, newS, 0, index);
            System.arraycopy(smooth, index + 1, newS, index, smooth.length - (index + 1));
        }
        points = newP;
        smooth = newS;
        size--;
    }

    /**
     * Inserts a new anchor point at parameter tp.
     *
     * Inserting an anchor point does not change the curve.  It just makes
     * an existing point that was not an anchor, now an anchor. This is the
     * natural behavior for inserting an index, as seen in Adobe Illustrator.
     *
     * This version of insertAnchor() specifies the segment for insertion,
     * simplifying the parameterization. For a single bezier, it is
     * parameterized with tp in 0..1, with tp = 0 representing the first
     * anchor and tp = 1 representing the second.
     *
     * The tangents of the new anchor point will be determined by de Castlejau's.
     * This is the natural behavior for inserting an anchor mid bezier, as seen
     * in Adobe Illustrator.
     *
     * @param  segment  the bezier segment to insert into
     * @param  param       the parameterization value
     */
    private void insertAnchor(int segment, float param){
        if(segment<0 || segment>size) throw new IllegalArgumentException("Illegal spline segment");
        if(param <= 0 || param >= 1) throw new IllegalArgumentException("Illegal insertion parameter");

        // Split the bezier.
        float [][] sub = subdivide(segment, param);

        // Replace first segment with left
        System.arraycopy(sub[0], 0, points, segment * 3 * 2, sub[0].length-2);

        // Now insert the right
        float[] newP = new float[points.length+sub[1].length];
        boolean[] newS = new boolean[smooth.length+1];
        System.arraycopy(points,0,newP, 0,(3*(segment+1))*2);
        System.arraycopy(sub[1],0,newP, (3 * (segment + 1))*2,sub[1].length-2);
        System.arraycopy(points,(3*(segment+1))*2,newP, (3*(segment+2))*2,
                points.length-(3*(segment+1))*2);
        System.arraycopy(smooth,0,newS, 0,segment);
        newS[segment] = true;
        System.arraycopy(smooth,segment,newS, segment+1,smooth.length-segment);
        points = newP;
        smooth = newS;
        size++;
    }

    /**
     * Inserts a new anchor point at parameter tp.
     *
     * Inserting an anchor point does not change the curve.  It just makes
     * an existing point that was not an anchor, now an anchor. This is the
     * natural behavior for inserting an index, as seen in Adobe Illustrator.
     *
     * A bezier spline is a parameterized curve. For a single bezier, it is
     * parameterized with tp in 0..1, with tp = 0 representing the first
     * anchor and tp = 1 representing the second. In the spline, we generalize
     * this idea, where tp is an anchor if it is an int, and is inbetween
     * the anchors floor(tp) and ceil(tp) otherwise.
     *
     * The tangents of the new anchor point will be determined by de Castlejau's.
     * This is the natural behavior for inserting an anchor mid bezier, as seen
     * in Adobe Illustrator.
     *
     * @param param     the parameterization value
     */
    public void insertAnchor(float param) {
        insertAnchor((int)param, param - (int)param);
    }

    /**
     * Returns the nearest point on the spline to the given point.
     *
     * The value is effectively the projection of the point onto the curve.  We
     * compute this point using the projection polynomial, described at
     *
     * http://jazzros.blogspot.com/2011/03/projecting-point-on-bezier-curve.html
     *
     * The point returned does not need to be an anchor point.  It can be anywhere
     * on the curve.  This allows us a way to select a non-anchor point with the
     * mouse (such as to add a new anchor point) in a level editor or other
     * program.
     *
     * @param  point    the point to project
     *
     * @return the nearest point on the spline to the given point.
     */
    public Vector2 nearestPoint(Vector2 point){
        return getPoint(nearestParameter(point));
    }

    /**
     * Returns the parameterization of the nearest point on the spline.
     *
     * The value is effectively the projection of the point onto the parametrized
     * curve. See getPoint() for an explanation of how the parameterization work. We
     * compute this value using the projection polynomial, described at
     *
     * http://jazzros.blogspot.com/2011/03/projecting-point-on-bezier-curve.html
     *
     * @param  point    the point to project
     *
     * @return the parameterization of the nearest point on the spline.
     */
    public float nearestParameter(Vector2 point) {
        float tmin = -1;
        float dmin = -1;
        int smin = -1;

        for (int i = 0; i < size; i++) {
            Vector2 pair = getProjectionSlow(point, i);
            if (smin == -1 || pair.y < dmin) {
                tmin = pair.x; dmin = pair.y; smin = i;
            }
        }
        return smin + tmin;
    }

    /**
     * Returns the index of the anchor nearest the given point.
     *
     * If there is no anchor whose distance to point is less than the square root
     * of threshold (we use lengthSquared for speed), then this method returns -1.
     *
     * @param  point        the point to compare
     * @param  threshold    the distance threshold for picking an anchor
     *
     * @return the index of the anchor nearest the given point.
     */
    public int nearestAnchor(Vector2 point, float threshold) {
        float  best = Integer.MAX_VALUE;
        int index = -1;

        for (int i = 0; i <= size; i++) {
            float tx = points[3*i*2] - point.x;
            float ty = points[3*i*2+1] - point.y;
            float d = tx*tx + ty*ty;
            if (d < threshold && d < best) {
                best = d;
                index = i;
            }
        }
        return index;
    }

    /**
     * Returns the index of the tangent nearest the given point.
     *
     * If there is no tangent whose distance to point is less than the square root
     * of threshold (we use lengthSquared for speed), then this method returns -1.
     *
     * @param  point        the point to compare
     * @param  threshold    the distance threshold for picking a tangent
     *
     * @return the index of the tangent nearest the given point.
     */
    public int nearestTangent(Vector2 point, float threshold) {
        float  best = Integer.MAX_VALUE;
        int index = -1;

        for(int i=0; i<size; i++) {
            float tx = points[(3*i+1)*2] - point.x;
            float ty = points[(3*i+1)*2+1] - point.y;
            float d = tx*tx + ty*ty;
            if (d < threshold && d < best) {
                best = d;
                index = 2 * i + 1; // Right side of index ii.
            }

            tx = points[(3*i+2)*2] - point.x;
            ty = points[(3*i+2)*2+1] - point.y;
            d = tx*tx + ty*ty;
            if (d < threshold && d < best) {
                best = d;
                index = 2 * i + 2; // Left side of index ii+1
            }
        }
        return index;
    }

    /**
     * Clears all control points, producing a degenerate spline.
     */
    public void clear() {
        points = new float[0];
        smooth = new boolean[0];
        closed = false;
        size = 0;
    }

    /**
     * Applies de Castlejau's to a bezier, putting the result in left & right
     *
     * de Castlejau's takes a parameter tp in (0,1) and splits the bezier into two,
     * preserving the geometric information, but not the parameterization.  The control
     * points for the resulting two beziers are stored in left and right.
     *
     * This static method is not restricted to the current spline.  It can work
     * from any list of control points (and offset into those control points).
     * This is useful for recursive subdivision.
     *
     * @param  segment  the bezier segment of this spine
     * @param  tp       the parameter to split at
     */
    private float[][] subdivide(int segment, float tp) {
        float src0x = points[2*segment*3];
        float src0y = points[2*segment*3+1];
        float src1x = points[2*segment*3+2];
        float src1y = points[2*segment*3+3];
        float src2x = points[2*segment*3+4];
        float src2y = points[2*segment*3+5];
        float src3x = points[2*segment*3+6];
        float src3y = points[2*segment*3+7];

        float hx = (1-tp)*src1x + tp*src2x;
        float hy = (1-tp)*src1y + tp*src2y;

        float[][] res = new float[2][8];
        // FIRST HALF
        res[0][0] = src0x;
        res[0][1] = src0y;
        res[0][2] = (1-tp)*src0x + tp*src1x;
        res[0][3] = (1-tp)*src0y + tp*src1y;
        res[0][4] = (1-tp)*res[0][2] + tp*hx;
        res[0][5] = (1-tp)*res[0][3] + tp*hy;

        // SECOND HALF
        res[1][7] = src3y;
        res[1][6] = src3x;
        res[1][5] = (1-tp)*src2y + tp*src3y;
        res[1][4] = (1-tp)*src2x + tp*src3x;
        res[1][3] = (1-tp)*hy + tp*res[1][5];
        res[1][2] = (1-tp)*hx + tp*res[1][4];
        res[1][1] = (1-tp)*res[0][5] + tp*res[1][3];
        res[1][0] = (1-tp)*res[0][4] + tp*res[1][2];

        res[0][6] = res[1][0];
        res[0][7] = res[1][1];
        return res;
    }

    /**
     * Returns the parameterization of the nearest point on the bezier segment.
     *
     * The value is effectively the projection of the point onto the parametrized
     * curve. See getPoint() for an explanation of how the parameterization work.
     *
     * This version does not use the projection polynomial.  Instead, it picks
     * a parameter resolution and walks the entire length of the curve.  The
     * result is both slow and inexact (as the actual point may be in-between
     * chose parameters). This version is only picked when getProjectionFast
     * fails because of an error with root finding.
     *
     * The value returned is a pair of the parameter, and its distance value.
     * This allows us to compare this result to other segments, picking the
     * best value for the entire spline.
     *
     * @param  point    the point to project
     * @param  segment  the bezier segment to project upon
     *
     * @return the parameterization of the nearest point on the spline.
     */
    private Vector2 getProjectionSlow(Vector2 point, int segment) {
        Vector2 result = new Vector2(-1,-1);
        int RESOLUTION = (1 << MAX_DEPTH);

        for(int i=0; i<RESOLUTION; i++) {
            float t = ((float)i) / RESOLUTION;
            Vector2 temp0 = getPoint(segment,t);
            temp0 = temp0.sub(point);
            float d = temp0.len2();
            if (result.x == -1 || d < result.y) {
                result.x = t; result.y = d;
            }
        }

        // Compare the last point.
        float tx = points[3*size*2] - point.x;
        float ty = points[3*size*2+1] - point.y;
        float d = tx*tx + ty*ty;
        if (d < result.y) {
            result.x = 1.0f; result.y = d;
        }
        return result;
    }

    /**
     * Returns true if the anchor point at the given index should be smooth.
     *
     * An anchor point should be smooth if the it is not an end point and
     * the left and right tangents are (suitably) parallel.
     *
     * @param index     The anchor point index
     *
     * @return true if the anchor point at the given index should be smooth.
     */
    private boolean checkSmooth(int index){
        Vector2 temp0 = new Vector2(points[3 * index*2-2] - points[3 * index*2], points[3 * index*2-1] - points[3 * index*2+1]);
        Vector2 temp1 = new Vector2(points[3 * index*2+2] - points[3 * index*2], points[3 * index*2+3] - points[3 * index*2+1]);
        temp0 = temp0.nor();
        temp1 = temp1.nor();
        temp0 = temp0.sub(temp1);
        return (temp0.len2() < SMOOTH_TOLERANCE);
    }

}