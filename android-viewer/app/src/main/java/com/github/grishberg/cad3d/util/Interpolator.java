package com.github.grishberg.cad3d.util;

import eu.printingin3d.javascad.coords.V3d;
import java.util.ArrayList;
import java.util.List;

public class Interpolator {

    private static final int CP_LEN = 4;
    private final V3d[] controlPoints;

    public Interpolator(V3d[] controlPoints) {
        this.controlPoints = controlPoints;
    }

    public List<V3d> interpolate(int resolution) {
        List<V3d> result = new ArrayList<>();

        ArrayList<Point> wp = new ArrayList<>();
        ArrayList<Double> slopes = new ArrayList<>();

        ArrayList<Point> pts = new ArrayList<>(controlPoints.length);

        for (int i = 0; i < controlPoints.length; i++) {
            pts.add(new Point(controlPoints[i].x, controlPoints[i].z));
        }

        for (int i = 0; i < pts.size() - 1; i++) {
            if (pts.get(i + 1).x == pts.get(i).x) {
                slopes.add(-1.0);
            } else {
                slopes.add(((pts.get(i + 1).y - pts.get(i).y) /
                    (pts.get(i).x - pts.get(i + 1).x)));
            }
        }

        if (pts.get(pts.size() - 1).x == pts.get(pts.size() - 2).x) {
            slopes.add(-1.0);
        } else {
            slopes.add((pts.get(pts.size() - 1).y - pts.get(pts.size() - 2).y) /
                (pts.get(pts.size() - 2).x - pts.get(pts.size() - 1).x));
        }

        Point prev = new Point(Double.MAX_VALUE, Double.MAX_VALUE);

        for (int i = 0; i < pts.size() - 1; i++) {
            Point p1, p2;
            p1 = pts.get(i);
            p2 = pts.get(i + 1);

            if (p1.x == p2.x) {
                double inc = (p2.y - p1.y) / 100;
                if (p1.y <= p2.y) {
                    for (double j = p1.y; j <= p2.y; j += inc) {
                        wp.add(new Point(p1.x, j));
                    }
                } else {
                    for (double j = p1.y; j >= p2.y; j += inc) {
                        wp.add(new Point(p1.x, j));
                    }
                }
            } else {
                double slope1, slope2, x1, x2;
                if (prev.x == Double.MAX_VALUE) {
                    slope1 = slopes.get(i);
                } else {
                    slope1 = (pts.get(i).y - prev.y) / (prev.x - pts.get(i).x);
                }

                slope2 = slopes.get(i + 1);
                if (i + 2 < pts.size()) {
                    boolean pos1 = (pts.get(i + 1).x - pts.get(i).x) >= 0;
                    boolean pos2 = (pts.get(i + 2).x - pts.get(i + 1).x) >= 0;

                    if (pos2 != pos1) {
                        slope2 = -slopes.get(i + 1);
                    }
                }

                if (prev.x == Double.MAX_VALUE) {
                    x1 = ((p2.x - p1.x) / 4) + p1.x;
                } else {
                    double mult;
                    if (p1.x - prev.x > 0) {
                        mult = 1;
                    } else {
                        mult = -1;
                    }
                    x1 = p1.x + ((Math.abs(p2.x - p1.x) / 4) * mult);
                }

                x2 = (3 * ((p2.x - p1.x) / 4)) + p1.x;
                Point c0, c1, c2, c3;
                c0 = new Point(p1.x, p1.y);
                c1 = new Point(x1, (slope1 * (p1.x - x1) + p1.y));
                c2 = new Point(x2, (slope2 * (p2.x - x2) + p2.y));
                c3 = new Point(p2.x, p2.y);
                prev = c2;

                // increase increment for less weigh points!
                for (double t = 0; t <= 1; t += 1.0 / resolution) {
                    final double oneMinusT = 1 - t;
                    wp.add(new Point(
                        oneMinusT * (oneMinusT * (oneMinusT * c0.x + t * c1.x) +
                            t * (oneMinusT * c1.x + t * c2.x)) + t *
                            (oneMinusT * (oneMinusT * c1.x + t * c2.x) +
                                t * (oneMinusT * c2.x + t * c3.x)),
                        (oneMinusT * (oneMinusT * (oneMinusT * c0.y + t * c1.y) +
                            t * (oneMinusT * c1.y + t * c2.y)) + t *
                            (oneMinusT * (oneMinusT * c1.y + t * c2.y) +
                                t * (oneMinusT * c2.y + t * c3.y)))
                    ));
                }
            }
        }

        for (Point p : wp) {
            result.add(new V3d(p.x, controlPoints[0].y, p.y));
        }

        return result;
    }

    public static class CachedBicubicInterpolator {

        private double a00, a01, a02, a03;
        private double a10, a11, a12, a13;
        private double a20, a21, a22, a23;
        private double a30, a31, a32, a33;

        public void updateCoefficients(double[][] p) {
            a00 = p[1][1];
            a01 = -.5 * p[1][0] + .5 * p[1][2];
            a02 = p[1][0] - 2.5 * p[1][1] + 2 * p[1][2] - .5 * p[1][3];
            a03 = -.5 * p[1][0] + 1.5 * p[1][1] - 1.5 * p[1][2] + .5 * p[1][3];
            a10 = -.5 * p[0][1] + .5 * p[2][1];
            a11 = .25 * p[0][0] - .25 * p[0][2] - .25 * p[2][0] + .25 * p[2][2];
            a12 = -.5 * p[0][0] + 1.25 * p[0][1] - p[0][2] + .25 * p[0][3] + .5 * p[2][0] -
                1.25 * p[2][1] + p[2][2] - .25 * p[2][3];
            a13 = .25 * p[0][0] - .75 * p[0][1] + .75 * p[0][2] - .25 * p[0][3] - .25 * p[2][0] +
                .75 * p[2][1] - .75 * p[2][2] + .25 * p[2][3];
            a20 = p[0][1] - 2.5 * p[1][1] + 2 * p[2][1] - .5 * p[3][1];
            a21 =
                -.5 * p[0][0] + .5 * p[0][2] + 1.25 * p[1][0] - 1.25 * p[1][2] - p[2][0] + p[2][2] +
                    .25 * p[3][0] - .25 * p[3][2];
            a22 = p[0][0] - 2.5 * p[0][1] + 2 * p[0][2] - .5 * p[0][3] - 2.5 * p[1][0] +
                6.25 * p[1][1] - 5 * p[1][2] + 1.25 * p[1][3] + 2 * p[2][0] - 5 * p[2][1] +
                4 * p[2][2] - p[2][3] - .5 * p[3][0] + 1.25 * p[3][1] - p[3][2] + .25 * p[3][3];
            a23 = -.5 * p[0][0] + 1.5 * p[0][1] - 1.5 * p[0][2] + .5 * p[0][3] + 1.25 * p[1][0] -
                3.75 * p[1][1] + 3.75 * p[1][2] - 1.25 * p[1][3] - p[2][0] + 3 * p[2][1] -
                3 * p[2][2] + p[2][3] + .25 * p[3][0] - .75 * p[3][1] + .75 * p[3][2] -
                .25 * p[3][3];
            a30 = -.5 * p[0][1] + 1.5 * p[1][1] - 1.5 * p[2][1] + .5 * p[3][1];
            a31 = .25 * p[0][0] - .25 * p[0][2] - .75 * p[1][0] + .75 * p[1][2] + .75 * p[2][0] -
                .75 * p[2][2] - .25 * p[3][0] + .25 * p[3][2];
            a32 = -.5 * p[0][0] + 1.25 * p[0][1] - p[0][2] + .25 * p[0][3] + 1.5 * p[1][0] -
                3.75 * p[1][1] + 3 * p[1][2] - .75 * p[1][3] - 1.5 * p[2][0] + 3.75 * p[2][1] -
                3 * p[2][2] + .75 * p[2][3] + .5 * p[3][0] - 1.25 * p[3][1] + p[3][2] -
                .25 * p[3][3];
            a33 = .25 * p[0][0] - .75 * p[0][1] + .75 * p[0][2] - .25 * p[0][3] - .75 * p[1][0] +
                2.25 * p[1][1] - 2.25 * p[1][2] + .75 * p[1][3] + .75 * p[2][0] - 2.25 * p[2][1] +
                2.25 * p[2][2] - .75 * p[2][3] - .25 * p[3][0] + .75 * p[3][1] - .75 * p[3][2] +
                .25 * p[3][3];
        }

        public double getValue(double x, double y) {
            double x2 = x * x;
            double x3 = x2 * x;
            double y2 = y * y;
            double y3 = y2 * y;

            return (a00 + a01 * y + a02 * y2 + a03 * y3) +
                (a10 + a11 * y + a12 * y2 + a13 * y3) * x +
                (a20 + a21 * y + a22 * y2 + a23 * y3) * x2 +
                (a30 + a31 * y + a32 * y2 + a33 * y3) * x3;
        }
    }

    static class Point implements Comparable<Point> {

        double x, y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Point o) {
            if (this.x < o.x) {
                return -1;
            }
            if (this.x == o.x) {
                return 0;
            }
            return 1;
        }

        @Override
        public String toString() {
            return "{" + "xP=" + x + "\n, yP=" + y + "\n, " + '}';
        }
    }
}
