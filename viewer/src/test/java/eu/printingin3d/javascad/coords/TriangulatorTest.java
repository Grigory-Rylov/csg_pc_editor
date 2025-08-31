package eu.printingin3d.javascad.coords;

import static com.github.grishberg.javascad.Triangulator.isValidTriangle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import eu.printingin3d.javascad.vrl.Facet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TriangulatorTest {

    @Test
    public void testTriangulate6Pts() {
        List<V3d> vertices = Arrays.asList(
            new V3d(0, 0, 0),
            new V3d(0, 1, 0),
            new V3d(0, 2, 0),
            new V3d(0, 3, 0),
            new V3d(1, 3, 0),
            new V3d(1, 0, 0)
        );
        List<Triangle3d> result =
            Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Triangle should return one triangle", 4, result.size());
        for (Triangle3d triangle : result) {
            List<V3d> pts = triangle.getPoints();
            assertTrue(
                "Triangle must be convex",
                isValidTriangle(pts.get(0), pts.get(1), pts.get(2))
            );
        }
    }

    @Test
    public void testTriangulate6Pts2() {
        List<V3d> vertices = Arrays.asList(
            new V3d(0, 0, 0),
            new V3d(0, 1, 0),
            new V3d(3, 1, 0),
            new V3d(3, 0, 0),
            new V3d(2, 0, 0),
            new V3d(1, 0, 0)
        );
        List<Triangle3d> result =
            Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Triangle should return one triangle", 4, result.size());
        for (Triangle3d triangle : result) {
            List<V3d> pts = triangle.getPoints();
            assertTrue(
                "Triangle must be convex",
                isValidTriangle(pts.get(0), pts.get(1), pts.get(2))
            );
        }
    }

    @Test
    public void testTriangulate6PtsReverse() {
        List<V3d> vertices = Arrays.asList(
            new V3d(1, 0, 0),
            new V3d(1, 3, 0),
            new V3d(0, 3, 0),
            new V3d(0, 2, 0),
            new V3d(0, 1, 0),
            new V3d(0, 0, 0)
        );
        List<Triangle3d> result = Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Triangle should return one triangle", 4, result.size());
        for (Triangle3d triangle : result) {
            List<V3d> pts = triangle.getPoints();
            assertTrue(
                "Triangle must be convex",
                isValidTriangle(pts.get(0), pts.get(1), pts.get(2))
            );
        }
    }

    @Test
    public void testTriangulateTriangle() {
        List<V3d> vertices = Arrays.asList(
            new V3d(0, 0, 0),
            new V3d(1, 0, 0),
            new V3d(0, 1, 0)
        );
        List<Triangle3d> result = Triangulator.triangulate(vertices, normal(vertices));

        assertEquals("Triangle should return one triangle", 1, result.size());

        Triangle3d triangle = result.get(0);
        List<V3d> trianglePoints = triangle.getPoints();
        assertEquals("First vertex should match", vertices.get(0), trianglePoints.get(0));
        assertEquals("Second vertex should match", vertices.get(1), trianglePoints.get(1));
        assertEquals("Third vertex should match", vertices.get(2), trianglePoints.get(2));
    }

    @Test
    public void testTriangulateSquare() {
        List<V3d> vertices = Arrays.asList(
            new V3d(0, 0, 0),  // Нижний левый
            new V3d(1, 0, 0),  // Нижний правый
            new V3d(1, 1, 0),  // Верхний правый
            new V3d(0, 1, 0)   // Верхний левый
        );
        List<Triangle3d> result = Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Square should be triangulated into 2 triangles", 2, result.size());
        for (Triangle3d triangle : result) {
            List<V3d> pts = triangle.getPoints();
            assertTrue(
                "Triangle must be convex",
                isValidTriangle(pts.get(0), pts.get(1), pts.get(2))
            );
        }
    }

    @Test
    public void testTriangulatePentagon() {
        List<V3d> vertices = Arrays.asList(
            new V3d(0, 0, 0),
            new V3d(1, 0, 0),
            new V3d(1.5, 0.5, 0),
            new V3d(0.5, 1, 0),
            new V3d(-0.5, 0.5, 0)
        );
        List<Triangle3d> result = Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Pentagon should be triangulated into 3 triangles", 3, result.size());
        for (Triangle3d triangle : result) {
            List<V3d> pts = triangle.getPoints();
            assertTrue(
                "Triangle must be convex",
                isValidTriangle(pts.get(0), pts.get(1), pts.get(2))
            );
        }
    }

    @Test
    public void testTriangulateHexagon() {
        List<V3d> vertices = Arrays.asList(
            new V3d(1, 0, 0),
            new V3d(0.5, 0.866, 0),
            new V3d(-0.5, 0.866, 0),
            new V3d(-1, 0, 0),
            new V3d(-0.5, -0.866, 0),
            new V3d(0.5, -0.866, 0)
        );
        List<Triangle3d> result = Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Hexagon should be triangulated into 4 triangles", 4, result.size());
        for (Triangle3d triangle : result) {
            List<V3d> pts = triangle.getPoints();
            assertTrue(
                "Triangle must be convex",
                isValidTriangle(pts.get(0), pts.get(1), pts.get(2))
            );
        }
    }

    @Test
    public void testTriangulateConvexPolygon() {
        // Создаем правильный восьмиугольник
        List<V3d> vertices = new ArrayList<>();
        int numSides = 8;
        double radius = 1.0;
        for (int i = 0; i < numSides; i++) {
            double angle = 2 * Math.PI * i / numSides;
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);
            vertices.add(new V3d(x, y, 0));
        }
        List<Triangle3d> result = Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Octagon should be triangulated into 6 triangles", 6, result.size());
        for (Triangle3d triangle : result) {
            List<V3d> pts = triangle.getPoints();
            assertTrue(
                "Triangle must be convex",
                isValidTriangle(pts.get(0), pts.get(1), pts.get(2))
            );
        }
    }

    @Test
    public void testTriangulateRectangle() {
        List<V3d> vertices = Arrays.asList(
            new V3d(0, 0, 0),
            new V3d(2, 0, 0),
            new V3d(2, 1, 0),
            new V3d(0, 1, 0)
        );
        List<Triangle3d> result = Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Rectangle should be triangulated into 2 triangles", 2, result.size());
        for (Triangle3d triangle : result) {
            List<V3d> pts = triangle.getPoints();
            assertTrue(
                "Triangle must be convex",
                isValidTriangle(pts.get(0), pts.get(1), pts.get(2))
            );
        }
    }

    @Test
    public void testTriangulateCollinearVertices() {
        List<V3d> vertices = Arrays.asList(
            new V3d(0, 0, 0),
            new V3d(1, 0, 0),
            new V3d(2, 0, 0), // Коллинеарные точки
            new V3d(0, 1, 0)
        );
        List<Triangle3d> result = Triangulator.triangulate(vertices, normal(vertices));

        // Алгоритм должен обработать коллинеарные точки
        assertNotNull("Result should not be null", result);
    }

    /**
     * Вспомогательный метод для вычисления площади треугольника
     */
    private double calculateTriangleArea(Triangle3d triangle) {
        List<V3d> points = triangle.getPoints();
        V3d v1 = points.get(0);
        V3d v2 = points.get(1);
        V3d v3 = points.get(2);

        V3d edge1 = v2.subtract(v1);
        V3d edge2 = v3.subtract(v1);
        V3d cross = edge1.cross(edge2);

        return cross.magnitude() / 2.0;
    }

    @Test
    public void testTriangulateComplexPolygon() {
        // Звездообразный полигон
        List<V3d> vertices = Arrays.asList(
            new V3d(0, 2, 0),    // Верхняя точка
            new V3d(0.5, 0.5, 0),
            new V3d(2, 0, 0),    // Правая точка
            new V3d(0.5, -0.5, 0),
            new V3d(0, -2, 0),   // Нижняя точка
            new V3d(-0.5, -0.5, 0),
            new V3d(-2, 0, 0),   // Левая точка
            new V3d(-0.5, 0.5, 0)
        );
        List<Triangle3d> result = Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Star polygon should be triangulated into 6 triangles", 6, result.size());
        for (Triangle3d triangle : result) {
            List<V3d> pts = triangle.getPoints();
            assertTrue(
                "Triangle must be convex",
                isValidTriangle(pts.get(0), pts.get(1), pts.get(2))
            );
        }
    }

    @Test
    public void convexText() {
        V3d a = new V3d(0.0, 0.0, 0.0);
        V3d b = new V3d(2.0, 0.0, 0.0);
        V3d c = new V3d(2.0, 1.0, 0.0);

        assertTrue("Triangle must be convex", isValidTriangle(a, b, c));
    }

    @Test
    public void convexText2() {
        V3d a = new V3d(0.0, 0.0, 0.0);
        V3d c = new V3d(0.0, 1.0, 0.0);
        V3d b = new V3d(2.0, 1.0, 0.0);

        assertTrue("Triangle must be convex", isValidTriangle(a, b, c));
    }

    @Test
    public void trianglesTest() {
        List<V3d> vertices = Arrays.asList(
            new V3d(-25.0, -25.0, -25.0),
            new V3d(-12.5, -25.0, -25.0),
            new V3d(-12.5, -12.5, -25.0),
            new V3d(-12.5, 12.5, -25.0),
            new V3d(-12.5, 25.0, -25.0),
            new V3d(-25.0, 25.0, -25.0)
        );
        List<Triangle3d> result = Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Triangle should return one triangle", 4, result.size());
        for (Triangle3d triangle : result) {
            List<V3d> pts = triangle.getPoints();
            assertTrue(
                "Triangle must be convex",
                isValidTriangle(pts.get(0), pts.get(1), pts.get(2))
            );
        }

        checkTrianglesWithoutPoints(result, vertices);
    }

    @Test
    public void trianglesTest2() {
        List<V3d> vertices = Arrays.asList(
            new V3d(12.5, -25.0, 25.0),
            new V3d(12.5, -12.5, 25.0),
            new V3d(12.5, 12.5, 25.0),
            new V3d(12.5, 25.0, 25.0),
            new V3d(25.0, 25.0, 25.0),
            new V3d(25.0, -25.0, 25.0)
        );
        List<Triangle3d> result = Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Triangle should return one triangle", 4, result.size());
        for (Triangle3d triangle : result) {
            List<V3d> pts = triangle.getPoints();
            assertTrue(
                "Triangle must be convex",
                isValidTriangle(pts.get(0), pts.get(1), pts.get(2))
            );
        }

        checkTrianglesWithoutPoints(result, vertices);
    }

    @Test
    public void testOverflow() {
        List<V3d> vertices = Arrays.asList(
            new V3d(-25.0, -11.1, -25.0),
            new V3d(-25.0, -25.0, -25.0),
            new V3d(-25.0, -25.0, -3.3),
            new V3d(-25.0, -15.6, 25.0),
            new V3d(-25.0, 5.6, 25.0)
        );
        List<Triangle3d> result = Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Triangle should return one triangle", 3, result.size());
    }

    @Test
    public void testTriangulate4Pts() {
        List<V3d> vertices = Arrays.asList(
            new V3d(0, 10, 0),
            new V3d(0, 0, 0),
            new V3d(10, 0, 0),
            new V3d(5, 5, 0)
        );
        List<Triangle3d> result =
            Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Triangle should return one triangle", 2, result.size());
    }

    @Test
    public void testTriangulate7Pts() {
        List<V3d> vertices = Arrays.asList(
            new V3d(0, 10, 0),
            new V3d(5, 10, 0),
            new V3d(10, 10, 0),
            new V3d(10, 0, 0),
            new V3d(8, 2, 0),
            new V3d(5, 5, 0),
            new V3d(2, 8, 0)
        );
        List<Triangle3d> result =
            Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Triangle should return one triangle", vertices.size() - 2, result.size());
    }

    @Test
    public void testTriangulate2() {
        List<V3d> vertices = Arrays.asList(
            new V3d(-12.99038105676658, -22.5, 25.0),
            new V3d(-8.6602540378444, -25.0, 25.0),
            new V3d(-8.660254037844403, -15, 25.0),
            new V3d(8.660254037844396, -25.0, 25.0),
            new V3d(-12.99038105676658, -12.5, 25.0)
        );
        List<Triangle3d> result =
            Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Triangle should return one triangle", 3, result.size());

        checkTrianglesWithoutPoints(result, vertices);
    }

    @Test
    public void testTriangulate3() {
        List<V3d> vertices = Arrays.asList(
            new V3d(25.0, -17.955837819827096, -24.433756729740683),
            new V3d(25.0, -12.5, 21.25),
            new V3d(25.0, 12.5, 21.25),
            new V3d(25.0, -16.816243270259367, 21.25),
            new V3d(25.0, -25.0, -3.3012701892219276)
        );
        List<Triangle3d> result =
            Triangulator.triangulate(vertices, normal(vertices));
        assertEquals("Triangle should return one triangle", 3, result.size());

        checkTrianglesWithoutPoints(result, vertices);
    }

    @Test
    public void triangulatePolygon1() {
        List<V3d> vertices = Arrays.asList(
            new V3d(25.0, -17, -25),
            new V3d(25.0, 17, -25),
            new V3d(25.0, 25, -3),
            new V3d(25.0, 17, 25),
            new V3d(25.0, 12, 25),
            new V3d(25.0, -25, -25)
        );

        List<Triangle3d> result = Triangulator.triangulate(vertices, normal(vertices));
        checkTrianglesWithoutPoints(result, vertices);
    }

    private static void checkFacetsWithoutPoints(List<Facet> result, List<V3d> vertices) {
        // Проверка, что внутри треугольников нет других точек
        for (Facet triangle : result) {
            List<V3d> triPoints = triangle.getTriangle().getPoints();
            for (V3d vertex : vertices) {
                // Пропускаем вершины самого треугольника
                if (triPoints.contains(vertex)) {
                    continue;
                }
                assertFalse(
                    "No other points should be inside the triangle",
                    isPointInsideTriangle(
                        vertex,
                        triPoints.get(0),
                        triPoints.get(1),
                        triPoints.get(2)
                    )
                );
            }
        }
    }

    private static void checkTrianglesWithoutPoints(List<Triangle3d> result, List<V3d> vertices) {
        // Проверка, что внутри треугольников нет других точек
        for (Triangle3d triangle : result) {
            List<V3d> triPoints = triangle.getPoints();
            for (V3d vertex : vertices) {
                // Пропускаем вершины самого треугольника
                if (triPoints.contains(vertex)) {
                    continue;
                }
                V3d a = triPoints.get(0);
                V3d b = triPoints.get(1);
                V3d c = triPoints.get(2);
                boolean pointInsideTriangle = isPointInsideTriangle(vertex, a, b, c);
                assertFalse(
                    "No other points should be inside the triangle",
                    pointInsideTriangle
                );
            }
        }
    }

    /**
     * Проверяет, лежит ли точка P внутри треугольника ABC (включая границы).
     * Точка P должна лежать в плоскости треугольника.
     *
     * @param p точка для проверки
     * @param a первая вершина треугольника
     * @param b вторая вершина треугольника
     * @param c третья вершина треугольника
     * @return true, если точка лежит внутри или на границе треугольника, false в противном случае
     */
    private static boolean isPointInsideTriangle(V3d p, V3d a, V3d b, V3d c) {
        return isPointInTriangle(p, a, b, c, 1e-10);
    }

    /**
     * Проверяет, лежит ли точка P внутри треугольника ABC (включая границы), с заданной точностью.
     * Точка P должна лежать в плоскости треугольника.
     *
     * @param p точка для проверки
     * @param a первая вершина треугольника
     * @param b вторая вершина треугольника
     * @param c третья вершина треугольника
     * @param epsilon точность для сравнения
     * @return true, если точка лежит внутри или на границе треугольника, false в противном случае
     */
    private static boolean isPointInTriangle(V3d p, V3d a, V3d b, V3d c, double epsilon) {
        // Используем барицентрические координаты

        // Векторы треугольника
        V3d v0 = new V3d(c.x - a.x, c.y - a.y, c.z - a.z); // AC
        V3d v1 = new V3d(b.x - a.x, b.y - a.y, b.z - a.z); // AB
        V3d v2 = new V3d(p.x - a.x, p.y - a.y, p.z - a.z); // AP

        // Скалярные произведения
        double dot00 = v0.x * v0.x + v0.y * v0.y + v0.z * v0.z;
        double dot01 = v0.x * v1.x + v0.y * v1.y + v0.z * v1.z;
        double dot02 = v0.x * v2.x + v0.y * v2.y + v0.z * v2.z;
        double dot11 = v1.x * v1.x + v1.y * v1.y + v1.z * v1.z;
        double dot12 = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;

        // Вычисляем барицентрические координаты
        double denom = dot00 * dot11 - dot01 * dot01;

        // Проверка вырожденности треугольника
        if (Math.abs(denom) < epsilon) {
            return false; // Треугольник вырожден
        }

        double invDenom = 1.0 / denom;
        double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        double v = (dot00 * dot12 - dot01 * dot02) * invDenom;

        // Точка находится внутри или на границе, если:
        // u >= 0, v >= 0 и u + v <= 1
        return (u >= -epsilon) && (v >= -epsilon) && (u + v <= 1 + epsilon);
    }

    private static V3d normal(List<V3d> points) {
        V3d point1 = points.get(0);
        V3d point2 = points.get(1);
        V3d point3 = points.get(2);

        return normal(point1, point2, point3);
    }

    private static V3d normal(V3d point1, V3d point2, V3d point3) {
        V3d edge1 = point2.subtract(point1);
        V3d edge2 = point3.subtract(point1);
        return edge1.cross(edge2).unit();
    }
}
