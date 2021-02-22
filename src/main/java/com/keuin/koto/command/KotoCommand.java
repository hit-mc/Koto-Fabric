package com.keuin.koto.command;

import com.keuin.koto.core.util.PermissionValidator;
import com.keuin.koto.core.waypoint.WayPoint;
import com.keuin.koto.core.waypoint.manager.WayPointManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class KotoCommand {

    private final WayPointManager wayPointManager;
    private final int COMMAND_SUCCESS = 1;
    private final int COMMAND_FAILED = -1;

    private final String DIMENSIONS_OVERWORLD = "overworld";
    private final String DIMENSIONS_NETHER = "the_nether";
    private final String DIMENSIONS_THE_END = "the_end";

    private final Map<UUID, WayPoint> previousWayPointMap = new ConcurrentHashMap<>();

    private final CommandRegistrationCallback commandRegistrationCallback = new CommandRegistrationCallback() {
        @Override
        public void register(CommandDispatcher<ServerCommandSource> commandDispatcher, boolean b) {

            // help menu
            commandDispatcher.register(CommandManager.literal("goto").executes(new Command<ServerCommandSource>() {
                @Override
                public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                    final Formatting commandFormatting = Formatting.DARK_PURPLE;
                    final String[] commands = new String[]{
                            "/goto list [<world>]",
                            "/goto <point>",
                            "/goto prev",
                            "/goto reload"
                    };
                    final String[] descriptions = new String[]{
                            "show all available waypoints. If world is not specified, waypoints in all worlds will be printed.",
                            "navigate to the given waypoint.",
                            "navigate to the last used waypoint.",
                            "reload all waypoints from disk."
                    };
                    MutableText feedback = new LiteralText("Usage:\n");
                    for (int i = 0; i < commands.length; i++) {
                        feedback.append(new LiteralText(commands[i]).formatted(commandFormatting))
                                .append(": ")
                                .append(descriptions[i])
                                .append("\n");
                    }
                    context.getSource().sendFeedback(feedback, false);
                    return COMMAND_SUCCESS;
                }
            }));

            // list waypoints
            Command<ServerCommandSource> listWayPointsCommand = new Command<ServerCommandSource>() {
                @Override
                public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                    MutableText feedback = null;
                    ServerCommandSource source = context.getSource();
                    ServerWorld world = source.getWorld();
                    Collection<String> wantedWorld = null;
                    try {
                        wantedWorld = Collections.singleton(context.getArgument("world", String.class));
                    } catch (Exception ignored) {
                    }
                    if (wantedWorld == null) {
                        wantedWorld = wayPointManager.getWorlds();
                    }

                    // get waypoints
                    feedback = new LiteralText("Available waypoints:").formatted(Formatting.GOLD);
                    boolean needNewLine = false;
                    for (String w : wantedWorld) {
                        // for all worlds
                        if (needNewLine)
                            feedback.append("\n");
                        needNewLine = true;
                        feedback.append(new LiteralText("[" + w + "]").formatted(Formatting.AQUA));
                        Collection<WayPoint> wayPoints = wayPointManager.getWayPoints(w);
                        for (WayPoint wayPoint : wayPoints) {
                            feedback.append("\n").append(new LiteralText(wayPoint.getName()).formatted(Formatting.WHITE));
                        }
                    }
                    if (feedback == null) {
                        feedback = new LiteralText("No waypoints available.").formatted(Formatting.GOLD);
                    }

//                    wayPointManager.getWayPoints()

                    context.getSource().sendFeedback(feedback, false);
                    return COMMAND_SUCCESS;
                }
            };
            commandDispatcher.register(CommandManager.literal("goto")
                    .then(CommandManager.literal("list")
                            .then(CommandManager.argument("world", StringArgumentType.word())
                                    .executes(listWayPointsCommand)).executes(listWayPointsCommand)));

            // navigate
            commandDispatcher.register(CommandManager.literal("goto")
                    .then(CommandManager.argument("world", StringArgumentType.string()).suggests(
                            new SuggestionProvider<ServerCommandSource>() {
                                // world suggesting
                                @Override
                                public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
                                    return CommandSource.suggestMatching(wayPointManager.getWorlds(), builder);
                                }
                            }
                    ).then(CommandManager.argument("target", StringArgumentType.greedyString()).suggests(
                            new SuggestionProvider<ServerCommandSource>() {
                                // target suggesting based on the typed world
                                @Override
                                public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
                                    String world = context.getArgument("world", String.class);
                                    return CommandSource.suggestMatching(
                                            wayPointManager.getWayPoints(world).stream().map(WayPoint::getName),
                                            builder
                                    );
                                }
                            }
                    )
                            .executes(new Command<ServerCommandSource>() {
                                @Override
                                public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                                    String world = context.getArgument("world", String.class);
                                    String target = context.getArgument("target", String.class);

                                    WayPoint wayPoint = wayPointManager.getWayPointsMap(world).get(target);
                                    if (wayPoint != null) {
                                        // record previous waypoint
                                        Optional.ofNullable(context.getSource().getEntity())
                                                .map(Entity::getUuid)
                                                .ifPresent(uuid -> previousWayPointMap.put(uuid, wayPoint));
                                        // navigate to
                                        Goto.execute(wayPoint, context);
                                    } else {
                                        context.getSource().sendFeedback(new LiteralText(
                                                "Specified target does not exist."
                                        ).formatted(Formatting.DARK_RED), false);
                                    }

                                    return COMMAND_SUCCESS;
                                }
                            }))));

            // reload
            commandDispatcher.register(CommandManager.literal("goto").then(CommandManager.literal("reload")
                    .requires(PermissionValidator::op).executes(new Command<ServerCommandSource>() {
                        @Override
                        public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                            if (wayPointManager.reload()) {
                                context.getSource().sendFeedback(new LiteralText(
                                        String.format("Successfully reloaded. World(s): %d. Waypoint(s): %d.",
                                                wayPointManager.getWorlds().size(),
                                                wayPointManager.getWorlds().stream()
                                                        .map(wayPointManager::getWayPoints)
                                                        .map(Collection::size)
                                                        .reduce(0, Integer::sum)
                                        )
                                ).formatted(Formatting.DARK_GREEN), true);
                                return COMMAND_SUCCESS;
                            } else {
                                context.getSource().sendFeedback(new LiteralText(
                                        "Failed to reload waypoint list. The list in memory is kept intact."
                                ).formatted(Formatting.DARK_RED), true);
                                return COMMAND_FAILED;
                            }
                        }
                    })));

            // previous waypoint
            commandDispatcher.register(CommandManager.literal("goto").then(CommandManager.literal("prev").executes(new Command<ServerCommandSource>() {
                @Override
                public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                    WayPoint wayPoint = Optional.ofNullable(context.getSource().getEntity())
                            .map(Entity::getUuid).map(previousWayPointMap::get).orElse(null);
                    if (wayPoint != null) {
                        Goto.execute(wayPoint, context);
                        return COMMAND_SUCCESS;
                    } else {
                        context.getSource().sendFeedback(new LiteralText(
                                "You have not navigated to any place."
                        ).formatted(Formatting.DARK_RED), true);
                        return COMMAND_FAILED;
                    }
                }
            })));
        }
    };

    public KotoCommand(WayPointManager wayPointManager) {
        this.wayPointManager = wayPointManager;
    }

    public CommandRegistrationCallback getCommandRegistrationCallback() {
        return commandRegistrationCallback;
    }

}
