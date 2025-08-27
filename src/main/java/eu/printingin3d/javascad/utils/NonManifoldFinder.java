package eu.printingin3d.javascad.utils;

import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.vrl.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


// Класс для поиска Non-manifold рёбер
public class NonManifoldFinder {

    // Главный метод для поиска проблемных рёбер
    public static List<Edge> findNonManifoldEdges(List<Polygon> polygons) {
        // 1. Собираем все рёбра и храним список полигонов, которым они принадлежат
        Map<Edge, List<Polygon>> edgePolygonsMap = new HashMap<>();

        var percent = 0;
        var percentF = 0f;
        int iter = 0;
        long startTime = System.currentTimeMillis();

        for (Polygon polygon : polygons) {
            List<V3d> vertices = polygon.getVertices();
            Edge[] edges = new Edge[vertices.size()];
            for (int i = 0; i < vertices.size(); i++) {
                int start = i;
                int end = (i + 1) % vertices.size();
                edges[i] = new Edge(vertices.get(start), vertices.get(end));
            }

            // Добавляем полигон в список для каждого ребра
            for (Edge edge : edges) {
                edgePolygonsMap.computeIfAbsent(edge, k -> new ArrayList<>()).add(polygon);

                // Проверяем, если ребро используется больше чем в 2 гранях
                List<Polygon> polygonList = edgePolygonsMap.get(edge);
                if (polygonList.size() > 2) {
                    StringBuilder errorMessage = new StringBuilder();
                    errorMessage.append("Ошибка: Ребро ")
                        .append(edge)
                        .append(" используется в ")
                        .append(polygonList.size())
                        .append(" гранях. Ожидается максимум 2. Полигоны: ");

                    // Добавляем информацию о каждом полигоне
                    for (int i = 0; i < polygonList.size(); i++) {
                        if (i > 0) {
                            errorMessage.append(", ");
                        }
                        errorMessage.append(polygonList.get(i).toString())
                            .append("\n");
                    }

                    System.err.println(errorMessage.toString());
                    // Можно также бросить исключение, если это критическая ошибка:
                    // throw new IllegalStateException(errorMessage.toString());
                }
            }

            percentF = ((float) iter / (float) polygons.size()) * 100f;
            int newPercent = Math.round(percentF);
            if (newPercent > percent) {
                long delta = System.currentTimeMillis() - startTime;
                startTime = System.currentTimeMillis();
                System.out.println(
                    "Progress = " + newPercent + ", cnt = " + iter + " time = " + delta);
            }
            percent = newPercent;
            iter++;
        }

        System.out.println("Check edges...");
        // 2. Ищем проблемные рёбра
        List<Edge> nonManifoldEdges = new ArrayList<>();

        for (Map.Entry<Edge, List<Polygon>> entry : edgePolygonsMap.entrySet()) {
            Edge edge = entry.getKey();
            List<Polygon> polygonList = entry.getValue();
            int usageCount = polygonList.size();

            if (usageCount > 2) {
                // Ребро используется более чем 2 гранями - это ошибка!
                StringBuilder detailMessage = new StringBuilder();
                detailMessage.append("Non-manifold edge (used by >2 faces): ")
                    .append(edge)
                    .append("\n");
                detailMessage.append("  Used by ").append(usageCount).append(" polygons:\n");

                for (int i = 0; i < polygonList.size(); i++) {
                    detailMessage.append("    Polygon ")
                        .append(i)
                        .append(": ")
                        .append(polygonList.get(i).toString())
                        .append("\n");
                }

                System.err.println(detailMessage);
                nonManifoldEdges.add(edge);
            } else if (usageCount == 1) {
                // Ребро используется только одной гранью - "висящее" ребро
                StringBuilder floatMessage = new StringBuilder();
                floatMessage.append("Floating edge (used by 1 face): ").append(edge).append("\n");
                floatMessage.append("  Used by polygon: ").append(polygonList.get(0).toString());

                System.out.println(floatMessage);
                nonManifoldEdges.add(edge);
            }
        }

        return nonManifoldEdges;
    }

    // Класс для представления ребра
    public static class Edge {

        public V3d p1, p2;

        public Edge(V3d p1, V3d p2) {
            // Всегда храним точки в одинаковом порядке для сравнения
            if (p1.hashCode() < p2.hashCode()) {
                this.p1 = p1;
                this.p2 = p2;
            } else {
                this.p1 = p2;
                this.p2 = p1;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Edge edge = (Edge) o;
            return Objects.equals(p1, edge.p1) && Objects.equals(p2, edge.p2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(p1, p2);
        }
    }
}
