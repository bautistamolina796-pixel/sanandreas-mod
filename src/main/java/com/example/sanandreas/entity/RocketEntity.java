package com.example.sanandreas.entity;

import com.example.sanandreas.ModEntities;
import com.example.sanandreas.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** Cohete del lanzacohetes: vuela recto y explota al impactar. */
public class RocketEntity extends ThrowableItemProjectile {

    public RocketEntity(EntityType<? extends RocketEntity> type, Level level) {
        super(type, level);
    }

    public RocketEntity(Level level, LivingEntity owner) {
        super(ModEntities.ROCKET.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ROCKET.get();
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0, 0);
            level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0, 0, 0);
        } else if (tickCount > 100) {
            explode();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            explode();
        }
    }

    private void explode() {
        if (isRemoved()) return;
        // NONE = daña a entidades pero no rompe bloques. Cambialo a TNT para destruir el terreno.
        level().explode(this, getX(), getY(), getZ(), 3.0f, Level.ExplosionInteraction.NONE);
        discard();
    }
}
