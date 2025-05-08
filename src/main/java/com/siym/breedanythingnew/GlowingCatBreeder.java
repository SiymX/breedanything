package com.siym.breedanythingnew;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class GlowingCatBreeder {

    private static final Set<CatEntity> guardians = new HashSet<>();

    public static void tick(World world) {
        if (world.isClient) return;
        ServerWorld serverWorld = (ServerWorld) world;

        // Glowstone + Cat = Guardian Baby
        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            List<CatEntity> cats = serverWorld.getEntitiesByClass(
                    CatEntity.class,
                    player.getBoundingBox().expand(3.0),
                    cat -> cat.isAlive() && cat.isTamed() && !cat.isBaby()
            );

            for (CatEntity cat : cats) {
                List<ItemEntity> glowstones = serverWorld.getEntitiesByClass(
                        ItemEntity.class,
                        cat.getBoundingBox().expand(1.5),
                        item -> item.getStack().getItem() == Items.GLOWSTONE_DUST
                );

                if (!glowstones.isEmpty()) {
                    ItemEntity glowstone = glowstones.get(0);
                    glowstone.discard();

                    CatEntity baby = new CatEntity(EntityType.CAT, serverWorld);
                    baby.setBaby(true);
                    baby.setPos(cat.getX(), cat.getY(), cat.getZ());
                    baby.setCustomName(Text.literal("Glowing Guardian 😼"));
                    baby.setCustomNameVisible(true);
                    baby.setPersistent();
                    baby.setGlowing(true);
                    baby.setTamed(true, true);
                    baby.setOwner(cat.getOwner());
                    baby.setInvulnerable(true);


                    serverWorld.spawnEntity(baby);
                    guardians.add(baby);

                    serverWorld.playSound(null, baby.getBlockPos(), SoundEvents.ENTITY_CAT_PURR, baby.getSoundCategory(), 1f, 1f);
                    player.sendMessage(Text.literal("✨ A magical guardian has been born!"), false);
                }
            }
        }


        guardians.removeIf(cat -> !cat.isAlive());

        for (CatEntity guardian : guardians) {
            ServerWorld server = (ServerWorld) guardian.getWorld();


            server.spawnParticles(ParticleTypes.END_ROD, guardian.getX(), guardian.getY(), guardian.getZ(), 2, 0.1, 0.1, 0.1, 0.0);


            List<HostileEntity> mobs = server.getEntitiesByClass(
                    HostileEntity.class,
                    guardian.getBoundingBox().expand(5.0),
                    mob -> mob.isAlive()
            );

            for (HostileEntity mob : mobs) {
                Vec3d from = guardian.getPos().add(0, 0.5, 0);
                Vec3d to = mob.getPos().add(0, 0.5, 0);
                Vec3d dir = to.subtract(from).normalize().multiply(0.5);

                SmallFireballEntity fireball = new SmallFireballEntity(
                        guardian.getWorld(),
                        from.x, from.y, from.z,
                        dir
                );


                server.spawnEntity(fireball);
                server.playSound(null, guardian.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, guardian.getSoundCategory(), 0.6f, 1f);
            }
        }
    }
}
