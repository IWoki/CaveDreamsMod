package com.imwoki.cavedreams.item;

import com.imwoki.cavedreams.util.DreamPlayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class StabilizedLulladustItem extends Item {
    public StabilizedLulladustItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack itemStack = super.finishUsing(stack, world, user);
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            long currentTime = world.getTime();
            long wakeTick = currentTime + 200;

            if (world.getServer() != null && !world.getServer().isDedicated() && world instanceof ServerWorld serverWorld) {
                long timeOfDay = serverWorld.getTimeOfDay();
                long days = timeOfDay / 24000;
                long nextTime;
                if (timeOfDay % 24000 < 13000) {
                    nextTime = days * 24000 + 13000;
                } else {
                    nextTime = (days + 1) * 24000;
                }
                serverWorld.setTimeOfDay(nextTime);
            }

            if (player instanceof DreamPlayer dreamPlayer) {
                dreamPlayer.cavedreams_startDream(wakeTick, false, true);
                player.getItemCooldownManager().set(this, 1200);
            }
        }
        return itemStack;
    }
}