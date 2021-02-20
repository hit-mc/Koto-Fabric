package com.keuin.koto.core.waypoint;

import java.util.Objects;

class ConcretePosition implements WorldedPos3d {

    private final double x;
    private final double y;
    private final double z;
    private final String world;
    private final String dimension;

    ConcretePosition(double x, double y, double z, String world, String dimension) {
        Objects.requireNonNull(world);
        Objects.requireNonNull(dimension);
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.dimension = dimension;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public String getWorld() {
        return world;
    }

    @Override
    public String getDimension() {
        return dimension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConcretePosition that = (ConcretePosition) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0 &&
                Double.compare(that.z, z) == 0 &&
                Objects.equals(world, that.world) &&
                Objects.equals(dimension, that.dimension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, world, dimension);
    }

    @Override
    public String toString() {
        return "ConcretePosition{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", world='" + world + '\'' +
                ", dimension='" + dimension + '\'' +
                '}';
    }
}
