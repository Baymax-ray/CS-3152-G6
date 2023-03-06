/*
 * PolyFactory.java
 *
 * This class is a factory for extruding a path polgyon into a stroke with width. It has support for joints and end caps.
 *
 * The code in this factory is inspired from the extrusion code in NanoVG  Mikko Mononen (memon@inside.org). This
 * extrusion guarantees that the triangle mesh is "in order" (instead of back-filling joints after the segments) while
 * still guaranteeing fast, linear performance.
 *
 * This code has been heavily profiled and optimized to guarantee real-time performance for most applications.
 * However, extruded paths that are the result of drawing should always be passed through a {@link PathSmoother}
 * first for best performance.
 *
 * @author Crystal Jin
 * @date   12/5/2022
 */

package edu.cornell.gdiac.math;

import java.util.ArrayList;

/**
 * A factory class for extruding paths into a solid polygon.
 *
 * An extrusion of a path is a polygon that follows the path but gives it width. Hence it takes a path and turns it
 * into a solid shape. This is more complicated than simply triangulating the original path. The new polygon has more
 * vertices,depending on the choice of joint (shape at the corners) and cap (shape at the end).
 *
 * As with many of our factory classes, the methods are broken up into three phases: initialization, calculation, and
 * materialization. To use the factory, you first set the data (in this case a set of vertices or Path2 object)
 * with the initialization methods. You then call the calculation method. Finally, you use the materialization methods
 * to access the data in several different ways.
 *
 * This division allows us to support multithreaded calculation if the data generation takes too long. However, note
 * that this factory is not thread safe in that you cannot access data while it is still in mid-calculation.
 *
 * CREDITS: This code heavily inspired by NanoVG by Mikko Mononen (memon@inside.org), and the bulk of the algorithm
 * is taken from that code. However it is has been altered and optimized according to extensive profiling.
 */
public class PathExtruder {

    /** Default rounding tolerance */
    private final float TOLERANCE = 0.25f;
    /** Default mitre limit */
    private final float MITER_LIMIT = 10.0f;
    /** Epsilon value for small angles/segments */
    private final float EPSILON = 0.000001f;
    /** Algorithm-specific scaling factor */
    private final float SCALE_LIMIT = 600.0f;

    /** The mark for a left-side vertex */
    private final float LEFT_MK = 1.0f;
    /** The mark for a right-side vertex */
    private final float RGHT_MK = 1.0f;
    /** The mark for a vertex at the path head */
    private final float HEAD_MK = -1.0f;
    /** The mark for a vertex at the path tail */
    private final float TAIL_MK = 1.0f;

    /** Mark a point as a corner (so it can take a mitre or rounded joint) */
    private final int FLAG_CORNER = 0x01;
    /** Mark a point as left turning */
    private final int FLAG_LEFT = 0x02;
    /** Mark a point as requiring a bevel/square joint */
    private final int FLAG_BEVEL = 0x04;
    /** Mark a point as requiring a special interior join */
    private final int FLAG_INNER = 0x08;

    /** The extrusion joint settings */
    private Poly2.Joint joint;
    /** The extrusion end cap settings */
    private Poly2.EndCap endCap;

    /** The rounded joint/cap tolerance */
    private float tolerance;
    /** The mitre limit (bevel joint if the mitre is too pointy) */
    private float mitreLimit;

    /** Whether the path is closed */
    private boolean closed;
    /** Whether the path is convex */
    private boolean convex;

    /** Whether or not the calculation has been run */
    private boolean calculated;

    /** The set of points in the path to extrude */
    private Point points;
    /** The number of elements currently in the point buffer */
    private int pSize;

    /** The set of vertices in the active extrusion */
    private ArrayList<Float> verts;
    /** The edge markings of each of the extruded vertices */
    private ArrayList<Float> sides;
    /** The left side of the extrusion */
    private ArrayList<Float>  lefts;
    /** The right side of the extrusion */
    private ArrayList<Float> rghts;
    /** The number of elements currently in the vertex buffer */
    private int vSize;

    /** The set of indices indicating the vertex triangulation */
    private ArrayList<Short> indxs;
    /** The first vertex for the next triangle to produce */
    private int iback2;
    /** The seconnd vertex for the next triangle to produce */
    private int iback1;

    /**
     * An annotated point in the path
     *
     * This struct class keeps track of information about the direction to
     * and from this point. It substantially cuts down on repeated calculation
     * in our extrusion algorithm.
     */
    private class Point {
        /** The point x-coordinate */
        float x;
        /** The point y-coordinate */
        float y;
        /** The (normalized) x-direction to the next point in the path */
        float dx;
        /** The (normalized) y-direction to the next point in the path */
        float dy;
        /** The distance to the next point in the path */
        float len;
        /** The x-coordinate of the vector average (incoming, outgoing) at this point */
        float dmx;
        /** The y-coordinate of the vector average (incoming, outgoing) at this point */
        float dmy;
        /** The flag annations (corner, left-turning) of this point */
        int flags;

        Point next;
        Point prev;
    }

    /**
     * Creates an extruder with no path data.
     */
    public PathExtruder(){
        joint = Poly2.Joint.SQUARE;
        endCap = Poly2.EndCap.BUTT;
        tolerance = TOLERANCE;
        mitreLimit = MITER_LIMIT;
        calculated = false;
        closed = false;
        convex = true;
        points = null;
        verts = null;
        sides = null;
        indxs = null;
        pSize = 0;
        vSize = 0;
    }

    /**
     * Creates an extruder with the given path.
     *
     * The path data is copied. The extruder does not retain any references
     * to the original data.
     *
     * @param points    The path to extrude
     * @param closed    Whether the path is closed
     */
    public PathExtruder(float[] points, boolean closed) {
        joint = Poly2.Joint.SQUARE;
        endCap = Poly2.EndCap.BUTT;
        tolerance = TOLERANCE;
        mitreLimit = MITER_LIMIT;
        calculated = false;
        convex = true;
        this.points = null;
        verts = null;
        sides = null;
        indxs = null;
        pSize = 0;
        vSize = 0;
        set(points, closed);
    }

    /**
     * Creates an extruder with the given path.
     *
     * The path data is copied. The extruder does not retain any references
     * to the original data.
     *
     * @param path        The path to extrude
     */
    public PathExtruder(Path2 path) {
        joint = Poly2.Joint.SQUARE;
        endCap = Poly2.EndCap.BUTT;
        tolerance = TOLERANCE;
        mitreLimit = MITER_LIMIT;
        calculated = false;
        closed = false;
        convex = true;
        points = null;
        verts = null;
        sides = null;
        indxs = null;
        pSize = 0;
        vSize = 0;
        set(path);
    }

    /**
     * Sets the path for this extruder.
     *
     * The path data is copied. The extruder does not retain any references
     * to the original data.  All points will be consider to be corner
     * points.
     *
     * This method resets all interal data. You will need to reperform the
     * calculation before accessing data.
     *
     * @param points    The path to extruder
     * @param closed    Whether the path is closed
     *
     */
    public void set(float[] points, boolean closed) {
        clear();
        this.closed = closed;
        pSize = points.length/2;

        Point prevHead = new Point();
        this.points = prevHead;
        for (int i=0; i<points.length-2; i+=2){
            Point p = new Point();
            p.x = points[i];
            p.y = points[i+1];
            p.flags = FLAG_CORNER;
            p.dx = points[i+2] - p.x;
            p.dy = points[i+3] - p.y;
            p.len = (float) Math.sqrt(p.dx*p.dx + p.dy*p.dy);
            if (p.len > 1e-6){
                p.dx /= p.len;
                p.dy /= p.len;
            }
            p.prev = this.points;
            this.points.next = p;
            this.points = this.points.next;
        }
        Point p = new Point();
        p.x = points[points.length-2];
        p.y = points[points.length-1];
        p.flags = FLAG_CORNER;
        p.dx = points[0] - p.x;
        p.dy = points[1] - p.y;
        p.len = (float) Math.sqrt(p.dx*p.dx + p.dy*p.dy);
        if (p.len > 1e-6){
            p.dx /= p.len;
            p.dy /= p.len;
        }

        p.prev = this.points;
        p.next = prevHead.next;
        this.points.next = p;
        this.points = prevHead.next;
        this.points.prev = p;
    }

    /**
     * Sets the path for this extruder.
     *
     * The path data is copied. The extruder does not retain any references
     * to the original data.
     *
     * This method resets all interal data. You will need to reperform the
     * calculation before accessing data.
     *
     * @param path        The path to extrude
     */
    public void set(Path2 path) {
        clear();
        this.closed = path.closed;
        pSize = path.vertices.length/2;

        Point prevHead = new Point();
        this.points = prevHead;
        for (int i=0; i<path.vertices.length-2; i+=2){
            Point p = new Point();
            p.x = path.vertices[i];
            p.y = path.vertices[i+1];
            p.flags = path.isCorner(i) ? FLAG_CORNER : 0;
            p.dx = path.vertices[i+2] - p.x;
            p.dy = path.vertices[i+3] - p.y;
            p.len = (float) Math.sqrt(p.dx*p.dx + p.dy*p.dy);
            if (p.len > 1e-6){
                p.dx /= p.len;
                p.dy /= p.len;
            }
            p.prev = this.points;
            this.points.next = p;
            this.points = this.points.next;
        }
        Point p = new Point();
        p.x = path.vertices[path.vertices.length-2];
        p.y = path.vertices[path.vertices.length-1];
        p.flags = path.isCorner(pSize-1) ? FLAG_CORNER : 0;
        p.dx = path.vertices[0] - p.x;
        p.dy = path.vertices[1] - p.y;
        p.len = (float) Math.sqrt(p.dx*p.dx + p.dy*p.dy);
        if (p.len > 1e-6){
            p.dx /= p.len;
            p.dy /= p.len;
        }

        p.prev = this.points;
        p.next = prevHead.next;
        this.points.next = p;
        this.points = prevHead.next;
        this.points.prev = p;
    }

    /**
     * Clears all computed data, but still maintains the settings.
     *
     * This method preserves all initial vertex data, as well as the joint,
     * cap, and precision settings.
     */
    public void reset(){
        vSize = 0;
        iback1 = 0;
        iback2 = 0;
        calculated = false;
    }

    /**
     * Clears all internal data, including initial vertex data.
     *
     * When this method is called, you will need to set a new vertices before
     * calling {@link #calculate}.  However, the joint, cap, and precision
     * settings are preserved.
     */
    public void clear() {
        reset();
        pSize = 0;
        closed = false;
        convex = true;
    }

    /**
     * Performs an asymmetric extrusion of the current path data.
     *
     * An extrusion of a path is a polygon that follows the path but gives it
     * width. Hence it takes a path and turns it into a solid shape. This is
     * more complicated than simply triangulating the original path. The new
     * polygon has more vertices, depending on the choice of joint (shape at
     * the corners) and cap (shape at the end).
     *
     * This version of the method allows you to specify the left and right side
     * widths independently. In particular, this allows us to define an "half
     * extrusion" that starts from the center line.
     *
     * @param lWidth    The width of the left side of the extrusion
     * @param rWidth    The width of the right side of the extrusion
     */
    public void calculate(float lWidth, float rWidth) {
        if (calculated) {
            return;
        }

        int ind;
        float leftmark = lWidth > 0 ? LEFT_MK : 0;
        float rghtmark = rWidth > 0 ? RGHT_MK : 0;

        float width = lWidth+rWidth;
        int nCap = curveSegs(width, (float) Math.PI,tolerance);
        int nBevel = analyze(width);
        int cverts = 0;
        if (joint == Poly2.Joint.ROUND) {
            cverts += (pSize + nBevel*(nCap+2) + 1) * 2;  // plus one for loop
        } else {
            cverts += (pSize + nBevel*5 + 1) * 2;         // plus one for loop
        }

        if (!closed){
            // space for caps
            if (endCap == Poly2.EndCap.ROUND) {
                cverts += (nCap*2 + 2)*2;
            } else {
                cverts += (3+3)*2;
            }
        }

        if(cverts <= 0 || pSize <=0) return;
        preAlloc(cverts);

        Point p0,p1;
        int s,e;
        if (closed){
            p0 = points.prev;
            p1 = points;
            s = 0;
            e = pSize;
        } else{
            p0 = points;
            p1 = points.next;
            s = 1;
            e = pSize-1;
            float dx = p1.x - p0.x;
            float dy = p1.y - p0.y;
            float mag = (float) Math.sqrt(dx*dx + dy*dy);
            if (mag > EPSILON) {
                dx /= mag; dy /= mag;
            }
            switch (endCap){
                case BUTT:
                    startButt(p0,dx,dy,lWidth,rWidth);
                    break;
                case SQUARE:
                    startSquare(p0,dx,dy,lWidth,rWidth,width);
                    break;
                case ROUND:
                    startRound(p0,dx,dy,lWidth,rWidth,nCap);
                    break;
            }
        }

        for (int i=s;i<e;i++){
            if((p1.flags & (FLAG_BEVEL | FLAG_INNER)) != 0) {
                if(joint == Poly2.Joint.ROUND){
                    joinRound(p0,p1,lWidth,rWidth,nCap,closed&&i==s);
                } else {
                    joinBevel(p0,p1,lWidth,rWidth,closed&&i==s);
                }
            } else if (closed && i == s){
                iback2 = addPoint(p1.x-(p1.dmx*lWidth),p1.y-(p1.dmy*lWidth),leftmark,0);
                iback1 = addPoint(p1.x+(p1.dmx*rWidth),p1.y+(p1.dmy*rWidth),rghtmark,0);
                addLeft(iback2);
                addRight(iback1);
            } else {
                ind = addPoint(p1.x - (p1.dmx * lWidth), p1.y - (p1.dmy * lWidth), leftmark, 0);
                addLeft(ind);
                triLeft(ind);
                ind = addPoint(p1.x + (p1.dmx * rWidth), p1.y + (p1.dmy * rWidth), rghtmark, 0);
                addRight(ind);
                triRight(ind);
            }
            p0 = p0.next;
            p1 = p1.next;
        }

        if(closed) {
            addLeft(0);
            triLeft(0);
            addRight(1);
            triRight(1);
        } else {
            // Add cap
            p1 = points;
            for (int i=0; i<e; i++){
                p1 = p1.next;
            }
            float dx = p1.x - p0.x;
            float dy = p1.y - p0.y;
            float mag = (float) Math.sqrt(dx*dx + dy*dy);
            if (mag > EPSILON) {
                dx /= mag; dy /= mag;
            }
            switch (endCap){
                case BUTT:
                    endButt(p1,dx,dy,lWidth,rWidth);
                    break;
                case SQUARE:
                    endSquare(p1,dx,dy,lWidth,rWidth,width);
                    break;
                case ROUND:
                    endRound(p1,dx,dy,lWidth,rWidth,nCap);
                    break;
            }
        }
        calculated = true;
    }

    /**
     * Performs a extrusion of the current path data.
     *
     * An extrusion of a path is a polygon that follows the path but gives it
     * width. Hence it takes a path and turns it into a solid shape. This is
     * more complicated than simply triangulating the original path. The new
     * polygon has more vertices, depending on the choice of joint (shape at
     * the corners) and cap (shape at the end).
     *
     * The stroke width is measured from the left side of the extrusion to the
     * right side of the extrusion. So a stroke of width 20 is actually 10 pixels
     * from the center on each side.
     *
     * @param width        The stroke width of the extrusion
     */
    public void calculate(float width) {
        calculate(width/2.0f,width/2.0f);
    }

    /**
     * Sets the joint value for the extrusion.
     *
     * The joint type determines how the extrusion joins the extruded
     * line segments together.  See {@link Poly2.Joint} for the
     * description of the types.
     *
     * @param joint     The extrusion joint type
     */
    public void setJoint(Poly2.Joint joint) {
        reset();
        this.joint = joint;
    }

    /**
     * Returns the joint value for the extrusion.
     *
     * The joint type determines how the extrusion joins the extruded
     * line segments together.  See {@link Poly2.Joint} for the
     * description of the types.
     *
     * @return the joint value for the extrusion.
     */
    public Poly2.Joint getJoint(){
        return  joint;
    }

    /**
     * Sets the end cap value for the extrusion.
     *
     * The end cap type determines how the extrusion draws the ends of
     * the line segments at the start and end of the path. See
     * {@link Poly2.EndCap} for the description of the types.
     *
     * @param endCap    The extrusion end cap type
     */
    public void setEndCap(Poly2.EndCap endCap) {
        this.endCap = endCap;
    }

    /**
     * Returns the end cap value for the extrusion.
     *
     * The end cap type determines how the extrusion draws the ends of
     * the line segments at the start and end of the path. See
     * {@link Poly2.EndCap} for the description of the types.
     *
     * @return the end cap value for the extrusion.
     */
    public Poly2.EndCap getEndCap(){
        return endCap;
    }

    /**
     * Sets the error tolerance of the extrusion.
     *
     * This value is mostly used to determine the number of segments needed
     * for a rounded joint or endCap. The default value is 0.25f.
     *
     * @param tolerance    The error tolerance of the extrusion.
     */
    public void setTolerance(float tolerance) {
        this.tolerance = tolerance;
    }

    /**
     * Returns the error tolerance of the extrusion.
     *
     * This value is mostly used to determine the number of segments needed
     * for a rounded joint or endcap. The default value is 0.25f.
     *
     * @return the error tolerance of the extrusion.
     */
    public float getTolerance() {
        return tolerance;
    }

    /**
     * Sets the mitre limit of the extrusion.
     *
     * The mitre limit sets how "pointy" a mitre joint is allowed to be before
     * the algorithm switches it back to a bevel/square joint. Small angles can
     * have very large mitre offsets that go way off-screen.
     *
     * To determine whether to switch a miter to a bevel, the algorithm will take
     * the two vectors at this joint, normalize them, and then average them. It
     * will multiple the magnitude of that vector by the mitre limit. If that
     * value is less than 1.0, it will switch to a bevel.  By default this value
     * is 10.0.
     *
     * @param limit    The mitre limit for joint calculations
     */
    public void setMitreLimit(float limit) {
        mitreLimit = limit;
    }

    /**
     * Returns the mitre limit of the extrusion.
     *
     * The mitre limit sets how "pointy" a mitre joint is allowed to be before
     * the algorithm switches it back to a bevel/square joint. Small angles can
     * have very large mitre offsets that go way off-screen.
     *
     * To determine whether to switch a miter to a bevel, the algorithm will take
     * the two vectors at this joint, normalize them, and then average them. It
     * will multiple the magnitude of that vector by the mitre limit. If that
     * value is less than 1.0, it will switch to a bevel.  By default this value
     * is 10.0.
     *
     * @return    the mitre limit for joint calculations
     */
    public float getMitreLimit() {
        return mitreLimit;
    }

    /**
     * Returns the estimated number of vertices in the extrusion
     *
     * This method is important for preallocating the number of vertices and
     * indices for the extrusion. In addition, this method will annotate the
     * path data to ensure that the proper joints are used as each turn.
     *
     * @param width     The stroke width of the extrusion
     *
     * @return the estimated number of vertices in the extrusion
     */
    private int analyze(float width) {
        float iWidth = width > 0.0f ? 1.0f/width : 0.0f;
        int nLeft = 0;
        int nBevel = 0;

        Point v0 = points.prev;
        Point v1 = points;

        for(int i =0; i<pSize; i++){
            float dlx0 = v0.dy;
            float dly0 = -v0.dx;
            float dlx1 = v1.dy;
            float dly1 = -v1.dx;

            // Calculate extrusions
            v1.dmx = (dlx0 + dlx1) * 0.5f;
            v1.dmy = (dly0 + dly1) * 0.5f;

            float dmr2 = v1.dmx*v1.dmx + v1.dmy*v1.dmy;
            if (dmr2 > EPSILON) {
                float scale = 1.0f / dmr2;
                if (scale > SCALE_LIMIT) {
                    scale = SCALE_LIMIT;
                }
                v1.dmx *= scale;
                v1.dmy *= scale;
            }

            // Clear flags, but keep the corner.
            v1.flags = (v1.flags & FLAG_CORNER)==1 ? FLAG_CORNER : 0;

            // Keep track of left turns.
            float cross = v1.dx * v0.dy - v0.dx * v1.dy;
            if (cross < 0.0) {
                nLeft += 1;
                v1.flags |= FLAG_LEFT;
            }

            // Calculate if we should use bevel or miter for inner join.
            float limit = Math.max(1.01f, Math.min(v0.len, v1.len) * iWidth);

            if ((dmr2 * limit*limit) < 1.0f) {
                v1.flags |= FLAG_INNER;
            }

            // Check to see if the corner needs to be beveled.
            if ((v1.flags & FLAG_CORNER) !=0) {
                if ((dmr2 * mitreLimit*mitreLimit) < 1.0 ||
                        joint == Poly2.Joint.SQUARE ||
                        joint == Poly2.Joint.ROUND) {
                    v1.flags |= FLAG_BEVEL;
                }
            }

            if ((v1.flags & (FLAG_BEVEL | FLAG_INNER)) != 0) {
                nBevel += 1;
            }

            v0 = v0.next;
            v1 = v1.next;
        }

        convex = (nLeft == pSize);
        return nBevel;
    }

    /**
     * Allocates space for the extrusion vertices and indices
     *
     * This method guarantees that the output buffers will have enough capacity
     * for the algorithm.
     *
     * @param size      The estimated number of vertices in the extrusion
     */
    private void preAlloc(int size) {
        verts = new ArrayList<>();
        lefts = new ArrayList<>();
        rghts = new ArrayList<>();
        sides = new ArrayList<>();
        indxs = new ArrayList<>();
    }

    /**
     * Computes the bevel vertices at the given joint
     *
     * The pair of vertices is assigned to (x0,y0) and (x1,y1).
     *
     * @param inner     Whether to use an inner bevel
     * @param p0        The point leading to the joint
     * @param p1        The point at the joint
     * @param w         The stroke width of the extrusion
     */
    private float[] chooseBevel(boolean inner, Point p0, Point p1, float w) {
        float[] res = new float[4];
        if (inner){
            res[0] = p1.x + p0.dy * w;
            res[1] = p1.y - p0.dx * w;
            res[2] = p1.x + p1.dy * w;
            res[3] = p1.y - p1.dx * w;
        } else {
            res[0] = p1.x + p1.dmx * w;
            res[1] = p1.y + p1.dmy * w;
            res[2] = p1.x + p1.dmx * w;
            res[3] = p1.y + p1.dmy * w;
        }
        return res;
    }

    /**
     * Produces a round joint at the point p1
     *
     * @param p0        The point leading to the joint
     * @param p1        The point at the joint
     * @param lw        The width of the left side of the extrusion
     * @param rw        The width of the right side of the extrusion
     * @param nCap      The number of segments in the rounded joint
     * @param start     Whether this is the first joint in an the extrusion
     */
    private void joinRound (Point p0, Point p1, float lw, float rw, int nCap, boolean start) {
        float dlx0 = p0.dy;
        float dly0 = -p0.dx;
        float dlx1 = p1.dy;
        float dly1 = -p1.dx;
        int ind = 0;

        float leftmark = lw > 0 ? LEFT_MK : 0;
        float rghtmark = rw > 0 ? RGHT_MK : 0;

        if((p1.flags & FLAG_LEFT) != 0){
            float[] res = chooseBevel((p1.flags & FLAG_INNER) != 0, p0, p1, -lw);
            float lx0 = res[0];
            float ly0 = res[1];
            float lx1 = res[2];
            float ly1 = res[3];
            float a0 = (float) Math.atan2(dly0,dlx0);
            float a1 = (float) Math.atan2(dly1,dlx1);
            if (a1 < a0) {
                a1 += Math.PI*2;
            }

            if (start) {
                iback2 = addPoint(lx0, ly0, leftmark, 0);
                addLeft(iback2);
                iback1 = addPoint(p1.x + dlx0*rw, p1.y + dly0*rw, rghtmark, 0);
                addRight(iback1);
            } else {
                ind = addPoint(lx0, ly0, leftmark, 0);
                addLeft(ind);
                triLeft(ind);
                ind = addPoint(p1.x + dlx0*rw, p1.y + dly0*rw, rghtmark, 0);
                addRight(ind);
                triRight(ind);
            }

            int n = clampi((int)Math.ceil(((a1 - a0) / Math.PI) * nCap), 2, nCap);
            int center = addPoint(p1.x,p1.y,0,0);
            triLeft(center);
            for(int i=0; i<n; i++){
                float u = i/((float)(n-1));
                float a = a0 + u*(a1-a0);
                float rx = (float) (p1.x + Math.cos(a) * rw);
                float ry = (float) (p1.y + Math.sin(a) * rw);

                ind = addPoint(rx, ry, rghtmark, 0);
                addRight(ind);
                triRight(ind);
                iback2 = ind;
                iback1 = center;
            }

            iback1 = iback2;
            iback2 = center;
            ind = addPoint(lx1, ly1, leftmark, 0);
            addLeft(ind);
            triLeft(ind);
            ind = addPoint(p1.x + dlx1*rw, p1.y + dly1*rw, rghtmark, 0);
            addRight(ind);
            triRight(ind);
        } else {
            float[] res = chooseBevel((p1.flags & FLAG_INNER) != 0, p0, p1, rw);
            float rx0 = res[0];
            float ry0 = res[1];
            float rx1 = res[2];
            float ry1 = res[3];
            float a0 = (float) Math.atan2(-dly0,-dlx0);
            float a1 = (float) Math.atan2(-dly1,-dlx1);
            if (a1 > a0) {
                a1 -= Math.PI*2;
            }

            if (start) {
                iback1 = addPoint(p1.x - dlx0*lw, p1.y - dly0*lw, leftmark, 0);
                iback2 = addPoint(rx0, ry0, rghtmark, 0);
            } else {
                ind = addPoint(p1.x - dlx0*lw, p1.y - dly0*lw, leftmark, 0);
                addLeft(ind);
                triLeft(ind);
                ind = addPoint(rx0, ry0, rghtmark, 0);
                addRight(ind);
                triRight(ind);

                float lx = (float) (p1.x + Math.cos(a0) * lw);
                float ly = (float) (p1.y + Math.sin(a0) * lw);
                ind = addPoint(lx, ly, leftmark, 0);
                addLeft(ind);
                triLeft(ind);
            }

            int n = clampi((int)Math.ceil(((a0 - a1) / Math.PI) * nCap), 2, nCap);
            int center = addPoint(p1.x,p1.y,0,0);
            triRight(center);

            for (int i=0; i<n; i++){
                float u = i/((float)(n-1));
                float a = a0 + u*(a1-a0);
                float lx = (float) (p1.x + Math.cos(a) * lw);
                float ly = (float) (p1.y + Math.sin(a) * lw);

                ind = addPoint(lx, ly, leftmark, 0);
                iback1 = center;
                addLeft(ind);
                triLeft(ind);
                iback2 = ind;
            }

            iback1 = center;
            ind = addPoint(p1.x - dlx1*lw, p1.y - dly1*lw, leftmark, 0);
            addLeft(ind);
            triLeft(ind);
            ind = addPoint(rx1, ry1, rghtmark, 0);
            addRight(ind);
            triRight(ind);
        }
    }

    /**
     * Produces a bevel/square joint at the point p1
     *
     * @param p0        The point leading to the joint
     * @param p1        The point at the joint
     * @param lw        The width of the left side of the extrusion
     * @param rw        The width of the right side of the extrusion
     * @param start     Whether this is the first joint in an the extrusion
     */
    private void joinBevel(Point p0, Point p1, float lw, float rw, boolean start){
        float dlx0 = p0.dy;
        float dly0 = -p0.dx;
        float dlx1 = p1.dy;
        float dly1 = -p1.dx;

        float leftmark = lw > 0 ? LEFT_MK : 0;
        float rghtmark = rw > 0 ? RGHT_MK : 0;

        int ind;
        if ((p1.flags & FLAG_LEFT) != 0) {
            float[] res =  chooseBevel((p1.flags & FLAG_INNER)!=0, p0, p1, -lw);
            float lx0 = res[0];
            float ly0 = res[1];
            float lx1 = res[2];
            float ly1 = res[3];

            if(start){
                iback2 = addPoint(lx0, ly0, leftmark, 0);
                iback1 = addPoint(p1.x + dlx0*rw, p1.y + dly0*rw, rghtmark, 0);
            } else {
                ind = addPoint(lx0, ly0, leftmark, 0);
                addLeft(ind);
                triLeft(ind);
                ind = addPoint(p1.x + dlx0*rw, p1.y + dly0*rw, rghtmark, 0);
                addRight(ind);
                triRight(ind);
            }

            if ((p1.flags & FLAG_BEVEL) != 0) {
                ind = addPoint(lx1, ly1, leftmark, 0);
                triLeft(ind);
                ind = addPoint(p1. x + dlx1 * rw, p1.y + dly1 * rw, rghtmark, 0);
                triRight(ind);
            } else {
                float rx0 = p1.x + p1.dmx * rw;
                float ry0 = p1.y + p1.dmy * rw;

                ind = addPoint(p1.x, p1.y, 0,0);
                triLeft(ind);
                ind = addPoint(p1.x + dlx0*rw, p1.y + dly0*rw, rghtmark, 0);
                addRight(ind);
                triRight(ind);

                ind = addPoint(rx0, ry0, rghtmark, 0);
                addRight(ind);
                triLeft(ind);

                iback2 = ind;
                iback1 = addPoint(p1.x, p1.y, 0, 0);
                ind = addPoint(p1.x + dlx1*rw, p1.y + dly1*rw, rghtmark, 0);
                addRight(ind);
                triRight(ind);
            }

            ind = addPoint(lx1, ly1, leftmark, 0);
            addLeft(ind);
            triLeft(ind);
            ind = addPoint(p1.x + dlx1*rw, p1.y + dly1*rw, rghtmark, 0);
            addRight(ind);
            triRight(ind);
        } else {
            float[] res = chooseBevel((p1.flags & FLAG_INNER) != 0, p0, p1, rw);
            float rx0 = res[0];
            float ry0 = res[1];
            float rx1 = res[2];
            float ry1 = res[3];

            if (start) {
                iback2 = addPoint(p1.x - dlx0*lw, p1.y - dly0*lw, leftmark, 0);
                iback1 = addPoint(rx0, ry0, rghtmark, 0);
            } else {
                ind = addPoint(p1.x - dlx0*lw, p1.y - dly0*lw, leftmark, 0);
                addLeft(ind);
                triLeft(ind);
                ind = addPoint(rx0, ry0, rghtmark, 0);
                addRight(ind);
                triRight(ind);
            }

            if ((p1.flags & FLAG_BEVEL) != 0) {
                ind = addPoint(p1.x - dlx1*lw, p1.y - dly1*lw, leftmark, 0);
                addLeft(ind);
                triLeft(ind);
                ind = addPoint(rx1, ry1, rghtmark, 0);
                addRight(ind);
                triRight(ind);
            } else {
                float lx0 = p1.x - p1.dmx * lw;
                float ly0 = p1.y - p1.dmy * lw;

                ind = addPoint(p1.x - dlx0*lw, p1.y - dly0*lw, leftmark, 0);
                addLeft(ind);
                triLeft(ind);
                ind = addPoint(p1.x, p1.y, 0, 0);
                triRight(ind);

                ind = addPoint(lx0, ly0, leftmark,0);
                addLeft(ind);
                triLeft(ind);

                iback2 = ind;
                iback1 = addPoint(p1.x - dlx1*lw, p1.y - dly1*lw, leftmark, 0);
                ind = addPoint(p1.x, p1.y, 0, 0);
                addLeft(iback1);
                triRight(ind);
            }

            ind = addPoint(p1.x - dlx1*lw, p1.y - dly1*lw, leftmark, 0);
            addLeft(ind);
            triLeft(ind);
            ind = addPoint(rx1, ry1, rghtmark, 0);
            addRight(ind);
            triRight(ind);
        }
    }

    /**
     * Produces a butt (degenerate) cap at the head of the extrusion.
     *
     * @param p     The head of the path
     * @param dx    The x-direction from the head to the next path point
     * @param dy    The y-direction from the head to the next path point
     * @param lw    The width of the left side of the extrusion
     * @param rw    The width of the right side of the extrusion
     */
    private void startButt(Point p, float dx, float dy, float lw, float rw){
        float dlx = dy;
        float dly = -dx;
        iback2 = addPoint(p.x - dlx*lw, p.y - dly*lw, lw > 0 ? LEFT_MK : 0, 0);
        addLeft(iback2);
        iback1 = addPoint(p.x + dlx*rw, p.y + dly*rw, rw > 0 ? RGHT_MK : 0, 0);
        addRight(iback1);
    }

    /**
     * Produces a butt (degenerate) cap at the tail of the extrusion.
     *
     * @param p     The tail of the path
     * @param dx    The x-direction from the penultimate path point to the tail
     * @param dy    The y-direction from the penultimate path point to the tail
     * @param lw    The width of the left side of the extrusion
     * @param rw    The width of the right side of the extrusion
     */
    private void endButt(Point p, float dx, float dy, float lw, float rw){
        float dlx = dy;
        float dly = -dx;
        int ind;
        ind = addPoint(p.x - dlx*lw, p.y - dly*lw, lw > 0 ? LEFT_MK : 0, 0);
        addLeft(ind);
        triLeft(ind);
        ind = addPoint(p.x + dlx*rw, p.y + dly*rw, rw > 0 ? RGHT_MK : 0, 0);
        addRight(ind);
        triRight(ind);
    }

    /**
     * Produces a square cap at the head of the extrusion.
     *
     * @param p     The head of the path
     * @param dx    The x-direction from the head to the next path point
     * @param dy    The y-direction from the head to the next path point
     * @param lw    The width of the left side of the extrusion
     * @param rw    The width of the right side of the extrusion
     * @param d     The length of the cap
     */
    private void startSquare(Point p, float dx, float dy, float lw, float rw, float d){
        float px = p.x - dx*d;
        float py = p.y - dy*d;
        float dlx = dy;
        float dly = -dx;

        float leftmark = lw > 0 ? LEFT_MK : 0;
        float rghtmark = rw > 0 ? RGHT_MK : 0;


        int ind;
        iback2 = addPoint(px - dlx*lw, py - dly*lw, leftmark, HEAD_MK);
        addLeft(iback2);
        iback1 = addPoint(px + dlx*rw, py + dly*rw, rghtmark, HEAD_MK);
        addRight(iback1);

        px = p.x;
        py = p.y;
        ind = addPoint(px - dlx*lw, py - dly*lw, leftmark, 0);
        addLeft(ind);
        triLeft(ind);
        ind = addPoint(px + dlx*rw, py + dly*rw, rghtmark, 0);
        addRight(ind);
        triRight(ind);
    }

    /**
     * Produces a square cap at the tail of the extrusion.
     *
     * @param p     The tail of the path
     * @param dx    The x-direction from the penultimate path point to the tail
     * @param dy    The y-direction from the penultimate path point to the tail
     * @param lw    The width of the left side of the extrusion
     * @param rw    The width of the right side of the extrusion
     * @param d     The length of the cap
     */
    private void endSquare(Point p, float dx, float dy, float lw, float rw, float d){
        float px = p.x;
        float py = p.y;
        float dlx = dy;
        float dly = -dx;

        float leftmark = lw > 0 ? LEFT_MK : 0;
        float rghtmark = rw > 0 ? RGHT_MK : 0;

        int ind;
        ind = addPoint(px - dlx*lw, py - dly*lw, leftmark, 0);
        triLeft(ind);
        ind = addPoint(px + dlx*rw, py + dly*rw, rghtmark, 0);
        triRight(ind);

        px = p.x + dx*d;
        py = p.y + dy*d;
        ind = addPoint(px - dlx*lw, py - dly*lw, leftmark, TAIL_MK);
        addLeft(ind);
        triLeft(ind);
        ind = addPoint(px + dlx*rw, py + dly*rw, rghtmark, TAIL_MK);
        addRight(ind);
        triRight(ind);
    }

    /**
     * Produces a rounded cap at the head of the extrusion.
     *
     * @param p        The head of the path
     * @param dx    The x-direction from the head to the next path point
     * @param dy    The y-direction from the head to the next path point
     * @param lw    The width of the left side of the extrusion
     * @param rw    The width of the right side of the extrusion
     * @param nCap    The number of segments in the rounded cap
     */
    private void startRound(Point p, float dx, float dy, float lw, float rw, int nCap){
        float dlx = dy;
        float dly = -dx;
        float w = (lw+rw)/2.0f;

        float px = p.x + (dlx*rw - dlx*lw)/2.0f;
        float py = p.y + (dly*rw - dly*lw)/2.0f;

        float leftmark = lw > 0 ? LEFT_MK : 0;
        float rghtmark = rw > 0 ? RGHT_MK : 0;

        int center = addPoint(px, py, 0, 0);
        int first  = addPoint(px - dlx*w, py - dly*w, leftmark, 0);
        iback1 = center;
        iback2 = first;
        addLeft(first);

        int ind = first;
        for(int i=0; i<nCap; i++) {
            float a = (float) ((i*Math.PI)/(nCap-1));
            float cx = (float) Math.cos(a);
            float ax = cx*w;
            float ay = (float) (Math.sin(a)*w);

            ind = addPoint(px - dlx*ax - dx*ay, py - dly*ax - dy*ay,
                    leftmark*(1+cx)/2+rghtmark*(1-cx)/2,HEAD_MK*ay/w);
            addRight(ind);
            triRight(ind);
            iback2 = iback1;
            iback1 = center;
        }

        iback1 = ind;
        iback2 = first;
    }

    /**
     * Produces a rounded cap at the tail of the extrusion.
     *
     * @param p        The tail of the path
     * @param dx    The x-direction from the penultimate path point to the tail
     * @param dy    The y-direction from the penultimate path point to the tail
     * @param lw    The width of the left side of the extrusion
     * @param rw    The width of the right side of the extrusion
     * @param nCap    The number of segments in the rounded cap
     */
    private void endRound(Point p, float dx, float dy, float lw, float rw, int nCap){
        float dlx = dy;
        float dly = -dx;
        float w = (lw+rw)/2.0f;

        float px = p.x + (dlx*rw - dlx*lw)/2.0f;
        float py = p.y + (dly*rw - dly*lw)/2.0f;

        float leftmark = lw > 0 ? LEFT_MK : 0;
        float rghtmark = rw > 0 ? RGHT_MK : 0;

        int first = addPoint(px - dlx*w, py - dly*w, leftmark, 0);
        int last = addPoint(px + dlx*w, py + dly*w, rghtmark, 0);
        addLeft(first);
        triLeft(first);
        addRight(last);
        triRight(last);

        int center = addPoint(px, py, 0, 0);
        iback1 = center;

        int ind;
        for(int i = 1; i < nCap-1; i++) {
            float a = (float) ((i*Math.PI)/(nCap-1));
            float cx = (float) Math.cos(a);
            float ax = cx*w;
            float ay = (float) (Math.sin(a) * w);

            ind = addPoint(px - dlx*ax + dx*ay, py - dly*ax + dy*ay,
                    leftmark*(1+cx)/2+rghtmark*(1-cx)/2,TAIL_MK*ay/w);
            addLeft(ind);
            triLeft(ind);
            iback2 = iback1;
            iback1 = center;
        }

        triLeft(last);
        iback1 = center;
    }

    /**
     * Returns a polygon representing the path extrusion.
     *
     * The polygon contains the original vertices together with the new
     * indices defining the wireframe path.  The extruder does not maintain
     * references to this polygon and it is safe to modify it.
     *
     * If the calculation is not yet performed, this method will return the
     * empty polygon.
     *
     * @return a polygon representing the path extrusion.
     */
    public Poly2 getPolygon() {
        if (calculated) {
            float[] resV = new float[verts.size()];
            for (int i=0; i<verts.size(); i++){
                resV[i] = verts.get(i);
            }
            short[] resI = new short[indxs.size()];
            for (int i=0; i<indxs.size(); i++){
                resI[i] = indxs.get(i);
            }
            return new Poly2(resV, resI);
        }
        return new Poly2(new float[0]);
    }

    /**
     * Stores the path extrusion in the given buffer.
     *
     * This method will add both the original vertices, and the corresponding
     * indices to the new buffer.  If the buffer is not empty, the indices
     * will be adjusted accordingly. You should clear the buffer first if
     * you do not want to preserve the original data.
     *
     * If the calculation is not yet performed, this method will do nothing.
     *
     * @param buffer    The buffer to store the extruded polygon
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 getPolygon(Poly2 buffer){
        if (calculated) {
            float[] vertices = new float[verts.size()+buffer.vertices.length];
            System.arraycopy(buffer.vertices, 0, vertices, 0, buffer.vertices.length);
            for (int i=0; i<verts.size(); i++){
                vertices[buffer.vertices.length+i] = verts.get(i);
            }

            short[] indices = new short[indxs.size()+buffer.indices.length];
            System.arraycopy(buffer.indices, 0, indices, 0, buffer.indices.length);
            for (int i=0; i<indxs.size(); i++){
                indices[buffer.indices.length+i] = indxs.get(i);
            }

            return new Poly2(vertices,indices);
        }
        return buffer;
    }

    /**
     * Returns a (closed) path representing the extrusion border(s)
     *
     * So long as the calculation is complete, the vector is guaranteed to
     * contain at least one path. Counter-clockwise paths correspond to
     * the exterior boundary of the stroke.  Clockwise paths are potential
     * holes in the extrusion. There is no guarantee on the order of the
     * returned paths.
     *
     * If the calculation is not yet performed, this method will return the
     * empty path.
     *
     * @return a (closed) path representing the extrusion border
     */
    public Path2[] getBorder() {
        return getBorder(new Path2[0]);
    }

    /**
     * Stores a (closed) path representing the extrusion border in the buffer
     *
     * So long as the calculation is complete, the vector is guaranteed to
     * contain at least one path. Counter-clockwise paths correspond to
     * the exterior boundary of the stroke. Clockwise paths are potential
     * holes in the extrusion. There is no guarantee on the order of the
     * returned paths.
     *
     * This method will append append its results to the provided buffer. It
     * will not erase any existing data. You should clear the buffer first if
     * you do not want to preserve the original data.
     *
     * If the calculation is not yet performed, this method will do nothing.
     *
     * @param buffer    The buffer to store the path around the extrusion
     *
     * @return buffer with a (closed) path representing the extrusion border being added
     */
    public Path2[] getBorder(Path2[] buffer){
        if (calculated){
            int size = closed ? 2 : 1;
            Path2[] res = new Path2[buffer.length+size];
            System.arraycopy(buffer, 0, res, 0, buffer.length);
            Path2 path = new Path2();
            path.closed = true;
            if (closed){
                float[] rg = new float[rghts.size()];
                for (int i=0; i< rghts.size(); i++){
                    rg[i] = rghts.get(i);
                }
                path.vertices = rg;
                res[res.length-2] = new Path2(path);
                float[] le = new float[lefts.size()];
                for (int i=0; i< lefts.size(); i++){
                    le[i] = lefts.get(i);
                }
                path.vertices = le;
            } else {
                float[] vts = new float[rghts.size()+lefts.size()];
                for (int i=0; i<rghts.size(); i++){
                    vts[i] = rghts.get(i);
                }
                for (int i=0; i<lefts.size(); i++){
                    vts[rghts.size()+i] = lefts.get(i);
                }
                path.vertices = vts;
            }
            res[res.length-1] = new Path2(path);
            return res;
        }
        return buffer;
    }

    /**
     * Returns the side information for the vertex at the given index
     *
     * The side information is a float array.  The first element indicates
     * left vs. right side.  A value of -1 is on the left while 1 is on the right.
     * A value of 0 means an interior node sitting on the path itself.
     *
     * On the other hand the second element indicates cap positioning for an open curve.
     * A value of -1 is on the start cap.  A value of 1 is on the end cap. 0 values
     * lie along the body of the main curve.
     *
     * It is possible to have intermediate cap values for both left-right and start-end
     * in the case of rounded caps.  In this case, the intermediate value tracks the
     * traversal from one side to another.
     *
     * @param index The vertex index
     *
     * @return the side information for the vertex at the given index
     */
    public float[] getSides(int index) {
        float[] res = new float[2];
        res[0] = sides.get(2*index);
        res[1] = sides.get(2*index+1);
        return res;
    }

    /**
     * Returns the index of the annotated point after adding it to the vertex buffer
     *
     * This method assumes that _vsize < _vlimit, but it does not check it (for
     * performance reasons).
     *
     * @param x     The vertex x-coordinate
     * @param y     The vertex y-coordinate
     * @param u     The vertex left-right annotation
     * @param v     The vertex head-tail annotation
     *
     * @return the index of the annotated point
     */
    private int addPoint(float x, float y, float u, float v){
        int index = vSize;
        verts.add(x);
        verts.add(y);
        sides.add(u);
        sides.add(v);
        vSize++;
        return index;
    }

    /**
     * Adds a vertex to the left side of the extrusion
     *
     * This method is used to build the extrusion boder
     *
     * @param index     The index of the left vertex
     */
    private void addLeft(int index) {
        lefts.add(verts.get(2*index));
        lefts.add(verts.get(2*index+1));
    }

    /**
     * Adds a vertex to the right side of the extrusion
     *
     * This method is used to build the extrusion boder
     *
     * @param index The index of the right vertex
     */
    private void addRight(int index){
        rghts.add(verts.get(2*index));
        rghts.add(verts.get(2*index+1));
    }

    /**
     * Creates a triangle on the left side of the extrusion.
     *
     * At any given time, this algorithm has a two buffered indices.  Calling
     * this method creates an counter-clockwise triangle from thes indices
     * (in order) extended to the given vertex.
     *
     * @param index     The index to complete the triangle
     */
    private void triLeft(int index){
        if (validTri(verts.get(iback1*2),verts.get(iback1*2+1),verts.get(iback2*2),verts.get(iback2*2+1),verts.get(2*index),verts.get(2*index+1))){
            indxs.add((short)iback2);
            indxs.add((short)iback1);
            indxs.add((short)index);
        }
        iback2 = iback1;
        iback1 = index;
    }

    /**
     * Creates a triangle on the right side of the extrusion.
     *
     * At any given time, this algorithm has a two buffered indices.  Calling
     * this method creates an counter-clockwise triangle from these indices
     * (in reverse order) extended to the given vertex.
     *
     * @param index     The index to complete the triangle
     */
    private void triRight(int index){
        if (validTri(verts.get(iback1*2),verts.get(iback1*2+1),verts.get(iback2*2),verts.get(iback2*2+1),verts.get(2*index),verts.get(2*index+1))){
            indxs.add((short)iback1);
            indxs.add((short)iback2);
            indxs.add((short)index);
        }
        iback2 = iback1;
        iback1 = index;
    }

    /**
     * Returns true if the given vertices are a non-degenerate triangle
     *
     * This method will return false if the vertices are colinear.
     *
     * @param px    The x coordinate of first vertex in the triangle
     * @param py    The y coordinate of first vertex in the triangle
     * @param qx    The x coordinate of second vertex in the triangle
     * @param qy    The y coordinate of second vertex in the triangle
     * @param rx    The x coordinate of third vertex in the triangle
     * @param ry    The y coordinate of third vertex in the triangle
     *
     * @return true if the given vertices are a non-degenerate triangle
     */
    private static boolean validTri(float px, float py, float qx, float qy, float rx, float ry){
        return (px * (qy - ry) + qx * (ry - py) + rx * (py - qy) != 0);
    }

    /**
     * Returns the number of segments necessary for the given tolerance
     *
     * This function is used to compute the number of segments to approximate
     * a radial curve at the given level of tolerance.
     *
     * @param rad   The circle radius
     * @param arc   The arc in radians
     * @param tol   The error tolerance
     *
     * @return the number of segments necessary for the given tolerance
     */
    private int curveSegs(float rad, float arc, float tol) {
        float da = (float) (Math.acos(rad / (rad + tol)) * 2.0f);
        return Math.max(2,(int)(Math.ceil(arc/da)));
    }

    /**
     * Returns value, clamped to the range [min,max]
     *
     * This function only works on integers
     *
     * @param value    The original value
     * @param min    The range minimum
     * @param max    The range maximum
     *
     * @return value, clamped to the range [min,max]
     */
    private int clampi(int value, int min, int max) {
        return value < min ? min : value < max? value : max;
    }
}
