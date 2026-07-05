package com.github.grishberg.cad3d.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import eu.printingin3d.javascad.coords.Triangle3d;
import eu.printingin3d.javascad.coords.Triangulator;
import eu.printingin3d.javascad.utils.PolygonValidator;
import eu.printingin3d.javascad.vrl.Facet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.models.Abstract3dModel;
import eu.printingin3d.javascad.models.Cube;
import eu.printingin3d.javascad.models.Sphere;
import eu.printingin3d.javascad.utils.Color;
import eu.printingin3d.javascad.vrl.CSG;
import eu.printingin3d.javascad.vrl.Polygon;

public class PolygonValidatorTest {

    @Test
    public void testV3dEquals() {
        V3d a1 = new V3d(0.9, -0.5, 0.0);
        V3d b1 = new V3d(0.9, -0.5, -0.0);

        assertEquals(a1, b1);
    }


    @Test
    public void testLineKey() {
        V3d a1 = new V3d(0, 0, 0);
        V3d b1 = new V3d(10, 10, 0);

        V3d a2 = new V3d(5, 5, 0);
        V3d b2 = new V3d(12, 12, 0);

        PolygonValidator.LineKey key1 = PolygonValidator.LineKey.fromSegment(a1, b1);
        PolygonValidator.LineKey key2 = PolygonValidator.LineKey.fromSegment(a2, b2);
        assertEquals(key1, key2);
    }

    @Test
    public void testLineKey2() {
        V3d a1 = new V3d(-8.6602540378444, -25.0, 25.0);
        V3d b1 = new V3d(-25.0, -15.566243270259356, 25.0);

        V3d a2 = new V3d(-25.0, -15.566243270259356, 25.0);
        V3d b2 = new V3d(-12.99038105676658, -22.5, 25.0);

        PolygonValidator.LineKey key1 = PolygonValidator.LineKey.fromSegment(a1, b1);
        PolygonValidator.LineKey key2 = PolygonValidator.LineKey.fromSegment(a2, b2);
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void testOppositeLineKey() {
        V3d a1 = new V3d(0, 0, 0);
        V3d b1 = new V3d(10, 10, 0);

        V3d a2 = new V3d(12, 12, 0);
        V3d b2 = new V3d(5, 5, 0);

        PolygonValidator.LineKey key1 = PolygonValidator.LineKey.fromSegment(a1, b1);
        PolygonValidator.LineKey key2 = PolygonValidator.LineKey.fromSegment(a2, b2);
        assertEquals(key1, key2);
    }

    @Test
    public void testLineKeysNotEquals() {
        V3d a1 = new V3d(0, 0, 0);
        V3d b1 = new V3d(10, 10, 0);

        V3d a2 = new V3d(5, 5.5, 0);
        V3d b2 = new V3d(12, 12, 0);

        PolygonValidator.LineKey key1 = PolygonValidator.LineKey.fromSegment(a1, b1);
        PolygonValidator.LineKey key2 = PolygonValidator.LineKey.fromSegment(a2, b2);
        assertNotEquals(key1, key2);
    }

    @Test
    public void testFindCommonEdges1() {
        ArrayList<Polygon> src = new ArrayList<>();
        src.add(createPolygon1());
        src.add(createPolygon2());

        Map<PolygonValidator.LineKey, List<PolygonValidator.PolygonEdge>> result =
            PolygonValidator.getCommonPolygons(src);

        assertEquals(5, result.size());
    }

    @Test
    public void testModel() {
        CSG model = createModel();
        List<Polygon> polygons = model.getPolygons();

        Map<PolygonValidator.LineKey, List<PolygonValidator.PolygonEdge>> result =
            PolygonValidator.getCommonPolygons(polygons);

        assertEquals(4, result.size());
    }


    @Test
    public void testFindNewPoints() {
        List<Polygon> polygons = new ArrayList<>();
        Polygon polygon1 =
            Polygon.fromPolygons(new V3d(0, 0, 0), new V3d(5, 5, 0), new V3d(10, 0, 0), Color.BLUE);
        Polygon polygon2 = Polygon.fromPolygons(
            new V3d(4, 0, 0),
            new V3d(9, -5, 0),
            new V3d(14, 0, 0),
            Color.BLUE
        );
        polygons.add(polygon1);
        polygons.add(polygon2);

        Map<PolygonValidator.LineKey, List<PolygonValidator.PolygonEdge>> edges =
            PolygonValidator.getCommonPolygons(polygons);
        List<PolygonValidator.PolygonEdge> commonPolygonsEdge = new ArrayList<>();

        for (Map.Entry<PolygonValidator.LineKey, List<PolygonValidator.PolygonEdge>> entry :
            edges.entrySet()) {
            if (entry.getValue().size() == 1) {
                continue;
            }
            commonPolygonsEdge = entry.getValue();
            break;
        }
        Map<Polygon, Set<PolygonValidator.PointInsert>> result =
            PolygonValidator.findNewPoints(commonPolygonsEdge);

        Set<PolygonValidator.PointInsert> newPointsPolygon1 = result.get(polygon1);
        Set<PolygonValidator.PointInsert> newPointsPolygon2 = result.get(polygon2);
        assertEquals(1, newPointsPolygon1.size());
        assertEquals(1, newPointsPolygon2.size());
        assertTrue(newPointsPolygon1.contains(new PolygonValidator.PointInsert(
            new V3d(4, 0, 0),
            2
        )));
        assertTrue(newPointsPolygon2.contains(new PolygonValidator.PointInsert(
            new V3d(10, 0, 0),
            2
        )));
    }

    @Test
    public void testSort() {
        Set<V3d> points = new HashSet<>();
        points.add(new V3d(10, 0, 0));
        points.add(new V3d(1, 0, 0));
        points.add(new V3d(4, 0, 0));
        points.add(new V3d(3, 0, 0));
        points.add(new V3d(2, 0, 0));

        V3d startPoint = new V3d(0, 0, 0);

        List<V3d> result = PolygonValidator.sortedPoints(startPoint, points);

        List<V3d> target = new ArrayList<>();
        target.add(new V3d(10, 0, 0));
        target.add(new V3d(4, 0, 0));
        target.add(new V3d(3, 0, 0));
        target.add(new V3d(2, 0, 0));
        target.add(new V3d(1, 0, 0));

        assertEquals(target, result);
    }

    @Test
    public void testInsert() {
        ArrayList<V3d> result = new ArrayList<>();
        result.add(new V3d(3, 3, 3));

        result.add(0, new V3d(2, 2, 2));
        result.add(0, new V3d(1, 1, 1));

        assertEquals(new V3d(1, 1, 1), result.get(0));
        assertEquals(new V3d(2, 2, 2), result.get(1));
        assertEquals(new V3d(3, 3, 3), result.get(2));
    }

    @Test
    public void testAddPolygonNewVertices() {
        List<Polygon> polygons = new ArrayList<>();
        Polygon polygon1 =
            Polygon.fromPolygons(
                new V3d(0, 0, 0),
                new V3d(5, 5, 0),
                new V3d(10, 0, 0), Color.BLUE
            );
        Polygon polygon2 = Polygon.fromPolygons(
            new V3d(4, 0, 0),
            new V3d(9, -5, 0),
            new V3d(14, 0, 0),
            Color.BLUE
        );
        polygons.add(polygon1);
        polygons.add(polygon2);

        Map<PolygonValidator.LineKey, List<PolygonValidator.PolygonEdge>> edges =
            PolygonValidator.getCommonPolygons(polygons);
        List<PolygonValidator.PolygonEdge> commonPolygonsEdge = new ArrayList<>();

        for (Map.Entry<PolygonValidator.LineKey, List<PolygonValidator.PolygonEdge>> entry :
            edges.entrySet()) {
            if (entry.getValue().size() == 1) {
                continue;
            }
            commonPolygonsEdge = entry.getValue();
            break;
        }
        Map<Polygon, Set<PolygonValidator.PointInsert>> result =
            PolygonValidator.findNewPoints(commonPolygonsEdge);

        List<Polygon> newPolygons = PolygonValidator.addPolygonNewVertices(result);

        ArrayList<V3d> valid1 = new ArrayList<>();
        valid1.add(new V3d(0, 0, 0));
        valid1.add(new V3d(5, 5, 0));
        valid1.add(new V3d(10, 0, 0));
        valid1.add(new V3d(4, 0, 0));

        assertEquals(valid1, newPolygons.get(0).getVertices());

        ArrayList<V3d> valid2 = new ArrayList<>();
        valid2.add(new V3d(4, 0, 0));
        valid2.add(new V3d(9, -5, 0));
        valid2.add(new V3d(14, 0, 0));
        valid2.add(new V3d(10, 0, 0));

        assertEquals(valid2, newPolygons.get(1).getVertices());
    }

    @Test
    public void testAddPolygonNewVertices2() {
        List<Polygon> polygons = new ArrayList<>();
        Polygon polygon1 =
            Polygon.fromPolygons(
                new V3d(0, 0, 0),
                new V3d(5, 5, 0),
                new V3d(10, 0, 0), Color.BLUE
            );
        Polygon polygon2 = Polygon.fromPolygons(
            new V3d(4, 0, 0),
            new V3d(9, -5, 0),
            new V3d(14, 0, 0),
            Color.BLUE
        );
        polygons.add(polygon1);
        polygons.add(polygon2);

        List<Polygon> newPolygons = PolygonValidator.fixPolygons(polygons);

        ArrayList<V3d> valid1 = new ArrayList<>();
        valid1.add(new V3d(0, 0, 0));
        valid1.add(new V3d(5, 5, 0));
        valid1.add(new V3d(10, 0, 0));
        valid1.add(new V3d(4, 0, 0));

        assertEquals(valid1, newPolygons.get(0).getVertices());

        ArrayList<V3d> valid2 = new ArrayList<>();
        valid2.add(new V3d(4, 0, 0));
        valid2.add(new V3d(9, -5, 0));
        valid2.add(new V3d(14, 0, 0));
        valid2.add(new V3d(10, 0, 0));

        assertEquals(valid2, newPolygons.get(1).getVertices());
    }


    @Test
    public void testPolygonNewVertices() {
        List<Polygon> result = PolygonValidator.fixPolygons(createPolygons());
        assertEquals(3, result.size());
        assertEquals(4, result.get(0).getVertices().size());
        assertEquals(5, result.get(1).getVertices().size());
        assertEquals(4, result.get(2).getVertices().size());
    }

    @Test
    public void testPolygonNewVerticesTriangulation() {
        List<Polygon> result = PolygonValidator.fixPolygons(createPolygons());

        List<Triangle3d> triangles = Triangulator.triangulate(result.get(0).getVertices());
        assertEquals(2, triangles.size());
    }

    private List<Polygon> filterTopPolygons(List<Polygon> src, double z) {
        ArrayList<Polygon> result = new ArrayList<>();

        for (Polygon p : src) {
            int count = 0;
            for (V3d v : p.getVertices()) {
                if (v.z == z) {
                    count++;
                }
            }
            if (count == p.getVertices().size()) {
                result.add(p);
            }
        }
        return result;
    }

    private List<Facet> triangulate(List<Polygon> polygons) {
        List<Facet> facetsFromPolygons = new ArrayList<>();
        for (Polygon p : polygons) {
            List<Triangle3d> triangles = Triangulator.triangulate(p.getVertices());
            for (Triangle3d t : triangles) {
                facetsFromPolygons.add(new Facet(t, p.getNormal(), p.getColor()));
            }
        }
        return  facetsFromPolygons;
    }


    private List<Polygon> createPolygons() {
        ArrayList<Polygon> src = new ArrayList<>();
        src.add(createPolygon1());
        src.add(createPolygon2());
        src.add(createPolygon3());
        return src;
    }

    private Polygon createPolygon1() {
        ArrayList<V3d> vertices1 = new ArrayList<>();
        vertices1.add(new V3d(-25.0, -15.566243270259356, 25.0));
        vertices1.add(new V3d(-25.0, -25.0, 25.0));
        vertices1.add(new V3d(-8.6602540378444, -25.0, 25.0));
        return Polygon.fromPolygons(vertices1, Color.WHITE);
    }

    private Polygon createPolygon2() {
        ArrayList<V3d> vertices2 = new ArrayList<>();
        vertices2.add(new V3d(-12.99038105676658, 12.5, 25.0));
        vertices2.add(new V3d(-25.0, 5.566243270259365, 25.0));
        vertices2.add(new V3d(-25.0, -15.566243270259356, 25.0));
        vertices2.add(new V3d(-12.99038105676658, -22.5, 25.0));
        return Polygon.fromPolygons(vertices2, Color.RED);
    }

    private Polygon createPolygon3() {
        ArrayList<V3d> vertices3 = new ArrayList<>();
        vertices3.add(new V3d(-12.99038105676658, -22.5, 25.0));
        vertices3.add(new V3d(-8.6602540378444, -25.0, 25.0));
        vertices3.add(new V3d(8.660254037844396, -25.0, 25.0));
        vertices3.add(new V3d(-12.99038105676658, -12.5, 25.0));
        return Polygon.fromPolygons(vertices3, Color.GREEN);
    }

    private CSG createModel() {
        Abstract3dModel cube = new Cube(50.0);
        Abstract3dModel sphere = new Sphere(25.0).move(0, 0, 40.0);
        Abstract3dModel model = cube.addModel(sphere);
        return model.toCSG();
    }
}
