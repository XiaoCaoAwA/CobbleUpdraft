package xiaocaoawa.minecraft.mod.cobbleupdraft.neoforge.compat;

import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import xiaocaoawa.minecraft.mod.cobbleupdraft.block.PokemonGrabberBlockEntity;

/**
 * 带 Sable 物理集成的宝可梦抓手：当方块被组装进飞行器（sublevel）后，
 * Sable 会在每个物理刻回调 {@link #sable$physicsTick}，此处以 BALLOON_LIFT 力组
 * 向船体施加世界坐标系竖直向上的浮力——机制与 Create Aeronautics 热气球气囊完全一致。
 */
public class SablePokemonGrabberBlockEntity extends PokemonGrabberBlockEntity implements BlockEntitySubLevelActor {

    public SablePokemonGrabberBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void sable$physicsTick(ServerSubLevel subLevel, RigidBodyHandle body, double dt) {
        double units = getLiftUnits();
        if (units <= 0.0) {
            return;
        }
        ServerLevel level = subLevel.getLevel();
        Pose3dc pose = subLevel.logicalPose();

        // 方块在飞行器（plot）坐标系中的中心点
        Vector3d plotPos = JOMLConversion.atCenterOf(getBlockPos());

        // 换算为世界坐标，用于采样重力与气压
        Vector3d worldPos = new Vector3d(plotPos).sub(pose.rotationPoint());
        pose.orientation().transform(worldPos).add(pose.position());

        Vector3d gravity = DimensionPhysicsData.getGravity(level, worldPos);
        if (gravity.lengthSquared() < 1.0e-8) {
            return;
        }
        double pressure = DimensionPhysicsData.getAirPressure(level, worldPos);
        if (pressure < 1.0e-5) {
            return;
        }

        // 浮力 = 重力反方向 × 升力 × 气压，乘 dt 作为冲量（与 ServerBalloon.applyForces 相同）
        Vector3d force = gravity.mul(-units * pressure * dt);
        // 世界坐标系 → 船体局部坐标系
        pose.orientation().transformInverse(force);

        subLevel.getOrCreateQueuedForceGroup(ForceGroups.BALLOON_LIFT.get())
                .applyAndRecordPointForce(plotPos, force);
    }
}
