package xiaocaoawa.minecraft.mod.cobbleupdraft.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xiaocaoawa.minecraft.mod.cobbleupdraft.lock.GrabberLockManager;

import java.util.concurrent.CompletableFuture;

/**
 * 让被宝可梦抓手锁定的宝可梦实体像野生宝可梦一样"常驻世界"：
 * <ul>
 *   <li>随区块保存（Cobblemon 默认只保存野生与牧场拴系的实体，有主的放出实体会在区块卸载时丢弃，
 *       导致抓手上的宝可梦消失、飞行器悄悄坠机）；</li>
 *   <li>屏蔽 Cobblemon 对有主实体的主动丢弃（存储坐标缺失、主人离线、拴系不匹配等），
 *       否则区块重载后实体会在第一刻就被删掉。</li>
 * </ul>
 * 抓手取回或方块被破坏时会先解锁，因此正常的收回、放生、死亡流程不受影响。
 */
@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin {

    @Inject(method = "shouldBeSaved", at = @At("HEAD"), cancellable = true)
    private void cobbleupdraft$saveGrabbedPokemon(CallbackInfoReturnable<Boolean> cir) {
        if (cobbleupdraft$isGrabbed()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void cobbleupdraft$keepGrabbedPokemon(Entity.RemovalReason reason, CallbackInfo ci) {
        // 只拦截 discard()（DISCARDED）；死亡、维度切换、区块卸载等正常流程照旧
        if (reason == Entity.RemovalReason.DISCARDED && cobbleupdraft$isGrabbed()) {
            ci.cancel();
        }
    }

    /**
     * 在收回动画开始前拦截。Cobblemon 的 recallWithAnimation 会立刻把实体切到收回光束状态
     * （模型缩没、noPhysics），最后才真正收回；如果只在末尾的 POKEMON_RECALL_PRE 取消，
     * 实体就会永远卡在"正在收回"的半途——模型消失但实体还在、宝可梦仍是放出状态。
     * 濒死时不拦截，让死亡收回流程正常进行。
     */
    @Inject(method = "recallWithAnimation", at = @At("HEAD"), cancellable = true, remap = false)
    private void cobbleupdraft$blockRecallAnimation(CallbackInfoReturnable<CompletableFuture<Pokemon>> cir) {
        PokemonEntity self = (PokemonEntity) (Object) this;
        if (!self.isDeadOrDying() && GrabberLockManager.isGrabbedEntity(self.getUUID())) {
            GrabberLockManager.notifyLocked(self.getPokemon());
            cir.setReturnValue(CompletableFuture.completedFuture(self.getPokemon()));
        }
    }

    /** 取消碰撞箱：不推挤玩家和其他实体。 */
    @Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
    private void cobbleupdraft$noPush(CallbackInfo ci) {
        if (cobbleupdraft$isCollisionDisabled()) {
            ci.cancel();
        }
    }

    /** 取消碰撞箱：不会被玩家或其他实体推动。 */
    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void cobbleupdraft$notPushable(CallbackInfoReturnable<Boolean> cir) {
        if (cobbleupdraft$isCollisionDisabled()) {
            cir.setReturnValue(false);
        }
    }

    private boolean cobbleupdraft$isCollisionDisabled() {
        PokemonEntity self = (PokemonEntity) (Object) this;
        return GrabberLockManager.isCollisionDisabled(self.getUUID());
    }

    private boolean cobbleupdraft$isGrabbed() {
        PokemonEntity self = (PokemonEntity) (Object) this;
        return GrabberLockManager.isGrabbedEntity(self.getUUID());
    }
}
