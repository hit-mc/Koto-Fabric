package com.keuin.koto.core.waypoint;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

class ConcreteWayPoint implements WayPoint {

    private final String name;
    private final WorldedPos3d position;

    ConcreteWayPoint(String name, String world, String dimension, double x, double y, double z) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(world);
        Objects.requireNonNull(dimension);
        this.name = name;
        this.position = new ConcretePosition(x, y, z, world, dimension);
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull WorldedPos3d getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConcreteWayPoint that = (ConcreteWayPoint) o;
        return name.equals(that.name) &&
                position.equals(that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, position);
    }

    @Override
    public String toString() {
        return "ConcreteWayPoint{" +
                "name='" + name + '\'' +
                ", position=" + position +
                '}';
    }
}
