/*
 * PolyFactory.java
 *
 * This class provides a convenient way to generate simple (solid) polygons, like circles and rounded rectangles.
 *
 * @author Crystal Jin
 * @date   12/5/2022
 */
package edu.cornell.gdiac.math;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;


/**
 * A factory class for generating common Poly2 objects.
 *
 * Most of the time that we create a solid polygon, we are using it to approximate a common shape,
 * like a circle, or a rounded rectangle. Instead of embedding all of this functionality into Poly2
 * (which already has enough to do on its own), we have factored this out into a a separate factory class.
 * This factory can generate new polygons or reset existing ones (conserving memory).
 *
 * This factory is much lighter weight than the triangulation or extrusion factories.
 * In this factory, the calculation step and the materialization step are one in the same.
 * That is because the calculations are short and do not need to be refactored for multithread calculation.
 * Indeed, the only reason this factory is not a collection of simple functions is because that we
 * have have some settings (like precision and geometry) that we want to set separately.
 */
public class PolyFactory {
    private float DEFAULT_TOLERANCE = 0.5F;
    private float tolerance;

    /**
     * Creates a PolyFactory for generating solid polygons.
     *
     * This factory will use the default tolerance.
     */
    public PolyFactory(float tol) {
        tolerance = tol;
    }

    /**
     * Creates a PolyFactory for generating solid polygons.
     *
     * This factory will use the default tolerance.
     */
    public PolyFactory() {
        tolerance = DEFAULT_TOLERANCE;
    }

    /**
     * Returns a solid polygon that represents a simple triangle.
     *
     * @param  ax   The x-coordinate of the first vertex.
     * @param  ay   The y-coordinate of the first vertex.
     * @param  bx   The x-coordinate of the second vertex.
     * @param  by   The y-coordinate of the second vertex.
     * @param  cx   The x-coordinate of the third vertex.
     * @param  cy   The y-coordinate of the third vertex.
     *
     * @return a solid polygon that represents a simple triangle.
     */
    public Poly2 makeTriangle(float ax, float ay, float bx, float by, float cx, float cy) {
        return makeTriangle(new Poly2(new float[0]), ax, ay, bx, by, cx, cy);
    }

    /**
     * Returns a solid polygon that represents a simple triangle.
     *
     * @param  a    The first vertex.
     * @param  b    The second vertex.
     * @param  c    The third vertex.
     *
     * @return a solid polygon that represents a simple triangle.
     */
    public Poly2 makeTriangle(Vector2 a, Vector2 b, Vector2 c) {
        return makeTriangle(a.x,a.y,b.x,b.y,c.x,c.y);
    }

    /**
     * Stores a simple triangle in the provided buffer.
     *
     * The triangle will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param poly  The polygon to store the result
     * @param ax    The x-coordinate of the first vertex
     * @param ay    The y-coordinate of the first vertex
     * @param bx    The x-coordinate of the second vertex
     * @param by    The y-coordinate of the second vertex
     * @param cx    The x-coordinate of the third vertex
     * @param cy    The y-coordinate of the third vertex
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeTriangle(Poly2 poly, float ax, float ay, float bx, float by, float cx, float cy){
        int offset = poly.vertices.length/2;
        float[] copy = new float[poly.vertices.length+6];
        System.arraycopy(poly.vertices, 0, copy, 0, poly.vertices.length);
        copy[poly.vertices.length] = ax;
        copy[poly.vertices.length+1] = ay;
        copy[poly.vertices.length+2] = bx;
        copy[poly.vertices.length+3] = by;
        copy[poly.vertices.length+4] = cx;
        copy[poly.vertices.length+5] = cy;
        poly.vertices = copy;

        Path2 path = new Path2();
        short[] copyI = new short[poly.indices.length+3];
        System.arraycopy(poly.indices, 0, copyI, 0, poly.indices.length);
        copyI[poly.indices.length+1] = (short) (offset+1);
        if (path.orientation(new Vector2(ax,ay), new Vector2(bx,by), new Vector2(cx,cy)) >= 0) {
            copyI[poly.indices.length] = (short) (offset+2);
            copyI[poly.indices.length+2] = (short) (offset);
        } else {
            copyI[poly.indices.length] = (short) (offset);
            copyI[poly.indices.length+2] = (short) (offset+2);
        }
        poly.indices = copyI;
        return poly;
    }

    /**
     * Stores a simple triangle in the provided buffer.
     *
     * The triangle will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param poly  The polygon to store the result
     * @param a     The first vertex
     * @param b     The second vertex
     * @param c     The third vertex
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeTriangle(Poly2 poly, Vector2 a, Vector2 b, Vector2 c) {
        return  makeTriangle(poly, a.x, a.y, b.x, b.y, c.x, c.y);
    }

    /**
     * Returns a solid polygon that represents a rectangle
     *
     * @param x     The x-coordinate of the bottom left corner
     * @param y     The y-coordinate of the bottom left corner
     * @param w     The rectangle width
     * @param h     The rectangle height
     *
     * @return a solid polygon that represents a rectangle
     */
    public Poly2 makeRect(float x, float y, float w, float h){
        return makeRect(new Poly2(new float[0]), x, y, w, h);
    }

    /**
     * Returns a solid polygon that represents a rectangle
     *
     * @param origin    The rectangle origin
     * @param size      The rectangle size
     *
     * @return a solid polygon that represents a rectangle
     */
    public Poly2 makeRect(Vector2 origin, Vector2 size) {
        return makeRect( origin.x, origin.y, size.x, size.y);
    }

    /**
     * Stores a rectangle in the provided buffer.
     *
     * The rectangle will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param poly  The polygon to store the result
     * @param x     The x-coordinate of the bottom left corner
     * @param y     The y-coordinate of the bottom left corner
     * @param w     The rectangle width
     * @param h     The rectangle height
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeRect(Poly2 poly, float x, float y, float w, float h){
        int offset = poly.vertices.length/2;
        float[] copy = new float[poly.vertices.length+8];
        System.arraycopy(poly.vertices, 0, copy, 0, poly.vertices.length);
        copy[poly.vertices.length] = x;
        copy[poly.vertices.length+1] = y;
        copy[poly.vertices.length+2] = x+w;
        copy[poly.vertices.length+3] = y;
        copy[poly.vertices.length+4] = x+w;
        copy[poly.vertices.length+5] = y+h;
        copy[poly.vertices.length+6] = x;
        copy[poly.vertices.length+7] = y+h;
        poly.vertices = copy;

        short[] copyI = new short[poly.indices.length+6];
        System.arraycopy(poly.indices, 0, copyI, 0, poly.indices.length);
        copyI[poly.indices.length] = (short) (offset);
        copyI[poly.indices.length+1] = (short) (offset+1);
        copyI[poly.indices.length+2] = (short) (offset+2);
        copyI[poly.indices.length+3] = (short) (offset+2);
        copyI[poly.indices.length+4] = (short) (offset+3);
        copyI[poly.indices.length+5] = (short) (offset);
        poly.indices = copyI;
        return poly;
    }

    /**
     * Stores a rectangle in the provided buffer.
     *
     * The rectangle will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param poly      The polygon to store the result
     * @param origin    The rectangle origin
     * @param size      The rectangle size
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeRect(Poly2 poly, Vector2 origin, Vector2 size) {
        return makeRect(poly, origin.x, origin.y, size.x, size.y );
    }

    /**
     * Returns a solid polygon that represents a regular, many-sided polygon.
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
     * @return a solid polygon that represents a regular, many-sided polygon.
     */
    public Poly2 makeNgon(float cx, float cy, float radius, int sides) {
        return makeNgon(new Poly2(new float[0]), cx, cy, radius, sides);
    }

    /**
     * Returns a solid polygon that represents a regular, many-sided polygon.
     *
     * The polygon will be centered at the given origin with the given radius.
     * A regular polygon is essentially a circle where the number of segments
     * is explicit (instead of implicit from the curve tolerance).
     *
     * @param center    The polygon center point
     * @param radius    The polygon radius
     * @param sides     The number of sides
     *
     * @return a solid polygon that represents a regular, many-sided polygon.
     */
    public Poly2 makeNgon(Vector2 center, float radius, int sides) {
        return makeNgon(center.x, center.y, radius, sides);
    }

    /**
     * Stores a regular, many-sided polygon in the provided buffer.
     *
     * The polygon will be centered at the given origin with the given radius.
     * A regular polygon is essentially a circle where the number of segments
     * is explicit (instead of implicit from the curve tolerance).
     *
     * The polygon will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param poly      The polygon to store the result
     * @param cx        The x-coordinate of the center point
     * @param cy        The y-coordinate of the center point
     * @param radius    The polygon radius
     * @param sides     The number of sides
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeNgon(Poly2 poly, float cx, float cy, float radius, int sides) {
        int offset = poly.vertices.length/2;
        float coef = 2.0f * (float) Math.PI / sides;
        float[] copy = new float[poly.vertices.length+sides*2+2];
        System.arraycopy(poly.vertices, 0, copy, 0, poly.vertices.length);
        for (int i=0; i<sides; i++) {
            float rads = i*coef;
            copy[poly.vertices.length+i*2] = (float) (radius * Math.cos(rads) +cx);
            copy[poly.vertices.length+i*2+1] = (float) (radius * Math.sin(rads) +cy);
        }
        copy[poly.vertices.length+sides*2] = cx;
        copy[poly.vertices.length+sides*2+1] = cy;
        poly.vertices = copy;

        short[] copyI = new short[poly.indices.length+3*sides];
        System.arraycopy(poly.indices, 0, copyI, 0, poly.indices.length);
        for (int i=0; i<sides-1; i++) {
            copyI[poly.indices.length+i*3] = (short) (i+offset);
            copyI[poly.indices.length+i*3+1] = (short) (i+offset+1);
            copyI[poly.indices.length+i*3+2] = (short) (sides+offset);
        }
        copyI[poly.indices.length+3*sides-3] = (short) (sides+offset-1);
        copyI[poly.indices.length+3*sides-2] = (short) offset;
        copyI[poly.indices.length+3*sides-1] = (short) (sides+offset);
        poly.indices = copyI;
        return poly;
    }

    /**
     * Stores a regular, many-sided polygon in the provided buffer.
     *
     * The polygon will be centered at the given origin with the given radius.
     * A regular polygon is essentially a circle where the number of segments
     * is explicit (instead of implicit from the curve tolerance).
     *
     * The polygon will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param poly      The polygon to store the result
     * @param center    The polygon center point
     * @param radius    The polygon radius
     * @param sides     The number of sides
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeNgon(Poly2 poly, Vector2 center, float radius, int sides) {
        return makeNgon(poly, center.x, center.y, radius, sides );
    }

    /**
     * Returns a solid polygon that represents an ellipse of the given dimensions.
     *
     * @param cx    The x-coordinate of the center point
     * @param cy    The y-coordinate of the center point
     * @param sx    The size (diameter) along the x-axis
     * @param sy    The size (diameter) along the y-axis
     *
     * @return a solid polygon that represents an ellipse of the given dimensions.
     */
    public Poly2 makeEllipse(float cx, float cy, float sx, float sy) {
        return makeEllipse(new Poly2(new float[0]), cx, cy, sx, sy);
    }

    /**
     * Returns a solid polygon that represents an ellipse of the given dimensions.
     *
     * @param center    The ellipse center point
     * @param size      The size of the ellipse
     *
     * @return a solid polygon that represents an ellipse of the given dimensions.
     */
    public Poly2 makeEllipse(Vector2 center, Vector2 size){
        return makeEllipse( center.x, center.y, size.x, size.y );
    }

    /**
     * Stores an ellipse in the provided buffer.
     *
     * The ellipse will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param poly  The polygon to store the result
     * @param cx    The x-coordinate of the center point
     * @param cy    The y-coordinate of the center point
     * @param sx    The size (diameter) along the x-axis
     * @param sy    The size (diameter) along the y-axis
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeEllipse(Poly2 poly, float cx, float cy, float sx, float sy) {
        int segments = curveSegs(Math.max(sx/2.0f,sy/2.0f),2.0f*(float)Math.PI, tolerance);
        float coef = 2.0f * (float)Math.PI/segments;
        int offset = poly.vertices.length/2;

        float[] copy = new float[poly.vertices.length+segments*2+2];
        System.arraycopy(poly.vertices, 0, copy, 0, poly.vertices.length);
        for (int i=0; i<segments; i++) {
            float rads = i*coef;
            copy[poly.vertices.length+i*2] = (float) (0.5f*sx*Math.cos(rads) + cx);
            copy[poly.vertices.length+i*2+1] =(float) (0.5f*sy*Math.sin(rads) + cy);
        }
        copy[poly.vertices.length+segments*2] = cx;
        copy[poly.vertices.length+segments*2+1] = cy;
        poly.vertices = copy;

        short[] copyI = new short[poly.indices.length+3*segments];
        System.arraycopy(poly.indices, 0, copyI, 0, poly.indices.length);
        for (int i=0; i<segments-1; i++) {
            copyI[poly.indices.length+i*3] = (short) (i+offset);
            copyI[poly.indices.length+i*3+1] = (short) (i+offset+1);
            copyI[poly.indices.length+i*3+2] = (short) (segments+offset);
        }
        copyI[poly.indices.length+3*segments-3] = (short) (segments+offset-1);
        copyI[poly.indices.length+3*segments-2] = (short) offset;
        copyI[poly.indices.length+3*segments-1] = (short) (segments+offset);
        poly.indices = copyI;
        return poly;
    }

    /**
     * Stores an ellipse in the provided buffer.
     *
     * The ellipse will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param poly      The polygon to store the result
     * @param center    The ellipse center point
     * @param size      The size of the ellipse
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeEllipse(Poly2 poly, Vector2 center, Vector2 size){
        return makeEllipse(poly, center.x, center.y, size.x, size.y );
    }

    /**
     * Returns a solid polygon that represents a circle of the given dimensions.
     *
     * @param cx        The x-coordinate of the center point
     * @param cy        The y-coordinate of the center point
     * @param radius    The circle radius
     *
     * @return a solid polygon that represents an circle of the given dimensions.
     */
    public Poly2 makeCircle(float cx, float cy, float radius) {
        return makeCircle(new Poly2(new float[0]), cx, cy, radius);
    }

    /**
     * Returns a solid polygon that represents a circle of the given dimensions.
     *
     * @param center    The circle center point
     * @param radius    The circle radius
     *
     * @return a solid polygon that represents an circle of the given dimensions.
     */
    public Poly2 makeCircle(Vector2 center, float radius) {
        return makeCircle(center.x, center.y, radius );
    }

    /**
     * Stores a circle in the provided buffer.
     *
     * The circle will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param poly      The polygon to store the result
     * @param cx        The x-coordinate of the center point
     * @param cy        The y-coordinate of the center point
     * @param radius    The circle radius
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeCircle(Poly2 poly, float cx, float cy, float radius) {
        return makeEllipse(poly, cx, cy, 2*radius, 2*radius);
    }

    /**
     * Stores a circle in the provided buffer.
     *
     * The circle will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param poly      The polygon to store the result
     * @param center    The circle center point
     * @param radius    The circle radius
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeCircle(Poly2 poly, Vector2 center, float radius) {
        return makeCircle(poly, center.x, center.y, radius );
    }

    /**
     * Returns a solid polygon that represents an arc of the given dimensions.
     *
     * All arc measurements are in degrees, not radians.
     *
     * @param cx        The x-coordinate of the center point
     * @param cy        The y-coordinate of the center point
     * @param radius    The radius from the center point
     * @param start     The starting angle in degrees
     * @param degrees   The number of degrees to generate
     *
     * @return a solid polygon that represents an arc of the given dimensions.
     */
    public Poly2 makeArc(float cx, float cy, float radius, float start, float degrees) {
        return makeArc(new Poly2(new float[0]), cx, cy, radius, start, degrees);
    }

    /**
     * Returns a solid polygon that represents an arc of the given dimensions.
     *
     * All arc measurements are in degrees, not radians.
     *
     * @param center    The arc center point (of the defining circle
     * @param radius    The radius from the center point
     * @param start     The starting angle in degrees
     * @param degrees   The number of degrees to generate
     *
     * @return a solid polygon that represents an arc of the given dimensions.
     */
    public Poly2 makeArc(Vector2 center, float radius, float start, float degrees) {
        return makeArc(center.x,center.y,radius,start,degrees);
    }

    /**
     * Stores an arc in the provided buffer.
     *
     * All arc measurements are in degrees, not radians.
     *
     * The arc will be appended to the buffer.  You should clear the buffer first
     * if you do not want to preserve the original data.
     *
     * @param poly      The polygon to store the result
     * @param cx        The x-coordinate of the center point
     * @param cy        The y-coordinate of the center point
     * @param radius    The radius from the center point
     * @param start     The starting angle in degrees
     * @param degrees   The number of degrees to generate
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeArc(Poly2 poly, float cx, float cy, float radius, float start, float degrees) {
        int offset = poly.vertices.length/2;
        int segments = curveSegs(radius, degrees*(float)Math.PI/180.0f, tolerance);
        segments = (degrees < segments ? (int)degrees : segments);
        float srad = ((float)Math.PI/180.0f)*start;
        float arad = ((float)Math.PI/180.0f)*degrees;
        float coef = arad/segments;

        float[] copy = new float[poly.vertices.length+segments*2+4];
        System.arraycopy(poly.vertices, 0, copy, 0, poly.vertices.length);
        for(int i=0; i<segments+1; i++) {
            float rads = srad+i*coef;
            copy[poly.vertices.length+i*2] = (float) (0.5f*radius*Math.cos(rads) + cx);
            copy[poly.vertices.length+i*2+1] =(float) (0.5f*radius*Math.sin(rads) + cy);
        }
        copy[poly.vertices.length+segments*2+2] = cx;
        copy[poly.vertices.length+segments*2+3] = cy;
        poly.vertices = copy;

        short[] copyI = new short[poly.indices.length+3*segments+3];
        System.arraycopy(poly.indices, 0, copyI, 0, poly.indices.length);
        for (int i=0; i<segments+1; i++) {
            copyI[poly.indices.length+i*3] = (short) (i+offset);
            copyI[poly.indices.length+i*3+1] = (short) (i+offset+1);
            copyI[poly.indices.length+i*3+2] = (short) (segments+offset+1);
        }
        poly.indices = copyI;
        return poly;
    }

    /**
     * Stores an arc in the provided buffer.
     *
     * All arc measurements are in degrees, not radians.
     *
     * The arc will be appended to the buffer.  You should clear the buffer first
     * if you do not want to preserve the original data.
     *
     * @param poly      The polygon to store the result
     * @param center    The arc center point (of the defining circle
     * @param radius    The radius from the center point
     * @param start     The starting angle in degrees
     * @param degrees   The number of degrees to generate
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeArc(Poly2 poly, Vector2 center, float radius, float start, float degrees) {
        return makeArc(poly, center.x,center.y,radius,start,degrees);
    }

    /**
     * Returns a solid polygon that represents a rounded rectangle of the given dimensions.
     *
     * The radius should not exceed either half the width or half the height.
     *
     * @param x     The x-coordinate of the bottom left corner of the bounding box
     * @param y     The y-coordinate of the bottom left corner of the bounding box
     * @param w     The rectangle width
     * @param h     The rectangle height
     * @param r     The radius of each corner
     *
     * @return a solid polygon that represents a rounded rectangle of the given dimensions.
     */
    public Poly2 makeRoundedRect(float x, float y, float w, float h, float r) {
        return makeRoundedRect(new Poly2(new float[0]), x,y,w,h,r);
    }

    /**
     * Returns a solid polygon that represents a rounded rectangle of the given dimensions.
     *
     * The radius should not exceed either half the width or half the height.
     *
     * @param origin    The enclosing rectangle origin
     * @param size      The enclosing rectangle size
     * @param radius    The radius of each corner
     *
     * @return a solid polygon that represents a rounded rectangle of the given dimensions.
     */
    public Poly2 makeRoundedRect(Vector2 origin, Vector2 size, float radius) {
        return makeRoundedRect(origin.x, origin.y, size.x, size.y, radius);
    }

    /**
     * Stores a rounded rectangle in the provided buffer.
     *
     * The radius should not exceed either half the width or half the height.
     *
     * The rounded rectangle will be appended to the buffer.  You should clear the
     * buffer first if you do not want to preserve the original data.
     *
     * @param poly  The polygon to store the result
     * @param x     The x-coordinate of the bottom left corner of the bounding box
     * @param y     The y-coordinate of the bottom left corner of the bounding box
     * @param w     The rectangle width
     * @param h     The rectangle height
     * @param r     The radius of each corner
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeRoundedRect(Poly2 poly, float x, float y, float w, float h, float r) {
        if (r>(w/2.0f)) throw new IllegalArgumentException("Radius exceeds width");
        if (r>(h/2.0f)) throw new IllegalArgumentException("Radius exceeds height");
        int offset = poly.vertices.length/2;
        int segments = curveSegs(r, 2.0f*(float) Math.PI, tolerance);
        float coef = (float) (Math.PI / (2.0f*segments));

        float c1x = w >= 0 ? w : 0;
        float c1y = h >= 0 ? h : 0;
        float c2x = w >= 0 ? 0 : w;
        float c2y = h >= 0 ? h : 0;
        float c3x = w >= 0 ? 0 : w;
        float c3y = h >= 0 ? 0 : h;
        float c4x = w >= 0 ? w : 0;
        float c4y = h >= 0 ? 0 : h;

        float[] copy = new float[poly.vertices.length+segments*8+10];
        System.arraycopy(poly.vertices, 0, copy, 0, poly.vertices.length);

        // TOP RIGHT
        float cx = x + c1x - r;
        float cy = y + c1y - r;
        for (int i=0; i<=segments; i++) {
            copy[poly.vertices.length+i*2] = (float) (r*Math.cos(i*coef) + cx);
            copy[poly.vertices.length+i*2+1] = (float) (r*Math.sin(i*coef) + cy);
        }

        // TOP LEFT
        cx = x + c2x + r;
        cy = y + c2y - r;
        for (int i=0; i<=segments; i++) {
            copy[poly.vertices.length+(segments+1)*2+i*2] = (float) (cx - r*Math.sin(i*coef));
            copy[poly.vertices.length+(segments+1)*2+i*2+1] = (float) (r*Math.cos(i*coef) + cy);
        }

        cx = x + c3x + r;
        cy = y + c3y + r;
        for(int i = 0; i <= segments; i++) {
            copy[poly.vertices.length+(segments+1)*4+i*2] = (float) (cx - r*Math.cos(i*coef));
            copy[poly.vertices.length+(segments+1)*4+i*2+1] = (float) (cy - r*Math.sin(i*coef));
        }

        cx = x + c4x - r;
        cy = y + c4y + r;
        for(int i = 0; i <= segments; i++) {
            copy[poly.vertices.length+(segments+1)*6+i*2] = (float) (r*Math.sin(i*coef) + cx);
            copy[poly.vertices.length+(segments+1)*6+i*2+1] = (float) (cy - r*Math.cos(i*coef));
        }

        cx = x + w/2.0f;
        cy = y + h/2.0f;
        copy[poly.vertices.length+segments*8+8] = cx;
        copy[poly.vertices.length+segments*8+9] = cy;
        poly.vertices = copy;

        int capacity = 4*segments+4;
        short[] copyI = new short[poly.indices.length+3*capacity];
        System.arraycopy(poly.indices, 0, copyI, 0, poly.indices.length);
        for (int i=0; i<capacity-1; i++) {
            copyI[poly.indices.length+i*3] = (short) (offset+i);
            copyI[poly.indices.length+i*3+1] = (short) (offset+i+1);
            copyI[poly.indices.length+i*3+2] = (short) (offset+capacity);
        }
        copyI[poly.indices.length+3*capacity-3] = (short) (capacity+offset-1);
        copyI[poly.indices.length+3*capacity-2] = (short) offset;
        copyI[poly.indices.length+3*capacity-1] = (short) (capacity+offset);
        poly.indices = copyI;
        return poly;
    }

    /**
     * Stores a rounded rectangle in the provided buffer.
     *
     * The radius should not exceed either half the width or half the height.
     *
     * The rounded rectangle will be appended to the buffer.  You should clear the
     * buffer first if you do not want to preserve the original data.
     *
     * @param poly      The polygon to store the result
     * @param origin    The enclosing rectangle origin
     * @param size      The enclosing rectangle size
     * @param radius    The radius of each corner
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeRoundedRect(Poly2 poly, Vector2 origin, Vector2 size, float radius) {
        return makeRoundedRect(poly, origin.x, origin.y, size.x, size.y, radius);
    }

    /**
     * Returns a solid polygon that represents a capsule of the given dimensions.
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
     * @return a solid polygon that represents a capsule of the given dimensions.
     */
    public Poly2 makeCapsule(float x, float y, float w, float h) {
        return makeCapsule(new Poly2(new float[0]), Poly2.Capsule.FULL, x, y, w, h);
    }

    /**
     * Returns a solid polygon that represents a (full) capsule of the given dimensions.
     *
     * A capsule is a pill-like shape that fits inside of given rectangle.  If
     * width < height, the capsule will be oriented vertically with the rounded
     * portions at the top and bottom. Otherwise it will be oriented horizontally.
     *
     * @param origin    The enclosing rectangle origin
     * @param size      The enclosing rectangle size
     *
     * @return a solid polygon that represents a (full) capsule of the given dimensions.
     */
    public Poly2 makeCapsule(Vector2 origin, Vector2 size) {
        return makeCapsule(origin.x, origin.y, size.x, size.y);
    }

    /**
     * Stores a capsule in the provided buffer.
     *
     * A capsule is a pill-like shape that fits inside of given rectangle.  If
     * width < height, the capsule will be oriented vertically with the rounded
     * portions at the top and bottom.Otherwise it will be oriented horizontally.
     *
     * The capsule will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param poly  The polygon to store the result
     * @param x     The x-coordinate of the bottom left corner of the bounding box
     * @param y     The y-coordinate of the bottom left corner of the bounding box
     * @param w     The capsule width
     * @param h     The capsule height
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeCapsule(Poly2 poly, float x, float y, float w, float h) {
        return makeCapsule(poly, Poly2.Capsule.FULL, x, y, w, h);
    }

    /**
     * Stores a (full) capsule in the provided buffer.
     *
     * A capsule is a pill-like shape that fits inside of given rectangle.  If
     * width < height, the capsule will be oriented vertically with the rounded
     * portions at the top and bottom. Otherwise it will be oriented horizontally.
     *
     * The capsule will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param poly      The polygon to store the result
     * @param origin    The enclosing rectangle origin
     * @param size      The enclosing rectangle size
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeCapsule(Poly2 poly, Vector2 origin, Vector2 size) {
        return makeCapsule(poly, origin.x, origin.y, size.x, size.y);
    }

    /**
     * Returns a solid polygon that represents a (full) capsule of the given dimensions.
     *
     * A capsule typically is a pill-like shape that fits inside of given rectangle.
     * If width < height, the capsule will be oriented vertically with the rounded
     * portions at the top and bottom. Otherwise it will be oriented horizontally.
     *
     * This method allows for the creation of half-capsules, simply by using the
     * enumeration {@link Poly2.Capsule}. The enumeration specifies which side
     * should be rounded in case of a half-capsule. Half-capsules are sized so that
     * the corresponding full capsule would fit in the bounding box.
     *
     * @param shape     The capsule shape
     * @param x         The x-coordinate of the bottom left corner of the bounding box
     * @param y         The y-coordinate of the bottom left corner of the bounding box
     * @param w         The capsule width
     * @param h         The capsule height
     *
     * @return a solid polygon that represents a capsule of the given dimensions.
     */
    public Poly2 makeCapsule(Poly2.Capsule shape, float x, float y, float w, float h) {
        return makeCapsule(new Poly2(new float[0]), shape, x, y, w, h);
    }

    /**
     * Returns a solid polygon that represents a capsule of the given dimensions.
     *
     * A capsule typically is a pill-like shape that fits inside of given rectangle.
     * If width < height, the capsule will be oriented vertically with the rounded
     * portions at the top and bottom. Otherwise it will be oriented horizontally.
     *
     * This method allows for the creation of half-capsules, simply by using the
     * enumeration {@link Poly2.Capsule}. The enumeration specifies which side
     * should be rounded in case of a half-capsule. Half-capsules are sized so that
     * the corresponding full capsule would fit in the bounding box.
     *
     * @param shape     The capsule shape
     * @param origin    The enclosing rectangle origin
     * @param size      The enclosing rectangle size
     *
     * @return a solid polygon that represents a capsule of the given dimensions.
     */
    public Poly2 makeCapsule(Poly2.Capsule shape, Vector2 origin, Vector2 size) {
        return makeCapsule(shape, origin.x, origin.y, size.x, size.y);
    }

    /**
     * Stores a capsule in the provided buffer.
     *
     * A capsule typically is a pill-like shape that fits inside of given rectangle.
     * If width < height, the capsule will be oriented vertically with the rounded
     * portions at the top and bottom. Otherwise it will be oriented horizontally.
     *
     * This method allows for the creation of half-capsules, simply by using the
     * enumeration {@link Poly2.Capsule}. The enumeration specifies which side
     * should be rounded in case of a half-capsule. Half-capsules are sized so that
     * the corresponding full capsule would fit in the bounding box.
     *
     * The capsule will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param poly      The polygon to store the result
     * @param shape     The capsule shape
     * @param x         The x-coordinate of the bottom left corner of the bounding box
     * @param y         The y-coordinate of the bottom left corner of the bounding box
     * @param w         The capsule width
     * @param h         The capsule height
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeCapsule(Poly2 poly, Poly2.Capsule shape, float x, float y, float w, float h) {
        if (shape == Poly2.Capsule.DEGENERATE) {
            return makeEllipse(poly, x+w/2, y+h/2, w, h);
        } else if (w==h) {
            return makeCircle(poly, x+w/2, y+h/2, w);
        }

        int segments = curveSegs(Math.min(w/2,h/2), (float) Math.PI, tolerance);
        int offset = poly.vertices.length/2;
        float coef = (float) (Math.PI/segments);

        float cx = x + w/2.0f;
        float cy = y + h/2.0f;
        int vCount = 0;
        ArrayList<Float> vList = new ArrayList<>();
        if (w<=h) {
            float radius = w/2.0f;
            float iy = y+radius;
            float ih = h-w;

            // Start at bottom left of interior rectangle
            if (shape == Poly2.Capsule.HALF_REVERSE) {
                vList.add(cx - radius);
                vList.add(iy);
                vList.add(cx+radius);
                vList.add(iy);
                vCount += 2;
            } else {
                for(int i = 0; i<=segments; i++) {
                    float rads = (i == segments ? (float) Math.PI : i * coef);
                    vList.add((float) (cx - radius*Math.cos(rads)));
                    vList.add((float) (iy - radius*Math.sin(rads)));
                }
                vCount += segments+1;
            }

            // Now around the top
            if (shape == Poly2.Capsule.HALF) {
                vList.add(cx+radius);
                vList.add(iy+ih);
                vList.add(cx-radius);
                vList.add(iy+ih);
                vCount += 2;
            } else {
                for(int i = 0; i<=segments; i++) {
                    // Try to handle round off gracefully
                    float rads = (i == segments ? (float) Math.PI : i * coef);
                    vList.add((float) (cx+radius*Math.cos(rads)));
                    vList.add((float) (iy+ih+radius*Math.sin(rads)));
                }
                vCount += segments+1;
            }
        } else {
            float radius = h / 2.0f;
            float ix = x+radius;
            float iw = w-h;

            // Start at the top left of the interior rectangle
            if (shape == Poly2.Capsule.HALF_REVERSE) {
                vList.add(ix);
                vList.add(cy+radius);
                vList.add(ix);
                vList.add(cy-radius);
                vCount += 2;
            } else {
                for (int i=0; i<=segments; i++) {
                    // Try to handle round off gracefully
                    float rads = (i == segments ? (float) Math.PI : i * coef);
                    vList.add((float) (ix - radius*Math.sin(rads)));
                    vList.add((float) (cy + radius*Math.cos(rads)));
                }
                vCount += segments+1;
            }

            // Now around the right side
            if (shape == Poly2.Capsule.HALF) {
                vList.add(ix+iw);
                vList.add(cy-radius);
                vList.add(ix+iw);
                vList.add(cy+radius);
                vCount += 2;
            } else {
                for (int i=0; i<=segments; i++) {
                    // Try to handle round off gracefully
                    float rads = (i == segments ? (float) Math.PI : i * coef);
                    vList.add((float) (ix + iw + radius*Math.sin(rads)));
                    vList.add((float) (cy - radius*Math.cos(rads)));
                }
                vCount += segments+1;
            }
        }
        vList.add(cx);
        vList.add(cy);

        float[] copy = new float[poly.vertices.length+vList.size()];
        System.arraycopy(poly.vertices, 0, copy, 0, poly.vertices.length);
        for (int i=0; i<vList.size(); i++) {
            copy[poly.vertices.length+i] = vList.get(i);
        }
        poly.vertices = copy;

        short[] copyI = new short[poly.indices.length+3*vCount];
        System.arraycopy(poly.indices, 0, copyI, 0, poly.indices.length);
        for (int i=0; i<vCount-1; i++) {
            copyI[poly.indices.length+i*3] = (short) (offset+i);
            copyI[poly.indices.length+i*3+1] = (short) (offset+i+1);
            copyI[poly.indices.length+i*3+2] = (short) (offset+vCount);
        }
        copyI[poly.indices.length+3*vCount-3] = (short) (offset+vCount-1);
        copyI[poly.indices.length+3*vCount-2] = (short) offset;
        copyI[poly.indices.length+3*vCount-1] = (short) (offset+vCount);
        poly.indices = copyI;
        return poly;
    }

    /**
     * Stores a capsule in the provided buffer.
     *
     * A capsule typically is a pill-like shape that fits inside of given rectangle.
     * If width < height, the capsule will be oriented vertically with the rounded
     * portions at the top and bottom. Otherwise it will be oriented horizontally.
     *
     * This method allows for the creation of half-capsules, simply by using the
     * enumeration {@link Poly2.Capsule}. The enumeration specifies which side
     * should be rounded in case of a half-capsule. Half-capsules are sized so that
     * the corresponding full capsule would fit in the bounding box.
     *
     * The capsule will be appended to the buffer.  You should clear the buffer
     * first if you do not want to preserve the original data.
     *
     * @param poly      The polygon to store the result
     * @param shape     The capsule shape
     * @param origin    The enclosing rectangle origin
     * @param size      The enclosing rectangle size
     *
     * @return a reference to the buffer for chaining.
     */
    public Poly2 makeCapsule(Poly2 poly, Poly2.Capsule shape, Vector2 origin, Vector2 size) {
        return makeCapsule(poly, shape, origin.x, origin.y, size.x, size.y);
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
