package org.example;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import javax.swing.*;
import java.util.*;

public class Voronoi3D extends JFrame implements GLEventListener {
    private final GLU glu = new GLU();
    private List<Point2D> sites = new ArrayList<>();
    private Map<Point2D, List<Point2D>> cells = new HashMap<>();
    private float rotationAngle = 0;
    private static final float BOUNDARY = 15f;

    public Voronoi3D() {
        setTitle("Voronoi Polygons (XZ plane)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        generateSites(50);
        buildVoronoiCells();

        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        GLCanvas canvas = new GLCanvas(caps);
        canvas.addGLEventListener(this);

        getContentPane().add(canvas);
        new FPSAnimator(canvas, 60).start();
    }

    private void generateSites(int count) {
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            sites.add(new Point2D(
                rand.nextFloat() * BOUNDARY * 2 - BOUNDARY,
                rand.nextFloat() * BOUNDARY * 2 - BOUNDARY
            ));
        }
    }

    private void buildVoronoiCells() {
        List<Point2D> sitesCopy = new ArrayList<>(sites);
        for (Point2D site : sitesCopy) {
            List<Point2D> polygon = new ArrayList<>();

            // Для каждой точки находим ближайшие соседи и строим биссектрисы
            List<Point2D> neighbors = findNearestNeighbors(site, 5);
            for (Point2D neighbor : neighbors) {
                Point2D mid = new Point2D(
                    (site.x + neighbor.x) / 2,
                    (site.z + neighbor.z) / 2
                );

                // Перпендикулярный вектор к линии между точками
                float dx = neighbor.x - site.x;
                float dz = neighbor.z - site.z;
                Point2D edgePoint = new Point2D(
                    mid.x - dz,
                    mid.z + dx
                );

                polygon.add(edgePoint);
            }

            // Сортировка точек по углу для формирования выпуклого многоугольника
            polygon.sort((a, b) -> {
                double angleA = Math.atan2(a.z - site.z, a.x - site.x);
                double angleB = Math.atan2(b.z - site.z, b.x - site.x);
                return Double.compare(angleA, angleB);
            });

            cells.put(site, polygon);
        }
    }

    private List<Point2D> findNearestNeighbors(Point2D site, int count) {
        sites.sort(Comparator.comparingDouble(p -> distanceSq(p, site)));
        return sites.subList(1, Math.min(count + 1, sites.size()));
    }

    private double distanceSq(Point2D a, Point2D b) {
        return Math.pow(a.x - b.x, 2) + Math.pow(a.z - b.z, 2);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        gl.glEnable(GL2.GL_DEPTH_TEST);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        glu.gluLookAt(0, BOUNDARY, 0, 0, 0, 0, 0, 0, -1);
        gl.glRotatef(rotationAngle, 0, 1, 0);
        rotationAngle += 0.3f;

        // Отрисовка полигонов
        Random rand = new Random();
        for (Map.Entry<Point2D, List<Point2D>> entry : cells.entrySet()) {
            gl.glColor3f(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            gl.glBegin(GL2.GL_POLYGON);
            for (Point2D p : entry.getValue()) {
                gl.glVertex3f(p.x, 0, p.z);
            }
            gl.glEnd();
        }

        // Отрисовка исходных точек
        gl.glColor3f(1, 1, 1);
        gl.glPointSize(5);
        gl.glBegin(GL2.GL_POINTS);
        for (Point2D p : sites) {
            gl.glVertex3f(p.x, 0, p.z);
        }
        gl.glEnd();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45, (float) width / height, 1, 100);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Voronoi3D().setVisible(true));
    }

    static class Point2D {
        float x, z;
        Point2D(float x, float z) {
            this.x = x;
            this.z = z;
        }
    }
}