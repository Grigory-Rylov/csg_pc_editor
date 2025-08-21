package eu.printingin3d.javascad.utils;

import eu.printingin3d.javascad.coords.Triangle3d;
import eu.printingin3d.javascad.coords.Triangulator;
import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.vrl.Facet;
import eu.printingin3d.javascad.vrl.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Утилита для исправления проблем в полигонах
 */
public class PolygonValidator {

    private static final double COLLINEAR_TOLERANCE = 1e-9;
    private static final double EPSILON = 1e-12;
    private static final double NEARBY_VERTICES_TOLERANCE = 1e-6;

    /**
     * Исправляет проблемы в полигонах: коллинеарные точки, близкие вершины, naked edges
     */
    public static List<Polygon> fixPolygons(List<Polygon> polygons) {
        Map<LineKey, List<PolygonEdge>> edges = PolygonValidator.getCommonPolygons(polygons);

        Map<Polygon, Set<PointInsert>> mergedPoints = new HashMap<>();

        for (Map.Entry<LineKey, List<PolygonEdge>> entry : edges.entrySet()) {
            Map<Polygon, Set<PointInsert>> newPoints = findNewPoints(entry.getValue());

            // Объединяем новые точки с общим результатом
            for (Map.Entry<Polygon, Set<PointInsert>> newEntry : newPoints.entrySet()) {
                Polygon polygon = newEntry.getKey();
                Set<PointInsert> points = newEntry.getValue();

                Set<PointInsert> dst = mergedPoints.computeIfAbsent(polygon, k -> new HashSet<>());
                dst.addAll(points);
            }
        }

        return addPolygonNewVertices(mergedPoints);
    }

    public static Map<LineKey, List<PolygonEdge>> getCommonPolygons(List<Polygon> polygons) {
        Map<LineKey, List<PolygonEdge>> map = new HashMap<>();

        int percent = 0;
        float percentF = 0f;

        int iter = 0;
        for (Polygon polygon : polygons) {
            List<V3d> vertices = polygon.getVertices();
            for (int i = 0; i < vertices.size(); i++) {
                V3d a = vertices.get(i);
                V3d b = vertices.get((i + 1) % vertices.size());
                LineKey key = LineKey.fromSegment(a, b);

                List<PolygonEdge> currentList = map.computeIfAbsent(key, k -> new ArrayList<>());
                currentList.add(new PolygonEdge(polygon, a, b, i));
            }

            percentF = ((float) iter / (float) polygons.size()) * 100f;
            int newPercent = (int) percentF;
            if (newPercent > percent) {
                System.out.println("Common edges: validator status: " + newPercent);
            }
            percent = newPercent;
            iter ++;
        }

        return map;
    }

    public static Map<Polygon, Set<PointInsert>> findNewPoints(List<PolygonEdge> polygons) {
        Map<Polygon, Set<PointInsert>> result = new HashMap<>();

        if (polygons.size() < 2) {
            result.put(polygons.get(0).polygon, Collections.emptySet());
            return result;
        }
        int percent = 0;
        float percentF = 0f;
        for (int i = 0; i < polygons.size() - 1; i++) {
            PolygonEdge currentPolygon = polygons.get(i);
            for (int j = i + 1; j < polygons.size(); j++) {
                PolygonEdge otherPolygon = polygons.get(j);
                V3d a1 = currentPolygon.p0;
                V3d b1 = currentPolygon.p1;

                V3d a2 = otherPolygon.p0;
                V3d b2 = otherPolygon.p1;

                // should insert points into current
                Set<PointInsert> currentPolygonToBeAdded =
                    result.computeIfAbsent(currentPolygon.polygon, k -> new HashSet<>());

                if (CrossEdgeValidator.isPointBetween(a2, a1, b1)) {
                    currentPolygonToBeAdded.add(new PointInsert(
                        a2,
                        currentPolygon.firstPointIndex
                    ));
                }
                if (CrossEdgeValidator.isPointBetween(b2, a1, b1)) {
                    currentPolygonToBeAdded.add(new PointInsert(
                        b2,
                        currentPolygon.firstPointIndex
                    ));
                }

                // should insert points into other
                Set<PointInsert> otherPolygonToBeAdded =
                    result.computeIfAbsent(otherPolygon.polygon, k -> new HashSet<>());
                if (CrossEdgeValidator.isPointBetween(a1, a2, b2)) {
                    otherPolygonToBeAdded.add(new PointInsert(a1, otherPolygon.firstPointIndex));
                }
                if (CrossEdgeValidator.isPointBetween(b1, a2, b2)) {
                    otherPolygonToBeAdded.add(new PointInsert(b1, otherPolygon.firstPointIndex));
                }
            }
            percentF = ((float) i / (float) polygons.size()) * 100f;
            int newPercent = (int) percentF;
            if (newPercent > percent) {
                System.out.println("Polygon validator findNewPoints status: " + newPercent);
            }
            percent = newPercent;
        }
        return result;
    }

    public static List<Polygon> addPolygonNewVertices(Map<Polygon, Set<PointInsert>> newVerticesInfo) {
        ArrayList<Polygon> polygons = new ArrayList<>();
        for (Map.Entry<Polygon, Set<PointInsert>> entry : newVerticesInfo.entrySet()) {
            ArrayList<V3d> currentPolygonVertices = new ArrayList<>(entry.getKey().getVertices());

            TreeMap<Integer, Set<V3d>> groupedPoints = new TreeMap<>(Collections.reverseOrder());

            // group vertices
            for (PointInsert pointInsert : entry.getValue()) {
                Set<V3d> pointsOfGroup =
                    groupedPoints.computeIfAbsent(pointInsert.position, k -> new HashSet<>());
                pointsOfGroup.add(pointInsert.point);
            }

            // Теперь итерация будет от большего ключа к меньшему
            for (Map.Entry<Integer, Set<V3d>> verticesEntry : groupedPoints.entrySet()) {
                Integer key = verticesEntry.getKey();
                Set<V3d> points = verticesEntry.getValue();
                List<V3d> sortedPoints = sortedPoints(currentPolygonVertices.get(key), points);

                if (key == currentPolygonVertices.size() - 1) {
                    for (int i = sortedPoints.size() - 1; i >= 0; i--) {
                        currentPolygonVertices.add(sortedPoints.get(i));
                    }
                } else {
                    for (V3d pointToBeAdded : sortedPoints) {
                        currentPolygonVertices.add(key + 1, pointToBeAdded);
                    }
                }
            }
            if (Polygon.isValid(currentPolygonVertices)) {
                polygons.add(Polygon.fromPolygons(
                    currentPolygonVertices,
                    entry.getKey().getColor()
                ));
            } else  {
                System.out.println("Invalid triangle");
            }
        }
        return polygons;
    }

    static List<V3d> sortedPoints(V3d startPoint, Set<V3d> newPoints) {
        List<V3d> sortedNewPoints = new ArrayList<>(newPoints);
        sortedNewPoints.sort(Comparator.comparingDouble(p -> -distanceSquared(startPoint, p)));
        return sortedNewPoints;
    }

    /**
     * Вычисляет квадрат расстояния между двумя точками (для избежания вычисления sqrt).
     */
    private static double distanceSquared(V3d a, V3d b) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double dz = b.z - a.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Исправляет naked edges путём конвертации в треугольники, исправления и обратной конвертации
     */
    private List<Polygon> fixNakedEdgesInPolygons(List<Polygon> polygons) {
        System.out.println(
            "PolygonValidator: конвертируем полигоны в треугольники для исправления naked edges");

        // Конвертируем полигоны в треугольники
        List<Facet> facets = new ArrayList<>();
        for (Polygon polygon : polygons) {
            try {
                List<Triangle3d> triangles =
                    Triangulator.triangulate(polygon.getVertices(), polygon.getNormal());
                for (Triangle3d triangle : triangles) {
                    facets.add(new Facet(triangle, polygon.getNormal(), polygon.getColor()));
                }
            } catch (Exception e) {
                System.out.println(
                    "PolygonValidator: не удалось триангулировать полигон: " + e.getMessage());
            }
        }

        System.out.println("PolygonValidator: получили " + facets.size() + " треугольников");

        // Исправляем naked edges через StlValidator
        List<Facet> repairedFacets = StlValidator.validateAndRepair(facets);
        System.out.println(
            "PolygonValidator: после исправления: " + repairedFacets.size() + " треугольников");

        // Конвертируем обратно в простые треугольные полигоны
        return convertFacetsToSimplePolygons(repairedFacets);
    }

    /**
     * Конвертирует треугольники в простые треугольные полигоны
     */
    private List<Polygon> convertFacetsToSimplePolygons(List<Facet> facets) {
        List<Polygon> result = new ArrayList<>();

        for (Facet facet : facets) {
            try {
                List<V3d> vertices = facet.getTriangle().getPoints();
                if (vertices.size() == 3) {
                    Polygon trianglePolygon =
                        new Polygon(vertices, facet.getNormal(), facet.getColor());
                    result.add(trianglePolygon);
                }
            } catch (Exception e) {
                System.out.println(
                    "PolygonValidator: не удалось создать треугольный полигон: " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * Детальный анализ висящих рёбер с возвратом структурированной информации
     */
    public List<PolygonNakedEdgeInfo> analyzeNakedEdges(List<Polygon> polygons) {
        // Конвертируем полигоны в треугольники для анализа
        List<Facet> facets = new ArrayList<>();
        Map<Facet, Polygon> facetToPolygon = new HashMap<>();

        for (Polygon polygon : polygons) {
            try {
                List<Triangle3d> triangles =
                    Triangulator.triangulate(polygon.getVertices(), polygon.getNormal());
                for (Triangle3d triangle : triangles) {
                    Facet facet = new Facet(triangle, polygon.getNormal(), polygon.getColor());
                    facets.add(facet);
                    facetToPolygon.put(facet, polygon);
                }
            } catch (Exception e) {
                System.out.println(
                    "PolygonValidator: ошибка триангуляции при анализе: " + e.getMessage());
            }
        }

        // Получаем информацию о naked edges
        List<StlValidator.NakedEdgeInfo> nakedEdgesInfo = StlValidator.analyzeNakedEdges(facets);

        // Преобразуем в информацию о полигонах
        List<PolygonNakedEdgeInfo> result = new ArrayList<>();

        for (StlValidator.NakedEdgeInfo edgeInfo : nakedEdgesInfo) {
            Polygon polygon = facetToPolygon.get(edgeInfo.getFacet());
            if (polygon != null) {
                PolygonNakedEdgeInfo polygonInfo = new PolygonNakedEdgeInfo(
                    polygon,
                    edgeInfo.getPointA(),
                    edgeInfo.getPointB(),
                    edgeInfo.getFacet(),
                    "Полигон с " + polygon.getVertices().size() + " вершинами имеет висящее ребро"
                );
                result.add(polygonInfo);
            }
        }

        return result;
    }

    /**
     * Информация о висящем ребре в полигоне
     */
    public static class PolygonNakedEdgeInfo {

        private final Polygon polygon;
        private final V3d nakedEdgeA;
        private final V3d nakedEdgeB;
        private final Facet facet;
        private final String description;

        public PolygonNakedEdgeInfo(
            Polygon polygon,
            V3d nakedEdgeA,
            V3d nakedEdgeB,
            Facet facet,
            String description
        ) {
            this.polygon = polygon;
            this.nakedEdgeA = nakedEdgeA;
            this.nakedEdgeB = nakedEdgeB;
            this.facet = facet;
            this.description = description;
        }

        public Polygon getPolygon() {
            return polygon;
        }

        public V3d getNakedEdgeA() {
            return nakedEdgeA;
        }

        public V3d getNakedEdgeB() {
            return nakedEdgeB;
        }

        public Facet getFacet() {
            return facet;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "PolygonNakedEdge[" + nakedEdgeA + " -> " + nakedEdgeB + "] в полигоне с " +
                polygon.getVertices().size() + " вершинами: " + description;
        }
    }

    /**
     * Информация о ребре
     */
    public static class EdgeInfo {

        private final Polygon polygon;
        private final int startIndex;
        private final V3d start;
        private final V3d end;

        public EdgeInfo(Polygon polygon, int startIndex, V3d start, V3d end) {
            this.polygon = polygon;
            this.startIndex = startIndex;
            this.start = start;
            this.end = end;
        }

        public Polygon getPolygon() {
            return polygon;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public V3d getStart() {
            return start;
        }

        public V3d getEnd() {
            return end;
        }
    }

    public static class LineKey {

        private final V3d pointOnLine;
        private final V3d directionUnitVector;

        private LineKey(V3d pointOnLine, V3d directionUnitVector) {
            this.pointOnLine = pointOnLine;
            this.directionUnitVector = directionUnitVector;
        }

        /**
         * Создает LineKey из двух точек, определяющих отрезок.
         *
         * @param p0 Первая точка отрезка.
         * @param p1 Вторая точка отрезка.
         * @return LineKey, представляющий прямую, проходящую через p0 и p1.
         * @throws IllegalArgumentException если p0 и p1 совпадают.
         */
        public static LineKey fromSegment(V3d p0, V3d p1) {
            if (p0.equals(p1)) {
                throw new IllegalArgumentException(
                    "Точки p0 и p1 не могут совпадать, так как не определяют прямую. " + p0 +
                        " - " + p1);
            }

            // Вычисляем направляющий вектор
            V3d directionVector = new V3d(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z);

            // Нормализуем направляющий вектор
            double length = Math.sqrt(directionVector.x * directionVector.x +
                directionVector.y * directionVector.y +
                directionVector.z * directionVector.z);

            if (length < EPSILON) {
                // Это условие уже проверено выше, но на всякий случай
                throw new IllegalArgumentException(
                    "Точки p0 и p1 совпадают (длина вектора близка к нулю).");
            }

            V3d unitDirection = new V3d(
                directionVector.x / length,
                directionVector.y / length,
                directionVector.z / length
            );

            // Вычисляем точку на прямой, ближайшую к началу координат (точка перпендикуляра из
            // (0,0,0) на прямую)
            // Параметр t для точки на прямой P(t) = p0 + t * (p1 - p0) ближайшей к (0,0,0):
            // t = - (p0 . (p1 - p0)) / |p1 - p0|^2
            double dotProduct =
                p0.x * directionVector.x + p0.y * directionVector.y + p0.z * directionVector.z;
            double t = -dotProduct / (length * length);

            V3d closestPoint = new V3d(
                p0.x + t * directionVector.x,
                p0.y + t * directionVector.y,
                p0.z + t * directionVector.z
            );

            // Для обеспечения канонического представления, убедимся, что направляющий вектор
            // имеет положительную компоненту X (или Y, если X=0, или Z, если X=Y=0)
            // Это предотвратит ситуацию, когда (p0,p1) и (p1,p0) дадут разные ключи.
            if (needsToFlipDirection(unitDirection)) {
                unitDirection = new V3d(-unitDirection.x, -unitDirection.y, -unitDirection.z);
                // Точка closestPoint остается той же, так как она определяет положение прямой
            }

            return new LineKey(closestPoint, unitDirection);
        }

        /**
         * Определяет, нужно ли инвертировать направляющий вектор для канонического представления.
         *
         * @param unitDirection Единичный направляющий вектор.
         * @return true, если вектор нужно инвертировать.
         */
        private static boolean needsToFlipDirection(V3d unitDirection) {
            // Стандартный способ: выбрать направление, при котором первая ненулевая компонента
            // положительна
            if (Math.abs(unitDirection.x) > EPSILON) {
                return unitDirection.x < 0;
            }
            if (Math.abs(unitDirection.y) > EPSILON) {
                return unitDirection.y < 0;
            }
            if (Math.abs(unitDirection.z) > EPSILON) {
                return unitDirection.z < 0;
            }
            // Все компоненты нулевые (не должно происходить для единичного вектора)
            return false;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LineKey lineKey = (LineKey) o;
            return pointOnLine.equals(lineKey.pointOnLine) &&
                directionUnitVector.equals(lineKey.directionUnitVector);
        }

        @Override
        public int hashCode() {
            // Используем Objects.hash для комбинирования хэш-кодов
            return Objects.hash(pointOnLine, directionUnitVector);
        }

        @Override
        public String toString() {
            return "LineKey{" +
                "pointOnLine=" + pointOnLine +
                ", directionUnitVector=" + directionUnitVector +
                '}';
        }

        // Геттеры (опционально, если нужно получить данные из ключа)
        public V3d getPointOnLine() {
            return pointOnLine; // Возвращаем копию, если V3d не иммутабельный
        }

        public V3d getDirectionUnitVector() {
            return directionUnitVector; // Возвращаем копию, если V3d не иммутабельный
        }
    }

    public static class PointInsert implements Comparable<PointInsert> {

        final V3d point;

        final int position;

        public PointInsert(V3d point, int position) {
            this.point = point;
            this.position = position;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PointInsert that = (PointInsert) o;
            return position == that.position && Objects.equals(point, that.point);
        }

        @Override
        public int hashCode() {
            return Objects.hash(point, position);
        }

        // Реализация метода compareTo для сортировки по position по УБЫВАНИЮ
        @Override
        public int compareTo(PointInsert other) {
            return Integer.compare(other.position, this.position);
        }
    }

    public static class PolygonEdge {

        public final Polygon polygon;

        public final V3d p0;
        public final V3d p1;
        public final int firstPointIndex;

        public PolygonEdge(Polygon polygon, V3d p0, V3d p1, int index) {
            this.polygon = polygon;
            this.p0 = p0;
            this.p1 = p1;
            firstPointIndex = index;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PolygonEdge that = (PolygonEdge) o;
            return firstPointIndex == that.firstPointIndex &&
                Objects.equals(polygon, that.polygon) && Objects.equals(p0, that.p0) &&
                Objects.equals(p1, that.p1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(polygon, p0, p1, firstPointIndex);
        }
    }
}
