package com.keuin.koto.core.waypoint;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface WayPoint {
    static @NotNull WayPoint fromVoxelMapRecord(@NotNull String recordString) throws ParseErrorException {
        Objects.requireNonNull(recordString);
        if (recordString.isEmpty() || !recordString.endsWith("#") || recordString.length() < 3)
            throw new ParseErrorException("Bad VoxelMap record string: not ends with `#`.");
        recordString = recordString.substring(0, recordString.length() - 1); // remove suffix `#`
        String[] args = recordString.split(",");

        String name = null, world = null, dimension = null;
        Double x = null, y = null, z = null;

        for (String pair : args) {
            String[] kv = pair.split(":");
            if (kv.length == 1)
                kv = new String[]{kv[0], ""};
            if (kv.length != 2)
                throw new ParseErrorException("Bad key-value pair: `" + pair + "`");
            if ("name".equals(kv[0])) {
                name = kv[1];
            } else if ("world".equals(kv[0])) {
                world = kv[1];
            } else if ("dimensions".equals(kv[0])) {
                dimension = kv[1];
            } else if ("x".equals(kv[0])) {
                x = Double.parseDouble(kv[1]);
            } else if ("y".equals(kv[0])) {
                y = Double.parseDouble(kv[1]);
            } else if ("z".equals(kv[0])) {
                z = Double.parseDouble(kv[1]);
            }
        }
        List<String> missingAttr = new ArrayList<>();
        if (name == null)
            missingAttr.add("name");
        if (world == null)
            missingAttr.add("world");
        if (dimension == null)
            missingAttr.add("dimension");
        if (x == null)
            missingAttr.add("x");
        if (y == null)
            missingAttr.add("y");
        if (z == null)
            missingAttr.add("z");

        if (!missingAttr.isEmpty()
                || name == null || world == null || dimension == null || x == null || y == null || z == null) // remove IDE complaints
            throw new ParseErrorException("Insufficient attributes. Missing one(s): " + missingAttr);

        // stupid VoxelMap mitigation
        if ("the_nether".equals(dimension)) {
            x /= 8;
            z /= 8;
        }

        return new ConcreteWayPoint(name, world, dimension, x, y, z);
    }

    @NotNull String getName();

    @NotNull WorldedPos3d getPosition();
}
