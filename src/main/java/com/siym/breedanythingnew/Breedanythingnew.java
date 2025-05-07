package com.siym.breedanythingnew;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.fabricmc.api.ModInitializer;

public class Breedanythingnew implements ModInitializer {

    @Override
    public void onInitialize() {

        //enderpearl breeding
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.isClient) return;

            for (ServerPlayerEntity player : world.getPlayers()) {
                if (!player.isSneaking()) continue;

                world.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(1.0), itemEntity ->
                        itemEntity.getStack().getItem() == Items.ENDER_PEARL
                ).forEach(pearl -> {
                    if (!TeleportBreeder.isMarked(player)) {

                        ((ServerWorld) world).spawnParticles(
                                ParticleTypes.HEART,
                                player.getX(), player.getY() + 1.0, player.getZ(),
                                20, 0.5, 0.5, 0.5, 0.1
                        );

                        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_LLAMA_SPIT, player.getSoundCategory(), 1.0f, 1.0f);
                        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GOAT_SCREAMING_DEATH, player.getSoundCategory(), 1.2f, 0.9f);



                        pearl.discard();


                        TeleportBreeder.breedWithEnderPearl(player);
                        player.sendMessage(Text.literal("You have bred with an Ender Pearl 💖"), false);
                        player.sendMessage(Text.literal("Ender Pearl? I don't even know her."), false);
                    }
                });
            }
        });

        //tntbreeding
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.isClient) return;

            for (ServerPlayerEntity player : world.getPlayers()) {
                if (!player.isSneaking()) continue;


                world.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(1.0), itemEntity ->
                        itemEntity.getStack().getItem() == Items.TNT
                ).forEach(tntItem -> {
                    if (!TNTBreeder.isMarked(player)) {

                        ((ServerWorld) world).spawnParticles(
                                ParticleTypes.HEART,
                                player.getX(), player.getY() + 1.0, player.getZ(),
                                20, 0.5, 0.5, 0.5, 0.1
                        );

                        // Sneeze sounds
                        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GOAT_SCREAMING_AMBIENT, player.getSoundCategory(), 1.0f, 1.0f);
                        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_TNT_PRIMED, player.getSoundCategory(), 0.8f, 1.5f);

                        // 💥 Re TNT item
                        tntItem.discard();

                        // Register the sneezer
                        TNTBreeder.infect(player);

                        player.sendMessage(Text.literal("You have bred with TNT 💣 Prepare to sneeze TNT out of your butt and your mouth."), false);
                    }
                });
            }
        });

        //ticks
        ServerTickEvents.END_WORLD_TICK.register(TNTBreeder::tick);
        ServerTickEvents.END_WORLD_TICK.register(TeleportBreeder::tick);

        //commands
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> TeleportBreeder.registerCommands(dispatcher)
        );

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        dispatcher.register(net.minecraft.server.command.CommandManager.literal("sneezestop")
                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                                    TNTBreeder.stop(player);
                                    return 1;
                                }))
        );
    }
}
