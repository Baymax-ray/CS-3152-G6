/*
 * PathFactory.java
 *
 * This class provides a convenient way to generate simple paths, like lines or circles.
 *
 * @author Crystal Jin
 * @date   12/5/2022
 */
package edu.cornell.gdiac.math;


/**
 * A factory class for generating common Path2 objects.
 *
 * Most of the time that we create a path, we are using it to approximate a common shape, like a circle, or a
 * rounded rectangle. Instead of embedding all of this functionality into Path2 (which already has enough to do
 * on its own), we have factored this out into a separate factory class. This factory can generate new path or
 * reset existing ones (conserving memory).
 *
 * Note that this factory only generates path, and does not create Path2 objects. It is intended for shapes that
 * will be drawn as lines or extruded later. If you want a solid (e.g. triangulated) shape, use PolyFactory instead.
 */
public class PathFactory {
    /** The curve tolerance for rounded shapes */
    private float tolerance;

    /** The default tolerance for rounded shapes */
    private final float DEFAULT_TOLERANCE = 0.5f;

    /**
     * Creates a factory for generating common paths.
     *
     * This class will use the default tolerance.
     */
    public PathFactory(){
        tolerance = DEFAULT_TOLERANCE;
    }

    /**
     * Creates a factory for generating common paths with the given tolerance.
     *
     * @param tol   The curve tolerance for rounded shapes
     */
    public PathFactory(float tol){
        tolerance = tol;
    }

    /**
     * Returns the curve tolerance for rounded shapes.
     *
     * The tolerance guarantees that curved shapes have enough segments so that
     * any points on the true shape are always within tolerance of the segmented
     * approximation.
     *
     * Rounded shapes include {@link #makeEllipse}, {@link #makeCircle},
     * {@link #makeArc}, and {@link #makeRoundedRect}.
     *
     * @return the curve tolerance for rounded shapes.
     */
    public float getTolerance(){
        return tolerance;
    }

    /**
     * Sets the curve tolerance for rounded shapes.
     *
     * The tolerance guarantees that curved shapes have enough segments so that
     * any points on the true shape are always within tolerance of the segmented
     * approximation.
     *
     * Rounded shapes include {@link #makeEllipse}, {@link #makeCircle},
     * {@link #makeArc}, and {@link #makeRoundedRect}.
     *
     * @param tol   The curve tolerance for rounded shapes.
     */
    public void setTolerance(float tol){
        tolerance = tol;
    }

    /**
     * Returns a path that represents a line segment from origin to dest.
     *
     * @param ox    The x-coordinate of the origin
     * @param oy    The y-coordinate of the origin
     * @param dx    The x-coordinate of the destination
     * @param dy    The y-coordinate of the destination
     *
     * @return a path that represents a line segment from origin to dest.
     */
    public Path2 makeLine(float ox, float oy, float dx, float dy){
        float[] vert = new float[4];
        vert[0] = ox;
        vert[1] = oy;
        vert[2] = dx;
        vert[3] = dy;
        Path2 path = new Path2(vert);
        path.corners.addAll(0,1);
        path.closed = false;
        return path;
    }

    /**
     * Returns a path that represents a simple triangle.
     *
     * @param  ax   The x-coordinate of the first vertex.
     * @param  ay   The y-coordinate of the first vertex.
     * @param  bx   The x-coordinate of the second vertex.
     * @param  by   The y-coordinate of the second vertex.
     * @param  cx   The x-coordinate of the third vertex.
     * @param  cy   The y-coordinate of the third vertex.
     *
     * @return a path that represents a simple triangle.
     */
    public Path2 makeTriangle(float ax, float ay, float bx, float by, float cx, float cy){
        float[] vert = new float[6];
        vert[0] = ax;
        vert[1] = ay;
        vert[2] = bx;
        vert[3] = by;
        vert[4] = cx;
        vert[5] = cy;
        Path2 path = new Path2(vert);
        path.corners.addAll(0,1,2);
        path.closed = true;
        return path;
    }

    /**
     * Returns a path that represents a rectangle
     *
     * @param x     The x-coordinate of the bottom left corner
     * @param y     The y-coordinate of the bottom left corner
     * @param w     The rectangle width
     * @param h     The rectangle height
     *
     * @return a path that represents a rectangle
     */
    public Path2 makeRect(float x, float y, float w, float h){
        float[] vert = new float[8];
        vert[0] = x;
        vert[1] = y;
        vert[2] = x+w;
        vert[3] = y;
        vert[4] = x+w;
        vert[5] = y+h;
        vert[6] = x;
        vert[7] = y+h;
        Path2 path = new Path2(vert);
        path.corners.addAll(0,1,2);
        path.closed = true;
        return path;
    }

    /**
     * Returns a path that represents a regular, many-sided polygon.
     *
     * The polygon will be centered at the given origin with the given radius.
     * A regular polygon is essentially a circle where the number of segments
     * is explicit (instead of implicit from the curve tolerance).
     *
     * @param cx        The x-coordinate of the center point
     * @param cy        The y-coordinate of the center point
     * @param radius    The polygon radius
     * @param sides     The number of sides
     *
     * @return a path that represents a regular, many-sided polygon.
     */
    public Path2 makeNgon(float cx, float cy, float radius, int sides){
        float coef = 2.0f * (float)Math.PI/sides;
        float[] vert = new float[sides*2];

        for(int i=0; i<sides; i++){
            float rads = i*coef;
            vert[i*2] = (float) (radius * Math.cos(rads) + cx);
            vert[i*2+1] = (float) (radius * Math.sin(rads) + cy);
        }
        Path2 path = new Path2(vert);
        path.closed = true;
        return path;
    }

    /**
     * Returns a path that represents an ellipse of the given dimensions.
     *
     * @param cx    The x-coordinate of the center point
     * @param cy    The y-coordinate of the center point
     * @param sx    The size (diameter) along the x-axis
     * @param sy    The size (diameter) along the y-axis
     *
     * @return a path that represents an ellipse of the given dimensions.
     */
    public Path2 makeEllipse(float cx, float cy, float sx, float sy){
        int segments = curveSegs(Math.max(sx/2.0f,sy/2.0f), (float) (2.0f * Math.PI), tolerance);
        float coef = (float) (2.0f*Math.PI/segments);

        float[] vert = new float[segments*2];

        for(int i=0; i<segments; i++){
            float rads = i*coef;
            vert[i*2] = (float) (0.5f * sx * Math.cos(rads) + cx);
            vert[i*2+1] = (float) (0.5f * sy * Math.sin(rads) + cy);
        }
        Path2 path = new Path2(vert);
        path.closed = true;
        return path;
    }

    /**
     * Returns a path that represents a circle of the given dimensions.
     *
     * @param cx        The x-coordinate of the center point
     * @param cy        The y-coordinate of the center point
     * @param radius    The circle radius
     *
     * @return a path that represents an circle of the given dimensions.
     */
    public Path2 makeCircle(float cx, float cy, float radius){
        return makeEllipse(cx, cy, 2*radius, 2*radius);
    }

    /**
     * Returns a path that represents an arc of the given dimensions.
     *
     * All arc measurements are in degrees, not radians.
     *
     * @param cx        The x-coordinate of the center point
     * @param cy        The y-coordinate of the center point
     * @param radius    The radius from the center point
     * @param start     The starting angle in degrees
     * @param degrees   The number of degrees to generate
     * @param closed    The arc is closed or not
     *
     * @return a path that represents an arc of the given dimensions.
     */
    public Path2 makeArc(float cx, float cy, float radius, float start, float degrees, boolean closed){
        if (degrees<=0 || degrees > 360) throw new IllegalArgumentException("degree out of range");

        int segments = curveSegs(radius, (float) (degrees*Math.PI/180.0f), tolerance);
        segments = (degrees < segments ? (int)degrees : segments);
        float srad = ((float)Math.PI/180.0f)*start;
        float arad = ((float)Math.PI/180.0f)*degrees;
        float coef = arad/segments;

        float[] vert = (degrees == 360 || !closed)? new float[segments*2+2] : new float[segments*2+4];
        for(int i=0; i<segments+1; i++){
            float rads = srad+i*coef;
            vert[i*2] = (float) (0.5f * radius * Math.cos(rads) + cx);
            vert[i*2+1] = (float) (0.5f * radius * Math.sin(rads) + cy);
        }
        if (degrees != 360 && closed){
            vert[segments*2+2] = cx;
            vert[segments*2+3] = cy;
        }
        Path2 path = new Path2(vert);
        path.closed = closed;
        return path;
    }

    /**
     * Returns a path that represents a rounded rectangle of the given dimensions.
     *
     * The radius should not exceed either half the width or half the height.
     *
     * @param x     The x-coordinate of the bottom left corner of the bounding box
     * @param y     The y-coordinate of the bottom left corner of the bounding box
     * @param w     The rectangle width
     * @param h     The rectangle height
     * @param r     The radius of each corner
     *
     * @return a path that represents a rounded rectangle of the given dimensions.
     */
    public Path2 makeRoundedRect(float x, float y, float w, float h, float r){
        if (r>(w/2.0f)) throw new IllegalArgumentException("Radius exceeds width");
        if (r>(h/2.0f)) throw new IllegalArgumentException("Radius exceeds height");
        int segments = curveSegs(r, 2.0f * (float)Math.PI, tolerance);
        float coef = (float) (Math.PI/(2.0f*segments));

        float c1x = w >= 0 ? w : 0;
        float c1y = h >= 0 ? h : 0;
        float c2x = w >= 0 ? 0 : w;
        float c2y = h >= 0 ? h : 0;
        float c3x = w >= 0 ? 0 : w;
        float c3y = h >= 0 ? 0 : h;
        float c4x = w >= 0 ? w : 0;
        float c4y = h >= 0 ? 0 : h;


        float[] vert = new float[segments*8+8];
        int ind = 0;

        // TOP RIGHT
        float cx = x + c1x - r;
        float cy = y + c1y - r;
        for(int i = 0; i <= segments; i++) {
            vert[ind] = (float) (r * Math.cos(i*coef) + cx);
            vert[ind+1] = (float) (r * Math.sin(i*coef) + cy);
            ind+=2;
        }

        // TOP LEFT
        cx = x + c2x + r;
        cy = y + c2y - r;
        for(int i = 0; i <= segments; i++) {
            vert[ind] = (float) (cx - r * Math.sin(i*coef));
            vert[ind+1] = (float) (r * Math.cos(i*coef) + cy);
            ind+=2;
        }

        cx = x + c3x + r;
        cy = y + c3y + r;
        for(int i = 0; i <= segments; i++) {
            vert[ind] = (float) (cx - r * Math.cos(i*coef));
            vert[ind+1] = (float) (cy - r * Math.sin(i*coef));
            ind+=2;
        }

        cx = x + c4x - r;
        cy = y + c4y + r;
        for(int i = 0; i <= segments; i++) {
            vert[ind] = (float) (r * Math.sin(i*coef) + cx);
            vert[ind+1] = (float) (cy - r * Math.cos(i*coef));
            ind+=2;
        }

        Path2 path = new Path2(vert);
        path.closed = true;
        return path;
    }

    /**
     * Returns a path that represents a (full) capsule of the given dimensions.
     *
     * A capsule is a pill-like shape that fits inside of given rectangle.  If
     * width < height, the capsule will be oriented vertically with the rounded
     * portions at the top and bottom. Otherwise it will be oriented horizontally.
     *
     * @param x     The x-coordinate of the bottom left corner of the bounding box
     * @param y     The y-coordinate of the bottom left corner of the bounding box
     * @param w     The capsule width
     * @param h     The capsule height
     *
     * @return a path that represents a (full) capsule of the given dimensions.
     */
    public Path2 makeCapsule(float x, float y, float w, float h){
        return makeCapsule(Poly2.Capsule.FULL, x, y, w, h);

    }

    /**
     * Returns a path that represents a capsule of the given dimensions.
     *
     * A capsule typically is a pill-like shape that fits inside of given rectangle.
     * If width < height, the capsule will be oriented vertically with the rounded
     * portions at the top and bottom. Otherwise it will be oriented horizontally.
     *
     * This method allows for the creation of half-capsules, simply by using the
     * enumeration {@link Path2::Capsule}. The enumeration specifies which side
     * should be rounded in case of a half-capsule. Half-capsules are sized so that
     * the corresponding full capsule would fit in the bounding box.
     *
     * @param shape     The capsule shape
     * @param x         The x-coordinate of the bottom left corner of the bounding box
     * @param y         The y-coordinate of the bottom left corner of the bounding box
     * @param w         The capsule width
     * @param h         The capsule height
     *
     * @return a path that represents a capsule of the given dimensions.
     */
    public Path2 makeCapsule(Poly2.Capsule shape, float x, float y, float w, float h) {
        if (shape == Poly2.Capsule.DEGENERATE) {
            return makeEllipse(x+w/2, y+h/2, w, h);
        } else if (w == h) {
            return makeCircle(x+w/2, y+h/2, w);
        }

        int segments = curveSegs(Math.min(w/2,h/2), 2.0f * (float)Math.PI, tolerance);
        float coef = (float) (Math.PI/segments);

        float cx = x + w/2.0f;
        float cy = y + h/2.0f;
        int offset;
        Path2 path = new Path2();

        if (w <= h) {
            float radius = w / 2.0f;
            float iy = y+radius;
            float ih = h-w;

            // Start at bottom left of interior rectangle
            if (shape == Poly2.Capsule.HALF_REVERSE) {
                path.push(cx-radius, iy, true);
                path.push(cx+radius,iy,true);
            } else{
                offset = path.vertices.length;
                path.reserve(segments*2+2);
                for(int i=0; i<=segments; i++){
                    // Try to handle round off gracefully
                    float rads = i == segments ? (float) Math.PI : i * coef;
                    path.vertices[offset+i*2] = (float) (cx - radius * Math.cos( rads ));
                    path.vertices[offset+i*2+1] = (float) (iy - radius * Math.sin( rads ));
                }
            }

            // Now around the top
            if (shape == Poly2.Capsule.HALF) {
                path.push(cx+radius,iy+ih,true);
                path.push(cx-radius,iy+ih,true);
            } else {
                offset = path.vertices.length;
                path.reserve(segments*2+2);
                for(int i=0; i<=segments; i++){
                    // Try to handle round off gracefully
                    float rads = i == segments ? (float) Math.PI : i * coef;
                    path.vertices[offset+i*2] = (float) (cx + radius * Math.cos( rads ));
                    path.vertices[offset+i*2+1] = (float) (iy + ih + radius * Math.sin( rads ));
                }
            }
        } else {
            float radius = h / 2.0f;
            float ix = x+radius;
            float iw = w-h;

            // Start at the top left of the interior rectangle
            if (shape == Poly2.Capsule.HALF_REVERSE) {
                path.push(ix, cy+radius,true);
                path.push(ix, cy-radius,true);
            } else {
                offset = path.vertices.length;
                path.reserve(segments*2+2);
                for(int i=0; i<=segments; i++){
                    // Try to handle round off gracefully
                    float rads = i == segments ? (float) Math.PI : i * coef;
                    path.vertices[offset+i*2] = (float) (ix - radius * Math.sin( rads ));
                    path.vertices[offset+i*2+1] = (float) (cy + radius * Math.cos( rads ));
                }
            }

            // Now around the right side
            if (shape == Poly2.Capsule.HALF) {
                path.push(ix+iw, cy-radius,true);
                path.push(ix+iw, cy+radius,true);
            } else{
                offset = path.vertices.length;
                path.reserve(segments*2+2);
                for(int i=0; i<=segments; i++){
                    // Try to handle round off gracefully
                    float rads = i == segments ? (float) Math.PI : i * coef;
                    path.vertices[offset+i*2] = (float) (ix + iw + radius * Math.sin( rads ));
                    path.vertices[offset+i*2+1] = (float) (cy - radius * Math.cos( rads ));
                }
            }
        }
        path.closed = true;
        return path;
    }

    /**
     * Returns a path set representing a wire frame of an existing polygon.
     *
     * Traversals generate not just one path, but a sequence of paths (which
     * may all be either open or closed). This method provides four types of
     * traversals: `NONE`, `OPEN`, `CLOSED` and `INTERIOR`. The open and closed
     * traversals apply to the boundary of the polygon. If there is more than
     * one boundary, then each boundary is traversed separately.
     *
     * The interior traversal creates a wire frame a polygon triangulation.
     * That means it will generate a separate path for each triangle.
     *
     * @param src   The source polygon to traverse
     * @param type  The traversal type
     *
     * @return a path set representing a wire frame of an existing polygon.
     */
    public Path2[] makeTraversal(Poly2 src, Poly2.Traversal type){
        switch (type) {
            case NONE:
                // Do nothing
                break;
            case OPEN:
                return makeBoundaryTraversal(src, false);
            case CLOSED:
                return makeBoundaryTraversal(src, true);
            case INTERIOR:
                return makeInteriorTraversal(src);
        }
        return new Path2[0];
    }

    /**
     * Stores a wire frame of an existing polygon in the provided buffer.
     *
     * This method is dedicated to either an `OPEN` or `CLOSED` traversal.
     * It creates a path for each boundary. These paths are either open or
     * closed as specified.
     *
     * The traversal will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param src       The source polygon to traverse
     * @param closed    Whether to close the paths
     *
     * @return a reference to the buffer for chaining.
     */
    public Path2[] makeBoundaryTraversal(Poly2 src, boolean closed){
        short[][] bound = src.boundaries();
        Path2[] paths = new Path2[bound.length];
        for(int i = 0; i<bound.length; i++){
            short[] sl = bound[i];
            Path2 path = new Path2();
            for(int pos = 0; pos < sl.length; pos++) {
                path.push(src.vertices[sl[pos]*2], src.vertices[sl[pos]*2+1], true);
            }
            path.closed = closed;
            paths[i] = path;
        }
        return paths;
    }

    /**
     * This method is dedicated to an `INTERIOR` traversal.  See the description
     * of {@link #makeTraversal} for more information.  This method simply
     * exists to make the code more readable.
     *
     * The traversal will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param src   The source polygon to traverse
     *
     * @return a reference to the buffer for chaining.
     */
    public Path2[] makeInteriorTraversal(Poly2 src){
        Path2[] paths = new Path2[src.indices.length/3];

        int ind = 0;
        for(int i=0; i<src.indices.length; i+=3){
            float[] vert = new float[6];
            vert[0] = src.vertices[src.indices[i]*2];
            vert[1] = src.vertices[src.indices[i]*2+1];
            vert[2] = src.vertices[src.indices[i+1]*2];
            vert[3] = src.vertices[src.indices[i+1]*2+1];
            vert[4] = src.vertices[src.indices[i+2]*2];
            vert[5] = src.vertices[src.indices[i+2]*2+1];

            Path2 path = new Path2(vert);
            path.closed = true;
            paths[ind] = path;
            ind++;
        }
        return paths;
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
}
