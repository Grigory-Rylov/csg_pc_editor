/**
 * Polygon.java
 * <p>
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights
 * reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info@michaelhoffer.de>.
 */
package eu.printingin3d.javascad.vrl;

import eu.printingin3d.javascad.coords.Triangle3d;
import eu.printingin3d.javascad.coords.Triangulator;
import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.coords2d.LineSegment;
import eu.printingin3d.javascad.tranform.ITransformation;
import eu.printingin3d.javascad.utils.AssertValue;
import eu.printingin3d.javascad.utils.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a convex polygon. A polygon is represented by its points which should be on the
 * same plane
 * (not tested, but guaranteed by the used algorithms), its normal (calculated from the points),
 * its color and its distance from the origin.
 */
public final class PolygonImproved {

    /**
     * Polygon vertices.
     */
    private final List<V3d> vertices;
    /**
     * Normal vector.
     */
    private final V3d normal;
    /**
     * Square of the distance to origin.
     */
    private final double dist;
    /**
     * The color of the polygon.
     */
    private final Color color;

     public PolygonImproved(List<V3d> vertices, V3d normal, Color color) {
        this.vertices = vertices;
        this.normal = normal;
        V3d a = vertices.get(0);
        this.dist = normal.dot(a);
        this.color = color;
    }

    private PolygonImproved(List<V3d> vertices, V3d normal, double dist, Color color) {
        this.vertices = vertices;
        this.normal = normal;
        this.dist = dist;
        this.color = color;

        for (V3d v : vertices) {
            VertexPosition position = calculateVertexPosition(v);
            if (position != VertexPosition.COPLANAR) {
            }
        }
    }

    /**
     * Creates a new polygon that consists of the specified vertices.
     *
     * <b>Note:</b> the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     *
     * @param vertices polygon vertices
     * @param color the color of the polygon
     * @return a new polygon that consists of the specified vertices
     */
    public static PolygonImproved fromPolygons(List<V3d> vertices, Color color) {
        AssertValue.isTrue(
            vertices.size() >= 3,
            "The coordinate list should contain at least 3 points."
        );

        V3d a = vertices.get(0);
        V3d b = vertices.get(1);
        V3d c = vertices.get(2);
        V3d n = b.add(a.inverse()).cross(c.add(a.inverse())).unit();

        return new PolygonImproved(vertices, n, n.dot(a), color);
    }

    public static PolygonImproved fromPolygons(V3d a, V3d b, V3d c, Color color) {
        V3d n = b.add(a.inverse()).cross(c.add(a.inverse())).unit();
        ArrayList<V3d> vertices = new ArrayList<>();
        vertices.add(a);
        vertices.add(b);
        vertices.add(c);
        return new PolygonImproved(vertices, n, n.dot(a), color);
    }

    public Color getColor() {
        return color;
    }

    public V3d getNormal() {
        return normal;
    }

    /**
     * Flips this polygon.
     *
     * @return a new polygon with the vertices in reversed order
     */
    public PolygonImproved flip() {
        List<V3d> newVertices = new ArrayList<>(vertices);

        Collections.reverse(newVertices);

        return new PolygonImproved(newVertices, normal.inverse(), -dist, color);
    }

    //-----------------------//

    /**
     * Converts this polygon to triangles using ear clipping algorithm.
     *
     * @return a list of triangles
     */
    public List<Facet> toFacets() {
        List<Facet> facets = new ArrayList<>();
        if (this.vertices.size() < 3) {
            return facets;
        }

        // Для треугольника - просто создаем один фасет
        if (this.vertices.size() == 3) {
            Triangle3d triangle = new Triangle3d(
                vertices.get(0),
                vertices.get(1),
                vertices.get(2)
            );
            facets.add(new Facet(triangle, normal, color));
            return facets;
        }

        List<Triangle3d> triangles = Triangulator.triangulate(vertices, normal);

        for (Triangle3d triangle : triangles) {
            facets.add(new Facet(triangle, normal, color));
        }

        return facets;
    }

    //----------------------//
    public List<V3d> getVertices() {
        return vertices;
    }

    /**
     * Returns a transformed copy of this polygon.
     *
     * <b>Note:</b> if the applied transformation performs a mirror operation
     * the vertex order of this polygon is reversed.
     *
     * <b>Note:</b> this polygon is not modified
     *
     * @param transform the transformation to apply
     * @return a transformed copy of this polygon
     */
    public PolygonImproved transformed(ITransformation transform) {
        List<V3d> newVertices = new ArrayList<>();

        for (V3d v : vertices) {
            newVertices.add(transform.transform(v));
        }

        PolygonImproved result = fromPolygons(newVertices, color);

        return transform.isMirror() ? result.flip() : result;
    }

    private VertexPosition calculateVertexPosition(V3d v) {
        double t = this.normal.dot(v) - this.dist;
        return VertexPosition.fromSquareDistance(t);
    }

    /**
     * Splits a {@link PolygonImproved} by this plane if needed. After that it puts the
     * polygons or the polygon fragments in the appropriate lists
     * ({@code front}, {@code back}). Coplanar polygons go into either
     * {@code coplanarFront}, {@code coplanarBack} depending on their
     * orientation with respect to this plane. Polygons in front or back of this
     * plane go into either {@code front} or {@code back}.
     *
     * @param polygon polygon to split
     * @param coplanarFront "coplanar front" polygons
     * @param coplanarBack "coplanar back" polygons
     * @param front front polygons
     * @param back back polgons
     */
    public void splitPolygon(
        PolygonImproved polygon,
        List<PolygonImproved> coplanarFront,
        List<PolygonImproved> coplanarBack,
        List<PolygonImproved> front,
        List<PolygonImproved> back
    ) {
        // Classify each point as well as the entire polygon into one of the four possible classes.
        VertexPosition polygonType = calculatePolygonPosition(polygon);

        // Put the polygon in the correct list, splitting it when necessary.
        switch (polygonType) {
            case COPLANAR:
                (this.normal.dot(polygon.normal) > 0 ? coplanarFront : coplanarBack).add(polygon);
                break;
            case FRONT:
                front.add(polygon);
                break;
            case BACK:
                back.add(polygon);
                break;
            case SPANNING:
                splitPolygon(polygon, front, back);
                break;
            default:
                break;
        }
    }

    // Classify the entire polygon into one of the four possible classes.
    private VertexPosition calculatePolygonPosition(PolygonImproved polygon) {
        VertexPosition polygonType = VertexPosition.COPLANAR;
        for (V3d v : polygon.vertices) {
            polygonType = polygonType.add(calculateVertexPosition(v));
        }

        return polygonType;
    }

    private void splitPolygon(PolygonImproved polygon, List<PolygonImproved> front, List<PolygonImproved> back) {
        List<V3d> f = new ArrayList<>();
        List<V3d> b = new ArrayList<>();
        for (LineSegment<V3d> ls : LineSegment.lineSegmentSeries(polygon.vertices)) {
            classifyAndSplitVertex(ls.getStart(), ls.getEnd(), f, b);
        }

        //f = removeDuplicates(f);
        //b = removeDuplicates(b);
        front.add(fromPolygons(f, polygon.color));
        back.add(fromPolygons(b, polygon.color));
    }

    public static List<V3d> removeDuplicates(List<V3d> list) {
        List<V3d> result = new ArrayList<>();
        for (V3d item : list) {
            if (!containsV3d(result, item)) {
                result.add(item);
            }
        }
        return result;
    }

    private static boolean containsV3d(List<V3d> list, V3d item) {
        for (V3d element : list) {
            if (element.equals(item)) {
                return true;
            }
        }
        return false;
    }

    private void classifyAndSplitVertex(
        V3d currentVertex, V3d nextVertex,
        List<V3d> f, List<V3d> b
    ) {
        VertexPosition position = calculateVertexPosition(currentVertex);

        switch (position) {
            case FRONT:
                addVertexToList(f, currentVertex, position);
                break;
            case BACK:
                addVertexToList(b, currentVertex, position);
                break;
            default:
                addVertexToList(f, currentVertex, VertexPosition.FRONT);
                addVertexToList(b, currentVertex, VertexPosition.BACK);
                break;
        }
        if (position.add(calculateVertexPosition(nextVertex)) == VertexPosition.SPANNING) {
            double t = (this.dist - this.normal.dot(currentVertex)) /
                this.normal.dot(nextVertex.add(currentVertex.inverse()));
            V3d v = currentVertex.lerp(nextVertex, t);

            addVertexToList(f, v, VertexPosition.FRONT);
            addVertexToList(b, v, VertexPosition.BACK);
        }
    }

    private void addVertexToList(List<V3d> list, V3d newVertex, VertexPosition vp) {
        /*
        if (!list.isEmpty()) {
            V3d lastVertex = list.get(list.size() - 1);

            for (int i = 0; i < vertices.size(); i++) {
                V3d prev = vertices.get(i);
                V3d c = vertices.get((i + 1) % vertices.size());

				//TODO: test V3d(25, -25, -15) - V3d(-25, -25, -15) x V3d(-25, -25, -15) - V3d(-12
				   .5, -25, -15)
                V3d cross = EdgeCrossSolver.findIntersection(prev, c, lastVertex, newVertex);
                if (cross != null && !cross.equals(newVertex) && !cross.equals(lastVertex)) {
                    System.out.println(
                        "Added new vertex: " + cross + " between " + lastVertex + " and " +
                            newVertex + " (" + prev + ", " + c + ")");
                    list.add(cross);
                }
            }
        }
         */
        list.add(newVertex);
    }

}
