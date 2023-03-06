/*
 * Poly2.java
 *
 * This class represents a simple polygon.  The purpose of this class is to separate the geometry (and math) of a
 * polygon mesh from the rendering data of a pipeline. It is one of the most important classes for 2D game design in
 * all of CUGL, now carried over to LibGDX.
 *
 *  This class is intentionally (based on experience in previous semesters) lightweight. There is no verification that
 * indices are properly defined. It is up to the user to verify and specify the components. If you need help
 *  with triangulation or path extrusion, use one the related factory classes.
 *
 * @author Crystal Jin
 * @date   12/5/2022
 */
package edu.cornell.gdiac.math;

import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntSet;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A class to represent a simple polygon.
 *
 * This class is intended to represent any polygon (including non-convex polygons).
 * that does not have self-interections (as these can cause serious problems with the mathematics).
 * Most polygons are simple, meaning that they have no holes. However, this class does support
 * complex polygons with holes, provided that the polygon is not implicit and has an corresponding mesh.
 *
 * To define a mesh, the user should provide a set of indices which will be used in rendering.
 * These indices should represent a triangulation of the polygon. However, this class performs no verification.
 * It will not check that a mesh is in proper form, nor will it search for holes or self-intersections.
 * These are the responsibility of the programmer.
 */
public class Poly2 {

    /** The vector of vertices in this polygon */
    public float[] vertices;
    /** The vector of indices in the triangulation */
    public short[] indices;

    /**
     * This class is a triangle in a mesh, interpreted as a node in tree decomposition
     *
     * Two triangles are adjacent in this decomposition if they share an edge. This
     * dual graph is not connected, though we do track direction when we are recursively
     * following a path.
     *
     * The elements in a tree node are ordered in ascending order, so that we can
     * uniquely identify a tree node from its contents.
     */
    private class TreeNode{
        /** Hash function for a tree node */
        float hashCode;

        /** The elements of this triangle */
        short[] elements = new short[3];

        /** The adjacent neighbors to this node */
        ArrayList<TreeNode> neighbors = new ArrayList<>();

        /** The node pointing to this one in a traversal */
        TreeNode previous;

        /**
         * Creates a Poly2TreeNode from the given three elements.
         *
         * @param a The first element
         * @param b The second element
         * @param c The third element
         */
        public TreeNode(short a, short b, short c){
            elements[0] = (short) Math.min( a, Math.min( b, c ) );
            elements[2] = (short) Math.max( a, Math.max( b, c ) );
            elements[1] = a;
            if (elements[0] == elements[1] || elements[2] == elements[1]) {
                elements[1] = b;
            }
            if (elements[0] == elements[1] || elements[2] == elements[1]) {
                elements[1] = c;
            }
            hashCode = a+37*b+37*37*c;
        }

        /**
         * Returns true if o is a TreeNode equal to this one
         *
         * Since TreeNode objects sort their contents, o must have its elements in
         * the same order as this on.
         *
         * @return true if o is a TreeNode equal to this one
         */
        public boolean equals(TreeNode o){
            return elements[0] == o.elements[0] && elements[1] == o.elements[1] && elements[2] == o.elements[2];
        }

        /**
         * Returns a string representation of this tree node
         *
         * @return a string representation of this tree node
         */
        public String toString(){
            return String.valueOf(elements[0])+" "+String.valueOf(elements[1])+" "+String.valueOf(elements[2]);
        }

        /**
         * Returns a string representation of a tree node with the given elements
         *
         * This method allows us to get the string of a tree node (from its contents)
         * without actually having to construct the tree node itself.  This is useful
         * for hashtable lookups.
         *
         * @param a The first element
         * @param b The second element
         * @param c The third element
         *
         * @return a string representation of a tree node with the given elements
         */
        public String toString(short a, short b, short c){
            short indx1 = (short) Math.min( a, Math.min( b, c ) );
            short indx3 = (short) Math.max( a, Math.max( b, c ) );
            short indx2 = a;
            if (indx1 == indx2 || indx3 == indx2) {
                indx2 = b;
            }
            if (indx1 == indx2 || indx3 == indx2) {
                indx2 = c;
            }
            return String.valueOf(indx1)+" "+String.valueOf(indx2)+" "+String.valueOf(indx3);
        }

        /**
         * Returns true if x is an element in this node
         *
         * @param x The element to check
         *
         * @return true if x is an element in this node
         */
        public boolean contains(int x){
            return x >= 0 && (elements[0] == x || elements[1] == x || elements[2] == x);
        }

        /**
         * Returns true if node is adjacent to this one.
         *
         * A node is adjacent if it shares exactly one side.
         *
         * @param node  The node to check
         *
         * @return true if node is adjacent to this one.
         */
        public boolean adjacent(TreeNode node){
            int count = 0;
            for (int i = 0; i < 3; i++) {
                count += contains( node.elements[i] ) ? 1 : 0;
            }
            return count == 2;
        }

        /**
         * Returns a boundary index from the node, not in inuse
         *
         * A boundary index is either one that does not appear in any
         * of its neighbors (so this is an ear in a triangulation) or
         * only appears in one neighbor (so this is the either the first
         * or last triangle with this index in a normal traversal).
         *
         * If no boundary index can be found, or they are all already
         * in inuse, this method returns -1.
         *
         * @param inuse The indices to exclude from the search
         *
         * @return a boundary index from the node, not in inuse
         */
        public int pick(IntSet inuse) {
            int[] count = new int[3];
            count[0] = 0;
            count[1] = 0;
            count[2] = 0;
            for (TreeNode node : neighbors) {
                for (int i = 0; i < 3; i++) {
                    if (node.contains( elements[i] )) {
                        count[i]++;
                    }
                }
            }
            int ptr = -1;
            for (int ii = 0; ii < 3; ii++) {
                if ((count[ii] == 0 || count[ii] == 1)) {
                    if (!inuse.contains(elements[ii])) {
                        if (ptr == -1) {
                            ptr = ii;
                        } else if (count[ii] < count[ptr]) {
                            ptr = ii;
                        }
                    }
                }
            }

            return ptr != -1 ? elements[ptr] : -1;
        }

        /**
         * Returns the opposite transition point for the given index.
         *
         * A transition point is a node that contains index and for which index is
         * a boundary value (either it has no neighbors with the same index or only
         * one neighbor).  It represents the first and/or last triangle with this
         * index in a normal traversal.
         *
         * If there is only one triangle with this index, this method returns this
         * node.  Otherwise, if this node corresponds to the first triangle, it
         * returns the last, and vice versa.  By following indices, we create a
         * traversal that can find an exterior boundary.
         *
         * @param index The index defining the traversal
         *
         * @return the opposite transition point for the given index.
         */
        public TreeNode follow(int index){
            previous = null;
            return crawl( index );
        }

        /**
         * Returns the opposite transition point for the given index.
         *
         * This method is the recursive helper for {@link #follow}. It uses
         * the internal previous attribute to track direction.
         *
         * @param index The index defining the traversal
         *
         * @return the opposite transition point for the given index.
         */
        public TreeNode crawl(int index){
            if (!contains(index)) {
                return null;
            }

            TreeNode next = null;
            for (TreeNode node : neighbors) {
                if (node != previous && node.contains( index )) {
                    next = node;
                }
            }

            if (next == null) {
                return this;
            } else if (next.previous == this) {
                return null;
            }
            next.previous = this;
            return next.crawl( index );
        }

    }

    /**
     * Returns true if the given points are colinear (within margin of error)
     *
     * @param vx     The x value of first point
     * @param vy     The y value of first point
     * @param wx     The x value of second point
     * @param wy     The y value of second point
     * @param px     The x value third point
     * @param py     The y value third point
     * @param err   The margin of error
     */
    private static boolean colinear(float vx, float vy, float wx, float wy, float px, float py, float err) {
        float l2 = (wx-vx)*(wx-vx) + (wy-vy)*(wy-vy);
        double distance = 0.0f;
        if (l2 == 0.0f) {
            distance = Math.sqrt((px-vx)*(px-vx)+(py-vy)+(py-vy));
        } else {
            float dot = (px-vx)*(wx-vx)+(py-vy)*(wy-vy);
            float t = Math.max(0.0f, Math.min(1.0f, dot / l2));
            float proX = vx+t*(wx-vx);
            float proY = vy+t*(wy-vy);
            distance = Math.sqrt((px-proX)*(px-proX)+(py-proY)+(py-proY));
        }
        return (distance <= err);
    }


    /**
     * Sets the polygon to have the given vertices
     *
     * The resulting polygon has no indices triangulating the vertices.
     *
     * @param vertices  The vector of vertices (as float in pairs) in this polygon
     */
    public Poly2(float[] vertices) {
        this.vertices = vertices;
        indices = new short[0];
    }

    /**
     * Sets the polygon to have the given vertices and indices
     *
     * @param vertices  The float array of vertices (as float in pairs) in this polygon
     * @param indices  The array of indices in this polygon
     *
     */
    public Poly2(float[] vertices, short[] indices) {
        this.vertices = vertices;
        this.indices = indices;
    }

    /**
     * Sets this polygon to be a copy of the given one.
     *
     * All of the contents are copied, so that this polygon does not hold any
     * references to elements of the other polygon.
     *
     * @param poly  The polygon to copy
     */
    public Poly2(Poly2 poly) {
        this.vertices = poly.vertices;
        this.indices = poly.indices;
    }

    /**
     * Sets the polygon to represent the given rectangle.
     *
     * The polygon will have four vertices, one for each corner of the rectangle.
     * The indices will define two triangles on these vertices. This method is
     * faster than using one of the more heavy-weight triangulators.
     *
     * @param rect  The rectangle to copy
     */
    public Poly2(Rectangle rect) {
        vertices = new float[8];
        indices = new short[6];
        vertices[0] = rect.x;
        vertices[1] = rect.y;
        vertices[2] = rect.x+rect.width;
        vertices[3] = rect.y;
        vertices[4] = rect.x+rect.width;
        vertices[5] = rect.y+rect.height;
        vertices[6] = rect.x;
        vertices[7] = rect.y+rect.height;
        indices[0] = 0;
        indices[1] = 1;
        indices[2] = 2;
        indices[3] = 0;
        indices[4] = 2;
        indices[5] = 3;
    }

    /**
     * Clears the contents of this polygon (both vertices and indices)
     */
    public void clear() {
        vertices = new float[0];
        indices = new short[0];
    }

    /**
     * Convert current Poly2 object to PolygonRegion. Used for drawing methods.
     *
     * @param region The TextureRegion used for PolygonRegion
     *
     * @return The PolygonRegion after conversion.
     */
    public PolygonRegion makePolyRegion(TextureRegion region) {
        return new PolygonRegion(region, vertices, indices);
    }

    /**
     * Uniformly scales all of the vertices of this polygon.
     *
     * The vertices are scaled from the origin of the coordinate space.  This
     * means that if the origin is not in the interior of this polygon, the
     * polygon will be effectively translated by the scaling.
     *
     * @param scale The uniform scaling factor
     *
     * @return This polygon, scaled uniformly.
     */
    public Poly2 scl(float scale) {
        for (int i=0; i< vertices.length; i++) {
            vertices[i] *= scale;
        }
        return this;
    }

    /**
     * Nonuniformly scales all of the vertices of this polygon.
     *
     * The vertices are scaled from the origin of the coordinate space.  This
     * means that if the origin is not in the interior of this polygon, the
     * polygon will be effectively translated by the scaling.
     *
     * @param scale The non-uniform scaling factor
     *
     * @return This polygon, scaled non-uniformly.
     */
    public Poly2 scl(Vector2 scale) {
        for (int i=0; i< vertices.length; i+=2) {
            vertices[i] *= scale.x;
            vertices[i+1] *= scale.y;
        }
        return this;
    }

    /**
     * Uniformly scales all of the vertices of this polygon.
     *
     * The vertices are scaled from the origin of the coordinate space.  This
     * means that if the origin is not in the interior of this polygon, the
     * polygon will be effectively translated by the scaling.
     *
     * @param scale The inverse of the uniform scaling factor
     *
     * @return This polygon, scaled uniformly.
     */
    public Poly2 div(float scale) {
        if (scale == 0) throw new ArithmeticException("Divided by zero operation cannot possible");
        return scl(1/scale);
    }

    /**
     * Nonuniformly scales all of the vertices of this polygon.
     *
     * The vertices are scaled from the origin of the coordinate space.  This
     * means that if the origin is not in the interior of this polygon, the
     * polygon will be effectively translated by the scaling.
     *
     * @param scale The inverse of the non-uniform scaling factor
     *
     * @return This polygon, scaled non-uniformly.
     */
    public Poly2 div(Vector2 scale) {
        if (scale.x == 0 || scale.y == 0) throw new ArithmeticException("Divided by zero operation cannot possible");
        return scl(new Vector2(1/scale.x,1/scale.y));
    }

    /**
     * Uniformly translates all of the vertices of this polygon.
     *
     * @param offset The uniform translation amount
     *
     * @return This polygon, translated uniformly.
     */
    public Poly2 add(float offset) {
        for (int i=0; i<vertices.length; i++) {
            vertices[i] += offset;
        }
        return this;
    }

    /**
     * Non-uniformly translates all of the vertices of this polygon.
     *
     * @param offset The non-uniform translation amount
     *
     * @return This polygon, translated non-uniformly.
     */
    public Poly2 add(Vector2 offset) {
        for (int i=0; i<vertices.length; i+=2) {
            vertices[i] += offset.x;
            vertices[i+1] += offset.y;
        }
        return this;
    }

    /**
     * Uniformly translates all of the vertices of this polygon.
     *
     * @param offset The inverse of the uniform translation amount
     *
     * @return This polygon, translated uniformly.
     */
    public Poly2 sub(float offset) {
        for (int i=0; i<vertices.length; i++) {
            vertices[i] -= offset;
        }
        return this;
    }

    /**
     * Non-uniformly translates all of the vertices of this polygon.
     *
     * @param offset The inverse of the non-uniform translation amount
     *
     * @return This polygon, translated non-uniformly.
     */
    public Poly2 sub(Vector2 offset) {
        for (int i=0; i< vertices.length; i+=2) {
            vertices[i] -= offset.x;
            vertices[i+1] -= offset.y;
        }
        return this;
    }

    /**
     * Returns the bounding box for the polygon
     *
     * The bounding box is the minimal rectangle that contains all of the vertices in
     * this polygon.  It is recomputed whenever the vertices are set.
     *
     * @return the bounding box for the polygon
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

    /**
     * Returns true if this polygon contains the given point.
     *
     * Unlike {@link Path2}, this method does not use an even-odd rule. Instead,
     * it checks for containment within the associated triangle mesh.
     *
     * Containment is not strict. Points on the boundary are contained within
     * this polygon.
     *
     * @param  x x-pos of the point
     * @param  y y-pos of the point
     *
     * @return true if this polygon contains the given point.
     */
    public boolean contains(float x, float y) {
        boolean inside = false;
        Vector2 temp2 = new Vector2(x,y);
        for (int i=0; !inside && 3*i < indices.length; i++) {
            Vector3 temp3 = getBarycentric(temp2, i);
            inside = (0 <= temp3.x && temp3.x <= 1 &&
                    0 <= temp3.y && temp3.y <= 1 &&
                    0 <= temp3.z && temp3.z <= 1);
        }
        return inside;
    }

    /**
     * Returns true if this polygon contains the given point.
     *
     * Unlike {@link Path2}, this method does not use an even-odd rule. Instead,
     * it checks for containment within the associated triangle mesh.
     *
     * Containment is not strict. Points on the boundary are contained within
     * this polygon.
     *
     * @param  point    The point to test
     *
     * @return true if this polygon contains the given point.
     */
    public boolean contains(Vector2 point) {
        return contains(point.x, point.y);
    }

    /**
     * Returns true if this polygon contains the given point.
     *
     * Unlike {@link Path2}, this method does not use an even-odd rule. Instead,
     * it checks for containment within the associated triangle mesh.
     *
     * Containment is not strict. Points on the boundary are contained within
     * this polygon.
     *
     * @param x         The x-coordinate to test
     * @param y         The y-coordinate to test
     *
     * @return true if this polygon contains the given point.
     */
    public boolean incident(float x, float y, float err) {
        short[][] bound = boundaries();
        for(short[] sl : bound){
            for (int ii = 0; ii < sl.length; ii += 2) {
                float vx = vertices[ii*2];
                float vy = vertices[ii*2+1];
                float wx = vertices[ii*2+2];
                float wy = vertices[ii*2+3];
                if (colinear(vx, vy, wx, wy, x, y, err)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the given point is on the boundary of this polygon.
     *
     * This method generates uses {@link #boundaries} to determine the boundaries.
     * It returns true if the point is within margin of error of a line segment
     * on one of the boundaries.
     *
     * @param point The point to check
     * @param err   The distance tolerance
     *
     * @return true if the given point is on the boundary of this polygon.
     */
    public boolean incident(Vector2 point, float err) {
        return incident(point.x, point.y, err);
    }

    /**
     * Returns the setx of indices that are on a boundary of this polygon
     *
     * This method can identify the outer hull using the graph properties of the
     * triangle mesh. An internal node if the number of neighbors is the same as
     * the number of attached triangles. An index that is not internal is external.
     *
     * Unlike {@link #boundaries}, this method does not order the boundary indices
     * or decompose them into connected components.
     *
     * @return the set of indices that are on a boundary of this polygon
     */
    public short[] exterior() {
        HashMap<Integer, ArrayList<Short>> neighbors = new HashMap<>();
        HashMap<Integer, Integer> count = new HashMap<>();

        for (int i=0; i< indices.length; i+=3){
            for(int j=0; j<3; j++){
                int ind = indices[i+j];
                ArrayList<Short> search = neighbors.get(ind);
                ArrayList<Short> slot;
                if (search == null){
                    slot = new ArrayList<>();
                    neighbors.put(ind,slot);
                    count.put(ind,0);
                } else {
                    slot = search;
                }

                slot.add(indices[i+((j+1) % 3)]);
                slot.add(indices[i+((j+2) % 3)]);
                count.put(ind, count.get(ind)+1);
                neighbors.put(ind,slot);
            }
        }

        ArrayList<Short> res = new ArrayList<>();
        for (int i=0; i< vertices.length; i+=2){
            if(neighbors.get(i/2).size() > count.get(i/2)){
                res.add((short) (i/2));
            }
        }
        short[] resC = new short[res.size()];
        for (int i=0; i< res.size(); i++){
            resC[i] = res.get(i);
        }
        return resC;
    }

    /**
     * Returns the connected boundary components for this polygon.
     *
     * This method allows us to reconstruct the exterior boundary of a solid
     * shape, or to compose a pathwise connected curve into components.
     *
     * This method detriangulates the polygon mesh, returning the outer hull,
     * discarding any interior points. This hull need not be convex. If the
     * mesh represents a simple polygon, only one boundary will be returned.
     * If the mesh is not continuous, the outer array will contain the boundary
     * of each disjoint polygon. If the mesh has holes, each hole will be returned
     * as a separate boundary. There is no guarantee on the order of boundaries
     * returned.
     *
     * @return the connected boundary components for this polygon.
     */
    public short[][] boundaries() {
        // Create the decomposition
        HashMap<String,TreeNode> decomp = new HashMap<>();
        for(int ii = 0; ii < indices.length; ii += 3) {
            TreeNode current = new TreeNode(indices[ii],indices[ii+1],indices[ii+2]);
            String key = current.toString();
            if (!decomp.containsKey(key)){
                for (TreeNode node : decomp.values()) {
                    if (node.adjacent( current )) {
                        node.neighbors.add(current);
                        current.neighbors.add(node);
                    }
                }
                decomp.put(key,current);
            }
        }

        // Create arrays for the result and to track our progress
        IntSet total = new IntSet();
        IntSet inuse = new IntSet();
        for(short n: indices){
            total.add(n);
        }
        boolean abort = false;
        ArrayList<ArrayList<Short>> res = new ArrayList<>();
        while (inuse.size != total.size && !abort) {
            ArrayList<Short> array = new ArrayList<>();
            // Pick a valid (exterior) starting point at the correct position
            TreeNode current = null;
            int start = -1;
            for (TreeNode node : decomp.values()) {
                start = node.pick(inuse);
                if (start != -1) {
                    current = node;
                }
            }

            // Self-crossings may allow a point to be reused, so we
            // need a local "visited" set for each path.
            IntSet visited = new IntSet();
            if (start != -1) {
                // Follow the path until no more indices to pick
                int index = start;
                current = current.follow( index );
                while (current != null) {
                    visited.add( index );
                    array.add((short) index);
                    index = current.pick( visited );
                    current = current.follow( index );
                }
                // Add this to the global results
                IntSet.IntSetIterator it = new IntSet.IntSetIterator(visited);
                while(it.hasNext){
                    inuse.add(it.next());
                }
                res.add(array);

                // Reset the tree node internal state for next pass
                if (inuse.size != total.size) {
                    for (TreeNode node : decomp.values()) {
                        node.previous = null;
                    }
                }
            } else {
                // All the indices found were internal
                abort = true;
            }
        }

        // Algorithm produces borders with REVERSE orientation (outside is CW)
        // We need to reverse all of them to guarantee CCW rule
        short[][] result = new short[res.size()][];
        for (int i=0; i< res.size(); i++){
            short[] n = new short[res.get(res.size()-1-i).size()];
            for (int m=0; m<res.get(res.size()-1-i).size(); m++){
                n[m] = res.get(res.size()-1-i).get(m);
            }
            result[i] = n;
        }
        return result;
    }

    /**
     * Returns a string representation of this rectangle for debugging purposes.
     *
     * If verbose is true, the string will include class information.  This
     * allows us to unambiguously identify the class.
     *
     * @param verbose Whether to include class information
     *
     * @return a string representation of this rectangle for debuggging purposes.
     */
    public String toString(boolean verbose) {
        String ss = verbose ? "cugl::Poly2[" : "[";
        for (int i=0; i< indices.length; i+=3) {
            ss += "("+indices[i] + ", " + indices[i+1] + ", " + indices[i+2]+")";
            if (i != vertices.length-3) {
                ss += "; ";
            }
        }
        ss += "]";
        return ss;
    }

    /**
     * Returns the barycentric coordinates for a point relative to a triangle.
     *
     * The triangle is identified by the given index.  For index ii, it is the
     * triangle defined by indices 3*ii, 3*ii+1, and 3*ii+2.
     *
     * This method is not defined if the polygon is not SOLID.
     */
    private Vector3 getBarycentric(Vector2 point, int index) {
        Vector2 a = new Vector2(vertices[2*indices[3*index]], vertices[2*indices[3*index]+1]);
        Vector2 b = new Vector2(vertices[2*indices[3*index+1]], vertices[2*indices[3*index+1]+1]);
        Vector2 c = new Vector2(vertices[2*indices[3*index+2]], vertices[2*indices[3*index+2]+1]);

        float det = (b.y-c.y)*(a.x-c.x)+(c.x-b.x)*(a.y-c.y);
        Vector3 result = new Vector3();
        result.x = (b.y-c.y)*(point.x-c.x)+(c.x-b.x)*(point.y-c.y);
        result.y = (c.y-a.y)*(point.x-c.x)+(a.x-c.x)*(point.y-c.y);
        result.x /= det;
        result.y /= det;
        result.z = 1 - result.x - result.y;
        return result;
    }

    /**
     * The types of joints supported in an extrusion.
     *
     * A joint is the rule for how to connect two extruded line segments.
     * If there is not joint, the path will look like a sequence of overlapping
     * rectangles.
     *
     * This enumeration is used by {@link SimpleExtruder}
     */
    public enum Joint {
        /** Mitre joint; ideal for paths with sharp corners */
        MITRE,
        /** Bevel joint; ideal for smoother paths (DEFAULT) */
        SQUARE,
        /** Round joint; used to smooth out paths with sharp corners */
        ROUND
    }

    /**
     * The types of caps supported in an extrusion.
     *
     * A cap is the rule for how to end an extruded line segment that has no
     * neighbor on that end.  If there is no cap, the path terminates at the
     * end vertices.
     *
     * This enumeration is used by {@link SimpleExtruder}
     */
    public enum EndCap {
        /** No end cap; the path terminates at the end vertices (DEFAULT) */
        BUTT,
        /** Square cap; like no cap, except the ends are padded by stroke width */
        SQUARE,
        /** Round cap; the ends are half circles whose radius is the stroke width */
        ROUND
    }

    /**
     * This enum lists the types of path traversal that are supported.
     *
     * This enumeration is used by {@link PathFactory}.
     */
    public enum Traversal {
        /** No traversal; the index list will be empty. */
        NONE,
        /** Traverse the border, but do not close the ends. */
        OPEN,
        /** Traverse the border, and close the ends. */
        CLOSED,
        /** Traverse the individual triangles in the standard tesselation. */
        INTERIOR
    }

    /**
     * This enum specifies a capsule shape
     *
     * A capsule is a box with semicircular ends along the major axis. They are a
     * popular physics object, particularly for character avatars.  The rounded ends
     * means they are less likely to snag, and they naturally fall off platforms
     * when they go too far.
     *
     * Sometimes we only want half a capsule (so a semicircle at one end, but not
     * both).  This enumeration allows us to specify the exact capsule we want.
     * This enumeration is used by {@link PolyFactory}
     */
    public enum Capsule {
        /**
         * A degenerate capsule (e.g. an ellipse)
         *
         * Any capsule with width and height the same is degenerate.
         */
        DEGENERATE,
        /**
         * A full capsule with round ends on the major axis.
         *
         * This type assumes that there is a major axis (e.g. that width
         * and height are not the same).
         */
        FULL,
        /**
         * A half capsule with a rounded end on the default side.
         *
         * The default side is the left if the major axis is x, and the
         * bottom if the major axis is y. This type assumes that there is
         * a major axis (e.g. that width and height are not the same).
         */
        HALF,
        /**
         * A half capsule with a rounded end on the side opposite the default.
         *
         * The opposite side is the right if the major axis is x, and the
         * top if the major axis is y. This type assumes that there is
         * a major axis (e.g. that width and height are not the same).
         */
        HALF_REVERSE
    }
}


