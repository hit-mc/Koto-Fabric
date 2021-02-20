package com.keuin.koto.core.waypoint;

import net.minecraft.entity.Entity;

import java.util.Objects;

public interface WorldedPos3d extends Worlded, Pos3d {
    static WorldedPos3d ofEntity(Entity entity) {
        Objects.requireNonNull(entity);
        String world = entity.getEntityWorld().getRegistryKey().getValue().toString();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        return new WorldedPos3d() {
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
                return "";
            }

            @Override
            public String getDimension() {
                return world;
            }
        };
    }
}
