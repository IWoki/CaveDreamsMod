package com.imwoki.cavedreams.entity;

import com.imwoki.cavedreams.event.LullabiteProximityHandler;
import com.imwoki.cavedreams.item.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public class LullabiteEntity extends AnimalEntity implements Flutterer {

    private static final TrackedData<Boolean> FLYING = DataTracker.registerData(LullabiteEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private UUID fearedPlayerUUID = null;
    private long fearUntil = 0;

    public LullabiteEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
        // Плавный полёт, как у Allay/пчёл: широкий угол поворота (20) + сглаживание (true)
        this.moveControl = new FlightMoveControl(this, 20, true);
        this.setNoGravity(true);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(FLYING, false);
    }

    public boolean isFlying() { return this.dataTracker.get(FLYING); }
    public void setFlying(boolean flying) { this.dataTracker.set(FLYING, flying); }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        // Основная реакция на игрока (страх/группа/одиночество)
        this.goalSelector.add(1, new LullabiteBehaviorGoal(this, 1.2));
        // Сбор в стаи / обычное блуждание, когда нет цели-игрока
        this.goalSelector.add(2, new LullabiteFlockGoal(this, 1.0));
        // Размножение
        this.goalSelector.add(3, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(5, new LookAroundGoal(this));
    }

    public static DefaultAttributeContainer.Builder createLullabiteAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 5.0)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 1.5)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1.5)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    public boolean isInAir() { return true; }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation nav = new BirdNavigation(this, world);
        nav.setCanPathThroughDoors(false);
        nav.setCanSwim(true);
        nav.setCanEnterOpenDoors(true);
        return nav;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity other) {
        return ModEntities.LULLABITE.create(world);
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (source.getAttacker() instanceof PlayerEntity player) {
            // Не боимся креативных/зрителей
            if (!player.isCreative() && !player.isSpectator()) {
                LullabiteProximityHandler.spreadFear(this.getWorld(), this.getBlockPos(), player, 20*60*12);
            }
        }
    }

    public void setFearedPlayer(PlayerEntity player, long durationTicks) {
        this.fearedPlayerUUID = player.getUuid();
        this.fearUntil = this.getWorld().getTime() + durationTicks;
    }

    public boolean isPlayerFeared(PlayerEntity player) {
        if (fearedPlayerUUID == null || !player.getUuid().equals(fearedPlayerUUID)) return false;
        return this.getWorld().getTime() < fearUntil;
    }

    @Override
    public boolean isPushable() { return true; }

    @Override
    protected void dropLoot(DamageSource source, boolean causedByPlayer) {
        super.dropLoot(source, causedByPlayer);
        if (causedByPlayer && this.random.nextFloat() < 0.2f) {
            this.dropItem(ModItems.UNTAMED_LULLADUST);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("Flying", this.isFlying());
        if (fearedPlayerUUID != null) {
            nbt.putUuid("FearedPlayer", fearedPlayerUUID);
            nbt.putLong("FearUntil", fearUntil);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setFlying(nbt.getBoolean("Flying"));
        if (nbt.contains("FearedPlayer")) {
            fearedPlayerUUID = nbt.getUuid("FearedPlayer");
            fearUntil = nbt.getLong("FearUntil");
        }
    }
}
