package com.github.grishberg.cad3d.ui;

import com.github.grishberg.cad3d.common.DebugCmd;
import com.github.grishberg.cad3d.util.BuffersContainer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.utils.Color;
import eu.printingin3d.javascad.vrl.Facet;
import eu.printingin3d.javascad.vrl.Polygon;

/**
 * Реализация DebugVisualizer для работы с MultipleObjectsRenderer
 */
public class DebugVisualizerImpl implements DebugVisualizer {
    private List<BuffersContainer> debugBuffers = new ArrayList<>();

    public DebugVisualizerImpl() {
    }

    @Override
    public void applyDebugVisualization(DebugCmd cmd) {
        cmd.render(this);
    }

    @Override
    public void clearVisualization() {
        debugBuffers.clear();
    }

	@Override
    public void drawDebugPolygon(List<V3d> vertices, double lineThickness, Color color) {
        if (vertices.size() < 2) return;

        // Создаем линии, соединяющие все точки полигона
        for (int i = 0; i < vertices.size(); i++) {
            V3d currentVertex = vertices.get(i);
            V3d nextVertex = vertices.get((i + 1) % vertices.size()); // Замыкаем полигон
            drawDebugLine(currentVertex, nextVertex, lineThickness, color);
        }
    }

    @Override
    public void drawDebugPolygon(
        List<V3d> vertices,
        double lineThickness,
        double pointThickness,
        Color lineColor,
        Color vertexColor
    ) {
        if (vertices.size() < 2) return;

        // Создаем линии, соединяющие все точки полигона
        for (int i = 0; i < vertices.size(); i++) {
            V3d currentVertex = vertices.get(i);
            V3d nextVertex = vertices.get((i + 1) % vertices.size()); // Замыкаем полигон

            drawDebugLine(currentVertex, nextVertex, lineThickness, lineColor);
            drawDebugPoint(currentVertex, pointThickness, vertexColor);
        }
    }

    @Override
    public void drawDebugPolygon(
        Polygon polygon,
        double lineThickness,
        double pointThickness,
        Color lineColor,
        Color vertexColor
    ) {
        List<V3d> vertices = polygon.getVertices();
        drawDebugPolygon(vertices, lineThickness, pointThickness, lineColor, vertexColor);
    }

    @Override
    public void drawDebugFacet(Facet facet, double lineThickness, double pointThickness, Color lineColor,
                               Color vertexColor) {
        List<V3d> vertices = facet.getTriangle().getPoints();

        drawDebugPolygon(vertices, lineThickness, pointThickness, lineColor, vertexColor);
    }

    @Override
    public void drawDebugLine(V3d p0, V3d p1, double thickness, Color color) {
        // Центр параллелепипеда — середина между p0 и p1
        V3d center = V3d.midPoint(p0, p1);
        double length = p1.subtract(p0).magnitude();
        double height = thickness;
        // Параллелепипед в локальных координатах (ось X — вдоль (length), Y/Z — по 1)
        List<V3d> localBox = new ArrayList<>();
        double hx = length / 2.0;
        double hy = thickness / 2.0;
        double hz = height / 2.0;
        // 8 вершин
        V3d v0 = new V3d(-hx, -hy, +hz); // перед-низ-лев
        V3d v1 = new V3d(+hx, -hy, +hz); // перед-низ-прав
        V3d v2 = new V3d(+hx, +hy, +hz); // перед-верх-прав
        V3d v3 = new V3d(-hx, +hy, +hz); // перед-верх-лев
        V3d v4 = new V3d(-hx, -hy, -hz); // зад-низ-лев
        V3d v5 = new V3d(+hx, -hy, -hz); // зад-низ-прав
        V3d v6 = new V3d(+hx, +hy, -hz); // зад-верх-прав
        V3d v7 = new V3d(-hx, +hy, -hz); // зад-верх-лев
        // Грани (по 2 треугольника на грань)
        // Передняя
        localBox.add(v0); localBox.add(v1); localBox.add(v2);
        localBox.add(v0); localBox.add(v2); localBox.add(v3);
        // Задняя
        localBox.add(v5); localBox.add(v4); localBox.add(v7);
        localBox.add(v5); localBox.add(v7); localBox.add(v6);
        // Левая
        localBox.add(v4); localBox.add(v0); localBox.add(v3);
        localBox.add(v4); localBox.add(v3); localBox.add(v7);
        // Правая
        localBox.add(v1); localBox.add(v5); localBox.add(v6);
        localBox.add(v1); localBox.add(v6); localBox.add(v2);
        // Нижняя
        localBox.add(v4); localBox.add(v5); localBox.add(v1);
        localBox.add(v4); localBox.add(v1); localBox.add(v0);
        // Верхняя
        localBox.add(v3); localBox.add(v2); localBox.add(v6);
        localBox.add(v3); localBox.add(v6); localBox.add(v7);
        // Ориентируем box вдоль направления p1-p0
        V3d dir = p1.subtract(p0).unit();
        V3d base = new V3d(1, 0, 0);
        List<V3d> rotatedBox = new ArrayList<>();
        if (Math.abs(dir.getX() - 1.0) < 1e-6 && Math.abs(dir.getY()) < 1e-6 && Math.abs(dir.getZ()) < 1e-6) {
            // Просто сдвигаем
            for (V3d v : localBox) {
                rotatedBox.add(v.add(center));
            }
        } else {
            V3d axis = base.cross(dir);
            double angle = Math.acos(base.dot(dir));
            if (axis.magnitude() < 1e-6) {
                axis = new V3d(0, 1, 0);
            } else {
                axis = axis.unit();
            }
            double ux = axis.getX(), uy = axis.getY(), uz = axis.getZ();
            double cosA = Math.cos(angle);
            double sinA = Math.sin(angle);
            double oneMinusCosA = 1 - cosA;
            double[][] rot = new double[3][3];
            rot[0][0] = cosA + ux * ux * oneMinusCosA;
            rot[0][1] = ux * uy * oneMinusCosA - uz * sinA;
            rot[0][2] = ux * uz * oneMinusCosA + uy * sinA;
            rot[1][0] = uy * ux * oneMinusCosA + uz * sinA;
            rot[1][1] = cosA + uy * uy * oneMinusCosA;
            rot[1][2] = uy * uz * oneMinusCosA - ux * sinA;
            rot[2][0] = uz * ux * oneMinusCosA - uy * sinA;
            rot[2][1] = uz * uy * oneMinusCosA + ux * sinA;
            rot[2][2] = cosA + uz * uz * oneMinusCosA;
            for (V3d v : localBox) {
                double x = v.getX(), y = v.getY(), z = v.getZ();
                double rx = rot[0][0] * x + rot[0][1] * y + rot[0][2] * z;
                double ry = rot[1][0] * x + rot[1][1] * y + rot[1][2] * z;
                double rz = rot[2][0] * x + rot[2][1] * y + rot[2][2] * z;
                rotatedBox.add(new V3d(rx, ry, rz).add(center));
            }
        }
        BuffersContainer buffer = createCubeBuffer(rotatedBox, color);
        debugBuffers.add(buffer);
    }

    @Override
    public void drawDebugPoint(V3d p, double thickness, Color color) {
        // Куб со стороной 1.5
        List<V3d> cubeVertices = createCubeVertices(p, thickness);
        BuffersContainer buffer = createCubeBuffer(cubeVertices, color);
        debugBuffers.add(buffer);
    }

    public List<BuffersContainer> getDebugBuffers() {
        return new ArrayList<>(debugBuffers);
    }

    /**
     * Создает буфер для треугольника
     */
    private BuffersContainer createTriangleBuffer(List<V3d> vertices, Color color) {
        // Создаем данные для треугольника (позиция + цвет)
        float[] vertexData = new float[vertices.size() * 7]; // X, Y, Z, R, G, B, A
        float[] normalData = new float[vertices.size() * 3]; // NX, NY, NZ

        int index = 0;
        int normalIndex = 0;

        // Вычисляем нормали для каждой тройки вершин (треугольника)
        for (int i = 0; i < vertices.size(); i += 3) {
            if (i + 2 < vertices.size()) {
                V3d v0 = vertices.get(i);
                V3d v1 = vertices.get(i + 1);
                V3d v2 = vertices.get(i + 2);

                V3d edge1 = v1.subtract(v0);
                V3d edge2 = v2.subtract(v0);
                V3d normal = edge1.cross(edge2).unit();

                // Добавляем вершины для этого треугольника
                for (int j = 0; j < 3; j++) {
                    V3d vertex = vertices.get(i + j);

                    // Позиция
                    vertexData[index++] = (float) vertex.getX();
                    vertexData[index++] = (float) vertex.getY();
                    vertexData[index++] = (float) vertex.getZ();

                    // Цвет
                    vertexData[index++] = (float) color.getRed() / 255.0f;
                    vertexData[index++] = (float) color.getGreen() / 255.0f;
                    vertexData[index++] = (float) color.getBlue() / 255.0f;
                    vertexData[index++] = (float) color.getAlpha() / 255.0f;

                    // Нормаль
                    normalData[normalIndex++] = (float) normal.getX();
                    normalData[normalIndex++] = (float) normal.getY();
                    normalData[normalIndex++] = (float) normal.getZ();
                }
            }
        }

        FloatBuffer vertexBuffer = createFloatBuffer(vertexData);
        FloatBuffer normalBuffer = createFloatBuffer(normalData);

        return new BuffersContainer(vertices.size(), vertexBuffer, normalBuffer);
    }

    /**
     * Создает буфер для куба
     */
    private BuffersContainer createCubeBuffer(List<V3d> vertices, Color color) {
        // Для куба используем тот же метод, что и для треугольников
        return createTriangleBuffer(vertices, color);
    }

    /**
     * Создает вершины для куба (треугольники)
     */
    private List<V3d> createCubeVertices(V3d center, double size) {
        List<V3d> vertices = new ArrayList<>();
        double halfSize = size / 2;

        // Определяем 8 вершин куба
        V3d v0 = new V3d(center.getX() - halfSize, center.getY() - halfSize, center.getZ() + halfSize); // перед-ниж-лев
        V3d v1 = new V3d(center.getX() + halfSize, center.getY() - halfSize, center.getZ() + halfSize); // перед-ниж-прав
        V3d v2 = new V3d(center.getX() + halfSize, center.getY() + halfSize, center.getZ() + halfSize); // перед-верх-прав
        V3d v3 = new V3d(center.getX() - halfSize, center.getY() + halfSize, center.getZ() + halfSize); // перед-верх-лев
        V3d v4 = new V3d(center.getX() - halfSize, center.getY() - halfSize, center.getZ() - halfSize); // зад-ниж-лев
        V3d v5 = new V3d(center.getX() + halfSize, center.getY() - halfSize, center.getZ() - halfSize); // зад-ниж-прав
        V3d v6 = new V3d(center.getX() + halfSize, center.getY() + halfSize, center.getZ() - halfSize); // зад-верх-прав
        V3d v7 = new V3d(center.getX() - halfSize, center.getY() + halfSize, center.getZ() - halfSize); // зад-верх-лев

        // Передняя грань (z = +halfSize)
        vertices.add(v0); vertices.add(v1); vertices.add(v2);
        vertices.add(v0); vertices.add(v2); vertices.add(v3);

        // Задняя грань (z = -halfSize)
        vertices.add(v5); vertices.add(v4); vertices.add(v7);
        vertices.add(v5); vertices.add(v7); vertices.add(v6);

        // Левая грань (x = -halfSize)
        vertices.add(v4); vertices.add(v0); vertices.add(v3);
        vertices.add(v4); vertices.add(v3); vertices.add(v7);

        // Правая грань (x = +halfSize)
        vertices.add(v1); vertices.add(v5); vertices.add(v6);
        vertices.add(v1); vertices.add(v6); vertices.add(v2);

        // Нижняя грань (y = -halfSize)
        vertices.add(v4); vertices.add(v5); vertices.add(v1);
        vertices.add(v4); vertices.add(v1); vertices.add(v0);

        // Верхняя грань (y = +halfSize)
        vertices.add(v3); vertices.add(v2); vertices.add(v6);
        vertices.add(v3); vertices.add(v6); vertices.add(v7);

        return vertices;
    }

    /**
     * Создает FloatBuffer из массива float
     */
    private FloatBuffer createFloatBuffer(float[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = bb.asFloatBuffer();
        buffer.put(data);
        buffer.position(0);
        return buffer;
    }

    /**
     * Создает буфер нормалей
     */
    private FloatBuffer createNormalBuffer(int vertexCount, V3d normal) {
        float[] normalData = new float[vertexCount * 3];
        int index = 0;
        for (int i = 0; i < vertexCount; i++) {
            normalData[index++] = (float) normal.getX();
            normalData[index++] = (float) normal.getY();
            normalData[index++] = (float) normal.getZ();
        }
        return createFloatBuffer(normalData);
    }
}
