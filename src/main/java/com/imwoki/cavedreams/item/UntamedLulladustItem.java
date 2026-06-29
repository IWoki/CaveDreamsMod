package com.imwoki.cavedreams.item;

import com.imwoki.cavedreams.util.DreamPlayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class UntamedLulladustItem extends Item {
    public UntamedLulladustItem(Settings settings) {
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
                    // day -> night
                    nextTime = days * 24000 + 13000;
                } else {
                    // night -> day
                    nextTime = (days + 1) * 24000;
                }
                serverWorld.setTimeOfDay(nextTime);
            }

            if (player instanceof DreamPlayer dreamPlayer) {
                dreamPlayer.cavedreams_startDream(wakeTick, true, false);
                player.getItemCooldownManager().set(this, 600);
            }
        }
        return itemStack;
    }
}