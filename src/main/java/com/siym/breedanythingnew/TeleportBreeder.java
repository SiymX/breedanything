package com.siym.breedanythingnew;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import com.mojang.brigadier.CommandDispatcher;

import java.util.HashSet;
import java.util.Set;

public class TeleportBreeder {
    private static final Set<ServerPlayerEntity> markedPlayers = new HashSet<>();

    public static void breedWithEnderPearl(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            markedPlayers.add(serverPlayer);
        }
    }

    public static void tick(World world) {
        if (world.isClient) return;

        for (ServerPlayerEntity player : new HashSet<>(markedPlayers)) {
            if (player.age % 60 == 0) {
                double newX = player.getX() + world.random.nextBetween(-10, 10);
                double newY = player.getY() + world.random.nextBetween(0, 5);
                double newZ = player.getZ() + world.random.nextBetween(-10, 10);


                player.networkHandler.requestTeleport(
                        newX, newY, newZ,
                        player.getYaw(),
                        player.getPitch()
                );
            }
        }
    }

    public static boolean isMarked(PlayerEntity player) {
        return markedPlayers.contains(player);
    }


    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("breedstop")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (markedPlayers.remove(player)) {
                        player.sendMessage(Text.literal("You have stopped breeding. You're safe... for now."), false);
                    } else {
                        player.sendMessage(Text.literal("You're not breeding with any Ender Pearl... yet."), false);
                    }
                    return 1;
                }));
    }
}
