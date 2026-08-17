package xiaocaoawa.minecraft.mod.cobbleupdraft.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xiaocaoawa.minecraft.mod.cobbleupdraft.lock.GrabberLockManager;

/**
 * 抓手上的宝可梦取消碰撞箱时，让射线穿透它：玩家可以正常点击其身后的方块，
 * 也不会误攻击它（渲染不受影响，宝可梦照常可见）。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    private void cobbleupdraft$grabbedPokemonNotPickable(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PokemonEntity pokemon
                && GrabberLockManager.isCollisionDisabled(pokemon.getUUID())) {
            cir.setReturnValue(false);
        }
    }
}
