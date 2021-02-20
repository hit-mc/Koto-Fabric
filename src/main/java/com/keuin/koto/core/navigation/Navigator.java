package com.keuin.koto.core.navigation;

import com.keuin.koto.core.waypoint.Pos3d;

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
            public String toString() {
                return String.format("Distance: %.0fm. Yaw: %.1f°.", distance, yaw * 180.0 / Math.PI);
            }
        };
    }
}
