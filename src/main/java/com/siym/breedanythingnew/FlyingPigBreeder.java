package com.siym.breedanythingnew;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class FlyingPigBreeder {

    private static final Map<PigEntity, List<ArmorStandEntity>> flyingPigs = new HashMap<>();
    private static final List<ItemStack> rainbowGlass = List.of(
            new ItemStack(Blocks.RED_STAINED_GLASS_PANE),
            new ItemStack(Blocks.ORANGE_STAINED_GLASS_PANE),
            new ItemStack(Blocks.YELLOW_STAINED_GLASS_PANE),
            new ItemStack(Blocks.LIME_STAINED_GLASS_PANE),
            new ItemStack(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE),
            new ItemStack(Blocks.BLUE_STAINED_GLASS_PANE),
            new ItemStack(Blocks.PURPLE_STAINED_GLASS_PANE)
    );
    private static final Map<UUID, Integer> pigColorIndex = new HashMap<>();
    private static final Map<UUID, List<ArmorStandEntity>> trailEntities = new HashMap<>();

    public static void tick(World world) {
        if (world.isClient) return;
        ServerWorld serverWorld = (ServerWorld) world;

        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            List<PigEntity> pigs = serverWorld.getEntitiesByClass(
                    PigEntity.class,
                    player.getBoundingBox().expand(3.0),
                    pig -> pig.isAlive() && !pig.isBaby()
            );

            for (PigEntity pig : pigs) {
                List<ItemEntity> elytras = serverWorld.getEntitiesByClass(
                        ItemEntity.class,
                        pig.getBoundingBox().expand(1.5),
                        item -> item.getStack().getItem() == Items.ELYTRA
                );

                if (!elytras.isEmpty()) {
                    ItemEntity elytra = elytras.get(0);
                    elytra.discard();

                    PigEntity baby = new PigEntity(EntityType.PIG, serverWorld);
                    baby.setPos(pig.getX(), pig.getY(), pig.getZ());
                    baby.setBaby(true);
                    baby.setCustomName(Text.literal("Flying Bacon ✈️"));
                    baby.setCustomNameVisible(true);
                    serverWorld.spawnEntity(baby);

                    // Mount player to pig
                    player.startRiding(baby, true);

                    // Begin rainbow trail cycling
                    pigColorIndex.put(baby.getUuid(), 0);
                    trailEntities.put(baby.getUuid(), new ArrayList<>());

                    spawnWings(serverWorld, baby);

                    serverWorld.playSound(null, baby.getBlockPos(), SoundEvents.ENTITY_PARROT_IMITATE_ENDER_DRAGON, baby.getSoundCategory(), 1f, 1f);
                    serverWorld.spawnParticles(ParticleTypes.HEART, baby.getX(), baby.getY() + 1, baby.getZ(), 12, 0.5, 0.5, 0.5, 0.1);

                    player.sendMessage(Text.literal("An abomination has been born!"), false);
                }
            }
        }


        flyingPigs.entrySet().removeIf(entry -> {
            PigEntity pig = entry.getKey();
            List<ArmorStandEntity> wings = entry.getValue();

            if (!pig.isAlive() || wings.stream().anyMatch(w -> !w.isAlive())) return true;

            // Flying motion
            pig.setNoGravity(true);
            Vec3d currentVelocity = pig.getVelocity();
            pig.setVelocity(new Vec3d(0, currentVelocity.y * 0.5, 0)); // cancel vertical drift

            double x = pig.getX();
            double y = pig.getY() - 0.05;
            double z = pig.getZ();

            wings.get(0).setPos(x - 0.5, y, z);
            wings.get(1).setPos(x + 0.5, y, z);

            // Rainbow trail
            int index = pigColorIndex.getOrDefault(pig.getUuid(), 0);
            ItemStack glass = rainbowGlass.get(index % rainbowGlass.size());
            pigColorIndex.put(pig.getUuid(), (index + 1) % rainbowGlass.size());

            ArmorStandEntity trail = new ArmorStandEntity(pig.getWorld(), x, pig.getY() - 0.3, z);

            trail.setInvisible(true);
            trail.setInvulnerable(true);
            trail.setNoGravity(true);
            trail.equipStack(EquipmentSlot.HEAD, glass);
            pig.getWorld().spawnEntity(trail);

            trailEntities.get(pig.getUuid()).add(trail);


            if (trailEntities.get(pig.getUuid()).size() > 40) {
                ArmorStandEntity toRemove = trailEntities.get(pig.getUuid()).remove(0);
                if (toRemove != null && toRemove.isAlive()) {
                    toRemove.discard();
                }
            }

            return false;
        });
    }

    private static void spawnWings(ServerWorld world, PigEntity pig) {
        double x = pig.getX();
        double y = pig.getY() + 0.6;
        double z = pig.getZ();

        ArmorStandEntity leftWing = new ArmorStandEntity(world, x - 0.5, y, z);
        leftWing.setInvisible(true);
        leftWing.setInvulnerable(true);
        leftWing.setNoGravity(true);
        leftWing.setHeadRotation(new EulerAngle(0, 0, (float) Math.toRadians(90)));
        leftWing.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.END_ROD));
        world.spawnEntity(leftWing);

        ArmorStandEntity rightWing = new ArmorStandEntity(world, x + 0.5, y, z);
        rightWing.setInvisible(true);
        rightWing.setInvulnerable(true);
        rightWing.setNoGravity(true);
        rightWing.setHeadRotation(new EulerAngle(0, 0, (float) Math.toRadians(-90)));
        rightWing.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.END_ROD));
        world.spawnEntity(rightWing);

        flyingPigs.put(pig, List.of(leftWing, rightWing));
    }
}
