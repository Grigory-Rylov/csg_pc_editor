package eu.printingin3d.javascad.coords;

import java.util.ArrayList;
import java.util.List;

public class Triangulator {

    public static List<Triangle3d> triangulate(List<V3d> vertices) {
        List<Triangle3d> result = new ArrayList<>();
        if (vertices == null || vertices.size() < 3) {
            return result;
        }

        int maxIterations = vertices.size() * 2;
        boolean[] disallowedIndices = new boolean[vertices.size()];

        int start = 0;
        int end = start + 2;

        while (end < maxIterations) {
            int modEnd = end % vertices.size();

            V3d a = vertices.get(start);
            V3d b = vertices.get((start + 1) % vertices.size());
            V3d c = vertices.get(modEnd);

            if (disallowedIndices[modEnd] || !isValidTriangle(a, b, c)) {
                end++;
            } else {
                for (int i = start; i < end - 1; i++) {
                    a = vertices.get(i % vertices.size());
                    b = vertices.get((i + 1) % vertices.size());

                    if (isValidTriangle(a, b, c)) {
                        result.add(new Triangle3d(a, b, c));
                        disallowedIndices[(i + 1) % vertices.size()] = true;
                    }
                    if (result.size() == vertices.size() - 2) {
                        return result;
                    }
                }
                start = modEnd;
            }

            if (result.size() == vertices.size() - 2) {
                break;
            }

        }
        return result;
    }

    /**
     * Проверяет, является ли угол выпуклым
     *
     * @param prev предыдущая вершина
     * @param curr текущая вершина
     * @param next следующая вершина
     * @return true, если угол выпуклый
     */
    static boolean isValidTriangle1(V3d prev, V3d curr, V3d next) {
        V3d edge1 = prev.subtract(curr);
        V3d edge2 = next.subtract(curr);
        V3d cross = edge1.cross(edge2);

        // Предполагаем, что полигон лежит в плоскости XY
        // Для правильной ориентации Z-компонента должна быть положительной
        return cross.getZ() > 0;
    }

    /**
     * Проверяет, являются ли три точки вершинами правильного треугольника.
     *
     * @param a первая точка
     * @param b вторая точка
     * @param c третья точка
     * @return true, если точки образуют правильный треугольник, false иначе
     */
    static boolean isValidTriangle(V3d a, V3d b, V3d c) {
        // Сначала проверяем, что точки не лежат на одной прямой
        if (areCollinear(a, b, c)) {
            return false;
        }

        return true;
    }

    /**
     * Проверяет, лежат ли три точки на одной прямой (коллинеарны).
     */
    private static boolean areCollinear(V3d a, V3d b, V3d c) {
        // Вектор AB
        double abX = b.x - a.x;
        double abY = b.y - a.y;
        double abZ = b.z - a.z;

        // Вектор AC
        double acX = c.x - a.x;
        double acY = c.y - a.y;
        double acZ = c.z - a.z;

        // Векторное произведение AB × AC
        double crossX = abY * acZ - abZ * acY;
        double crossY = abZ * acX - abX * acZ;
        double crossZ = abX * acY - abY * acX;

        // Если векторное произведение близко к нулю, точки коллинеарны
        final double EPSILON = 1e-10;
        return Math.abs(crossX) < EPSILON &&
            Math.abs(crossY) < EPSILON &&
            Math.abs(crossZ) < EPSILON;
    }

    /**
     * Вычисляет расстояние между двумя точками.
     */
    private static double distance(V3d p1, V3d p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double dz = p2.z - p1.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
