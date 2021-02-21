package com.keuin.koto.core.waypoint.manager;

import com.keuin.koto.core.waypoint.ParseErrorException;
import com.keuin.koto.core.waypoint.WayPoint;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class VoxelMapWayPointManager implements WayPointManager {

    private final Logger logger = Logger.getLogger(VoxelMapWayPointManager.class.getName());
    private final String voxelMapWayPointsFile;
    private Map<String, Map<String, WayPoint>> map = new ConcurrentHashMap<>();

    public VoxelMapWayPointManager(String voxelMapWayPointsFile) throws IOException {
        Objects.requireNonNull(voxelMapWayPointsFile);
        this.voxelMapWayPointsFile = voxelMapWayPointsFile;
        loadInto(voxelMapWayPointsFile, map);
    }

    private void loadInto(String voxelMapWayPointsFile, Map<String, Map<String, WayPoint>> destMap) throws IOException {
        try (FileReader fileReader = new FileReader(voxelMapWayPointsFile)) {
            try (BufferedReader reader = new BufferedReader(fileReader)) {
                for (String ln = reader.readLine(); ln != null; ln = reader.readLine()) {
                    if (!ln.endsWith("#")) // skip other lines
                        continue;
                    try {
                        // read all waypoints
                        WayPoint wayPoint = WayPoint.fromVoxelMapRecord(ln);
                        String worldName = wayPoint.getPosition().getWorld();
                        Map<String, WayPoint> collection = destMap.computeIfAbsent(worldName, coll -> new ConcurrentHashMap<>());
                        logger.fine("Add waypoint " + wayPoint);
                        collection.put(wayPoint.getName(), wayPoint);
                    } catch (ParseErrorException e) {
                        logger.warning("Cannot parse record line: " + e + " Skip.");
                    }
                }
            }
        }
    }

    @Override
    public Collection<String> getWorlds() {
        return Collections.unmodifiableCollection(map.keySet());
    }

    @Override
    public Collection<WayPoint> getWayPoints(String world) {
        return Collections.unmodifiableCollection(map.getOrDefault(world, Collections.emptyMap()).values());
    }

    @Override
    public Map<String, WayPoint> getWayPointsMap(String world) {
        return Collections.unmodifiableMap(map.getOrDefault(world, Collections.emptyMap()));
    }

    @Override
    public boolean reload() {
        try {
            Map<String, Map<String, WayPoint>> tmp = new ConcurrentHashMap<>();
            loadInto(voxelMapWayPointsFile, tmp);
            this.map = tmp; // switch to the new map
            return true;
        } catch (IOException e) {
            logger.severe("Failed to load: " + e);
            return false;
        }
    }

}
