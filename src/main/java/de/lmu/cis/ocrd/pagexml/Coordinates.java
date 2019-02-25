package de.lmu.cis.ocrd.pagexml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Coordinates {
    public static class Point {
        private final int x, y;
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public int getX() {return x;}
        public int getY() {return y;}
        @Override
        public String toString() {
            return String.format("%d,%d", x, y);
        }
    }

    private final List<Point> points;

    public Coordinates() {
        this.points = new ArrayList<>();
    }

    public Coordinates(List<Point> points) {
        this.points = points;
    }

    public int getMinX() {
        int min = Integer.MAX_VALUE;
        for (Point point: points) {
            if (point.getX() < min) {
                min = point.getX();
            }
        }
        return min;
    }

    public int getMinY() {
        int min = Integer.MAX_VALUE;
        for (Point point: points) {
            if (point.getY() < min) {
                min = point.getY();
            }
        }
        return min;
    }

    public int getMaxX() {
        int max = 0;
        for (Point point: points) {
            if (point.getX() > max) {
                max = point.getX();
            }
        }
        return max;
    }

    public int getMaxY() {
        int max = 0;
        for (Point point: points) {
            if (point.getY() > max) {
                max = point.getY();
            }
        }
        return max;
    }

    @Override
    public String toString() {
        return String.join(" ", points.stream().toString());
    }

    public static Coordinates fromCoordinates(List<Coordinates> coordinates) {
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        for (Coordinates c: coordinates) {
            if (c.getMinX() < minX) {
                minX = c.getMinX();
            }
            if (c.getMinY() < minY) {
                minY = c.getMinY();
            }
            if (c.getMaxX() > maxX) {
                maxX = c.getMaxX();
            }
            if (c.getMaxY() > maxY) {
                maxY = c.getMaxY();
            }
        }
        Point[] xy = {new Point(minX,minY), new Point(maxX, maxY)};
        return new Coordinates(Arrays.asList(xy));
    }

    public static Coordinates fromString(String str) throws Exception {
        String[] strPoints = str.split("\\s+");
        List<Point> points = new ArrayList<>();
        for (String strPoint: strPoints) {
            String []coords = strPoint.split(",");
            if (coords.length != 2) {
                throw new Exception("invalid point: " + strPoint);
            }
            points.add(new Point((int)Float.parseFloat(coords[0]), (int)Float.parseFloat(coords[1])));
        }
        return new Coordinates(points);
    }
}
