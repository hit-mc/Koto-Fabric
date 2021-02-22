package com.keuin.koto.command;

import com.keuin.koto.core.navigation.Navigation;
import com.keuin.koto.core.navigation.Navigator;
import com.keuin.koto.core.util.InteractionUtil;
import com.keuin.koto.core.waypoint.Pos3d;
import com.keuin.koto.core.waypoint.WayPoint;
import com.keuin.koto.core.waypoint.WorldedPos3d;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class Goto {
    public static void execute(@NotNull WayPoint wayPoint, @NotNull CommandContext<ServerCommandSource> context) {
        Objects.requireNonNull(wayPoint);
        Objects.requireNonNull(context);

        context.getSource().sendFeedback(new LiteralText("Location of ").append(new LiteralText(
                "[" + wayPoint.getName() + "]").formatted(Formatting.GOLD))
                .append(new LiteralText(": " +
                        InteractionUtil.getPosVoxelMapLocationString(
                                (Pos3d) wayPoint.getPosition()
                        ))), false);

        Optional.ofNullable(context.getSource().getEntity()).ifPresent(entity -> {
            // create navigation if executed by player
            WorldedPos3d startPoint = WorldedPos3d.ofEntity(entity);
            WorldedPos3d endPoint = wayPoint.getPosition();

            Navigation navigation = Navigator
                    .navigateTo(startPoint, endPoint);
            context.getSource().sendFeedback(
                    navigation.getRichText()
                            .append("\nDirection: ")
                            .append(InteractionUtil.getColoredPositionDelta(
                                    startPoint,
                                    endPoint,
                                    false
                            )),
                    false
            );
        });
    }
}
