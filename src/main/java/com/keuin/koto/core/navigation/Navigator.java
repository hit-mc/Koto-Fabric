package com.keuin.koto.core.navigation;

import com.keuin.koto.core.waypoint.Pos3d;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public class Navigator {
    public static Navigation navigateTo(Pos3d startPoint, Pos3d endPoint) {
        final double dX = endPoint.getX() - startPoint.getX();
        final double dY = endPoint.getY() - startPoint.getY();
        final double dZ = endPoint.getZ() - startPoint.getZ();
        final double distance = Math.sqrt(
                Math.pow(dX, 2)
                        + Math.pow(dY, 2)
                        + Math.pow(dZ, 2)
        );
        final double yaw = -Math.atan2(dX, dZ);
        return new Navigation() {
            @Override
            public double getDistance() {
                return distance;
            }

            @Override
            public double getYaw() {
                return yaw;
            }

            @Override
            public MutableText getRichText() {
                return new LiteralText("Distance: ")
                        .append(new LiteralText(String.format("%.0fm", distance)).formatted(Formatting.AQUA))
                        .append(" Yaw: ")
                        .append(new LiteralText(String.format("%.1f°", yaw * 180.0 / Math.PI)).formatted(Formatting.AQUA))
                        .append(".");
            }

            @Override
            public String toString() {
                return getRichText().getString();
            }
        };
    }
}
