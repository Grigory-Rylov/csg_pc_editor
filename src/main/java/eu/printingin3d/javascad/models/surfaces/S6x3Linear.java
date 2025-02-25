package eu.printingin3d.javascad.models.surfaces;

import eu.printingin3d.javascad.coords.V3d;

public class S6x3Linear {
   private final V3d[][] controlPoints;
   private final int resolution;

   public S6x3Linear(V3d[][] points, int resolution) {
      this.controlPoints = points;
      this.resolution = resolution;
   }

   public V3d[][] buildSurface() {
      int rows = controlPoints.length;
      int cols = controlPoints[0].length;

      int outRows = (rows - 1) * resolution + 1;
      int outCols = (cols - 1) * resolution + 1;
      V3d[][] surface = new V3d[outRows][outCols];

      for (int i = 0; i < outRows; i++) {
         double v = (double) i / (outRows - 1);
         for (int j = 0; j < outCols; j++) {
            double u = (double) j / (outCols - 1);

            if (isBoundary(i, outRows) || isBoundary(j, outCols)) {
               surface[i][j] = safeLinearInterpolation(u, v);
            } else {
               surface[i][j] = bezierInterpolation(u, v);
            }
         }
      }
      return surface;
   }

   private boolean isBoundary(int index, int maxIndex) {
      return index == 0 || index == maxIndex - 1;
   }

   private V3d safeLinearInterpolation(double u, double v) {
      int maxRow = controlPoints.length - 1;
      int maxCol = controlPoints[0].length - 1;

      // Ограничиваем индексы в пределах массива
      int row = Math.min(maxRow - 1, (int)(v * maxRow));
      int col = Math.min(maxCol - 1, (int)(u * maxCol));

      double fracV = v * maxRow - row;
      double fracU = u * maxCol - col;

      // Корректируем коэффициенты для крайних точек
      fracV = Math.max(0, Math.min(1, fracV));
      fracU = Math.max(0, Math.min(1, fracU));

      V3d p1 = controlPoints[row][col];
      V3d p2 = controlPoints[row][col + 1];
      V3d p3 = controlPoints[row + 1][col];
      V3d p4 = controlPoints[row + 1][col + 1];

      V3d top = lerp(p1, p2, fracU);
      V3d bottom = lerp(p3, p4, fracU);
      return lerp(top, bottom, fracV);
   }

   // Остальные методы без изменений
   private V3d bezierInterpolation(double u, double v) {
      V3d[] uCurve = new V3d[controlPoints.length];
      for (int i = 0; i < controlPoints.length; i++) {
         uCurve[i] = bezierPoint(u, controlPoints[i]);
      }
      return bezierPoint(v, uCurve);
   }

   private V3d lerp(V3d a, V3d b, double t) {
      return new V3d(
          a.getX() + t*(b.getX()-a.getX()),
          a.getY() + t*(b.getY()-a.getY()),
          a.getZ() + t*(b.getZ()-a.getZ())
      );
   }

   private static V3d bezierPoint(double t, V3d... points) {
      int n = points.length - 1;
      double x = 0, y = 0, z = 0;

      for (int i = 0; i <= n; i++) {
         double blend = binomialCoefficient(n, i) * Math.pow(1-t, n-i) * Math.pow(t, i);
         x += points[i].getX() * blend;
         y += points[i].getY() * blend;
         z += points[i].getZ() * blend;
      }
      return new V3d(x, y, z);
   }

   private static int binomialCoefficient(int n, int k) {
      if (k < 0 || k > n) return 0;
      if (k == 0 || k == n) return 1;
      return binomialCoefficient(n-1, k-1) + binomialCoefficient(n-1, k);
   }

   public static S6x3Linear create(V3d[][] controlPoints, int resolution) {
      return new S6x3Linear(controlPoints, resolution);
   }
}
