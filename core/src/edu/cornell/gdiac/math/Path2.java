/*
 * Path2.java
 *
 * This class represents a flattened polyline (e.g. a 1-dimensional, piecewise linear path). Paths can be converted
 * into Poly2 objects by using either a triangulator or  an extruder.  In the case of triangulation, the interior of
 * a Path is always determined by the left (counter-clockwise) sides.  Hence the boundary of of a shape should be a
 * counter-clockwise path, while any hole should be a clockwise path.
 *
 * @author Crystal Jin
 * @date   12/5/2022
 */
package edu.cornell.gdiac.math;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntSet;
import java.util.Arrays;


/**
 * A class to represent a flattened polyline.
 *
 * This class is intended to represent any continuous polyline. While it may be either open or closed,
 * it should not have any gaps between vertices. If you need a path with gaps, that should be represented
 * by multiple Path2 objects.
 *
 * It is possible to draw a path object directly to a SpriteBatch. However, in most applications you will
 * want to convert a path object to a Poly2 for width and texturing. In particular, you will often want to
 * either extrude (give stroke width) or triangulate (fill) a path.
 */
public class Path2 {

    /** The vector of vertices in this path */
    public float[] vertices;
    /** The corner points of this path (used for extrusion). */
    public IntSet corners;
    /** Whether or not this path is closed */
    public boolean closed;

    /** Normal epsilon for testing and other applications */
    private final float CU_MATH_EPSILON  = 5.0e-4f;

    public Path2 () {
        this.vertices = new float[0];
        this.corners = new IntSet();
        this.closed = false;
    }

    /**
     * Sets the path to have the given vertices
     *
     * No vertices are marked are as corner vertices. The path will be open.
     *
     * @param vertices  The vector of vertices (as float array in pairs) in this path
     */
    public Path2 (float[] vertices) {
        if (vertices.length < 4) throw new IllegalArgumentException("path2 must contain at least 2 points.");
        this.vertices = vertices;
        this.corners = new IntSet();
        this.closed = false;
    }

    /**
     * Sets this path to be a copy of the given one.
     *
     * All of the contents are copied, so that this path does not hold any
     * references to elements of the other path.
     *
     * @param path  The path to copy
     */
    public Path2(Path2 path) {
        this.vertices = path.vertices;
        this.corners = path.corners;
        this.closed = path.closed;
    }

    /**
     * Sets the path to represent the given rectangle.
     *
     * The path will have four vertices, one for each corner of the rectangle.
     * The path will be closed.
     *
     * This method returns a reference to this path for chaining.
     *
     * @param rect  The rectangle to copy
     */
    public Path2(Rectangle rect) {
        this.vertices = new float[8];
        this.corners = new IntSet();
        corners.addAll(0,1,2,3);
        vertices[0] = rect.x;
        vertices[1] = rect.y;
        vertices[2] = rect.x+rect.width;
        vertices[3] = rect.y;
        vertices[4] = rect.x+rect.width;
        vertices[5] = rect.y+rect.height;
        vertices[6] = rect.x;
        vertices[7] = rect.y+rect.height;
        this.closed = true;
    }

    /**
     * Clears the contents of this path
     */
    public void clear() {
        vertices = new float[0];
        corners = new IntSet();
        closed = false;
    }

    /**
     * Returns a polyline represent by this path2.
     *
     * @return a polyline represent by this path2.
     */
    public Polyline getPolyline(){
        return new Polyline(vertices);
    }

    /**
     * Returns a polygon represent by this path2.
     *
     * @return a polygon represent by this path2.
     */
    public Polygon getPolygon(){
        return new Polygon(vertices);
    }

    /**
     * Returns a list of vertex indices representing this path.
     *
     * The indices are intended to be used in a drawing mesh to
     * display this path. The number of indices will be a multiple
     * of two.
     *
     * @return a list of vertex indices for using in a path mesh.
     */
    public short[] getIndices() {
        return getIndices(new short[0]);
    }

    /**
     * Stores a list of vertex indices in the given buffer.
     *
     * The indices are intended to be used in a drawing mesh to
     * display this path. The number of indices will be a multiple
     * of two.
     *
     * The indices will be appended to the provided vector. You should clear
     * the vector first if you do not want to preserve the original data.
     * If the calculation is not yet performed, this method will do nothing.
     *
     * @param buffer    a buffer to store the list of indices.
     *
     * @return buffer after adding indices
     */
    public short[] getIndices(short[] buffer) {
        int iSize = buffer.length;
        short[] copy = new short[iSize+ vertices.length];
        System.arraycopy(buffer, 0, copy, 0, buffer.length);
        int curIndex = iSize;
        for(int i=0; i< vertices.length/2-1; i++) {
            copy[curIndex] = (short)i;
            copy[curIndex+1] = (short)(i+1);
            curIndex+=2;
        }
        if (closed) {
            copy[curIndex] = (short)(vertices.length/2-1);
            copy[curIndex+1] = 0;
        }
        return copy;
    }

    /**
     * Returns the point at given index
     *
     * @param index the index of the point
     *
     * @return the former end point in the path
     */
    public Vector2 vertexAt(int index) {
        if (index < 0 || index > (vertices.length-2)/2) throw new IndexOutOfBoundsException();
        return new Vector2(vertices[2*index], vertices[2*index+1]);
    }

    /**
     * Returns the former end point in the path, after removing it
     *
     * If this path is empty, this will return the zero vector(null).
     *
     * @return the former end point in the path
     */
    public Vector2 pop() {
        if (vertices.length == 0){
            return null;
        }
        corners.remove((vertices.length-2)/2);
        Vector2 point = new Vector2(vertices[vertices.length-2], vertices[vertices.length-1]);
        vertices = Arrays.copyOfRange(vertices, 0, vertices.length-2);
        return point;
    }

    /**
     * Adds a point to the end of this path
     *
     * @param point     The point to add
     * @param corner    Whether this point is a corner
     */
    public void push(Vector2 point, boolean corner){
        push(point.x, point.y, corner);
    }

    /**
     * Adds a point to the end of this path
     *
     * @param x         The x-coordinate to add
     * @param y         The y-coordinate to add
     * @param corner    Whether this point is a corner
     */
    public void push(float x, float y, boolean corner){
        float[] copy = new float[vertices.length+2];
        System.arraycopy(vertices, 0, copy, 0, vertices.length);
        copy[vertices.length] = x;
        copy[vertices.length+1] = y;
        vertices = copy;
        if (corner) {
            corners.add((vertices.length-2)/2);
        }
    }

    /**
     * Returns the former point at the given index, after removing it
     *
     * If this path is empty, this will return the zero vector(null).
     *
     * @return the former point at the given index
     */
    public Vector2 remove(int index) {
        if(vertices.length-1 < index*2) {
            return null;
        }
        Vector2 point = vertexAt(index);
        corners.remove(index);
        float[] copy = new float[vertices.length-2];
        if (index * 2 >= 0) System.arraycopy(vertices, 0, copy, 0, index * 2);
        if (vertices.length - (2 * index + 2) >= 0)
            System.arraycopy(vertices, 2 * index + 2, copy, 2 * index + 2 - 2,
                    vertices.length - (2 * index + 2));
        vertices = copy;
        return point;
    }

    /**
     * Adds a point at the given index
     *
     * @param index     The index to add the point
     * @param point     The point to add
     * @param corner    Whether this point is a corner
     */
    public void add(int index, Vector2 point, boolean corner) {
        add(index, point.x, point.y, corner);
    }

    /**
     * Adds a point at the given index
     * If the index exceed the current index, then add the point to the end of the vertices
     *
     * @param index     The index to add the point
     * @param x         The x-coordinate to add
     * @param y         The y-coordinate to add
     * @param corner    Whether this point is a corner
     */
    public void add(int index, float x, float y, boolean corner) {
        if (index > (vertices.length-2)/2) {
            index = (vertices.length-2)/2 + 1;
        }
        float[] copy = new float[vertices.length+2];
        if (index * 2 >= 0) System.arraycopy(vertices, 0, copy, 0, index * 2);
        copy[2*index] = x;
        copy[2*index + 1] = y;
        if (vertices.length + 2 - (2 * index + 2) >= 0)
            System.arraycopy(vertices, 2 * index + 2 - 2, copy, 2 * index + 2,
                    vertices.length + 2 - (2 * index + 2));
        vertices = copy;
        if (corner){
            corners.add(index);
        }
    }

    /**
     * Allocates space in this path for the given number of points.
     *
     * This method can help performance when a path is being constructed
     * piecemeal.
     *
     * @param size  The number of spots allocated for future points.
     */
    public void reserve(int size) {
        if (size <= 0) return;
        float[] copy = new float[vertices.length+size];
        System.arraycopy(vertices, 0, copy, 0, vertices.length);
        vertices = copy;
    }

    /**
     * Returns true if the given point is incident to the given line segment.
     *
     * The variance specifies the tolerance that we allow for begin off the line
     * segment.
     *
     * @param point     The point to check
     * @param a         The start of the line segment
     * @param b         The end of the line segment
     * @param variance  The distance tolerance
     *
     * @return true if the given point is incident to the given line segment.
     */
    public boolean onSegment(Vector2 point, Vector2 a, Vector2 b, float variance) {
        float d1 = point.dst(a);
        float d2 = point.dst(b);
        float d3 = a.dst(b);
        return Math.abs(d3-d2-d1) <= variance;
    }

    /**
     * Returns true if the interior of this path contains the given point.
     *
     * This method returns false if the path is open.  Otherwise, it uses an even-odd
     * crossing rule to determine containment. Containment is not strict. Points on the
     * boundary are contained within this polygon.
     *
     * @param x         The x-coordinate to test
     * @param y         The y-coordinate to test
     *
     * @return true if this path contains the given point.
     */
    public boolean contains(float x, float y) {
        if (!closed) {
            return false;
        }
        // Use a winding rule otherwise
        int intersects = 0;

        for (int i = 0; i < vertices.length-2; i+=2) {
            float x1 = vertices[i];
            float y1 = vertices[i+1];
            float x2 = vertices[i+2];
            float y2 = vertices[i+3];
            if (((y1 <= y && y < y2) || (y2 <= y && y < y1)) && x < ((x2 - x1) / (y2 - y1) * (y - y1) + x1)) {
                intersects++;
            }
        }

        float x1 = vertices[vertices.length-2];
        float y1 = vertices[vertices.length-1];
        float x2 = vertices[0];
        float y2 = vertices[1];
        if (((y1 <= y && y < y2) || (y2 <= y && y < y1)) && x < ((x2 - x1) / (y2 - y1) * (y - y1) + x1)) {
            intersects++;
        }

        return (intersects & 1) == 1;
    }

    /**
     * Returns true if the interior of this path contains the given point.
     *
     * This mehtod returns false if the path is open.  Otherwise, it uses an even-odd
     * crossing rule to determine containment. Containment is not strict. Points on the
     * boundary are contained within this polygon.
     *
     * @param  point    The point to test
     *
     * @return true if this path contains the given point.
     */
    public boolean contains(Vector2 point) {
        return contains(point.x,point.y);
    }

    /**
     * Returns true if the given point is on the path.
     *
     * This method returns true if the point is within margin of error of a
     * line segment.
     *
     * @param x     The x-coordinate to test
     * @param y     The y-coordinate to test
     * @param err   The distance tolerance
     *
     * @return true if the given point is on the path.
     */
    public boolean incident(float x, float y, float err){
        Vector2 point = new Vector2(x,y);

        for (int i=0; i<vertices.length-2; i+=2) {
            if (onSegment(point, new Vector2(vertices[i],vertices[i+1]), new Vector2(vertices[i+2],vertices[i+3]), err)) {
                return true;
            }
        }

        if(closed) {
            return onSegment(point, new Vector2(vertices[vertices.length-2], vertices[vertices.length-1]),
                    new Vector2(vertices[0], vertices[1]), err);
        }

        return false;
    }

    /**
     * Returns true if the given point is on the path.
     *
     * This method returns true if the point is within margin of error of a
     * line segment.
     *
     * @param point The point to check
     * @param err   The distance tolerance
     *
     * @return true if the given point is on the path.
     */
    public boolean incident(Vector2 point, float err) {
        return incident(point.x, point.y, err);
    }

    /**
     * Returns the number of left turns in this path.
     *
     * Left turns are determined by looking at the interior angle generated at
     * each point (assuming that the path is intended to be counterclockwise).
     * In the case of an open path, the first and last vertexes are not counted.
     *
     * This method is a generalization of isConvex that can be used to
     * analyze the convexity of a path.
     *
     * @return the number of left turns in this path.
     */
    public int leftTurn() {
        if (vertices.length <= 4){
            return 0;
        }
        int nLeft = 0;
        if(closed){
            float p0x = vertices[vertices.length-2];
            float p0y = vertices[vertices.length-1];
            float p1x = vertices[0];
            float p1y = vertices[1];
            for (int i=2; i<vertices.length; i+=2) {
                float p2x = vertices[i];
                float p2y = vertices[i+1];
                float cross = (p2x - p1x) * (p1y -p0y) - (p1x - p0x) * (p2y - p1y);
                if (cross<0.0) {nLeft++;}
                p0x = p1x;
                p0y = p1y;
                p1x = p2x;
                p1y = p2y;
            }
            float p2x = vertices[0];
            float p2y = vertices[1];
            float cross = (p2x - p1x) * (p1y - p0y) - (p1x - p0x) * (p2y - p1y);
            if (cross<0.0) {nLeft++;}
        } else{
            float p0x = vertices[0];
            float p0y = vertices[1];
            float p1x = vertices[2];
            float p1y = vertices[3];
            for(int i = 4; i<vertices.length; i+=2){
                float p2x = vertices[i];
                float p2y = vertices[i+1];
                float cross = (p2x - p1x) * (p1y -p0y) - (p1x - p0x) * (p2y - p1y);
                if (cross<0.0) {nLeft++;}
                p0x = p1x;
                p0y = p1y;
                p1x = p2x;
                p1y = p2y;
            }
        }
        return nLeft;
    }

    /**
     * Returns true if this path defines a convex shape.
     *
     * This method returns false if the path is open.
     *
     * @return true if this path defines a convex shape.
     */
    public boolean isConvex() {
        if (vertices.length <= 4 || !closed){
            return false;
        }
        int nLeft = 0;
        float p0x = vertices[vertices.length-2];
        float p0y = vertices[vertices.length-1];
        float p1x = vertices[0];
        float p1y = vertices[1];
        for(int i=2; i<vertices.length; i+=2) {
            float p2x = vertices[i];
            float p2y = vertices[i+1];
            float cross = (p2x - p1x) * (p1y - p0y) - (p1x - p0x) * (p2y - p1y);
            if (cross<0.0) {nLeft++;}
            p0x = p1x;
            p0y = p1y;
            p1x = p2x;
            p1y = p2y;
        }
        float p2x = vertices[0];
        float p2y = vertices[1];
        float cross = (p2x - p1x) * (p1y - p0y) - (p1x - p0x) * (p2y - p1y);
        if (cross<0.0) {nLeft++;}
        return nLeft == vertices.length/2;
    }

    /**
     * Returns the area enclosed by this path.
     *
     * The area is defined as the sum of oriented triangles in a triangle
     * fan from a point on the convex hull. Counter-clockwise triangles
     * have positive area, while clockwise triangles have negative area.
     * The result agrees with the traditional concept of area for counter
     * clockwise paths.
     *
     * The area can be used to determine the orientation.  It the area is
     * negative, that means this path essentially represents a hole (e.g.
     * is clockwise instead of counter-clockwise).
     *
     * @return the area enclosed by this path.
     */
    public float area() {
        Vector2 ab,ac;
        Vector2 a = new Vector2(vertices[0], vertices[1]);
        float area = 0.0f;
        for (int i = 4; i<vertices.length; i+=2) {
            ab = new Vector2(vertices[i-2],vertices[i-1]);
            ac = new Vector2(vertices[i],vertices[i+1]);
            ab.sub(a);
            ac.sub(a);
            area += ab.x * ac.y - ac.x * ab.y;
        }
        return area * 0.5f;
    }

    /**
     * Returns -1, 0, or 1 indicating the path orientation.
     *
     * If the method returns -1, this is a counter-clockwise path. If 1, it
     * is a clockwise path.  If 0, that means it is undefined.  The
     * orientation can be undefined if all the points are colinear.
     *
     * @return -1, 0, or 1 indicating the path orientation.
     */
    public int orientation() {
        return orientation(vertices);
    }

    /**
     * Returns -1, 0, or 1 indicating the orientation of a -> b -> c
     *
     * If the function returns -1, this is a counter-clockwise turn.  If 1, it
     * is a clockwise turn.  If 0, it is colinear.
     *
     * @param a     The first point
     * @param b     The second point
     * @param c     The third point
     *
     * @return -1, 0, or 1 indicating the orientation of a -> b -> c
     */
    public int orientation(Vector2 a, Vector2 b, Vector2 c) {
        float val = (b.y - a.y) * (c.x - a.x) - (b.x - a.x) * (c.y - a.y);
        if (-CU_MATH_EPSILON < val && val < CU_MATH_EPSILON) return 0;  // colinear
        return (val > 0) ? 1: -1; // clock or counterclock wise
    }

    /**
     * Returns -1, 0, or 1 indicating the path orientation.
     *
     * If the method returns -1, this is a counter-clockwise path. If 1, it
     * is a clockwise path.  If 0, that means it is undefined.  The
     * orientation can be undefined if all the points are colinear.
     *
     * @param path  The path to check
     *
     * @return -1, 0, or 1 indicating the path orientation.
     */
    public int orientation(float[] path) {
        int idx = hullPoint(path);//index
        int vCount = path.length/2;
        int bx = idx == 0 ? vCount-1 : idx-1;
        int ax = idx == vCount-1 ? 0 : idx+1;
        return orientation(new Vector2(path[bx*2],path[bx*2+1]),
                new Vector2(path[idx*2],path[idx*2+1]), new Vector2(path[ax*2],path[ax*2+1]));
    }

    /**
     * Reverses the orientation of this path in place
     *
     * The path will have all of its vertices in the reverse order from the
     * original. This path will not be affected.
     *
     * @return This path, returned for chaining
     */
    public Path2 reverse() {
        int end = vertices.length-2;
        for (int i=0; i<vertices.length/4; i+=2){
            float x = vertices[end-i];
            float y = vertices[end-i+1];
            vertices[end-i] = vertices[i];
            vertices[end-i+1] = vertices[i+1];
            vertices[i] = x;
            vertices[i+1] = y;
        }
        IntSet.IntSetIterator it = corners.iterator();
        corners.clear();
        while (it.hasNext) {
            corners.add(vertices.length/2-it.next()-1);
        }
        return this;
    }

    /**
     * Returns a path with the reverse orientation of this one.
     *
     * The path will have all of its vertices in the reverse order from the
     * original. This path will not be affected.
     *
     * @return a path with the reverse orientation of this one.
     */
    public Path2 reversed() {
        Path2 copy = this;
        copy.reverse();
        return copy;
    }

    /**
     * Returns an index of a point on the convex hull
     *
     * The expact point returned is not guaranteed, but it is typically
     * with the least x and y values (whenever that is possible).
     * @return an index of a point on the convex hull
     */
    private int hullPoint() {
        return hullPoint(vertices);
    }

    /**
     * Returns an index of a point on the convex hull
     *
     * The expact point returned is not guaranteed, but it is typically
     * with the least x and y values (whenever that is possible).
     *
     * @param path  The path to check
     *
     * @return an index of a point on the convex hull
     */
    private int hullPoint(float[] path) {
        if (path.length==0) throw new IllegalArgumentException("path is empty.");
        double mx = path[0];
        double my = path[1];
        int pos = 0;
        for (int i = 2; i < path.length; i+=2) {
            if (path[i] < mx) {
                mx = path[i];
                my = path[i+1];
                pos = i/2;
            } else if (path[i] == mx && path[i+1] < my) {
                my = path[i+1];
                pos = i/2;
            }
        }
        return pos;
    }

    /**
     * Uniformly scales all of the vertices of this path.
     *
     * The vertices are scaled from the origin of the coordinate space. This
     * means that if the origin is not path of this path, then the path will
     * be effectively translated by the scaling.
     *
     * @param scale The uniform scaling factor
     *
     * @return This path, scaled uniformly.
     */
    public Path2 scl(float scale) {
        for (int i=0; i< vertices.length; i++) {
            vertices[i] *= scale;
        }
        return this;
    }

    /**
     * Nonuniformly scales all of the vertices of this path.
     *
     * The vertices are scaled from the origin of the coordinate space. This
     * means that if the origin is not path of this path, then the path will
     * be effectively translated by the scaling.
     *
     * @param scale The non-uniform scaling factor
     *
     * @return This path, scaled non-uniformly.
     */
    public Path2 scl(Vector2 scale) {
        for (int i=0; i< vertices.length; i+=2) {
            vertices[i] *= scale.x;
            vertices[i+1] *= scale.y;
        }
        return this;
    }

    /**
     * Uniformly scales all of the vertices of this path.
     *
     * The vertices are scaled from the origin of the coordinate space. This
     * means that if the origin is not path of this path, then the path will
     * be effectively translated by the scaling.
     *
     * @param scale The inverse of the uniform scaling factor
     *
     * @return This path, scaled uniformly.
     */
    public Path2 div(float scale) {
        if (scale == 0) throw new ArithmeticException("Divided by zero operation cannot possible");
        return scl(1/scale);
    }

    /**
     * Nonuniformly scales all of the vertices of this path.
     *
     * The vertices are scaled from the origin of the coordinate space. This
     * means that if the origin is not path of this path, then the path will
     * be effectively translated by the scaling.
     *
     * @param scale The non-uniform scaling factor
     *
     * @return This path, scaled non-uniformly.
     */
    public Path2 div(Vector2 scale) {
        if (scale.x == 0 || scale.y == 0) throw new ArithmeticException("Divided by zero operation cannot possible");
        return scl(new Vector2(1/scale.x,1/scale.y));
    }

    /**
     * Translates all of the vertices of this path.
     *
     * @param offset The translation amount
     *
     * @return This path, translated
     */
    public Path2 add(Vector2 offset) {
        for (int i=0; i< vertices.length; i+=2) {
            vertices[i] += offset.x;
            vertices[i+1] += offset.y;
        }
        return this;
    }

    /**
     * Translates all of the vertices of this path.
     *
     * @param offset The inverse of the translation amount
     *
     * @return This path, translated
     */
    public Path2 sub(Vector2 offset) {
        for (int i=0; i< vertices.length; i+=2) {
            vertices[i] -= offset.x;
            vertices[i+1] -= offset.y;
        }
        return this;
    }

    /**
     * Appends the given path to the end of this one
     *
     * The vertices are appended in order to the end of the path.  If
     * the original path was closed, it is now open (regardless of
     * whether or not extra is closed)
     *
     * @param extra The path to append
     *
     * @return This path, extended.
     */
    public Path2 add(Path2 extra) {
        closed = false;
        reserve(extra.vertices.length);
        for (int i = vertices.length; i< (vertices.length+extra.vertices.length); i++){
            vertices[i] = extra.vertices[i- vertices.length];
        }
        IntSet.IntSetIterator it = extra.corners.iterator();
        while (it.hasNext) {
            corners.add(it.next()+ vertices.length);
        }
        return this;
    }


    /**
     * Returns the slice of this path between start and end.
     *
     * The sliced path will use the indices from start to end (not including
     * end). It will include the vertices referenced by those indices, and
     * only those vertices. The resulting path is open.
     *
     * @param start The start index
     * @param end   The end index
     *
     * @return the slice of this mesh between start and end.
     */
    public Path2 slice(int start, int end){
        if (start>end) throw new IllegalArgumentException("The indices are invalid");

        Path2 copy = new Path2(Arrays.copyOfRange(vertices, start*2, end*2));

        for (int i = start; i<end; i++){
            if (isCorner(i)){
                copy.corners.add(i-start);
            }
        }
        return copy;
    }

    /**
     * Returns the slice of this mesh from the start index to the end.
     *
     * The sliced mesh will use the indices from start to the end. It will
     * include the vertices referenced by those indices, and only those
     * vertices. The resulting path is open.
     *
     * @param start The start index
     *
     * @return the slice of this mesh between start and end.
     */
    public Path2 sliceForm(int start) {
        return slice(start,vertices.length/2);
    }

    /**
     * Returns the slice of this mesh from the begining to end.
     *
     * The sliced mesh will use the indices up to  (but not including) end.
     * It will include the vertices referenced by those indices, and only
     * those vertices. The resulting path is open.
     *
     * @param end   The end index
     *
     * @return the slice of this mesh between start and end.
     */
    public Path2 sliceTo(int end) {
        return slice(0,end);
    }

    /**
     * Returns true if the point at the given index is a corner
     *
     * Corner points will be assigned a joint style when extruded. Points
     * that are not corners will be extruded smoothly (typically because
     * they are the result of a bezier expansion).
     *
     * @param index  The attribute index
     *
     * @return true if the point at the given index is a corner
     */
    public boolean isCorner(int index) {
        return corners.contains(index);
    }

    /**
     * Returns a string representation of this path for debugging purposes.
     *
     * If verbose is true, the string will include class information.  This
     * allows us to unambiguously identify the class.
     *
     * @param verbose Whether to include class information
     *
     * @return a string representation of this path for debuggging purposes.
     */
    public String toString(boolean verbose){
        String ss = verbose ? "cugl::Path2[" : "[";
        for (int i=0; i<vertices.length; i+=2){
            ss += "("+vertices[i] + ", " + vertices[i+1]+")";
            if (i != vertices.length-2) {
                ss += "; ";
            }
        }
        ss += "]";
        return ss;
    }

    /**
     * Returns the bounding box for the path
     *
     * The bounding box is the minimal rectangle that contains all of the vertices in
     * this path.  This method will recompute the bounds and is hence O(n).
     *
     * @return the bounding box for the path
     */
    public Rectangle getBounds() {
        if (vertices.length==0) return null;

        float minX, maxX;
        float minY, maxY;
        minX = vertices[0];
        minY = vertices[1];
        maxX = minX;
        maxY = minY;

        for (int i=0; i< vertices.length; i+=2){
            float x = vertices[i];
            float y = vertices[i+1];

            if (x < minX) {
                minX = x;
            } else if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            } else if (y > maxY) {
                maxY = y;
            }
        }
        return new Rectangle(minX,minY,maxX-minX,maxY-minY);
    }

}
