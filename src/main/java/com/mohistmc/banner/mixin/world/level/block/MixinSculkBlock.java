package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SculkBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SculkBlock.class)
public class MixinSculkBlock {

    @Redirect(method = "attemptUseCharge", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean banner$blockSpread(LevelAccessor level, BlockPos pos, BlockState state, int i,
                                         SculkSpreader.ChargeCursor p_222073_, LevelAccessor p_222074_, BlockPos source) {
        return CraftEventFactory.handleBlockSpreadEvent(level, source, pos, state, i);
    }
}
