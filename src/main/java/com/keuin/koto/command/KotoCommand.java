package com.keuin.koto.command;

import com.keuin.koto.core.navigation.Navigation;
import com.keuin.koto.core.navigation.Navigator;
import com.keuin.koto.core.util.InteractionUtil;
import com.keuin.koto.core.util.PermissionValidator;
import com.keuin.koto.core.waypoint.WayPoint;
import com.keuin.koto.core.waypoint.WorldedPos3d;
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
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class KotoCommand {

    private final WayPointManager wayPointManager;
    private final int COMMAND_SUCCESS = 1;
    private final int COMMAND_FAILED = -1;

    private final String DIMENSIONS_OVERWORLD = "overworld";
    private final String DIMENSIONS_NETHER = "the_nether";
    private final String DIMENSIONS_THE_END = "the_end";

    private final CommandRegistrationCallback commandRegistrationCallback = new CommandRegistrationCallback() {
        @Override
        public void register(CommandDispatcher<ServerCommandSource> commandDispatcher, boolean b) {

            // help menu
            commandDispatcher.register(CommandManager.literal("goto").executes(new Command<ServerCommandSource>() {
                @Override
                public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                    context.getSource().sendFeedback(new LiteralText(
                            "Usage:\n" +
                                    "/goto list: show all available waypoints.\n" +
                                    "/goto <point>: navigate to the given waypoint."
                    ), false);
                    return COMMAND_SUCCESS;
                }
            }));

            // list waypoints
            Command<ServerCommandSource> listWayPointsCommand = new Command<ServerCommandSource>() {
                @Override
                public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                    StringBuilder builder = new StringBuilder();
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
                    boolean noWayPoints = true;
                    builder.append("Available waypoints:");
                    for (String w : wantedWorld) {
                        noWayPoints = false;
                        // for all worlds
                        builder.append("\n").append("[").append(w).append("]");
                        Collection<WayPoint> wayPoints = wayPointManager.getWayPoints(w);
                        for (WayPoint wayPoint : wayPoints) {
                            builder.append("\n").append(wayPoint.getName());
                        }
                    }
                    if (noWayPoints) {
                        builder = new StringBuilder("No waypoints available.");
                    }

                    System.out.println(context.getSource().getWorld());
//                    wayPointManager.getWayPoints()

                    context.getSource().sendFeedback(new LiteralText(builder.toString()), false);
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
                                        context.getSource().sendFeedback(new LiteralText("Location of ").append(new LiteralText(
                                                "[" + wayPoint.getName() + "]"))
                                                .append(new LiteralText(": " +
                                                        InteractionUtil.getPosVoxelMapLocationString(
                                                                wayPoint.getPosition(), null
                                                        ))), false);

                                        Optional.ofNullable(context.getSource().getEntity()).ifPresent(entity -> {
                                            // create navigation
                                            Navigation navigation = Navigator
                                                    .navigateTo(WorldedPos3d.ofEntity(entity), wayPoint.getPosition());
                                            context.getSource().sendFeedback(new LiteralText(
                                                    navigation.toString()
                                            ), false);
                                        });

                                    } else {
                                        context.getSource().sendFeedback(new LiteralText(
                                                "Specified target does not exist."
                                        ), false);
                                    }

                                    return COMMAND_SUCCESS;
                                }
                            }))));

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
                                ), true);
                                return COMMAND_SUCCESS;
                            } else {
                                context.getSource().sendFeedback(new LiteralText(
                                        "Failed to reload waypoint list. The list in memory is kept intact."
                                ), true);
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
