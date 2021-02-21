package com.keuin.koto.core.util;

import com.keuin.koto.core.waypoint.Pos3d;
import com.keuin.koto.core.waypoint.WorldedPos3d;
import net.minecraft.entity.Entity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
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
        return getPosVoxelMapLocationString(pos3d, null);
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

    /**
     * Generate a colored directional navigating string.
     *
     * @param a     start point.
     * @param b     destination point.
     * @param withY whether to take Y axis into account.
     * @return the navigating string.
     */
    public static MutableText getColoredPositionDelta(Pos3d a, Pos3d b, boolean withY) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        final double threshold = 1f;

        double dX = b.getX() - a.getX();
        double dY = b.getY() - a.getY();
        double dZ = b.getZ() - a.getZ();

        if (dX < threshold && dX > -threshold)
            dX = 0f;
        if (dY < threshold && dY > -threshold)
            dY = 0f;
        if (dZ < threshold && dZ > -threshold)
            dZ = 0f;

        MutableText text = new LiteralText("X");
        if (dX < 0)
            text.append(new LiteralText("(" + Math.round(dX) + ")").formatted(Formatting.DARK_GREEN));
        else if (dX > 0)
            text.append(new LiteralText("(+" + Math.round(dX) + ")").formatted(Formatting.DARK_RED));
        else
            text.append(new LiteralText("(=)").formatted(Formatting.DARK_BLUE));

        if (withY) {
            text.append(" Y");
            if (dY < 0)
                text.append(new LiteralText("(" + Math.round(dY) + ")").formatted(Formatting.DARK_GREEN));
            else if (dY > 0)
                text.append(new LiteralText("(+" + Math.round(dY) + ")").formatted(Formatting.DARK_RED));
            else
                text.append(new LiteralText("(=)").formatted(Formatting.DARK_BLUE));
        }

        text.append(" Z");
        if (dZ < 0)
            text.append(new LiteralText("(" + Math.round(dZ) + ")").formatted(Formatting.DARK_GREEN));
        else if (dZ > 0)
            text.append(new LiteralText("(+" + Math.round(dZ) + ")").formatted(Formatting.DARK_RED));
        else
            text.append(new LiteralText("(=)").formatted(Formatting.DARK_BLUE));

        if (dX != 0 || dZ != 0) {
            text.append("  Seq: ");
            final int AXIS_X = 1;
            final int AXIS_Z = 2;
            List<Integer> list = new ArrayList<>(2);
            if (Math.abs(dX) >= Math.abs(dZ)) {
                list.add(AXIS_X);
                list.add(AXIS_Z);
            } else {
                list.add(AXIS_Z);
                list.add(AXIS_X);
            }

            // print directions in decremental order
            for (Integer direction : list) {
                switch (direction) {
                    case AXIS_X:
                        if (dX > 0)
                            text.append(new LiteralText("EAST").formatted(Formatting.RED));
                        else if (dX < 0)
                            text.append(new LiteralText("WEST").formatted(Formatting.BLUE));
                        text.append(" ");
                        break;
                    case AXIS_Z:
                        if (dZ > 0)
                            text.append(new LiteralText("SOUTH").formatted(Formatting.GOLD));
                        else if (dZ < 0)
                            text.append(new LiteralText("NORTH").formatted(Formatting.LIGHT_PURPLE));
                        text.append(" ");
                        break;
                    default:
                        break;
                }
            }
        }

        return text;
    }
}
