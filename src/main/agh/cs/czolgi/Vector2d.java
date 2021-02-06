package agh.cs.czolgi;

public class Vector2d {
    public final int x;
    public final int y;

    public Vector2d(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public boolean precedes(Vector2d other) {
        return this.x <= other.x && this.y <= other.y;
    }

    public boolean follows(Vector2d other) {
        return this.x >= other.x && this.y >= other.y;
    }

    public Vector2d upperRight(Vector2d other) {
        int xmax = Math.max(this.x, other.x);
        int ymax = Math.max(this.y, other.y);
        return new Vector2d(xmax, ymax);
    }

    public Vector2d lowerLeft(Vector2d other) {
        int xmin = Math.min(this.x, other.x);
        int ymin = Math.min(this.y, other.y);
        return new Vector2d(xmin, ymin);
    }

    public Vector2d add(Vector2d other) {
        return new Vector2d(this.x + other.x, this.y + other.y);
    }

    public Vector2d subtract(Vector2d other) {
        return new Vector2d(this.x - other.x, this.y - other.y);
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Vector2d))
            return false;
        Vector2d that = (Vector2d) other;
        return this.x == that.x && this.y == that.y;
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash += this.x * 31;
        hash += this.y * 17;
        return hash;
    }

    public Vector2d opposite() {
        return new Vector2d(-this.x, -this.y);
    }

    public double getAngleTo(Vector2d pos) {
        double angle = Math.atan2(pos.y - this.y, pos.x - this.x);
        if (angle < 0) {
            angle += 2*Math.PI;
        }
        return angle;
    }

    public double calculateDistanceTo(Vector2d pos) {
        return Math.sqrt(Math.pow(this.x - pos.x, 2) + Math.pow(this.y - pos.y, 2));
    }
}
