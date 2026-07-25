package xiaocaoawa.minecraft.mod.cobbleupdraft.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * 宝可梦抓手的隐形拴绳锚点：钉在方块顶面，由自定义渲染器绘制它与目标宝可梦之间的绳子。
 * 不使用原版拴绳数据（Cobblemon 禁止有主宝可梦拴在非主人实体上，会掉落拴绳物品），
 * 目标通过同步数据（实体 ID）传给客户端，纯视觉连接。
 * 位置由方块实体每 tick 维护（飞行器移动时跟随）；
 * 超过 {@link #TIMEOUT_TICKS} 未被方块实体刷新即自毁（方块被破坏等情况的兜底清理）。
 */
public class GrabberAnchorEntity extends Entity {
    private static final int TIMEOUT_TICKS = 60;

    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(GrabberAnchorEntity.class, EntityDataSerializers.INT);

    private int idleTicks;

    public GrabberAnchorEntity(EntityType<? extends GrabberAnchorEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /** 由宝可梦抓手方块实体每 tick 调用，维持锚点存活。 */
    public void keepAlive() {
        idleTicks = 0;
    }

    /** 绳子另一端的实体 ID（0 表示无目标）。 */
    public void setTargetId(int id) {
        if (entityData.get(DATA_TARGET_ID) != id) {
            entityData.set(DATA_TARGET_ID, id);
        }
    }

    public int getTargetId() {
        return entityData.get(DATA_TARGET_ID);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && ++idleTicks > TIMEOUT_TICKS) {
            discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TARGET_ID, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 64.0 * 64.0;
    }
}
