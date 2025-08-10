package com.github.grishberg.cad3d.util;

import eu.printingin3d.javascad.coords.Triangle3d;
import eu.printingin3d.javascad.coords.V3d;
import java.util.ArrayList;
import java.util.List;

/**
 * Адаптированный Earcut алгоритм для триангуляции 3D полигонов
 */
public final class Earcut3D {

    private Earcut3D() {
        // Utility class
    }

    /**
     * Триангуляция 3D полигона
     *
     * @param polygon Список точек V3d в 3D пространстве
     * @return Список треугольников (Triangle3d)
     */
    public static List<Triangle3d> triangulate(List<V3d> polygon) {
        if (polygon == null || polygon.size() < 3) {
            return new ArrayList<>();
        }

        // 1. Найти плоскость полигона и спроецировать точки на нее
        ProjectionInfo projectionInfo = findBestProjection(polygon);
        double[] projectedPoints = projectTo2D(polygon, projectionInfo);

        // 2. Применить оригинальный Earcut алгоритм
        List<Integer> indices = Earcut.earcut(projectedPoints, null, 2);

        // 3. Создать треугольники из индексов
        List<Triangle3d> triangles = new ArrayList<>();
        for (int i = 0; i < indices.size(); i += 3) {
            int i0 = indices.get(i);
            int i1 = indices.get(i + 1);
            int i2 = indices.get(i + 2);

            // Проверка на вырожденность треугольника
            if (!isDegenerate(polygon.get(i0), polygon.get(i1), polygon.get(i2))) {
                triangles.add(new Triangle3d(
                    polygon.get(i0),
                    polygon.get(i1),
                    polygon.get(i2)
                ));
            }
        }

        return triangles;
    }

    /**
     * Находит лучшую плоскость проекции для полигона
     */
    private static ProjectionInfo findBestProjection(List<V3d> polygon) {
        if (polygon.size() < 3) {
            return new ProjectionInfo(2, 0, 1); // XY plane по умолчанию
        }

        // Вычисляем нормаль к плоскости (приблизительно)
        V3d normal = calculateNormal(polygon);

        // Определяем, на какую плоскость проецировать (наибольшая компонента нормали)
        double absX = Math.abs(normal.x);
        double absY = Math.abs(normal.y);
        double absZ = Math.abs(normal.z);

        if (absX > absY && absX > absZ) {
            // Проекция на YZ плоскость
            return new ProjectionInfo(0, 1, 2); // X - нормаль, проецируем на YZ
        } else if (absY > absZ) {
            // Проекция на XZ плоскость
            return new ProjectionInfo(1, 0, 2); // Y - нормаль, проецируем на XZ
        } else {
            // Проекция на XY плоскость
            return new ProjectionInfo(2, 0, 1); // Z - нормаль, проецируем на XY
        }
    }

    /**
     * Вычисляет нормаль к плоскости полигона
     */
    private static V3d calculateNormal(List<V3d> polygon) {
        if (polygon.size() < 3) {
            return new V3d(0, 0, 1);
        }

        // Используем первые три точки для вычисления нормали
        V3d p0 = polygon.get(0);
        V3d p1 = polygon.get(1);
        V3d p2 = polygon.get(2);

        V3d v1 = new V3d(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z);
        V3d v2 = new V3d(p2.x - p0.x, p2.y - p0.y, p2.z - p0.z);

        V3d normal = new V3d(
            v1.y * v2.z - v1.z * v2.y,
            v1.z * v2.x - v1.x * v2.z,
            v1.x * v2.y - v1.y * v2.x
        );

        // Нормализуем
        double length = Math.sqrt(normal.x * normal.x + normal.y * normal.y + normal.z * normal.z);
        if (length > 1e-10) {
            return new V3d(normal.x / length, normal.y / length, normal.z / length);
        }

        return new V3d(0, 0, 1);
    }

    /**
     * Проекция 3D точек на 2D плоскость
     */
    private static double[] projectTo2D(List<V3d> polygon, ProjectionInfo projInfo) {
        double[] result = new double[polygon.size() * 2];

        for (int i = 0; i < polygon.size(); i++) {
            V3d point = polygon.get(i);
            result[i * 2] = getCoordinate(point, projInfo.coord1);
            result[i * 2 + 1] = getCoordinate(point, projInfo.coord2);
        }

        return result;
    }

    /**
     * Получение координаты точки по индексу
     */
    private static double getCoordinate(V3d point, int coordIndex) {
        switch (coordIndex) {
            case 0: return point.x;
            case 1: return point.y;
            case 2: return point.z;
            default: return 0;
        }
    }

    /**
     * Проверяет, является ли треугольник вырожденным
     */
    private static boolean isDegenerate(V3d a, V3d b, V3d c) {
        final double EPSILON = 1e-10;

        V3d ab = new V3d(b.x - a.x, b.y - a.y, b.z - a.z);
        V3d ac = new V3d(c.x - a.x, c.y - a.y, c.z - a.z);

        V3d cross = new V3d(
            ab.y * ac.z - ab.z * ac.y,
            ab.z * ac.x - ab.x * ac.z,
            ab.x * ac.y - ab.y * ac.x
        );

        double areaSquared = cross.x * cross.x + cross.y * cross.y + cross.z * cross.z;

        return areaSquared < EPSILON * EPSILON;
    }

    /**
     * Вспомогательный класс для хранения информации о проекции
     */
    private static class ProjectionInfo {
        final int normalCoord; // Индекс координаты нормали
        final int coord1;      // Первая координата для проекции
        final int coord2;      // Вторая координата для проекции

        ProjectionInfo(int normalCoord, int coord1, int coord2) {
            this.normalCoord = normalCoord;
            this.coord1 = coord1;
            this.coord2 = coord2;
        }
    }
}
