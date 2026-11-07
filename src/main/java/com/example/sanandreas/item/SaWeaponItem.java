package com.example.sanandreas.item;

import com.example.sanandreas.entity.RocketEntity;
import com.example.sanandreas.weapon.WeaponType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/** Arma de fuego con cargador y reserva guardados en el NBT del item. */
public class SaWeaponItem extends Item {
    private static final String CLIP = "Clip";
    private static final String RESERVE = "Reserve";
    private static final Map<UUID, Long> LAST_SHOT = new HashMap<>();

    public final WeaponType type;

    public SaWeaponItem(WeaponType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    // ------------------------------------------------------------------ municion

    public int getClip(ItemStack stack) {
        CompoundTag t = stack.getTag();
        return (t != null && t.contains(CLIP)) ? t.getInt(CLIP) : type.clipSize;
    }

    public int getReserve(ItemStack stack) {
        CompoundTag t = stack.getTag();
        return (t != null && t.contains(RESERVE)) ? t.getInt(RESERVE) : type.startReserve;
    }

    public void setClip(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(CLIP, value);
    }

    public void setReserve(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(RESERVE, value);
    }

    /** Municion completa (valores iniciales del video). */
    public void fill(ItemStack stack) {
        setClip(stack, type.clipSize);
        setReserve(stack, type.startReserve);
    }

    private void ensureInit(ItemStack stack) {
        CompoundTag t = stack.getTag();
        if (t == null || !t.contains(CLIP) || !t.contains(RESERVE)) {
            fill(stack);
        }
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        fill(stack);
        return stack;
    }

    public static void forget(UUID player) {
        LAST_SHOT.remove(player);
    }

    // ------------------------------------------------------------------ disparo

    /** Llamado desde el servidor cuando el jugador mantiene el boton de usar. */
    public void tryFire(ServerPlayer player, ItemStack stack) {
        if (!player.isAlive() || player.isSpectator()) return;
        if (player.getCooldowns().isOnCooldown(this)) return;

        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        Long last = LAST_SHOT.get(player.getUUID());
        int minGap = Math.max(1, type.fireDelay - 1);
        if (last != null && now >= last && now - last < minGap) return;
        LAST_SHOT.put(player.getUUID(), now);

        fire(level, player, stack);
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    private void fire(ServerLevel level, ServerPlayer player, ItemStack stack) {
        ensureInit(stack);
        int clip = getClip(stack);
        if (clip <= 0) {
            if (getReserve(stack) > 0) {
                reload(player, stack);
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.5f, 1.8f);
            }
            return;
        }
        setClip(stack, clip - 1);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                type.sound, SoundSource.PLAYERS, type.volume, type.pitch);

        Vec3 look = player.getLookAngle();
        if (type.kind == WeaponType.Kind.HITSCAN) {
            for (int i = 0; i < type.pellets; i++) {
                shootRay(level, player, look);
            }
        } else if (type.kind == WeaponType.Kind.ROCKET) {
            RocketEntity rocket = new RocketEntity(level, player);
            Vec3 eye = player.getEyePosition();
            rocket.setPos(eye.add(look.scale(0.8)).subtract(0, 0.15, 0));
            rocket.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 2.2f, 0.0f);
            level.addFreshEntity(rocket);
        } else {
            flame(level, player, look);
        }
    }

    private void shootRay(ServerLevel level, ServerPlayer player, Vec3 look) {
        RandomSource rnd = level.random;
        double s = Math.tan(Math.toRadians(type.spreadDeg)) * 0.5;
        Vec3 dir = new Vec3(
                look.x + rnd.nextGaussian() * s,
                look.y + rnd.nextGaussian() * s,
                look.z + rnd.nextGaussian() * s).normalize();

        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(dir.scale(type.range));
        BlockHitResult blockHit = level.clip(new ClipContext(eye, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 limit = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();

        AABB box = player.getBoundingBox().expandTowards(dir.scale(type.range)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, player, eye, limit, box,
                e -> !e.isSpectator() && e.isPickable());

        Vec3 impact = limit;
        if (entityHit != null) {
            impact = entityHit.getLocation();
            Entity target = entityHit.getEntity();
            target.invulnerableTime = 0;
            target.hurt(player.damageSources().playerAttack(player), type.damage);
            level.sendParticles(ParticleTypes.CRIT, impact.x, impact.y, impact.z, 3, 0.1, 0.1, 0.1, 0.1);
        } else if (blockHit.getType() == HitResult.Type.BLOCK) {
            level.sendParticles(ParticleTypes.SMOKE, impact.x, impact.y, impact.z, 2, 0.05, 0.05, 0.05, 0.01);
        }

        // Trazadora: unos puntos de luz a lo largo del disparo
        double dist = eye.distanceTo(impact);
        int n = (int) Math.min(10, dist / 3.0);
        for (int i = 1; i <= n; i++) {
            Vec3 p = eye.add(dir.scale(dist * i / (n + 1)));
            level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    private void flame(ServerLevel level, ServerPlayer player, Vec3 look) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(look.scale(type.range));
        BlockHitResult blockHit = level.clip(new ClipContext(eye, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double maxDist = blockHit.getType() == HitResult.Type.MISS ? type.range : eye.distanceTo(blockHit.getLocation());

        for (int d = 1; d <= (int) maxDist; d++) {
            Vec3 p = eye.add(look.scale(d)).add(0, -0.3, 0);
            double spread = 0.15 + d * 0.03;
            level.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 2, spread, spread, spread, 0.01);
            AABB box = new AABB(p, p).inflate(0.6 + d * 0.06);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, x -> x != player && x.isAlive())) {
                e.setSecondsOnFire(6);
                e.invulnerableTime = 0;
                e.hurt(player.damageSources().playerAttack(player), type.damage);
            }
        }
    }

    // ------------------------------------------------------------------ recarga

    public void reload(ServerPlayer player, ItemStack stack) {
        ensureInit(stack);
        if (player.getCooldowns().isOnCooldown(this)) return;
        int clip = getClip(stack);
        int reserve = getReserve(stack);
        int need = type.clipSize - clip;
        if (need <= 0 || reserve <= 0) return;

        int take = Math.min(need, reserve);
        setClip(stack, clip + take);
        setReserve(stack, reserve - take);
        player.getCooldowns().addCooldown(this, type.reloadTicks);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS, 0.8f, 1.0f);
    }

    // ------------------------------------------------------------------ detalles de item

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getClip(stack) < type.clipSize;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getClip(stack) / (float) type.clipSize);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xE8B923;
    }

    /** Evita que la mano "rebote" cada vez que cambia la municion. */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(getReserve(stack) + " - " + getClip(stack)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }
        });
    }
}
