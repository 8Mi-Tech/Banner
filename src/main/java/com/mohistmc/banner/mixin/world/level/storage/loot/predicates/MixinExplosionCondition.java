package com.mohistmc.banner.mixin.world.level.storage.loot.predicates;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ExplosionCondition.class)
public class MixinExplosionCondition {

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public boolean test(LootContext context) {
        Float f = context.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
        if (f != null) {
            RandomSource random = context.getRandom();
            float f1 = 1.0F / f;
            return random.nextFloat() < f1;
        } else {
            return true;
        }
    }
}
