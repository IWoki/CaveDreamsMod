package com.imwoki.cavedreams.entity;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.EnumSet;
import java.util.List;

public class LullabiteBehaviorGoal extends Goal {
    private static final double MIN_HEIGHT = 2.0;
    private static final double MAX_HEIGHT = 3.0;
    private static final int FLOOR_SCAN_RANGE = 32;
    // Во сколько раз быстрее моб улетает, когда боится игрока
    private static final double FEAR_SPEED_MULTIPLIER = 1.8;

    private final LullabiteEntity lullabite;
    private final double speed;
    private PlayerEntity targetPlayer;
    private int updateCountdown = 0;

    public LullabiteBehaviorGoal(LullabiteEntity mob, double speed) {
        this.lullabite = mob;
        this.speed = speed;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        // Пока моб "влюблён" - уступаем управление AnimalMateGoal, чтобы размножение реально происходило
        if (this.lullabite.isInLove()) return false;

        if (--this.updateCountdown > 0) return false;
        this.updateCountdown = 10;
        // Креативщиков и спектаторов в принципе не рассматриваем как цель
        this.targetPlayer = this.lullabite.getWorld().getClosestPlayer(
                this.lullabite.getX(), this.lullabite.getY(), this.lullabite.getZ(),
                this.lullabite.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE),
                p -> !p.isSpectator() && !((PlayerEntity) p).isCreative()
        );
        return this.targetPlayer != null;
    }

    @Override
    public void tick() {
        if (this.targetPlayer == null) return;

        Vec3d groupCenter = getGroupCenter(); // центр ближайшей стаи
        Vec3d targetPos;
        double moveSpeed = this.speed;

        if (this.lullabite.isPlayerFeared(this.targetPlayer)) {
            // Убегаем от игрока быстрее обычного, но тянемся к стае
            moveSpeed = this.speed * FEAR_SPEED_MULTIPLIER;
            Vec3d away = this.lullabite.getPos().subtract(this.targetPlayer.getPos()).normalize().multiply(8.0);
            targetPos = this.lullabite.getPos().add(away);
            if (groupCenter != null) {
                targetPos = targetPos.add(groupCenter.subtract(this.lullabite.getPos()).normalize().multiply(2.0));
            }
        } else {
            List<LullabiteEntity> nearby = this.lullabite.getWorld().getEntitiesByClass(
                    LullabiteEntity.class,
                    this.lullabite.getBoundingBox().expand(10),
                    e -> e != this.lullabite && e.isAlive()
            );
            int count = nearby.size() + 1;

            if (count >= 3) {
                // Летим к игроку (на его уровень, не к глазам) + тянемся к стае
                Vec3d toPlayer = new Vec3d(this.targetPlayer.getX(), this.targetPlayer.getY(), this.targetPlayer.getZ());
                targetPos = toPlayer;
                if (groupCenter != null) {
                    targetPos = toPlayer.add(groupCenter.subtract(this.lullabite.getPos()).normalize().multiply(1.5));
                }
            } else {
                // Отлетаем, но к стае
                Vec3d away = this.lullabite.getPos().subtract(this.targetPlayer.getPos()).normalize().multiply(6.0);
                targetPos = this.lullabite.getPos().add(away);
                if (groupCenter != null) {
                    targetPos = targetPos.add(groupCenter.subtract(this.lullabite.getPos()).normalize().multiply(2.5));
                }
            }
        }
        moveToWithFlightHeight(targetPos.x, targetPos.y, targetPos.z, moveSpeed);
        this.lullabite.getLookControl().lookAt(this.targetPlayer, 30, 30);
    }

    @Override
    public boolean shouldContinue() {
        return !this.lullabite.isInLove()
                && this.targetPlayer != null && this.targetPlayer.isAlive()
                && !this.targetPlayer.isSpectator() && !this.targetPlayer.isCreative()
                && this.lullabite.squaredDistanceTo(this.targetPlayer) < 32*32;
    }

    @Override
    public void stop() {
        this.targetPlayer = null;
    }

    // Держим высоту полёта в 2-3 блоках над реальным полом (а не над "верхом мира" из heightmap,
    // который в пещерах указывает на поверхность земли, а не на потолок/пол пещеры)
    private void moveToWithFlightHeight(double x, double y, double z, double moveSpeed) {
        double floorY = findFloorY(this.lullabite.getWorld(), x, y, z);
        double clampedY = Math.min(Math.max(y, floorY + MIN_HEIGHT), floorY + MAX_HEIGHT);
        this.lullabite.getMoveControl().moveTo(x, clampedY, z, moveSpeed);
    }

    // Сканируем вниз от целевой точки, чтобы найти ближайший пол — корректно работает и на
    // поверхности, и в пещерах
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

    // Центр масс сородичей в радиусе 10 блоков (предпочитаем группы, где уже есть 2+)
    private Vec3d getGroupCenter() {
        List<LullabiteEntity> mates = this.lullabite.getWorld().getEntitiesByClass(
                LullabiteEntity.class,
                this.lullabite.getBoundingBox().expand(10),
                e -> e != this.lullabite && e.isAlive()
        );
        if (mates.isEmpty()) return null;
        // Если есть хотя бы двое – группируемся, иначе только если один, но приоритет меньше
        if (mates.size() >= 2) {
            Vec3d sum = Vec3d.ZERO;
            for (LullabiteEntity m : mates) sum = sum.add(m.getPos());
            return sum.multiply(1.0 / mates.size());
        } else {
            // Одна особь – возвращаем её позицию, но с меньшим весом (используется в смешивании)
            return mates.get(0).getPos();
        }
    }
}