package org.example;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class VoronoiDiagram extends JFrame implements GLEventListener {
   private final List<Point> sites = new ArrayList<>();
   private final int width = 800;
   private final int height = 600;
   private GLU glu = new GLU();
   private final double EPSILON = 2.0; // Порог для определения границ

   public VoronoiDiagram() {
      super("Voronoi Diagram Borders");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(width, height);

      // Генерация случайных точек
      Random rand = new Random();
      for (int i = 0; i < 50; i++) {
         sites.add(new Point(rand.nextInt(width), rand.nextInt(height)));
      }

      // Настройка OpenGL
      GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
      GLCanvas canvas = new GLCanvas(caps);
      canvas.addGLEventListener(this);

      canvas.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            sites.add(new Point(e.getX(), height - e.getY()));
            canvas.display();
         }
      });

      add(canvas, BorderLayout.CENTER);
      setVisible(true);
   }

   @Override
   public void init(GLAutoDrawable drawable) {
      GL2 gl = drawable.getGL().getGL2();
      gl.glClearColor(1, 1, 1, 1);
   }

   @Override
   public void display(GLAutoDrawable drawable) {
      GL2 gl = drawable.getGL().getGL2();
      gl.glClear(GL.GL_COLOR_BUFFER_BIT);
      gl.glLoadIdentity();
      glu.gluOrtho2D(0, width, 0, height);

      // Отрисовка границ
      drawVoronoiBorders(gl);

      gl.glFlush();
   }

   private void drawVoronoiBorders(GL2 gl) {
      gl.glColor3f(0, 0, 0); // Черный цвет границ
      gl.glBegin(GL.GL_POINTS);

      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            if (isBorderPixel(x, y)) {
               gl.glVertex2i(x, y);
            }
         }
      }
      gl.glEnd();
   }

   private boolean isBorderPixel(int x, int y) {
      // Проверяем соседние пиксели
      return isDifferentSite(x, y, x+1, y) ||
          isDifferentSite(x, y, x-1, y) ||
          isDifferentSite(x, y, x, y+1) ||
          isDifferentSite(x, y, x, y-1);
   }

   private boolean isDifferentSite(int x1, int y1, int x2, int y2) {
      if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height) return false;

      Point p1 = findClosestSite(x1, y1);
      Point p2 = findClosestSite(x2, y2);

      return p1 != p2 &&
          Math.abs(distance(x1, y1, p1) - distance(x1, y1, p2)) < EPSILON;
   }

   private Point findClosestSite(int x, int y) {
      Point closest = null;
      double minDist = Double.MAX_VALUE;
      for (Point site : sites) {
         double dist = distance(x, y, site);
         if (dist < minDist) {
            minDist = dist;
            closest = site;
         }
      }
      return closest;
   }

   private double distance(int x, int y, Point site) {
      return Math.hypot(x - site.x, y - site.y);
   }

   @Override
   public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}

   @Override
   public void dispose(GLAutoDrawable drawable) {}

   static class Point {
      int x, y;
      public Point(int x, int y) {
         this.x = x;
         this.y = y;
      }
   }

   public static void main(String[] args) {
      new VoronoiDiagram();
   }
}
