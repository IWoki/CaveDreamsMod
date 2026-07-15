package com.imwoki.cavedreams.entity;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.EnumSet;
import java.util.List;

public class LullabiteFlockGoal extends Goal {
    private static final double MIN_HEIGHT = 2.0;
    private static final double MAX_HEIGHT = 3.0;
    private static final int FLOOR_SCAN_RANGE = 32;

    private final LullabiteEntity lullabite;
    private final double speed;
    private Vec3d targetPos;
    private int cooldown;

    public LullabiteFlockGoal(LullabiteEntity mob, double speed) {
        this.lullabite = mob;
        this.speed = speed;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
        this.cooldown = 0;
    }

    @Override
    public boolean canStart() {
        // Пока моб "влюблён" - уступаем управление AnimalMateGoal, чтобы размножение реально происходило
        if (this.lullabite.isInLove()) return false;

        // Работает только если рядом нет "настоящей" цели-игрока
        // (креативщиков и спектаторов в расчёт не берём - они моба не интересуют)
        PlayerEntity closest = this.lullabite.getWorld().getClosestPlayer(
                this.lullabite.getX(), this.lullabite.getY(), this.lullabite.getZ(),
                32.0,
                p -> !p.isSpectator() && !((PlayerEntity) p).isCreative()
        );
        if (closest != null) return false;

        if (--cooldown > 0) return false;
        cooldown = 20;

        // Ищем сородичей в радиусе 12 блоков
        List<LullabiteEntity> mates = this.lullabite.getWorld().getEntitiesByClass(
                LullabiteEntity.class,
                this.lullabite.getBoundingBox().expand(12),
                e -> e != this.lullabite && e.isAlive()
        );
        if (mates.isEmpty()) {
            // Одинокий – обычное блуждание (как у других летающих мобов), но без больших скачков по высоте
            targetPos = this.lullabite.getPos().add(
                    (this.lullabite.getRandom().nextDouble() - 0.5) * 10,
                    (this.lullabite.getRandom().nextDouble() - 0.5) * 2,
                    (this.lullabite.getRandom().nextDouble() - 0.5) * 10
            );
            return true;
        }
        // Летим к центру группы (роевое поведение)
        Vec3d center = Vec3d.ZERO;
        for (LullabiteEntity m : mates) center = center.add(m.getPos());
        center = center.multiply(1.0 / mates.size());
        targetPos = center;
        return true;
    }

    @Override
    public void start() {
        moveToTarget();
    }

    @Override
    public void tick() {
        moveToTarget();
    }

    // Держим высоту полёта в 2-3 блоках над реальным полом под целью (работает и в пещерах)
    private void moveToTarget() {
        double floorY = findFloorY(this.lullabite.getWorld(), targetPos.x, targetPos.y, targetPos.z);
        double clampedY = Math.min(Math.max(targetPos.y, floorY + MIN_HEIGHT), floorY + MAX_HEIGHT);
        this.lullabite.getMoveControl().moveTo(targetPos.x, clampedY, targetPos.z, speed);
    }

    // Сканируем вниз от целевой точки, чтобы найти пол под ней
    private double findFloorY(World world, double x, double y, double z) {
        BlockPos.Mutable pos = new BlockPos.Mutable((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        for (int i = 0; i < FLOOR_SCAN_RANGE; i++) {
            if (!world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()) {
                return pos.getY() + 1;
            }
            pos.move(Direction.DOWN);
        }
        return this.lullabite.getY();
    }

    @Override
    public boolean shouldContinue() {
        return !this.lullabite.isInLove() && this.lullabite.squaredDistanceTo(targetPos) > 2.0;
    }
}