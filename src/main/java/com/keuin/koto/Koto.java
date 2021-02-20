package com.keuin.koto;

import com.keuin.koto.command.KotoCommand;
import com.keuin.koto.core.waypoint.manager.VoxelMapWayPointManager;
import com.keuin.koto.core.waypoint.manager.WayPointManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import java.io.IOException;

public class Koto implements ModInitializer {

    private final WayPointManager wayPointManager = new VoxelMapWayPointManager("map");
    private final KotoCommand kotoCommand = new KotoCommand(wayPointManager);

    public Koto() throws IOException {
    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        CommandRegistrationCallback.EVENT.register(kotoCommand.getCommandRegistrationCallback());
    }
}
