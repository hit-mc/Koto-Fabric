package com.keuin.koto.core.waypoint.manager;

import com.keuin.koto.core.waypoint.WayPoint;

import java.util.Collection;
import java.util.Map;

public interface WayPointManager {
    Collection<String> getWorlds();

    Collection<WayPoint> getWayPoints(String world);

    Map<String, WayPoint> getWayPointsMap(String world);

    boolean reload();
}
