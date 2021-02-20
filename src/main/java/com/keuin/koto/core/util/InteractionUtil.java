package com.keuin.koto.core.util;

import com.keuin.koto.core.waypoint.Pos3d;
import com.keuin.koto.core.waypoint.WorldedPos3d;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class InteractionUtil {
    public static String getEntityVoxelMapLocationString(Entity entity) {
        Objects.requireNonNull(entity);
        String name = entity.getEntityName();
        Vec3d pos = entity.getPos();
        String rawWorldName = entity.getEntityWorld().getRegistryKey().getValue().toString();
        String dimName = rawWorldName.startsWith("minecraft:") ? rawWorldName.substring("minecraft:".length()) : rawWorldName;
        return String.format("[x:%.0f, y:%.0f, z:%.0f, dim:%s]", pos.x, pos.y, pos.z, dimName);
    }

    public static String getPosVoxelMapLocationString(Pos3d pos3d) {
        return getPosVoxelMapLocationString(pos3d, "world");
    }

    public static String getPosVoxelMapLocationString(Pos3d pos3d, String world) {
        Objects.requireNonNull(pos3d);
        if (world != null)
            return String.format("[x:%.0f, y:%.0f, z:%.0f, dim:%s]", pos3d.getX(), pos3d.getY(), pos3d.getZ(), world);
        else
            return String.format("[x:%.0f, y:%.0f, z:%.0f]", pos3d.getX(), pos3d.getY(), pos3d.getZ());
    }

    public static String getPosVoxelMapLocationString(WorldedPos3d worldedPos3d) {
        return getPosVoxelMapLocationString(worldedPos3d, worldedPos3d.getWorld());
    }
}
