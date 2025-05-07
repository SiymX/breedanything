package com.siym.breedanythingnew;

import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TNTBreeder {
    private static final Set<ServerPlayerEntity> tntSneezers = new HashSet<>();
    private static final HashMap<ServerPlayerEntity, Integer> sneezeTimers = new HashMap<>();

    public static void infect(ServerPlayerEntity player) {
        tntSneezers.add(player);
        sneezeTimers.put(player, 0);
    }

    public static boolean isMarked(PlayerEntity player) {
        return tntSneezers.contains(player);
    }

    public static void tick(World world) {
        if (world.isClient) return;

        ServerWorld serverWorld = (ServerWorld) world;

        Set<ServerPlayerEntity> toRemove = new HashSet<>();

        for (ServerPlayerEntity player : tntSneezers) {
            int time = sneezeTimers.getOrDefault(player, 0) + 1;

            if (time >= 100) {
                sneezeTimers.put(player, 0);


                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GOAT_SCREAMING_AMBIENT, player.getSoundCategory(), 1.0f, 1.0f);
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_TNT_PRIMED, player.getSoundCategory(), 1.0f, 1.0f);


                Vec3d look = player.getRotationVec(1.0F);
                Vec3d pos = player.getPos();


                TntEntity front = new TntEntity(world, pos.x + look.x, pos.y, pos.z + look.z, player);
                front.setFuse(40);
                serverWorld.spawnEntity(front);


                TntEntity back = new TntEntity(world, pos.x - look.x, pos.y, pos.z - look.z, player);
                back.setFuse(40);
                serverWorld.spawnEntity(back);


                player.sendMessage(Text.literal("ACHOO! 💣💥"), false);
            } else {
                sneezeTimers.put(player, time);
            }
        }
    }

    public static void stop(ServerPlayerEntity player) {
        tntSneezers.remove(player);
        sneezeTimers.remove(player);
        player.sendMessage(Text.literal("You have stopped sneezing TNT. You're safe... for now."), false);
    }
}
