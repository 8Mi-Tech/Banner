package com.mohistmc.banner.mixin.world.entity.item;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {

    // @formatter:off
    @Shadow @Final private static EntityDataAccessor<ItemStack> DATA_ITEM;
    @Shadow public int pickupDelay;
    @Shadow public abstract ItemStack getItem();
    @Shadow public UUID target;
    // @formatter:on

    public MixinItemEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;)V", cancellable = true, at = @At("HEAD"))
    private static void banner$itemMerge(ItemEntity from, ItemStack stack1, ItemEntity to, ItemStack stack2, CallbackInfo ci) {
        if (CraftEventFactory.callItemMergeEvent(to, from).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "hurt", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;markHurt()V"))
    private void banner$damageNonLiving(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CraftEventFactory.handleNonLivingEntityDamageEvent((ItemEntity) (Object) this, source, amount)) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "mergeWithNeighbours", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
    private AABB banner$resetMerge(AABB instance, double x, double y, double z) {
        // Spigot start
        double radius = level.bridge$spigotConfig().itemMerge;
        return instance.inflate(radius, radius - 0.5D, radius);
        // Spigot end
    }

    @Inject(method = "playerTouch", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/item/ItemEntity;pickupDelay:I"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$pickUpEvent(Player player, CallbackInfo ci, ItemStack itemStack,
                                    net.minecraft.world.item.Item item, int i) {
        // CraftBukkit start - fire PlayerPickupItemEvent
        int canHold = player.getInventory().canHold(itemStack);
        int remaining = i - canHold;

        if (this.pickupDelay <= 0 && canHold > 0) {
            itemStack.setCount(canHold);
            // Call legacy event
            PlayerPickupItemEvent playerEvent = new PlayerPickupItemEvent((org.bukkit.entity.Player) player.getBukkitEntity(), (org.bukkit.entity.Item) this.getBukkitEntity(), remaining);
            playerEvent.setCancelled(!playerEvent.getPlayer().getCanPickupItems());
            this.level.getCraftServer().getPluginManager().callEvent(playerEvent);
            if (playerEvent.isCancelled()) {
                itemStack.setCount(i); // SPIGOT-5294 - restore count
                ci.cancel();
            }

            // Call newer event afterwards
            EntityPickupItemEvent entityEvent = new EntityPickupItemEvent((org.bukkit.entity.Player) player.getBukkitEntity(), (org.bukkit.entity.Item) this.getBukkitEntity(), remaining);
            entityEvent.setCancelled(!entityEvent.getEntity().getCanPickupItems());
            this.level.getCraftServer().getPluginManager().callEvent(entityEvent);
            if (entityEvent.isCancelled()) {
                itemStack.setCount(i); // SPIGOT-5294 - restore count
                ci.cancel();
            }

            // Update the ItemStack if it was changed in the event
            ItemStack current = this.getItem();
            if (!itemStack.equals(current)) {
                itemStack = current;
            } else {
                itemStack.setCount(canHold + remaining); // = i
            }

            // Possibly < 0; fix here so we do not have to modify code below
            this.pickupDelay = 0;
        } else if (this.pickupDelay == 0) {
            // ensure that the code below isn't triggered if canHold says we can't pick the items up
            this.pickupDelay = -1;
        }
        // CraftBukkit end
    }



    @Inject(method = "setItem", at = @At("RETURN"))
    private void banner$markDirty(ItemStack stack, CallbackInfo ci) {
        this.getEntityData().markDirty(DATA_ITEM);
    }

    @Redirect(method = "merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;setItem(Lnet/minecraft/world/item/ItemStack;)V"))
    private static void banner$setNonEmpty(ItemEntity itemEntity, ItemStack stack) {
        if (!stack.isEmpty()) {
            itemEntity.setItem(stack);
        }
    }
}
